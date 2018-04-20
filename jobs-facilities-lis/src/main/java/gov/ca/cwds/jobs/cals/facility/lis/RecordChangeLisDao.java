package gov.ca.cwds.jobs.cals.facility.lis;

import static gov.ca.cwds.jobs.cals.facility.lis.LisRecordChange.INCREMENTAL_LOAD_SQL;
import static gov.ca.cwds.jobs.cals.facility.lis.LisRecordChange.INITIAL_LOAD_SQL;
import static gov.ca.cwds.jobs.cals.facility.lis.LisRecordChange.LIS_INCREMENTAL_LOAD_QUERY_NAME;

import com.google.inject.Inject;
import gov.ca.cwds.cals.inject.LisSessionFactory;
import gov.ca.cwds.data.BaseDaoImpl;
import gov.ca.cwds.data.stream.QueryCreator;
import gov.ca.cwds.jobs.common.batch.PageRequest;
import java.math.BigInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

/**
 * @author CWDS CALS API Team
 */
public class RecordChangeLisDao extends BaseDaoImpl<LisRecordChange> {

  @Inject
  public RecordChangeLisDao(@LisSessionFactory SessionFactory sessionFactory) {
    super(sessionFactory);
  }

  public Stream<LisRecordChange> getInitialLoadStream(
      PageRequest pageRequest) {

    QueryCreator<LisRecordChange> queryCreator = buildNativeQueryCreator(INITIAL_LOAD_SQL,
        pageRequest, query -> query.setParameter("facNbr", pageRequest.getLastId()));
    return new LisRecordChangesStreamer(this, queryCreator).createStream();
  }

  public Stream<LisRecordChange> getIncrementalLoadStream(final BigInteger dateAfter,
      PageRequest pageRequest) {
    QueryCreator<LisRecordChange> queryCreator = buildNativeQueryCreator(
        INCREMENTAL_LOAD_SQL,
        pageRequest, query -> query.setParameter("dateAfter", dateAfter));
    return new LisRecordChangesStreamer(this, queryCreator).createStream();
  }

  private QueryCreator<LisRecordChange> buildQueryCreator(String queryName,
      PageRequest pageRequest,
      Consumer<Query<LisRecordChange>> parametersSetter) {
    return (session, entityClass) -> prepareQuery(session, pageRequest, queryName,
        parametersSetter);
  }

  private QueryCreator<LisRecordChange> buildNativeQueryCreator(String nativeQuery,
      PageRequest pageRequest,
      Consumer<Query<LisRecordChange>> parametersSetter) {
    return (session, entityClass) -> prepareNativeQuery(session, pageRequest, nativeQuery,
        parametersSetter);
  }

  private Query<LisRecordChange> prepareQuery(Session session, PageRequest pageRequest,
      String queryName, Consumer<Query<LisRecordChange>> parametersSetter) {
    Query<LisRecordChange> query = session
        .createNamedQuery(queryName, LisRecordChange.class)
        .setMaxResults(pageRequest.getLimit())
        .setReadOnly(true);
    parametersSetter.accept(query);
    return query;
  }

  private Query<LisRecordChange> prepareNativeQuery(Session session, PageRequest pageRequest,
      String nativeQuery, Consumer<Query<LisRecordChange>> parametersSetter) {
    Query<LisRecordChange> query = session.createNativeQuery(nativeQuery, LisRecordChange.class)
        .setMaxResults(pageRequest.getLimit())
        .setReadOnly(true);
    parametersSetter.accept(query);
    return query;
  }


}
