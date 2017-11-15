package com.binance.api;

import com.binance.api.beans.Depth;
import com.binance.api.messages.DepthMessage;

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
//    Account account = new Account();
//    account.setRecvWindow(50000L);
//    System.out.println(account.getQuery(privateKey));
    BinanceApiPublic api = new BinanceApiPublic();
//    AllOrders allOrders = new AllOrders("BCCETH");
//    allOrders.setRecvWindow(15_000L);
//    System.out.println(api.getAllOrders(allOrders));
//    OrderTest test = new OrderTest("BCCETH", Side.BUY, 0.1, 0.1, LIMIT, TimeInForce.GTC);
//    System.out.println(test.getQuery(privateKey));
//    System.out.println(api.sendTestOrder(test));
//    DepositHistory history = new DepositHistory();
//    System.out.println(api.getDepositHistory(history));
    DepthMessage depth = new DepthMessage("BCCETH");
    Depth result = api.getDepth(depth);
    System.out.println(result);
  }
}
