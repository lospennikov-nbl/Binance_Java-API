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
//    DepthMessage depth = new DepthMessage("BCCETH");
//    Depth result = api.getDepth(depth);
//    System.out.println(result);
//
//    AggTradesMessage aggTradesMessage = new AggTradesMessage("BCCETH");
//    AggTrades aggTrades = api.getAggTrades(aggTradesMessage);
//    System.out.println(aggTrades);
//
//    System.out.println("ping: " + api.getPing());
//    System.out.println("time: " + api.getTime());
//    System.out.println("tickers: " + api.getAllBockTickers());
//    System.out.println("all prices: " + api.getAllPrices());
//
//    TickerMessage tickerMessage = new TickerMessage("BCCETH");
//    System.out.println("BCCETH 24h: " + api.get24h(tickerMessage));
//
//    System.out.println("klines: " + api.getKlines(new KlinesMessage("BCCETH", Interval.FIVE_MINUTES)));
//
//
//    NewOrder order = api.sendNewOrder(new NewOrderMessage("BCCETH", Side.SELL, OrderType.LIMIT, TimeInForce.GTC, 0.03, 3.43304));
//    System.out.println(order);
//    QueryOrderMessage queryOrderMessage = new QueryOrderMessage("BCCETH");
//    queryOrderMessage.setOrderId(order.getOrderId());
//    System.out.println(api.getQueryOrder(queryOrderMessage));
//    CancelOrderMessage cancelOrderMessage = new CancelOrderMessage("BCCETH");
//    cancelOrderMessage.setOrderId(order.getOrderId());
//    System.out.println(api.cancelOrder(cancelOrderMessage));
//    System.out.println(api.getAllOrders(new AllOrdersMessage("BCCETH")));
//    System.out.println(api.getOpenOrders(new OpenOrdersMessage("BCCETH")));
//    System.out.println(api.getAccountInfo());
//    System.out.println(api.getTrades(new TradesMessage("BCCETH")));
//    System.out.println(api.getWithdrawHistory());
//    System.out.println(api.getDepositHistory());

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
