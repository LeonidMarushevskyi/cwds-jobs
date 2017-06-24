package gov.ca.cwds.data.persistence.cms;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import gov.ca.cwds.data.es.ElasticSearchPerson.ElasticSearchPersonRelationship;

public class EsRelationshipTest {

  @Test
  public void type() throws Exception {
    assertThat(EsRelationship.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    EsRelationship target = new EsRelationship();
    assertThat(target, notNullValue());
  }

  @Test
  public void mapRow_Args__ResultSet() throws Exception {
    ResultSet rs = mock(ResultSet.class);
    EsRelationship actual = EsRelationship.mapRow(rs);
    EsRelationship expected = new EsRelationship();
    expected.setRelCode(Short.valueOf((short) 0));
    expected.setReverseRelationship(false);
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void mapRow_Args__ResultSet_T__SQLException() throws Exception {
    ResultSet rs = mock(ResultSet.class);
    doThrow(new SQLException()).when(rs).getString(any());
    try {
      EsRelationship.mapRow(rs);
      fail("Expected exception was not thrown!");
    } catch (SQLException e) {

    }
  }

  @Test
  public void getNormalizationClass_Args__() throws Exception {
    EsRelationship target = new EsRelationship();
    Class<ReplicatedRelationships> actual = target.getNormalizationClass();
    Class<ReplicatedRelationships> expected = ReplicatedRelationships.class;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void parseBiDirectionalRelationship_Args__ElasticSearchPersonRelationship()
      throws Exception {
    EsRelationship target = new EsRelationship();
    ElasticSearchPersonRelationship rel = mock(ElasticSearchPersonRelationship.class);
    target.parseBiDirectionalRelationship(rel);
  }

  @Test
  public void normalize_Args__Map() throws Exception {
    EsRelationship target = new EsRelationship();
    Map<Object, ReplicatedRelationships> map = new HashMap<Object, ReplicatedRelationships>();
    ReplicatedRelationships actual = target.normalize(map);
    ReplicatedRelationships expected = new ReplicatedRelationships();
    expected.addRelation(new ElasticSearchPersonRelationship());
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getNormalizationGroupKey_Args__() throws Exception {
    EsRelationship target = new EsRelationship();
    Object actual = target.getNormalizationGroupKey();
    Object expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getPrimaryKey_Args__() throws Exception {
    EsRelationship target = new EsRelationship();
    Serializable actual = target.getPrimaryKey();
    Serializable expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void hashCode_Args__() throws Exception {
    EsRelationship target = new EsRelationship();
    int actual = target.hashCode();
    int expected = -1130789619;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void equals_Args__Object() throws Exception {
    EsRelationship target = new EsRelationship();
    Object obj = null;
    boolean actual = target.equals(obj);
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  // @Test
  public void toString_Args__() throws Exception {
    EsRelationship target = new EsRelationship();
    String actual = target.toString();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getThisLegacyId_Args__() throws Exception {
    EsRelationship target = new EsRelationship();
    String actual = target.getThisLegacyId();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setThisLegacyId_Args__String() throws Exception {
    EsRelationship target = new EsRelationship();
    String thisLegacyId = null;
    target.setThisLegacyId(thisLegacyId);
  }

  @Test
  public void getThisFirstName_Args__() throws Exception {
    EsRelationship target = new EsRelationship();
    String actual = target.getThisFirstName();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setThisFirstName_Args__String() throws Exception {
    EsRelationship target = new EsRelationship();
    String thisFirstName = null;
    target.setThisFirstName(thisFirstName);
  }

  @Test
  public void getThisLastName_Args__() throws Exception {
    EsRelationship target = new EsRelationship();
    String actual = target.getThisLastName();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setThisLastName_Args__String() throws Exception {
    EsRelationship target = new EsRelationship();
    String thisLastName = null;
    target.setThisLastName(thisLastName);
  }

  @Test
  public void getRelCode_Args__() throws Exception {
    EsRelationship target = new EsRelationship();
    Short actual = target.getRelCode();
    Short expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setRelCode_Args__Short() throws Exception {
    EsRelationship target = new EsRelationship();
    Short relCode = null;
    target.setRelCode(relCode);
  }

  @Test
  public void getRelatedLegacyId_Args__() throws Exception {
    EsRelationship target = new EsRelationship();
    String actual = target.getRelatedLegacyId();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setRelatedLegacyId_Args__String() throws Exception {
    EsRelationship target = new EsRelationship();
    String relatedLegacyId = null;
    target.setRelatedLegacyId(relatedLegacyId);
  }

  @Test
  public void getRelatedFirstName_Args__() throws Exception {
    EsRelationship target = new EsRelationship();
    String actual = target.getRelatedFirstName();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setRelatedFirstName_Args__String() throws Exception {
    EsRelationship target = new EsRelationship();
    String relatedFirstName = null;
    target.setRelatedFirstName(relatedFirstName);
  }

  @Test
  public void getRelatedLastName_Args__() throws Exception {
    EsRelationship target = new EsRelationship();
    String actual = target.getRelatedLastName();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setRelatedLastName_Args__String() throws Exception {
    EsRelationship target = new EsRelationship();
    String relatedLastName = null;
    target.setRelatedLastName(relatedLastName);
  }

  @Test
  public void getReverseRelationship_Args__() throws Exception {
    EsRelationship target = new EsRelationship();
    Boolean actual = target.getReverseRelationship();
    Boolean expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setReverseRelationship_Args__Boolean() throws Exception {
    EsRelationship target = new EsRelationship();
    Boolean reverseRelationship = null;
    target.setReverseRelationship(reverseRelationship);
  }

}
