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

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.same;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.*;

import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.lib.StoredConfig;
import org.junit.Test;
import org.slf4j.Logger;

import java.io.IOException;

public class ConfigFileValidatorTest {

  private static class TextBasedConfig extends StoredConfig {
    private final String text;

    public TextBasedConfig(String text) {
      this.text = text;
    }

    @Override
    public void load() throws IOException, ConfigInvalidException {
      fromText(text);
    }

    @Override
    public void save() throws IOException {
      throw new UnsupportedOperationException();
    }

  }

  @Test
  public void testEmptyIsValid() throws Exception {
    StoredConfig configFile = new TextBasedConfig("");
    ConfigFileValidator classUnderTest =
        new ConfigFileValidator(null, configFile);

    classUnderTest.assertValid();
  }

  @Test
  public void testIOException() throws Exception {
    IOException e = new IOException("message");
    checkException(e, e);
  }

  @Test
  public void testConfigInvalidException() throws Exception {
    ConfigInvalidException e = new ConfigInvalidException("message");
    checkException(e, e);
  }

  @Test
  public void testConfigInvalidExceptionWithCause() throws Exception {
    ConfigInvalidException cause = new ConfigInvalidException("message of cause");
    ConfigInvalidException e = new ConfigInvalidException("message", cause);
    checkException(e, cause);
  }

  private void checkException(Exception exception, Exception cause)
      throws IOException, ConfigInvalidException {
    StoredConfig configFile = createMock(StoredConfig.class);
    configFile.load();
    expectLastCall().andThrow(exception);

    Logger log = createMock(Logger.class);
    log.warn(anyString(), same(exception));
    expectLastCall();
    replay(configFile, log);

    ConfigFileValidator classUnderTest =
        new ConfigFileValidator(log, configFile);

    try {
      classUnderTest.assertValid();
    } catch (ConfigInvalidException | IOException e) {
      assertSame(e, cause);
    }

    verify(configFile, log);
  }

  @Test
  public void testNonEmptyButValid() throws Exception {
    StoredConfig configFile =
        new TextBasedConfig("[core]\n\trepositoryformatversion = 0");
    ConfigFileValidator classUnderTest =
        new ConfigFileValidator(null, configFile);

    classUnderTest.assertValid();
  }

  @Test
  public void testInvalidConfig() throws IOException {
    StoredConfig configFile = new TextBasedConfig("[core");

    Logger log = createMock(Logger.class);
    log.warn(anyString(), anyObject(ConfigInvalidException.class));
    expectLastCall();
    replay(log);

    ConfigFileValidator classUnderTest =
        new ConfigFileValidator(log, configFile);

    try {
      classUnderTest.assertValid();
      fail();
    } catch (ConfigInvalidException e) {
      assertEquals("Unexpected end of config file", e.getMessage());
    }

    verify(log);
  }

}
