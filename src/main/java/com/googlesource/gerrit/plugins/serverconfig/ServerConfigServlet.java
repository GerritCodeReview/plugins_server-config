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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.io.ByteStreams;
import com.google.gerrit.server.CurrentUser;
import com.google.gerrit.server.config.SitePaths;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class ServerConfigServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private final File site_path;
  private final File etc_dir;
  private final File static_dir;
  private final Provider<CurrentUser> currentUser;

  @Inject
  ServerConfigServlet(SitePaths sitePaths, Provider<CurrentUser> currentUser) {
    this.site_path = sitePaths.site_path;
    this.etc_dir = sitePaths.etc_dir;
    this.static_dir = sitePaths.static_dir;
    this.currentUser = currentUser;
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    if (!isValidFile(req)) {
      res.setStatus(HttpServletResponse.SC_FORBIDDEN);
      return;
    }
    streamFile(req, res);
  }

  @Override
  public void doPut(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    if (!isValidFile(req)) {
      res.setStatus(HttpServletResponse.SC_FORBIDDEN);
      return;
    }
    writeFile(req, res);
  }

  private boolean isValidFile(HttpServletRequest req) throws IOException {
    File f = configFile(req);
    if (!f.isFile()) {
      return false;
    }
    return isParent(etc_dir, f) || isParent(static_dir, f);
  }

  private File configFile(HttpServletRequest req) {
    return new File(site_path, req.getPathInfo());
  }

  private boolean isParent(File parent, File child) throws IOException {
    File p = parent.getCanonicalFile();
    File c = child.getCanonicalFile();
    for (;;) {
      c = c.getParentFile();
      if (c == null) {
        return false;
      }
      if (c.equals(p)) {
        return true;
      }
    }
  }

  private void streamFile(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    File f = configFile(req);
    res.setStatus(HttpServletResponse.SC_OK);
    res.setContentType("application/octet-stream");
    res.setContentLength((int) f.length());
    OutputStream out = res.getOutputStream();
    InputStream in = new FileInputStream(f);
    try {
      ByteStreams.copy(in, out);
    } finally {
      in.close();
    }
  }

  private void writeFile(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    res.setStatus(HttpServletResponse.SC_NO_CONTENT);
    InputStream in = req.getInputStream();
    OutputStream out = new FileOutputStream(configFile(req));
    try {
      ByteStreams.copy(in, out);
    } finally {
      out.close();
    }
  }
}
