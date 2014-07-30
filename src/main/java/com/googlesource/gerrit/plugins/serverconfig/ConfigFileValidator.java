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

import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.lib.StoredConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

class ConfigFileValidator {
  private static final Logger log = LoggerFactory
      .getLogger(ConfigFileValidator.class);

  private final StoredConfig configFile;
  private final Logger logger;

  ConfigFileValidator(StoredConfig configFile) {
    this(log, configFile);
  }

  ConfigFileValidator(Logger logger, StoredConfig configFile) {
    this.logger = logger;
    this.configFile = configFile;
  }

  public void assertValid() throws ConfigInvalidException, IOException {
    try {
      configFile.load();
    } catch (IOException e) {
      logger.warn("IOException while reading configuration file", e);
      throw e;
    } catch (ConfigInvalidException e) {
      logger.warn("Configuration file is invalid", e);
      Throwable cause = e.getCause();
      if (cause instanceof ConfigInvalidException) {
        throw (ConfigInvalidException) cause;
      }
      throw e;
    }
  }

}
