package gov.ca.cwds.jobs.common.api;

import gov.ca.cwds.jobs.common.identifier.ChangedEntityIdentifier;

/**
 * This service uses target API to load target entity by identifier.
 *
 * Created by Alexander Serbin on 2/5/2018.
 */
@FunctionalInterface
public interface ChangedEntityService<T> {

  /**
   * Loads entity from the source. Usually target API services are used to retrieve entity
   * by identifier.
   *
   * @return loaded target entity
   */
  T loadEntity(ChangedEntityIdentifier identifier);

}
