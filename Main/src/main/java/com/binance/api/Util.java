package com.binance.api;

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

public final class Util {
  private Util() {

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
}
