package com.binance.api;

import com.binance.api.message.client.Account;
import com.binance.api.message.client.AllOrders;

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
    Long time  = Util.getCurrentTime();
    System.out.println(Util.generateSigQuery("v3/openOrders", privateKey, "symbol", "CTRETH" ,"recvWindow", "50000"));
    Account account = new Account();
    account.setRecvWindow(50000L);
    System.out.println(account.getQuery(privateKey));
    BinanceApi api = new BinanceApi(privateKey, apiKey);
    AllOrders allOrders = new AllOrders("BCCETH");
    allOrders.setRecvWindow(15_000L);
    System.out.println(api.getAllOrders(allOrders));
  }
}
