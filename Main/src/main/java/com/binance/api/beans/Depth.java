package com.binance.api.beans;

import java.util.List;
import java.util.stream.Collectors;

public class Depth {
  private int lastUpdateId;

  private List<List<Double>> bids;

  private List<List<Double>> asks;

  void setAsks(List<List<Object>> asks) {
    this.asks = parseList(asks);
  }

  void setBids(List<List<Object>> bids) {
    this.bids = parseList(bids);
  }

  private List<List<Double>> parseList(List<List<Object>> lists) {
    return lists.stream().map(
        list -> list.stream().filter(
            obj -> obj instanceof String
        ).map(obj -> Double.parseDouble((String) obj)).collect(Collectors.toList())
    ).collect(Collectors.toList());
  }

  public void setLastUpdateId(int lastUpdateId) {
    this.lastUpdateId = lastUpdateId;
  }

  public int getLastUpdateId() {
    return lastUpdateId;
  }

  public List<List<Double>> getBids() {
    return bids;
  }

  public List<List<Double>> getAsks() {
    return asks;
  }

  @Override
  public String toString() {
    return "{" + lastUpdateId + ", " + bids + ", " + asks + "}";
  }
}
