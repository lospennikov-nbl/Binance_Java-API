package com.binance.api.websocket;

import com.binance.api.beans.AggTradeEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

public abstract class AggTradeSocket extends WebSocketAdapter {
  public abstract void onMessage(AggTradeEndpoint a);

  @Override
  public void onWebSocketText(String s) {
    EndpointUtils.handleMessage(s, this::onMessage, AggTradeEndpoint.class);
  }

  @Override
  public void onWebSocketConnect(Session session) {
  }

  @Override
  public void onWebSocketError(Throwable throwable) {
  }
}
