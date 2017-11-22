package com.binance.api.websocket;

import com.binance.api.beans.KlineEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

public abstract class KlinesSocket extends WebSocketAdapter {
  public abstract void onMessage(KlineEndpoint a);

  @Override
  public void onWebSocketText(String s) {
    EndpointUtils.handleMessage(s, this::onMessage, KlineEndpoint.class);
  }

  @Override
  public void onWebSocketConnect(Session session) {
  }

  @Override
  public void onWebSocketError(Throwable throwable) {
  }
}