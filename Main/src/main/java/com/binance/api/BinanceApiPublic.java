package com.binance.api;

import com.binance.api.beans.Depth;
import com.binance.api.messages.AggTradesMessage;
import com.binance.api.messages.AllBookTickersMessage;
import com.binance.api.messages.AllPricesMessage;
import com.binance.api.messages.DepthMessage;
import com.binance.api.messages.KlinesMessage;
import com.binance.api.messages.PingMessage;
import com.binance.api.messages.TickerMessage;
import com.binance.api.messages.TimeMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

import static com.binance.api.Util.httpGet;

public class BinanceApiPublic {

  private ObjectMapper mapper = new ObjectMapper();

  public String getPing() {
    return httpGet(PingMessage.getQuery());
  }

  public String getTime() {
    return httpGet(TimeMessage.getQuery());
  }

  public Depth getDepth(DepthMessage depth) {
    try {
      return mapper.readValue(httpGet(depth.getQuery()), Depth.class);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public String getAggTrades(AggTradesMessage aggTrades) {
    return httpGet(aggTrades.getQuery());
  }

  public String getKlines(KlinesMessage klines){
    return httpGet(klines.getQuery());
  }

  public String get24h(TickerMessage ticker) {
    return httpGet(ticker.getQuery());
  }

  public String getAllPrices() {
    return httpGet(AllPricesMessage.getQuery());
  }

  public String getAllBockTickers() {
    return httpGet(AllBookTickersMessage.getQuery());
  }
}
