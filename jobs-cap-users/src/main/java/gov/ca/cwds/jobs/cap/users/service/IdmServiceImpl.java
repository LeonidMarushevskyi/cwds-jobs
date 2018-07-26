package gov.ca.cwds.jobs.cap.users.service;

import com.google.inject.Inject;
import gov.ca.cwds.idm.dto.User;
import gov.ca.cwds.idm.dto.UsersPage;
import gov.ca.cwds.jobs.cap.users.inject.PerryApiPassword;
import gov.ca.cwds.jobs.cap.users.inject.PerryApiUrl;
import gov.ca.cwds.jobs.cap.users.inject.PerryApiUser;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

public class IdmServiceImpl implements IdmService {

  private static final String PAGINATION_TOKEN = "paginationToken";

  private static final Logger LOGGER = LoggerFactory.getLogger(IdmServiceImpl.class);


  @Inject
  private Client client;

  @Inject
  @PerryApiUrl
  private String apiURL;

  @Inject
  @PerryApiUser
  private String perryApiUser;

  @Inject
  @PerryApiPassword
  private String perryApiPassword;

  private String basicAuthHeader;

  @Inject
  public void init() {
    String authString = perryApiUser + ":" + perryApiPassword;
    byte[] authEncBytes = Base64.encodeBase64(authString.getBytes(StandardCharsets.UTF_8));
    String authStringEnc = new String(authEncBytes, StandardCharsets.UTF_8);
    basicAuthHeader = "Basic " + authStringEnc;
  }

  @Override
  public UsersPage getUserPage(String paginationToken) {
    return client.target(apiURL + "/users")
            .queryParam(PAGINATION_TOKEN, paginationToken)
            .request(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, basicAuthHeader)
            .get(UsersPage.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<User> getUsersByRacfIds(Set<String> racfIds) {

    Response response = client
            .target(apiURL + "/users/search")
            .request(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, basicAuthHeader)
            .post(Entity.entity(racfIds, MediaType.APPLICATION_JSON));

    return response.readEntity(new GenericType<List<User>>() {
    });


  }

  //TODO change after COG-333 is done
  @Override
  public void getCapChanges() {
    LOGGER.info("CAP changes to be added");
  }
}
