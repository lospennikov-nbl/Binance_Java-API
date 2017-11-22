package com.binance.api.websocket;

import com.binance.api.beans.AccountUpdateEndpoint;
import com.binance.api.beans.BinanceJsonException;
import com.binance.api.beans.ExecutionReport;
import com.fasterxml.jackson.databind.JsonNode;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

import java.io.IOException;

public abstract class UserDataSocket extends WebSocketAdapter {
  private static final String OUTBOUND_ACCOUNT_INFO = "outboundAccountInfo";

  private static final String EXECUTION_REPORT = "executionReport";

  public abstract void onMessage(Object a);

  @Override
  public void onWebSocketText(String s) {
    try {
      JsonNode root = EndpointUtils.MAPPER.readTree(s);
      String type = root.get("e").asText();
      if (type.equals(OUTBOUND_ACCOUNT_INFO)) {
        EndpointUtils.handleMessage(s, this::onMessage, AccountUpdateEndpoint.class);
      } else if (type.equals(EXECUTION_REPORT)) {
        EndpointUtils.handleMessage(s, this::onMessage, ExecutionReport.class);
      } else {
        throw new BinanceJsonException(String.format(
            "Invalid even type %s", type));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onWebSocketConnect(Session session) {
  }

  @Override
  public void onWebSocketError(Throwable throwable) {
  }
}

