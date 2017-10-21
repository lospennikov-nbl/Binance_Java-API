package com.binance.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

  public static void main(String[] args) {
    Path input = Paths.get("secret_key");
    String privateKey = "";
    try {
      privateKey = Files.readAllLines(input).get(0);
    } catch (IOException e) {
      e.printStackTrace();
    }

    System.out.println(Util.generateSigQuery("v3/account", "recvWindow=60000",
        privateKey));

    BinanceApiPublic apiPublic = new BinanceApiPublic();
    System.out.println(apiPublic.getTime());
    System.out.println(apiPublic.getDepth("CTRETH"));
  }
}
