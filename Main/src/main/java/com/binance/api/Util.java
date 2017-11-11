package com.binance.api;

import org.apache.commons.codec.digest.HmacUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.time.Instant;

public final class Util {
  private static final String API_BASE = "https://www.binance.com/api/";

  private Util() {

  }

  public static long getCurrentTime() {
    return Instant.now().toEpochMilli();
  }

  public static String hmacSha256(String key, String value) {
    return HmacUtils.hmacSha256Hex(key, value);
  }

  public static String generateSigQuery(String method, String inputQuery, String key) {
    String query = String.format("%s%stimestamp=%s",
        inputQuery,
        inputQuery.isEmpty() ? "" : "&",
        getCurrentTime());
    return String.format("%s%s?%s&signature=%s",
        API_BASE,
        method,
        query,
        hmacSha256(key, query)
    );
  }

  static String httpGet(String query) {
    HttpGet httpGet = new HttpGet(query);
    try {
      HttpClient httpClient = HttpClients.custom()
          .setDefaultRequestConfig(RequestConfig.custom()
              .setCookieSpec(CookieSpecs.STANDARD).build())
          .build();

      HttpResponse response = httpClient.execute(httpGet);
      HttpEntity entity = response.getEntity();
      return EntityUtils.toString(entity);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      httpGet.releaseConnection();
    }
    return null;
  }

  public static String generateQuery(String method, String... params) {
    assert params.length % 2 == 0;
    StringBuilder builder = new StringBuilder(API_BASE);
    builder.append(method);
    if (params.length > 0) {
      builder.append('?').append(params[0]).append('=').append(params[1]);
      for (int i = 2; i < params.length; i += 2) {
        builder.append('&').append(params[i]).append('=').append(params[i + 1]);
      }
    }
    return builder.toString();
  }
}
