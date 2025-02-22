package au.com.dius.pactconsumer.data;


import android.content.Context;

import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pactconsumer.app.di.NetworkModule;
import au.com.dius.pactconsumer.data.model.Animal;
import au.com.dius.pactconsumer.data.model.ServiceResponse;
import au.com.dius.pactconsumer.util.DateHelper;
import io.reactivex.observers.TestObserver;

import static org.mockito.Mockito.mock;

@PactTestFor(
        providerName = "our_provider",
        port = "9292" // run stubserver on defined port. The client must send the requests against this port!
)
@ExtendWith(PactConsumerTestExt.class)
public class ServicePactTest {

  static final DateTime DATE_TIME;
  static final Map<String, String> HEADERS;

  static {
    DATE_TIME = DateTime.now();

    HEADERS = new HashMap<>();
    HEADERS.put("Content-Type", "application/json");
  }

  Service service;

  @BeforeEach
  public void setUp() {
    NetworkModule networkModule = new NetworkModule();
    service = new Service(networkModule.getRetrofit(mock(Context.class), "http://localhost:9292").create(Service.Api.class));
  }

  @Pact(provider = "our_provider", consumer = "our_consumer")
  public RequestResponsePact createFragment(PactDslWithProvider builder) throws UnsupportedEncodingException {
    PactDslJsonBody body = new PactDslJsonBody()
        .stringType("test")
        .stringType("valid_date", DateHelper.toString(DATE_TIME))
        .eachLike("animals", 3)
        .stringType("name", "Doggy")
        .stringType("image", "dog")
        .closeObject()
        .closeArray()
        .asBody();

    return builder
        .given("data count is > 0")
        .uponReceiving("a request for json data")
        .path("/provider.json")
        .method("GET")
        .query("valid_date=" + DateHelper.encodeDate(DATE_TIME))
        .willRespondWith()
        .status(200)
        .headers(HEADERS)
        .body(body)
        .toPact();
  }

  @Test
  @PactTestFor(pactMethod = "createFragment")
  public void should_process_the_json_payload_from_provider() {
    TestObserver<ServiceResponse> observer = service.fetchResponse(DATE_TIME).test();
    observer.assertNoErrors();
    observer.assertValue(ServiceResponse.create(DATE_TIME, Arrays.asList(
        Animal.create("Doggy", "dog"),
        Animal.create("Doggy", "dog"),
        Animal.create("Doggy", "dog")
    )));
  }
}
