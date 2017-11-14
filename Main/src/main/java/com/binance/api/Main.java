package com.binance.api;

import com.binance.api.message.client.Account;
import com.binance.api.message.client.AllOrders;
import com.binance.api.message.client.DepositHistory;
import com.binance.api.message.client.OrderTest;
import com.binance.api.message.client.Side;
import com.binance.api.message.client.TimeInForce;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.binance.api.message.client.OrderType.LIMIT;

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
    Account account = new Account();
    account.setRecvWindow(50000L);
    System.out.println(account.getQuery(privateKey));
    BinanceApi api = new BinanceApi(privateKey, apiKey);
    AllOrders allOrders = new AllOrders("BCCETH");
    allOrders.setRecvWindow(15_000L);
    System.out.println(api.getAllOrders(allOrders));
    OrderTest test = new OrderTest("BCCETH", Side.BUY, 0.1, 0.1, LIMIT, TimeInForce.GTC);
    System.out.println(test.getQuery(privateKey));
    System.out.println(api.sendTestOrder(test));
    DepositHistory history = new DepositHistory();
    System.out.println(api.getDepositHistory(history));
  }
}
