package com.binance.api.websocket;

import com.binance.api.beans.DepthEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

public abstract class DepthSocket extends WebSocketAdapter {
  public abstract void onMessage(DepthEndpoint a);

  @Override
  public void onWebSocketText(String s) {
    EndpointUtils.handleMessage(s, this::onMessage, DepthEndpoint.class);
  }

  @Override
  public void onWebSocketConnect(Session session) {
  }

  @Override
  public void onWebSocketError(Throwable throwable) {
  }
}
