package com.binance.api;

import org.apache.commons.codec.digest.HmacUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;

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

  public static String generateSigQuery(String method, String key, String... params) {
    String[] extended = Arrays.copyOf(params, params.length + 2);
    extended[params.length] = "timestamp";
    extended[params.length + 1] = Long.toString(Util.getCurrentTime());
    StringBuilder paramsBuilder = getParamsBuilder(extended);
    String query = paramsBuilder.toString();
    paramsBuilder.append("&signature=").append(hmacSha256(key, query));
    StringBuilder baseBuilder = getBaseBuilder(method);
    return generateQuery(baseBuilder, paramsBuilder);
  }

  static String httpGet(String query,  Header... headers) {
    return getResponse(new HttpGet(query), headers);
  }

  static String httpPost(String query, Header... headers) {
    return getResponse(new HttpPost(query), headers);
  }

  static String httpDelete(String query, Header... headers) {
    return getResponse(new HttpDelete(query), headers);
  }

  static String httpPut(String query, Header... headers) {
    return getResponse(new HttpPut(query), headers);
  }

  private static String getResponse(HttpRequestBase request,  Header... headers) {
    for (Header h : headers) {
      request.addHeader(h);
    }
    try {
      HttpClient httpClient = HttpClients.custom()
          .setDefaultRequestConfig(RequestConfig.custom()
              .setCookieSpec(CookieSpecs.STANDARD).build())
          .build();

      HttpResponse response = httpClient.execute(request);
      HttpEntity entity = response.getEntity();
      return EntityUtils.toString(entity);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      request.releaseConnection();
    }
    return null;
  }

  public static String generateQuery(String method, String... params) {
    return generateQuery(getBaseBuilder(method), getParamsBuilder(params));
  }

  private static StringBuilder getBaseBuilder(String method) {
    StringBuilder builder = new StringBuilder(API_BASE);
    builder.append(method);
    return builder;
  }

  private static String generateQuery(StringBuilder builder, StringBuilder params) {
    return builder.append(new StringBuilder("?").append(params)).toString();
  }

  private static StringBuilder getParamsBuilder(String... params) {
    assert params.length % 2 == 0;
    StringBuilder builder = new StringBuilder();
    if (params.length > 0) {
      builder.append(params[0]).append('=').append(params[1]);
      for (int i = 2; i < params.length; i += 2) {
        builder.append('&').append(params[i]).append('=').append(params[i + 1]);
      }
    }
    return builder;
  }
}
