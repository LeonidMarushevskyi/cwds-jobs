package gov.ca.cwds.jobs.common.elastic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.ca.cwds.jobs.common.ChangedDTO;
import gov.ca.cwds.jobs.common.ElasticSearchIndexerDao;
import gov.ca.cwds.jobs.common.RecordChangeOperation;
import gov.ca.cwds.jobs.common.exception.JobsException;
import gov.ca.cwds.jobs.common.job.BulkWriter;
import gov.ca.cwds.jobs.common.job.utils.ConsumerCounter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.slf4j.LoggerFactory;


/**
 * @param <T> persistence class type
 * @author CWDS TPT-2
 */
public class ElasticWriter<T extends ChangedDTO<?>> implements BulkWriter<T> {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ElasticWriter.class);
  protected ElasticSearchIndexerDao elasticsearchDao;
  protected BulkProcessor bulkProcessor;
  protected ObjectMapper objectMapper;

  /**
   * Constructor.
   *
   * @param elasticsearchDao ES DAO
   * @param objectMapper Jackson object mapper
   */
  public ElasticWriter(ElasticSearchIndexerDao elasticsearchDao, ObjectMapper objectMapper) {
    this.elasticsearchDao = elasticsearchDao;
    this.objectMapper = objectMapper;
    bulkProcessor =
        BulkProcessor.builder(elasticsearchDao.getClient(), new BulkProcessor.Listener() {
          @Override
          public void beforeBulk(long executionId, BulkRequest request) {
            LOGGER.warn("Ready to execute bulk of {} actions", request.numberOfActions());
          }

          @Override
          public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
            LOGGER.warn("Executed bulk of {} actions", request.numberOfActions());
          }

          @Override
          public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
            LOGGER.error("ERROR EXECUTING BULK", failure);
          }
        }).build();
  }

  @Override
  public void write(List<T> items) {
    items.stream().forEach(item -> {
      try {
        RecordChangeOperation recordChangeOperation = item.getRecordChangeOperation();

        LOGGER.debug("Preparing to delete item: ID {}", item.getId());
        bulkProcessor.add(elasticsearchDao.bulkDelete(item.getId()));

        if (RecordChangeOperation.I == recordChangeOperation
            || RecordChangeOperation.U == recordChangeOperation) {
          LOGGER.debug("Preparing to insert item: ID {}", item.getId());
          bulkProcessor.add(elasticsearchDao.bulkAdd(objectMapper, item.getId(), item.getDTO()));
        }
      } catch (JsonProcessingException e) {
        throw new JobsException(e);
      }
    });
    bulkProcessor.flush();
    ConsumerCounter.addToCounter(items.size());
  }

  @Override
  public void destroy() {
    try {
      try {
        bulkProcessor.awaitClose(3000, TimeUnit.MILLISECONDS);
      } finally {
        elasticsearchDao.close();
      }
    } catch (IOException | InterruptedException e) {
      throw new JobsException(e);
    }
  }
}
