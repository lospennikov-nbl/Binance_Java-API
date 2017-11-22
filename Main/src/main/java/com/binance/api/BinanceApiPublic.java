package com.binance.api;

import com.binance.api.beans.AggTrade;
import com.binance.api.beans.AggTrades;
import com.binance.api.beans.AllBookTickers;
import com.binance.api.beans.AllPrices;
import com.binance.api.beans.BookTicker;
import com.binance.api.beans.Candlestick;
import com.binance.api.beans.Depth;
import com.binance.api.beans.Klines;
import com.binance.api.beans.PriceChangeStat;
import com.binance.api.beans.Success;
import com.binance.api.beans.SymbolPrice;
import com.binance.api.beans.Time;
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
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static com.binance.api.Util.httpGet;

public class BinanceApiPublic {

  ObjectMapper mapper = new ObjectMapper();

  public Success getPing() {
    return getJsonMap(PingMessage.getQuery(), Success.class);
  }

  public Time getTime() {
    return getJsonMap(TimeMessage.getQuery(), Time.class);
  }

  public Depth getDepth(DepthMessage depth) {
    return getJsonMap(depth.getQuery(), Depth.class);
  }

  public AggTrades getAggTrades(AggTradesMessage aggTrades) {
    return getJsonMapToList(aggTrades.getQuery(), AggTrades::new, AggTrade[].class);
  }

  public Klines getKlines(KlinesMessage message) {
    return getJsonMapToList(message.getQuery(), Klines::new, Candlestick[].class);
  }

  public PriceChangeStat get24h(TickerMessage ticker) {
    return getJsonMap(ticker.getQuery(), PriceChangeStat.class);
  }

  public AllPrices getAllPrices() {
    return getJsonMapToList(AllPricesMessage.getQuery(), AllPrices::new, SymbolPrice[].class);
  }

  public AllBookTickers getAllBockTickers() {
    return getJsonMapToList(AllBookTickersMessage.getQuery(), AllBookTickers::new, BookTicker[].class);
  }

  private <T, E> T getJsonMapToList(String query, Function<List<E>, T> constructor, Class<E[]> inner) {
    return jsonMapToList(httpGet(query), constructor, inner);
  }

  private <T> T getJsonMap(String query, Class<T> clazz) {
    return jsonMap(httpGet(query), clazz);
  }

  <T, E> T jsonMapToList(String data, Function<List<E>, T> constructor, Class<E[]> inner) {
    try {
      return constructor.apply(Arrays.asList(mapper.readValue(data, inner)));
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  <T> T jsonMap(String data, Class<T> clazz) {
    try {
      return mapper.readValue(data, clazz);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }
}
