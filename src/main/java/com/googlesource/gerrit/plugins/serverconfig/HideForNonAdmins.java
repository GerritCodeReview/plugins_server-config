// Copyright (C) 2014 The Android Open Source Project
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

import com.google.gerrit.server.CurrentUser;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * Allow only administrators to access the default UI.
 */
@Singleton
public class HideForNonAdmins implements Filter {

  private Provider<CurrentUser> user;

  @Inject
  public HideForNonAdmins(Provider<CurrentUser> user) {
    this.user = user;
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse rsp,
      FilterChain chain) throws IOException, ServletException {
    if (!user.get().getCapabilities().canAdministrateServer()) {
      ((HttpServletResponse) rsp).sendError(HttpServletResponse.SC_NOT_FOUND,
          "Not Found");
      return;
    }
    chain.doFilter(req, rsp);
  }

  @Override
  public void init(FilterConfig cfg) throws ServletException {
  }

  @Override
  public void destroy() {
  }
}
