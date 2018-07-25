package gov.ca.cwds.jobs.cap.users;

import com.google.inject.Inject;
import gov.ca.cwds.jobs.cap.users.dto.ChangedUserDto;
import gov.ca.cwds.jobs.cap.users.iterator.CapUsersIterator;
import gov.ca.cwds.jobs.common.elastic.ElasticSearchBulkCollector;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

public class CapUsersBatchProcessor {

  @Inject
  private CapUsersIterator capUsersJobBatchIterator;

  @Inject
  private ElasticSearchBulkCollector<ChangedUserDto> elasticSearchBulkCollector;

  public void processBatches() {
    List<ChangedUserDto> portion = capUsersJobBatchIterator.getNextPortion();
    while (!CollectionUtils.isEmpty(portion)) {
      loadEntities(portion);
      portion = capUsersJobBatchIterator.getNextPortion();
    }
    elasticSearchBulkCollector.flush();
  }

  private void loadEntities(List<ChangedUserDto> userList) {
    userList.forEach(elasticSearchBulkCollector::addEntity);
  }

  public void destroy() {
    elasticSearchBulkCollector.destroy();
  }
}
