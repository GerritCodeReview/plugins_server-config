package com.googlesource.gerrit.plugins.serverconfig;

import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.MyersDiff;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class UnifiedDiffer {

  static final String CHARSET_NAME = "UTF-8";

  public String diff(RawText v0, RawText v1) throws IOException {
    DiffAlgorithm algorithm = MyersDiff.INSTANCE;

    EditList editList = algorithm.diff(RawTextComparator.DEFAULT, v0, v1);

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    DiffFormatter formatter = new DiffFormatter(os);
    formatter.format(editList, v0, v1);

    return os.toString(CHARSET_NAME);
  }

}
