package com.binance.api;

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
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

public class BinanceApi extends BinanceApiPublic {
  private static final String BASIC_HEADER_NAME = "X-MBX-APIKEY";

  private final String privateKey;

  private final Header basicHeader;

  public BinanceApi(String privateKey, String apiKey) {
    this.privateKey = privateKey;
    basicHeader = new BasicHeader(BASIC_HEADER_NAME, apiKey);
  }

  public String sendNewOrder(NewOrderMessage newOrder) {
    return Util.httpPost(newOrder.getQuery(privateKey), basicHeader);
  }

  public String sendTestOrder(OrderTestMessage order) {
    return Util.httpPost(order.getQuery(privateKey), basicHeader);
  }

  public String getQueryOrder(QueryOrderMessage order) {
    return Util.httpGet(order.getQuery(privateKey), basicHeader);
  }

  public String cancelOrder(CancelOrderMessage cancelOrderMessage) {
    return Util.httpDelete(cancelOrderMessage.getQuery(privateKey), basicHeader);
  }

  public String getOpenOrders(OpenOrdersMessage openOrders) {
    return Util.httpGet(openOrders.getQuery(privateKey), basicHeader);
  }

  public String getAllOrders(AllOrdersMessage allOrders) {
    return Util.httpGet(allOrders.getQuery(privateKey), basicHeader);
  }

  public String getAccount(AccountMessage account) {
    return Util.httpGet(account.getQuery(privateKey), basicHeader);
  }

  public String getTrades(TradesMessage trades) {
    return Util.httpGet(trades.getQuery(privateKey), basicHeader);
  }

  public String withdraw(WithdrawMessage withdraw) {
    return Util.httpPost(withdraw.getQuery(privateKey), basicHeader);
  }

  public String getDepositHistory(DepositHistoryMessage depositHistory){
    return Util.httpPost(depositHistory.getQuery(privateKey), basicHeader);
  }

  public String getWithdrawHistory(WithdrawHistoryMessage withdrawHistory){
    return Util.httpPost(withdrawHistory.getQuery(privateKey), basicHeader);
  }

  public String startStream() {
    return Util.httpPost(StartStreamMessage.getQuery(), basicHeader);
  }

  public String keepAliveStream(KeepAliveStreamMessage stream) {
    return Util.httpPut(stream.getQuery(), basicHeader);
  }

  public String closeStream(CloseStreamMessage stream) {
    return Util.httpDelete(stream.getQuery(), basicHeader);
  }
}
