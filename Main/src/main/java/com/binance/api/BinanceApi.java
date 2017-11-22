package com.binance.api;

import com.binance.api.beans.AccountInfo;
import com.binance.api.beans.AllOrders;
import com.binance.api.beans.CancelOrder;
import com.binance.api.beans.DataStream;
import com.binance.api.beans.DepositHistory;
import com.binance.api.beans.NewOrder;
import com.binance.api.beans.OpenOrders;
import com.binance.api.beans.OrderStatus;
import com.binance.api.beans.Success;
import com.binance.api.beans.Trade;
import com.binance.api.beans.Trades;
import com.binance.api.beans.WithdrawHistory;
import com.binance.api.beans.WithdrawResponse;
import com.binance.api.messages.AccountMessage;
import com.binance.api.messages.AllOrdersMessage;
import com.binance.api.messages.CancelOrderMessage;
import com.binance.api.messages.CloseStreamMessage;
import com.binance.api.messages.DepositHistoryMessage;
import com.binance.api.messages.KeepAliveStreamMessage;
import com.binance.api.messages.NewOrderMessage;
import com.binance.api.messages.OpenOrdersMessage;
import com.binance.api.messages.OrderTestMessage;
import com.binance.api.messages.QueryOrderMessage;
import com.binance.api.messages.StartStreamMessage;
import com.binance.api.messages.TradesMessage;
import com.binance.api.messages.WithdrawHistoryMessage;
import com.binance.api.messages.WithdrawMessage;
import com.binance.api.websocket.UserDataSocket;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.net.URI;

public class BinanceApi extends BinanceApiPublic {
  private static final String BASIC_HEADER_NAME = "X-MBX-APIKEY";

  private final String privateKey;

  private final Header basicHeader;

  public BinanceApi(String privateKey, String apiKey) {
    this.privateKey = privateKey;
    basicHeader = new BasicHeader(BASIC_HEADER_NAME, apiKey);
  }

  public NewOrder sendNewOrder(NewOrderMessage newOrderMessage) {
    return jsonMap(Util.httpPost(newOrderMessage.getQuery(privateKey), basicHeader), NewOrder.class);
  }

  public Success sendTestOrder(OrderTestMessage testMessage) {
    return jsonMap(Util.httpPost(testMessage.getQuery(privateKey), basicHeader), Success.class);
  }

  public OrderStatus getQueryOrder(QueryOrderMessage queryOrderMessage) {
    return jsonMap(Util.httpGet(queryOrderMessage.getQuery(privateKey), basicHeader), OrderStatus.class);
  }

  public CancelOrder cancelOrder(CancelOrderMessage cancelOrderMessage) {
    return jsonMap(Util.httpDelete(cancelOrderMessage.getQuery(privateKey), basicHeader), CancelOrder.class);
  }

  public OpenOrders getOpenOrders(OpenOrdersMessage openOrdersMessage) {
    return jsonMapToList(Util.httpGet(openOrdersMessage.getQuery(privateKey), basicHeader), OpenOrders::new, OrderStatus[].class);
  }

  public AllOrders getAllOrders(AllOrdersMessage allOrdersMessage) {
    return jsonMapToList(Util.httpGet(allOrdersMessage.getQuery(privateKey), basicHeader), AllOrders::new, OrderStatus[].class);
  }

  public AccountInfo getAccountInfo(AccountMessage account) {
    return jsonMap(Util.httpGet(account.getQuery(privateKey), basicHeader), AccountInfo.class);
  }

  public AccountInfo getAccountInfo() {
    return jsonMap(Util.httpGet(AccountMessage.getStaticQuery(privateKey), basicHeader), AccountInfo.class);
  }

  public Trades getTrades(TradesMessage tradesMessage) {
    return jsonMapToList(Util.httpGet(tradesMessage.getQuery(privateKey), basicHeader), Trades::new, Trade[].class);
  }

  public WithdrawResponse withdraw(WithdrawMessage withdrawMessage) {
    return jsonMap(Util.httpPost(withdrawMessage.getQuery(privateKey), basicHeader), WithdrawResponse.class);
  }

  public DepositHistory getDepositHistory(DepositHistoryMessage depositHistoryMessage) {
    return jsonMap(Util.httpPost(depositHistoryMessage.getQuery(privateKey), basicHeader), DepositHistory.class);
  }

  public DepositHistory getDepositHistory() {
    return jsonMap(Util.httpPost(DepositHistoryMessage.getStaticQuery(privateKey), basicHeader), DepositHistory.class);
  }

  public WithdrawHistory getWithdrawHistory(WithdrawHistoryMessage withdrawHistoryMessage) {
    return jsonMap(Util.httpPost(withdrawHistoryMessage.getQuery(privateKey), basicHeader), WithdrawHistory.class);
  }

  public WithdrawHistory getWithdrawHistory() {
    return jsonMap(Util.httpPost(WithdrawHistoryMessage.getStaticQuery(privateKey), basicHeader), WithdrawHistory.class);
  }

  public DataStream startStream() {
    return jsonMap(Util.httpPost(StartStreamMessage.getQuery(), basicHeader), DataStream.class);
  }

  public Success keepAliveStream(KeepAliveStreamMessage stream) {
    return jsonMap(Util.httpPut(stream.getQuery(), basicHeader), Success.class);
  }

  public Success closeStream(CloseStreamMessage stream) {
    return jsonMap(Util.httpPut(stream.getQuery(), basicHeader), Success.class);
  }

  public Session getUserDataSession(String listenKey, UserDataSocket socket) {
    String uri = String.format("wss://stream.binance.com:9443/ws/%s", listenKey);
    WebSocketClient client = new WebSocketClient();
    try {
      client.start();
      return client.connect(socket, new URI(uri)).get();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
