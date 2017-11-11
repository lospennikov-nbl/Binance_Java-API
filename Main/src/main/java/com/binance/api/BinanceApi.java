package com.binance.api;

import com.binance.api.message.client.Account;
import com.binance.api.message.client.AggTrades;
import com.binance.api.message.client.AllBookTickers;
import com.binance.api.message.client.AllOrders;
import com.binance.api.message.client.AllPrices;
import com.binance.api.message.client.CancelOrder;
import com.binance.api.message.client.CloseStream;
import com.binance.api.message.client.Depth;
import com.binance.api.message.client.KeepAliveStream;
import com.binance.api.message.client.Klines;
import com.binance.api.message.client.NewOrder;
import com.binance.api.message.client.OpenOrders;
import com.binance.api.message.client.OrderTest;
import com.binance.api.message.client.Ping;
import com.binance.api.message.client.QueryOrder;
import com.binance.api.message.client.StartStream;
import com.binance.api.message.client.Ticker;
import com.binance.api.message.client.Time;
import com.binance.api.message.client.Trades;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

public class BinanceApi {
  private static final String BASIC_HEADER_NAME = "X-MBX-APIKEY";

  private final String privateKey;

  private final Header basicHeader;

  private final BinanceApiPublic apiPublic;

  public BinanceApi(String privateKey, String apiKey) {
    this.privateKey = privateKey;
    basicHeader = new BasicHeader(BASIC_HEADER_NAME, apiKey);
    apiPublic = new BinanceApiPublic();
  }

  public String getPing(Ping ping) {
    return apiPublic.getPing(ping);
  }

  public String getTime(Time time) {
    return apiPublic.getTime(time);
  }

  public String getDepth(Depth depth) {
    return apiPublic.getDepth(depth);
  }

  public String getAggTrades(AggTrades aggTrades) {
    return apiPublic.getAggTrades(aggTrades);
  }

  public String getKlines(Klines klines) {
    return apiPublic.getKlines(klines);
  }

  public String get24h(Ticker ticker) {
    return apiPublic.get24h(ticker);
  }

  public String getAllPrices(AllPrices allPrices) {
    return apiPublic.getAllPrices(allPrices);
  }

  public String getAllBockTickers(AllBookTickers allBookTickers) {
    return apiPublic.getAllBockTickers(allBookTickers);
  }

  public String sendNewOrder(NewOrder newOrder) {
    return Util.httpPost(newOrder.getQuery(privateKey), basicHeader);
  }

  public String sendTestOrder(OrderTest order) {
    return Util.httpPost(order.getQuery(privateKey), basicHeader);
  }

  public String getQueryOrder(QueryOrder order) {
    return Util.httpGet(order.getQuery(privateKey), basicHeader);
  }

  public String cancelOrder(CancelOrder cancelOrderMessage) {
    return Util.httpDelete(cancelOrderMessage.getQuery(privateKey), basicHeader);
  }

  public String getOpenOrders(OpenOrders openOrders) {
    return Util.httpGet(openOrders.getQuery(privateKey), basicHeader);
  }

  public String getAllOrders(AllOrders allOrders) {
    return Util.httpGet(allOrders.getQuery(privateKey), basicHeader);
  }

  public String getAccount(Account account) {
    return Util.httpGet(account.getQuery(privateKey), basicHeader);
  }

  public String getTrades(Trades trades) {
    return Util.httpGet(trades.getQuery(privateKey), basicHeader);
  }

  //TODO: withdraw

  public String startStream(StartStream stream) {
    return Util.httpPost(stream.getQuery(), basicHeader);
  }

  public String keepAliveStream(KeepAliveStream stream) {
    return Util.httpPut(stream.getQuery(), basicHeader);
  }

  public String closeStream(CloseStream stream) {
    return Util.httpDelete(stream.getQuery(), basicHeader);
  }
}
