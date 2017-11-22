package com.binance.api;

import com.binance.api.websocket.UserDataSocket;
import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main {

  public static void main(String[] args) {
    Path input = Paths.get("config");
    String privateKey = "";
    String apiKey = "";
    try {
      List<String> config = Files.readAllLines(input);
      privateKey = config.get(0);
      apiKey = config.get(1);
    } catch (IOException e) {
      e.printStackTrace();
    }
    BinanceApi api = new BinanceApi(privateKey, apiKey);
    UserDataSocket socket = new UserDataSocket() {
      @Override
      public void onMessage(Object a) {
        System.out.println(a);
      }
    };
    Session s = api.getUserDataSession(api.startStream().getListenKey(), socket);
    try {
      Thread.sleep(1500000);
    } catch (InterruptedException e) {
    }
    s.close();
  }
}
