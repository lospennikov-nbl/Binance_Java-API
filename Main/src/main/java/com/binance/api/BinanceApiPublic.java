package com.binance.api;

import com.binance.api.message.client.AggTrades;
import com.binance.api.message.client.AllBookTickers;
import com.binance.api.message.client.AllPrices;
import com.binance.api.message.client.Depth;
import com.binance.api.message.client.Klines;
import com.binance.api.message.client.Ping;
import com.binance.api.message.client.Ticker;
import com.binance.api.message.client.Time;

import static com.binance.api.Util.httpGet;

public class BinanceApiPublic {

  public String getPing() {
    return httpGet(Ping.getQuery());
  }

  public String getTime() {
    return httpGet(Time.getQuery());
  }

  public String getDepth(Depth depth) {
    return httpGet(depth.getQuery());
  }

  public String getAggTrades(AggTrades aggTrades) {
    return httpGet(aggTrades.getQuery());
  }

  public String getKlines(Klines klines){
    return httpGet(klines.getQuery());
  }

  public String get24h(Ticker ticker) {
    return httpGet(ticker.getQuery());
  }

  public String getAllPrices() {
    return httpGet(AllPrices.getQuery());
  }

  public String getAllBockTickers() {
    return httpGet(AllBookTickers.getQuery());
  }
}
