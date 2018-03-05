package gov.ca.cwds.jobs.common.elastic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.ca.cwds.Identifiable;
import gov.ca.cwds.jobs.common.ElasticSearchIndexerDao;
import gov.ca.cwds.jobs.common.exception.JobsException;
import gov.ca.cwds.jobs.common.job.JobWriter;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * @author CWDS TPT-2
 *
 * @param <T> persistence class type
 */
public class ElasticJobWriter<T extends Identifiable<String>> implements JobWriter<T> {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ElasticJobWriter.class);
  protected ElasticSearchIndexerDao elasticsearchDao;
  protected BulkProcessor bulkProcessor;
  protected ObjectMapper objectMapper;

  /**
   * Constructor.
   * 
   * @param elasticsearchDao ES DAO
   * @param objectMapper Jackson object mapper
   */
  public ElasticJobWriter(ElasticSearchIndexerDao elasticsearchDao, ObjectMapper objectMapper) {
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
    items.stream().map(item -> {
      try {
        return elasticsearchDao.bulkAdd(objectMapper, item.getId(), item);
      } catch (JsonProcessingException e) {
        throw new JobsException(e);
      }
    }).forEach(bulkProcessor::add);
    bulkProcessor.flush();
  }

  @Override
  public void destroy() {
    try {
      try {
        bulkProcessor.awaitClose(3000, TimeUnit.MILLISECONDS);
      } finally {
        elasticsearchDao.close();
      }
    } catch (IOException |InterruptedException e) {
      throw new JobsException(e);
    }
  }
}
