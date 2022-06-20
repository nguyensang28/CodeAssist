package com.tyron.editor.util.diff;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class LineTokenizer extends LineTokenizerBase<String> {
  private final char[] myChars;
  private final String myText;

  public LineTokenizer(@NotNull String text) {
    myChars = text.toCharArray();
    myText = text;
  }

  public String @NotNull [] execute() {
    ArrayList<String> lines = new ArrayList<>();
    doExecute(lines);
    return lines.toArray(new String[0]);
  }

  @Override
  protected void addLine(List<? super String> lines, int start, int end, boolean appendNewLine) {
    if (appendNewLine) {
      lines.add(myText.substring(start, end) + "\n");
    }
    else {
      lines.add(myText.substring(start, end));
    }
  }

  @Override
  protected char charAt(int index) {
    return myChars[index];
  }

  @Override
  protected int length() {
    return myChars.length;
  }

  @NotNull
  @Override
  protected String substring(int start, int end) {
    return myText.substring(start, end);
  }

  @NotNull
  public static String concatLines(String @NotNull [] lines) {
    StringBuilder buffer = new StringBuilder();
    for (String line : lines) {
      buffer.append(line);
    }
    return buffer.substring(0, buffer.length());
  }

  @NotNull
  public static String correctLineSeparators(@NotNull String text) {
    return concatLines(new LineTokenizer(text).execute());
  }

}