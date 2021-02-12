// Copyright (C) 2013 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.googlesource.gerrit.plugins.serverconfig;

import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.io.ByteStreams;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.extensions.registration.DynamicItem;
import com.google.gerrit.httpd.WebSession;
import com.google.gerrit.server.CurrentUser;
import com.google.gerrit.server.AuditEvent;
import com.google.gerrit.server.audit.AuditService;
import com.google.gerrit.server.config.SitePaths;
import com.google.gerrit.server.util.time.TimeUtil;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Singleton
public class ServerConfigServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private static final Logger log = LoggerFactory
      .getLogger(ServerConfigServlet.class);

  private final SitePaths sitePaths;
  private final AuditService auditService;
  private final DynamicItem<WebSession> webSession;
  private final String pluginName;

  @Inject
  ServerConfigServlet(SitePaths sitePaths, DynamicItem<WebSession> webSession,
      AuditService auditService, @PluginName String pluginName) {
    this.webSession = webSession;
    this.auditService = auditService;
    this.pluginName = pluginName;
    this.sitePaths = sitePaths;
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    if (!isAllowedPath(req)) {
      res.setStatus(HttpServletResponse.SC_FORBIDDEN);
      return;
    }
    streamFile(req, res);
  }

  @Override
  public void doPut(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    if (!isAllowedPath(req)) {
      res.setStatus(HttpServletResponse.SC_FORBIDDEN);
      return;
    }
    if (isGerritConfig(req)) {
      writeFileAndFireAuditEvent(req, res);
    } else {
      writeFile(req, res);
    }
  }

  private void writeFileAndFireAuditEvent(HttpServletRequest req,
      HttpServletResponse res) throws IOException {
    File oldFile = resolvePath(req).toFile();
    File dir = oldFile.getParentFile();
    File newFile = File.createTempFile(oldFile.getName(), ".new", dir);
    streamRequestToFile(req, newFile);

    try {
      FileBasedConfig config = new FileBasedConfig(newFile, FS.DETECTED);
      config.load();

      String diff = diff(oldFile, newFile);
      audit("about to change config file", oldFile.getPath(), diff);

      newFile.renameTo(oldFile);
      audit("changed config file", oldFile.getPath(), diff);

      res.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } catch (ConfigInvalidException e) {
      log.warn("Configuration file is invalid", e);

      Throwable cause = e.getCause();
      final String msg =
          cause instanceof ConfigInvalidException ? cause.getMessage()
              : e.getMessage();

      newFile.delete();
      respondInvalidConfig(req, res, msg);
    }
  }

  private void respondInvalidConfig(HttpServletRequest req,
      HttpServletResponse res, String messageTxt) throws IOException {
    String message =
        MessageFormat.format("Invalid config file {0}: {1}", req.getPathInfo(),
            messageTxt);
    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    res.setContentType("application/octet-stream");
    res.setContentLength(message.length());
    byte[] bytes = message.getBytes(Charsets.UTF_8);
    OutputStream out = res.getOutputStream();
    try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
      ByteStreams.copy(in, out);
    }
  }

  private static String diff(File oldFile, File newFile) throws IOException {
    RawText oldContext = new RawText(oldFile);
    RawText newContext = new RawText(newFile);
    UnifiedDiffer differ = new UnifiedDiffer();
    return differ.diff(oldContext, newContext);
  }

  private void audit(String what, String path, String diff) {
    String sessionId = webSession.get().getSessionId();
    CurrentUser who = webSession.get().getUser();
    long when = TimeUtil.nowMs();
    ListMultimap<String, Object> params =
        MultimapBuilder.hashKeys().arrayListValues().build();
    params.put("plugin", pluginName);
    params.put("class", ServerConfigServlet.class);
    params.put("diff", diff);
    params.put("file", path);
    auditService.dispatch(new AuditEvent(sessionId, who, what, when, params, null));
  }

  private boolean isGerritConfig(HttpServletRequest req) throws IOException {
    return Files.isSameFile(sitePaths.gerrit_config, resolvePath(req));
  }

  private boolean isAllowedPath(HttpServletRequest req) throws IOException {
    Path p = resolvePath(req);
    if (!Files.isRegularFile(p)) {
      return false;
    }
    return isParent(sitePaths.etc_dir, p) || isParent(sitePaths.static_dir, p);
  }

  private Path resolvePath(HttpServletRequest req) {
    return sitePaths.resolve(CharMatcher.is('/').trimLeadingFrom(
        req.getServletPath() + req.getPathInfo()));
  }

  private boolean isParent(Path parent, Path child) throws IOException {
    Path p = child;
    for (;;) {
      p = p.getParent();
      if (p == null) {
        return false;
      }
      if (Files.isSameFile(p, parent)) {
        return true;
      }
    }
  }

  private void streamFile(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    File f = resolvePath(req).toFile();
    res.setStatus(HttpServletResponse.SC_OK);
    res.setContentType("application/octet-stream");
    res.setContentLength((int) f.length());
    OutputStream out = res.getOutputStream();
    try (InputStream in = new FileInputStream(f)) {
      ByteStreams.copy(in, out);
    }
  }

  private void writeFile(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    res.setStatus(HttpServletResponse.SC_NO_CONTENT);
    streamRequestToFile(req, resolvePath(req).toFile());
  }

  private void streamRequestToFile(HttpServletRequest req, File file)
      throws IOException, FileNotFoundException {
    InputStream in = req.getInputStream();
    try (OutputStream out = new FileOutputStream(file)) {
      ByteStreams.copy(in, out);
    }
  }
}
