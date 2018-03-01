package gov.ca.cwds.jobs.cals.facility;

import com.google.inject.Inject;
import gov.ca.cwds.data.BaseDaoImpl;
import gov.ca.cwds.data.stream.QueryCreator;
import gov.ca.cwds.inject.CmsSessionFactory;
import org.hibernate.SessionFactory;

import java.time.LocalDateTime;
import java.util.stream.Stream;

/**
 * @author CWDS TPT-2
 */
public class RecordChangeCwsCmsDao extends BaseDaoImpl<RecordChange> {

  @Inject
  public RecordChangeCwsCmsDao(@CmsSessionFactory SessionFactory sessionFactory) {
    super(sessionFactory);
  }

  @SuppressWarnings("unchecked")
  public Stream<RecordChange> getInitialLoadStream() {
    QueryCreator<RecordChange> queryCreator = (session, entityClass) -> session
        .getNamedNativeQuery("RecordChange.cwscmsInitialLoadQuery")
        .setReadOnly(true);
    return new RecordChangesStreamer(this, queryCreator).createStream();
  }

  @SuppressWarnings("unchecked")
  public Stream<RecordChange> getIncrementalLoadStream(final LocalDateTime dateAfter) {
    QueryCreator<RecordChange> queryCreator = (session, entityClass) -> session
            .getNamedNativeQuery("RecordChange.cwscmsIncrementalLoadQuery")
            .setParameter("dateAfter", dateAfter)
            .setReadOnly(true);
    return new RecordChangesStreamer(this, queryCreator).createStream();
  }

}
