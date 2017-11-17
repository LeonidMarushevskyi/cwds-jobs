package gov.ca.cwds.data.persistence.cms.rep;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import gov.ca.cwds.data.es.ElasticSearchLegacyDescriptor;
import gov.ca.cwds.data.es.ElasticSearchRaceAndEthnicity;
import gov.ca.cwds.data.persistence.cms.EsClientAddress;
import gov.ca.cwds.data.std.ApiAddressAware;
import gov.ca.cwds.data.std.ApiPhoneAware;
import gov.ca.cwds.jobs.Goddard;

public class ReplicatedClientTest extends Goddard<ReplicatedClient, EsClientAddress> {

  ReplicatedClient target;

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();
    target = new ReplicatedClient();
    target.setId(DEFAULT_CLIENT_ID);
  }

  @Test
  public void testReplicationOperation() throws Exception {
    target.setReplicationOperation(CmsReplicationOperation.I);
    CmsReplicationOperation actual = target.getReplicationOperation();
    CmsReplicationOperation expected = CmsReplicationOperation.I;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void testReplicationDate() throws Exception {
    DateFormat fmt = new SimpleDateFormat("yyyy-mm-dd");
    Date date = fmt.parse("2012-10-31");
    target.setReplicationDate(date);
    Date actual = target.getReplicationDate();
    Date expected = fmt.parse("2012-10-31");
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void type() throws Exception {
    assertThat(ReplicatedClient.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void getClientAddresses_Args__() throws Exception {
    Set<ReplicatedClientAddress> actual = target.getClientAddresses();
    Set<ReplicatedClientAddress> expected = new HashSet<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setClientAddresses_Args__Set() throws Exception {
    Set<ReplicatedClientAddress> clientAddresses = mock(Set.class);
    target.setClientAddresses(clientAddresses);
  }

  @Test
  public void addClientAddress_Args__ReplicatedClientAddress() throws Exception {
    ReplicatedClientAddress clientAddress = mock(ReplicatedClientAddress.class);
    target.addClientAddress(clientAddress);
  }

  @Test
  public void getAddresses_Args__() throws Exception {
    ApiAddressAware[] actual = target.getAddresses();
    ApiAddressAware[] expected = new ApiAddressAware[0];
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getPhones_Args__() throws Exception {
    ApiPhoneAware[] actual = target.getPhones();
    ApiPhoneAware[] expected = new ApiPhoneAware[0];
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getLegacyId_Args__() throws Exception {
    String actual = target.getLegacyId();
    String expected = DEFAULT_CLIENT_ID;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void toString_Args__() throws Exception {
    String actual = target.toString();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void hashCode_Args__() throws Exception {
    int actual = target.hashCode();
    assertThat(actual, is(not(0)));
  }

  @Test
  public void equals_Args__Object() throws Exception {
    Object obj = null;
    boolean actual = target.equals(obj);
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getLegacyDescriptor_Args__() throws Exception {
    Date lastUpdatedTime = new Date();
    target.setReplicationOperation(CmsReplicationOperation.U);
    target.setLastUpdatedId("0x5");
    target.setLastUpdatedTime(lastUpdatedTime);
    target.setReplicationDate(lastUpdatedTime);
    ElasticSearchLegacyDescriptor actual = target.getLegacyDescriptor();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void getClientCounty_Args__() throws Exception {
    Short actual = target.getClientCounty();
    Short expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setClientCounty_Args__Short() throws Exception {
    Short clinetCountyId = null;
    target.setClientCounty(clinetCountyId);
  }

  @Test
  public void getClientRaces_Args__() throws Exception {
    List<Short> actual = target.getClientRaces();
    List<Short> expected = new ArrayList<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setClientRaces_Args__List() throws Exception {
    List<Short> clientRaces = new ArrayList<Short>();
    target.setClientRaces(clientRaces);
  }

  @Test
  public void addClientRace_Args__Short() throws Exception {
    Short clientRace = null;
    target.addClientRace(clientRace);
  }

  @Test
  public void getReplicatedEntity_Args__() throws Exception {
    EmbeddableCmsReplicatedEntity actual = target.getReplicatedEntity();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void getRaceAndEthnicity_Args__() throws Exception {
    final List<Short> clientRaces = new ArrayList<>();
    clientRaces.add((short) 825);
    clientRaces.add((short) 824);
    clientRaces.add((short) 3164);

    target.setClientRaces(clientRaces);
    ElasticSearchRaceAndEthnicity actual = target.getRaceAndEthnicity();
    assertThat(actual, is(notNullValue()));
  }

}
