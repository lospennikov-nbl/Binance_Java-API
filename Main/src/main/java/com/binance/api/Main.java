package com.binance.api;

import com.binance.api.message.client.AggTrades;
import com.binance.api.message.client.Depth;
import com.binance.api.message.client.Interval;
import com.binance.api.message.client.Klines;
import com.binance.api.message.client.Time;

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

    BinanceApiPublic apiPublic = new BinanceApiPublic();
    Time time = new Time();
    System.out.println(time);
    Depth depth = new Depth("CTRETH");
    System.out.println(depth);
    System.out.println(apiPublic.getTime(time));
    AggTrades trades = new AggTrades("CTRETH");
    Klines klines = new Klines("CTRETH", Interval.ONE_DAY);
    System.out.println(apiPublic.getAggTrades(trades));
    System.out.println(apiPublic.getKlines(klines));
  }
}
