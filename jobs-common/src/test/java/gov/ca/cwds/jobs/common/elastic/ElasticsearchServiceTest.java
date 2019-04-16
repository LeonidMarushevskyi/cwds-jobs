package gov.ca.cwds.jobs.common.elastic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class ElasticsearchServiceTest {

  @Spy @InjectMocks private ElasticsearchService elasticsearchService;
  @Mock private ElasticsearchConfiguration configuration;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testCheckAliasExists() {
    when(configuration.getElasticsearchAlias()).thenReturn("elasticsearchAlias");
    ArgumentCaptor<GetAliasesRequest> getAliasesRequestCaptor =
        ArgumentCaptor.forClass(GetAliasesRequest.class);
    doReturn(true).when(elasticsearchService).checkAliasExists(any(GetAliasesRequest.class));

    boolean result = elasticsearchService.checkAliasExists();

    verify(elasticsearchService, times(1)).checkAliasExists(getAliasesRequestCaptor.capture());
    List<GetAliasesRequest> capturedRequest = getAliasesRequestCaptor.getAllValues();
    assertEquals(1, capturedRequest.size());
    assertFalse(capturedRequest.get(0).getShouldStoreResult());
    assertTrue(result);
    String[] aliases = capturedRequest.get(0).aliases();
    assertEquals(1, aliases.length);
    assertEquals("elasticsearchAlias", aliases[0]);
  }

  @Test
  public void testGetExistingIndex() {
    existingIndexHelper("index");
  }

  @Test(expected = IllegalStateException.class)
  public void testGetExistingIndexNotFound() {
    existingIndexHelper("notFoundIndex");
  }

  private void existingIndexHelper(String index) {
    when(configuration.getElasticsearchAlias()).thenReturn("elasticsearchAlias");
    List<String> indexes = Arrays.asList("indexName", "anotherIndex");
    doReturn(true).when(elasticsearchService).checkAliasExists();
    doReturn(indexes).when(elasticsearchService).getIndexesForAlias();
    doReturn(index).when(configuration).getElasticSearchIndexPrefix();

    String alias = elasticsearchService.getExistingIndex();

    verify(elasticsearchService, times(1)).getExistingIndex();
    assertEquals("indexName", alias);
  }

  @Test
  public void testHandleAliasesPerformAliasOperations() {
    initHandleAliases(true);

    verifyHandleAliases();
  }

  @Test
  public void testHandleAliasesCreateAliasWithOneIndex() {
    initHandleAliases(false);

    doReturn(true).when(elasticsearchService).checkIndicesExists(any(IndicesExistsRequest.class));
    doReturn(null).when(elasticsearchService).deleteIndex(any(DeleteIndexRequest.class));

    verifyHandleAliases();
  }

  private void initHandleAliases(boolean checkAliasExists) {
    when(configuration.getElasticsearchAlias()).thenReturn("elasticsearchAlias");
    List<String> indexes = Arrays.asList("indexName", "anotherIndex");
    doReturn(checkAliasExists).when(elasticsearchService).checkAliasExists();
    doReturn(indexes).when(elasticsearchService).getIndexesForAlias();
    doNothing().when(elasticsearchService).getAliasesAction(any(IndicesAliasesRequest.class));
    doReturn("index").when(configuration).getElasticSearchIndexPrefix();
    doReturn("index_Name").when(elasticsearchService).getIndexName();
  }

  private void verifyHandleAliases() {
    elasticsearchService.handleAliases();
    verify(elasticsearchService, times(1)).getAliasesAction(any(IndicesAliasesRequest.class));
  }
}
