package com.binance.api;

import com.binance.api.beans.AggTrades;
import com.binance.api.beans.Depth;
import com.binance.api.messages.AggTradesMessage;
import com.binance.api.messages.DepthMessage;
import com.binance.api.messages.Interval;
import com.binance.api.messages.KlinesMessage;
import com.binance.api.messages.TickerMessage;

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
    BinanceApiPublic api = new BinanceApiPublic();
    DepthMessage depth = new DepthMessage("BCCETH");
    Depth result = api.getDepth(depth);
    System.out.println(result);

    AggTradesMessage aggTradesMessage = new AggTradesMessage("BCCETH");
    AggTrades aggTrades = api.getAggTrades(aggTradesMessage);
    System.out.println(aggTrades);

    System.out.println("ping: " + api.getPing());
    System.out.println("time: " + api.getTime());
    System.out.println("tickers: " + api.getAllBockTickers());
    System.out.println("all prices: " + api.getAllPrices());

    TickerMessage tickerMessage = new TickerMessage("BCCETH");
    System.out.println("BCCETH 24h: " + api.get24h(tickerMessage));

    System.out.println("klines: " + api.getKlines(new KlinesMessage("BCCETH", Interval.FIVE_MINUTES)));
  }
}
