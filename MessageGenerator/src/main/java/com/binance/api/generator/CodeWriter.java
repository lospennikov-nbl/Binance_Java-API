package com.binance.api.generator;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

class CodeWriter {
  private static final String TAB = "  ";

  private static final String NL = String.valueOf((char) Character.LINE_SEPARATOR);

  private PrintWriter out;

  private int tabsCount;

  CodeWriter(PrintWriter out) {
    this.out = out;
  }

  CodeWriter write(List<String> args) {
    return write(args.toArray(new String[args.size()]));
  }

  CodeWriter write(String... args) {
    String tab = TAB;
    StringJoiner joiner = new StringJoiner("");
    StringBuilder tabs = new StringBuilder();
    String[] tabsArray = new String[tabsCount];
    Arrays.fill(tabsArray, tab);
    tabs.append(Arrays.stream(tabsArray).collect(Collectors.joining()));
    for (String str : args) {
      if (!str.isEmpty()) {
        if (str.charAt(0) == '}') {
          tabs.delete(tabs.length() - tab.length(), tabs.length());
        }
        joiner.add(tabs);
        joiner.add(str);
        if (str.charAt(str.length() - 1) == '{') {
          tabs.append(tab);
        }
      }
      joiner.add(NL);
    }
    out.print(joiner);
    tabsCount = tabs.length() / tab.length();
    return this;
  }
}
