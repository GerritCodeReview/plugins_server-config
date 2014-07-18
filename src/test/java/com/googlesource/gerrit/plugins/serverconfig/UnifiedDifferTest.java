package com.googlesource.gerrit.plugins.serverconfig;

import static org.junit.Assert.*;

import org.eclipse.jgit.diff.RawText;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

public class UnifiedDifferTest {

  @Test
  public void testUnchange() throws Exception {
    UnifiedDiffer classUnderTest = new UnifiedDiffer();
    String diff = classUnderTest.diff(t("key = old\n"), t("key = old\n"));
    assertEquals("", diff);
  }

  @Test
  public void testChangeValue() throws Exception {
    UnifiedDiffer classUnderTest = new UnifiedDiffer();
    String diff = classUnderTest.diff(t("key = old\n"), t("key = new\n"));
    assertEquals("@@ -1 +1 @@\n" + "-key = old\n" + "+key = new\n", diff);
  }

  @Test
  public void testAddedValue() throws Exception {
    UnifiedDiffer classUnderTest = new UnifiedDiffer();
    String diff =
        classUnderTest.diff(t("key1 = old\n"), t("key1 = old\n"
            + "key2 = new\n"));
    assertEquals("@@ -1 +1,2 @@\n" + " key1 = old\n" + "+key2 = new\n", diff);
  }

  @Test
  public void testDeletedValue() throws Exception {
    UnifiedDiffer classUnderTest = new UnifiedDiffer();
    String diff =
        classUnderTest.diff(t("key1 = old1\n" + "key1 = old2\n"),
            t("key1 = old1\n"));
    assertEquals("@@ -1,2 +1 @@\n" + " key1 = old1\n" + "-key1 = old2\n", diff);
  }

  private static RawText t(String text) throws UnsupportedEncodingException {
    return new RawText(text.toString().getBytes(UnifiedDiffer.CHARSET_NAME));
  }


}
