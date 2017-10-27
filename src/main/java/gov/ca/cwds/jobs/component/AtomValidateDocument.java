package gov.ca.cwds.jobs.component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.slf4j.Logger;

import gov.ca.cwds.data.es.ElasticSearchPerson;
import gov.ca.cwds.data.es.ElasticsearchDao;
import gov.ca.cwds.jobs.exception.NeutronException;
import gov.ca.cwds.jobs.util.JobLogs;

public interface AtomValidateDocument {

  Logger getLogger();

  JobProgressTrack getTrack();

  ElasticsearchDao getEsDao();

  default boolean validateDocument(final ElasticSearchPerson person) throws NeutronException {
    return true;
  }

  default List<ElasticSearchPerson> validateDocuments() throws NeutronException {
    List<ElasticSearchPerson> persons = new ArrayList<>();
    final String[] docIds = getTrack().getAffectedDocumentIds();
    long totalHits = 0;

    if (docIds != null && docIds.length > 0) {
      final Client esClient = getEsDao().getClient();
      final MultiSearchResponse multiResponse = esClient.prepareMultiSearch()
          .add(esClient.prepareSearch()
              .setQuery(QueryBuilders.idsQuery().addIds(getTrack().getAffectedDocumentIds())))
          .get();

      for (MultiSearchResponse.Item item : multiResponse.getResponses()) {
        final SearchResponse response = item.getResponse();
        final SearchHits hits = response.getHits();
        totalHits += hits.getTotalHits();

        int docId = 0;
        String json;
        ElasticSearchPerson person;
        try {
          for (SearchHit hit : hits.getHits()) {
            docId = hit.docId();
            json = hit.getSourceAsString();

            getLogger().info("docId: {}", docId);
            getLogger().trace("json: {}", json);

            person = ElasticSearchPerson.readPerson(json);
            getLogger().trace("person: {}", person);

            validateDocument(person);
          }
        } catch (IOException e) {
          throw JobLogs.buildCheckedException(getLogger(), e, "ERROR READING DOCUMENT! doc id: {}",
              docId);
        }
      }
    }

    getLogger().info("total hits: {}", totalHits);
    return persons;
  }

}
