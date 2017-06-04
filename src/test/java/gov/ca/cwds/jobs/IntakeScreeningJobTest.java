package gov.ca.cwds.jobs;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.SessionFactory;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.ca.cwds.dao.ns.EsIntakeScreeningDao;
import gov.ca.cwds.dao.ns.IntakeParticipantDao;
import gov.ca.cwds.data.ApiTypedIdentifier;
import gov.ca.cwds.data.es.ElasticSearchPerson;
import gov.ca.cwds.data.es.ElasticSearchPerson.ESOptionalCollection;
import gov.ca.cwds.data.es.ElasticsearchDao;
import gov.ca.cwds.data.persistence.ns.EsIntakeScreening;
import gov.ca.cwds.data.persistence.ns.IntakeParticipant;

public class IntakeScreeningJobTest {

  @Test
  public void type() throws Exception {
    assertThat(IntakeScreeningJob.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    IntakeParticipantDao normalizedDao = null;
    EsIntakeScreeningDao viewDao = null;
    ElasticsearchDao esDao = null;
    String lastJobRunTimeFilename = null;
    ObjectMapper mapper = null;
    SessionFactory sessionFactory = null;
    IntakeScreeningJob target = new IntakeScreeningJob(normalizedDao, viewDao, esDao,
        lastJobRunTimeFilename, mapper, sessionFactory);
    assertThat(target, notNullValue());
  }

  // TODO: devise plan to test threads.
  // @Test
  // public void threadExtractJdbc_Args__() throws Exception {
  // IntakeParticipantDao normalizedDao = null;
  // EsIntakeScreeningDao viewDao = null;
  // ElasticsearchDao esDao = null;
  // String lastJobRunTimeFilename = null;
  // ObjectMapper mapper = null;
  // SessionFactory sessionFactory = null;
  // final IntakeScreeningJob target = new IntakeScreeningJob(normalizedDao, viewDao, esDao,
  // lastJobRunTimeFilename, mapper, sessionFactory);
  // // given
  // // e.g. : given(mocked.called()).willReturn(1);
  // // when
  // target.threadExtractJdbc();
  // // then
  // // e.g. : verify(mocked).called();
  // }

  @Test
  public void getDenormalizedClass_Args__() throws Exception {
    IntakeParticipantDao normalizedDao = null;
    EsIntakeScreeningDao viewDao = null;
    ElasticsearchDao esDao = null;
    String lastJobRunTimeFilename = null;
    ObjectMapper mapper = null;
    SessionFactory sessionFactory = null;
    final IntakeScreeningJob target = new IntakeScreeningJob(normalizedDao, viewDao, esDao,
        lastJobRunTimeFilename, mapper, sessionFactory);
    final Object actual = target.getDenormalizedClass();
    final Object expected = EsIntakeScreening.class;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getViewName_Args__() throws Exception {
    IntakeParticipantDao normalizedDao = null;
    EsIntakeScreeningDao viewDao = null;
    ElasticsearchDao esDao = null;
    String lastJobRunTimeFilename = null;
    ObjectMapper mapper = null;
    SessionFactory sessionFactory = null;
    IntakeScreeningJob target = new IntakeScreeningJob(normalizedDao, viewDao, esDao,
        lastJobRunTimeFilename, mapper, sessionFactory);
    final String actual = target.getViewName();
    final String expected = "VW_SCREENING_HISTORY";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void keepCollections_Args__() throws Exception {
    IntakeParticipantDao normalizedDao = null;
    EsIntakeScreeningDao viewDao = null;
    ElasticsearchDao esDao = null;
    String lastJobRunTimeFilename = null;
    ObjectMapper mapper = null;
    SessionFactory sessionFactory = null;
    final IntakeScreeningJob target = new IntakeScreeningJob(normalizedDao, viewDao, esDao,
        lastJobRunTimeFilename, mapper, sessionFactory);
    final ESOptionalCollection[] actual = target.keepCollections();
    final ESOptionalCollection[] expected =
        new ESOptionalCollection[] {ESOptionalCollection.SCREENING};;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getOptionalElementName_Args__() throws Exception {
    IntakeParticipantDao normalizedDao = null;
    EsIntakeScreeningDao viewDao = null;
    ElasticsearchDao esDao = null;
    String lastJobRunTimeFilename = null;
    ObjectMapper mapper = null;
    SessionFactory sessionFactory = null;
    final IntakeScreeningJob target = new IntakeScreeningJob(normalizedDao, viewDao, esDao,
        lastJobRunTimeFilename, mapper, sessionFactory);
    // given
    // e.g. : given(mocked.called()).willReturn(1);
    // when
    final String actual = target.getOptionalElementName();
    // then
    // e.g. : verify(mocked).called();
    final String expected = "screenings";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setInsertCollections_Args__ElasticSearchPerson__IntakeParticipant__List()
      throws Exception {
    IntakeParticipantDao normalizedDao = null;
    EsIntakeScreeningDao viewDao = null;
    ElasticsearchDao esDao = null;
    String lastJobRunTimeFilename = null;
    ObjectMapper mapper = null;
    SessionFactory sessionFactory = null;
    IntakeScreeningJob target = new IntakeScreeningJob(normalizedDao, viewDao, esDao,
        lastJobRunTimeFilename, mapper, sessionFactory);
    // given
    ElasticSearchPerson esp = mock(ElasticSearchPerson.class);
    IntakeParticipant t = mock(IntakeParticipant.class);
    List list = new ArrayList();
    // e.g. : given(mocked.called()).willReturn(1);
    // when
    target.setInsertCollections(esp, t, list);
    // then
    // e.g. : verify(mocked).called();
  }

  @Test
  public void getOptionalCollection_Args__ElasticSearchPerson__IntakeParticipant()
      throws Exception {
    IntakeParticipantDao normalizedDao = null;
    EsIntakeScreeningDao viewDao = null;
    ElasticsearchDao esDao = null;
    String lastJobRunTimeFilename = null;
    ObjectMapper mapper = null;
    SessionFactory sessionFactory = null;
    final IntakeScreeningJob target = new IntakeScreeningJob(normalizedDao, viewDao, esDao,
        lastJobRunTimeFilename, mapper, sessionFactory);
    // given
    final ElasticSearchPerson esp = mock(ElasticSearchPerson.class);
    final IntakeParticipant t = mock(IntakeParticipant.class);
    // e.g. : given(mocked.called()).willReturn(1);
    // when
    final List<? extends ApiTypedIdentifier<String>> actual = target.getOptionalCollection(esp, t);
    // then
    // e.g. : verify(mocked).called();
    final Object expected = new ArrayList<ElasticSearchPerson.ElasticSearchPersonScreening>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void normalize_Args__List() throws Exception {
    IntakeParticipantDao normalizedDao = null;
    EsIntakeScreeningDao viewDao = null;
    ElasticsearchDao esDao = null;
    String lastJobRunTimeFilename = null;
    ObjectMapper mapper = null;
    SessionFactory sessionFactory = null;
    final IntakeScreeningJob target = new IntakeScreeningJob(normalizedDao, viewDao, esDao,
        lastJobRunTimeFilename, mapper, sessionFactory);
    // given
    final List<EsIntakeScreening> recs = new ArrayList<EsIntakeScreening>();
    // e.g. : given(mocked.called()).willReturn(1);
    // when
    final List<IntakeParticipant> actual = target.normalize(recs);
    // then
    // e.g. : verify(mocked).called();
    final List<IntakeParticipant> expected = new ArrayList<>();
    assertThat(actual, is(equalTo(expected)));
  }

  // @Test
  // public void main_Args__StringArray() throws Exception {
  // // given
  // String[] args = new String[] {};
  // // e.g. : given(mocked.called()).willReturn(1);
  // // when
  // IntakeScreeningJob.main(args);
  // // then
  // // e.g. : verify(mocked).called();
  // }

}