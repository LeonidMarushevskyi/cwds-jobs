package gov.ca.cwds.jobs;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import gov.ca.cwds.data.ApiTypedIdentifier;
import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.std.ApiPersonAware;

@JsonPropertyOrder(alphabetic = true)
public class TestNormalizedEntity
    implements PersistentObject, ApiPersonAware, ApiTypedIdentifier<String> {

  private String id;

  private String firstName;

  private String lastName;

  private String title;

  public TestNormalizedEntity(String id) {
    this.id = id;
  }

  @Override
  public Serializable getPrimaryKey() {
    return id;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  @Override
  public Date getBirthDate() {
    return null;
  }

  @Override
  public String getFirstName() {
    return firstName;
  }

  @Override
  public String getGender() {
    return null;
  }

  @Override
  public String getLastName() {
    return lastName;
  }

  @Override
  public String getMiddleName() {
    return null;
  }

  @Override
  public String getNameSuffix() {
    return null;
  }

  @Override
  public String getSsn() {
    return null;
  }

  public String getName() {
    return firstName;
  }

  public void setName(String name) {
    this.firstName = name;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

}
