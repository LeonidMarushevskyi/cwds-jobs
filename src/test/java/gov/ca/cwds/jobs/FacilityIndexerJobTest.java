package gov.ca.cwds.jobs;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.function.Function;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import gov.ca.cwds.data.es.Elasticsearch5xDao;
import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.jobs.component.Job;
import gov.ca.cwds.jobs.config.JobConfiguration;
import gov.ca.cwds.jobs.facility.FacilityProcessor;
import gov.ca.cwds.jobs.facility.FacilityRowMapper;
import gov.ca.cwds.jobs.util.JobProcessor;
import gov.ca.cwds.jobs.util.JobReader;
import gov.ca.cwds.jobs.util.JobWriter;

public class FacilityIndexerJobTest
    extends PersonJobTester<PersistentObject, ApiGroupNormalizer<?>> {

  private static class TestFacilityIndexerJob extends FacilityIndexerJob {
    public TestFacilityIndexerJob(File config) {
      super(config);
    }

    @Override
    protected TransportClient produceTransportClient(Settings settings) {
      TransportClient client = mock(TransportClient.class);
      return client;
    }

  }

  File config;
  TestFacilityIndexerJob target;

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();
    target = new TestFacilityIndexerJob(config);
  }

  @Test
  public void type() throws Exception {
    assertThat(FacilityIndexerJob.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void elasticsearchClient_Args__JobConfiguration() throws Exception {
    JobConfiguration config_ = mock(JobConfiguration.class);
    when(config_.getElasticsearchCluster()).thenReturn("elasticsearch");
    when(config_.getElasticsearchHost()).thenReturn("localhost");
    when(config_.getElasticsearchPort()).thenReturn("9200");
    Client actual = target.elasticsearchClient(config_);
    assertThat(actual, notNullValue());
  }

  @Test
  public void elasticsearchDao_Args__Client__JobConfiguration() throws Exception {
    Client client = mock(Client.class);
    JobConfiguration configuration = mock(JobConfiguration.class);
    Elasticsearch5xDao actual = target.elasticsearchDao(client, configuration);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void config_Args__() throws Exception {
    JobConfiguration actual = target.config();
    JobConfiguration expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void lisItemReader_Args__JobConfiguration__FacilityRowMapper__SessionFactory()
      throws Exception {
    JobConfiguration jobConfiguration = mock(JobConfiguration.class);
    FacilityRowMapper facilityRowMapper = mock(FacilityRowMapper.class);
    SessionFactory sessionFactory = mock(SessionFactory.class);
    PreparedStatement statement = mock(PreparedStatement.class);
    Function<Connection, PreparedStatement> func = c -> statement;

    JobReader actual =
        target.lisItemReader(jobConfiguration, facilityRowMapper, sessionFactory, func);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void lisItemProcessor_Args__() throws Exception {
    JobProcessor actual = target.lisItemProcessor();
    JobProcessor expected = new FacilityProcessor();
    assertThat(actual.getClass(), is(equalTo(expected.getClass())));
  }

  @Test
  @Ignore
  public void lisItemWriter_Args__Elasticsearch5xDao__ObjectMapper() throws Exception {
    Elasticsearch5xDao elasticsearchDao = mock(Elasticsearch5xDao.class);
    JobWriter actual = target.lisItemWriter(elasticsearchDao, MAPPER);
    JobWriter expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void lisItemWriter_Args__JobReader__JobProcessor__JobWriter() throws Exception {
    JobReader jobReader = mock(JobReader.class);
    JobProcessor jobProcessor = mock(JobProcessor.class);
    JobWriter jobWriter = mock(JobWriter.class);
    Job actual = target.lisItemWriter(jobReader, jobProcessor, jobWriter);
    assertThat(actual, is(notNullValue()));
  }

}
