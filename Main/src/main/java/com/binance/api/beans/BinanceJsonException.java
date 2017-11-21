package com.binance.api.beans;

import java.io.IOException;

public class BinanceJsonException extends IOException {
  public BinanceJsonException(String message) {
    super(message);
  }
}
