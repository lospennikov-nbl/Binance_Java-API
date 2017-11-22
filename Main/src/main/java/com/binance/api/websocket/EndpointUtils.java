package com.binance.api.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.function.Consumer;

final class EndpointUtils {
  final static ObjectMapper MAPPER = new ObjectMapper();

  private EndpointUtils() {

  }

  static <T> void handleMessage(String s, Consumer<T> onMessage, Class<T> clazz) {
    T endpoint = null;
    try {
      endpoint = MAPPER.readValue(s, clazz);
    } catch (IOException e) {
      e.printStackTrace();
    }
    onMessage.accept(endpoint);
  }
}
