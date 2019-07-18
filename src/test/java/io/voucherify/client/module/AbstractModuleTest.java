package io.voucherify.client.module;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;
import io.voucherify.client.VoucherifyClient;
import io.voucherify.client.callback.VoucherifyCallback;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import retrofit.RestAdapter;

import java.io.IOException;
import java.util.concurrent.Callable;

public class AbstractModuleTest {

  protected ObjectMapper mapper = new ObjectMapper();
  protected static VoucherifyClient client;
  private static MockWebServer server;
  private boolean[] callbackFired = new boolean[]{false};

  @BeforeClass
  public static void onSetup() throws IOException {
    server = new MockWebServer();
    server.play();
    client = new VoucherifyClient.Builder()
            .setClientSecretKey("some token")
            .setAppId("some app id")
            .setLogLevel(RestAdapter.LogLevel.FULL)
            .withoutSSL()
            .setEndpoint(server.getUrl("").toString().replaceFirst("http://", ""))
            .build();
  }

  protected void enqueueResponse(Object body) {
    try {
      server.enqueue(new MockResponse().setBody(mapper.writeValueAsString(body)).setResponseCode(200));
    } catch (JsonProcessingException ignore) {}
  }

  protected void enqueueResponse(String body) {
    server.enqueue(new MockResponse().setBody(body).setResponseCode(200));
  }

  void enqueueEmptyResponse() {
    server.enqueue(new MockResponse().setResponseCode(204));
  }

  RecordedRequest getRequest() {
    try {
      return server.takeRequest();
    } catch (InterruptedException e) {
      return null;
    }
  }

  VoucherifyCallback createCallback() {
    return new VoucherifyCallback<Object>() {
      @Override
      public void onSuccess(Object result) {
        callbackFired[0] = true;
      }
    };
  }

  Callable<Boolean> wasCallbackFired() {
    return new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        return callbackFired[0];
      }
    };
  }

  @After
  public void afterEach() {
    callbackFired[0] = false;
  }

  @AfterClass
  public static void onTeardown() throws IOException {
    server.shutdown();
  }

}
