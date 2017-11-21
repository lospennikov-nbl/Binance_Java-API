package com.binance.api;

import com.binance.api.beans.AggTrades;
import com.binance.api.beans.AllBookTickers;
import com.binance.api.beans.AllPrices;
import com.binance.api.beans.BookTicker;
import com.binance.api.beans.Depth;
import com.binance.api.beans.Ping;
import com.binance.api.beans.PriceChangeStat;
import com.binance.api.beans.SymbolPrice;
import com.binance.api.beans.Time;
import com.binance.api.beans.Trade;
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
import java.util.List;
import java.util.function.Function;

import static com.binance.api.Util.httpGet;

public class BinanceApiPublic {

  private ObjectMapper mapper = new ObjectMapper();

  public Ping getPing() {
    return jsonMap(PingMessage.getQuery(), Ping.class);
  }

  public Time getTime() {
    return jsonMap(TimeMessage.getQuery(), Time.class);
  }

  public Depth getDepth(DepthMessage depth) {
    return jsonMap(depth.getQuery(), Depth.class);
  }

  public AggTrades getAggTrades(AggTradesMessage aggTrades) {
    return jsonMapToList(aggTrades.getQuery(), AggTrades::new, Trade[].class);
  }

  public String getKlines(KlinesMessage klines){
    return httpGet(klines.getQuery());
  }

  public PriceChangeStat get24h(TickerMessage ticker) {
    return jsonMap(ticker.getQuery(), PriceChangeStat.class);
  }

  public AllPrices getAllPrices() {
    return jsonMapToList(AllPricesMessage.getQuery(), AllPrices::new, SymbolPrice[].class);
  }

  public AllBookTickers getAllBockTickers() {
    return jsonMapToList(AllBookTickersMessage.getQuery(), AllBookTickers::new, BookTicker[].class);
  }

  private <T, E> T jsonMapToList(String query, Function<List<E>, T> constructor, Class<E[]> inner) {
    try {
      return constructor.apply(List.of(mapper.readValue(httpGet(query), inner)));
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  private <T> T jsonMap(String query, Class<T> clazz) {
    try {
      return mapper.readValue(httpGet(query), clazz);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }
}
