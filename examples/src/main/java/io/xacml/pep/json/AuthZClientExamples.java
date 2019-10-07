package io.xacml.pep.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.xacml.json.model.Attribute;
import io.xacml.json.model.Category;
import io.xacml.json.model.Request;
import io.xacml.json.model.Response;
import io.xacml.json.model.Result;
import io.xacml.pep.json.client.AuthZClient;
import io.xacml.pep.json.client.ClientConfiguration;
import io.xacml.pep.json.client.DefaultClientConfiguration;
import io.xacml.pep.json.client.DefaultContextConfiguration;
import io.xacml.pep.json.client.feign.FeignAuthZClient;
import io.xacml.pep.json.client.jaxrs.JaxRsAuthZClient;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;

/**
 * This class contains sample code using JAX-RS to invoke a Policy Decision Point.
 * It supports both the JSON Profile of XACML 1.0 (where the response could be either an Object or
 * an Array) and the JSON Profile of XACML 1.1 (where the response is always an array - to simplify
 * things)
 *
 * @author djob
 */
@Slf4j
public class AuthZClientExamples {

  public static void main(String[] args) {

    String url = args[0];
    String username = args[1];
    String password = args[2];

    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    ClientConfiguration clientConfiguration = DefaultClientConfiguration.builder()
        .pdpUrl(url)
        .username(username)
        .password(password)
        .build();

    callPDPWithJaxRsClient(clientConfiguration, mapper, createRequest());
    callPDPWithFeignClient(clientConfiguration, mapper, createRequest());

    // Enable, if needed, basic authentication
    HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(username, password);

    Client client = ClientBuilder.newBuilder()
        .hostnameVerifier(DefaultContextConfiguration.getHostnameVerifier())
        .sslContext(DefaultContextConfiguration.getContext())
        .build();
    client.register(feature);
    WebTarget webTarget = client.target(url);

    callPDPWithJaxRsClient2(webTarget, createRequest());
  }

  private static void callPDPWithFeignClient(ClientConfiguration clientConfiguration, ObjectMapper mapper, Request request) {
    AuthZClient authZClient = new FeignAuthZClient(clientConfiguration, mapper);
    Response xacmlResponse = authZClient.makeAuthorizationRequest(request);
    for (Result r : xacmlResponse.getResults()) {
      log.debug("Decision: {}", r.getDecision());
    }
  }

  private static void callPDPWithJaxRsClient(ClientConfiguration clientConfiguration, ObjectMapper mapper, Request request) {
    AuthZClient authZClient = new JaxRsAuthZClient(clientConfiguration, mapper);
    Response xacmlResponse = authZClient.makeAuthorizationRequest(request);
    for (Result r : xacmlResponse.getResults()) {
      log.debug("Decision: {}", r.getDecision());
    }
  }

  /**
   * Show the full build of a JaxRs client and use to get a PDP response
   */
  private static void callPDPWithJaxRsClient2(WebTarget webTarget, Request request) {

    Invocation.Builder builder = webTarget.request("application/xacml+json");

    javax.ws.rs.core.Response response = builder.post(Entity.entity(request, "application/xacml+json"));
    Response xacmlResponse = response.readEntity(io.xacml.json.model.Response.class);
    for (Result r : xacmlResponse.getResults()) {
      log.debug("Decision: {}", r.getDecision());
    }
  }


  private static Request createRequest() {
    // Start building the XACML request.
    Request xacmlRequest = new Request();

    // Create iub attribute
    Category subject = new Category();
    subject.addAttribute(new Attribute("Attributes.access_subject.iub", "123"));
    // Create role attribute
    subject.addAttribute(new Attribute("Attributes.access_subject.role", "user"));

    // Add user attributes to the request.
    xacmlRequest.addAccessSubjectCategory(subject);

    Category resource = new Category();
    resource.addAttribute(new Attribute("bnc.object.objectId", "sbie"));
    xacmlRequest.addResourceCategory(resource);

    Category action = new Category();
    action.addAttribute(new Attribute("bnc.action.actionId", "access"));
    xacmlRequest.addActionCategory(action);


    xacmlRequest.setReturnPolicyIdList(true);
    return xacmlRequest;

  }
}
