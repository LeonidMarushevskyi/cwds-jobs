package gov.ca.cwds.dao.cms;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import gov.ca.cwds.dao.DaoException;
import gov.ca.cwds.dao.DocumentMetadataDao;
import gov.ca.cwds.data.model.cms.DocumentMetadata;
import gov.ca.cwds.inject.CmsSessionFactory;

/**
 * Implementation of {@link DocumentMetadataDao} which is backed by Hibernate
 * 
 * @author CWDS API Team
 */
@Singleton
public class DocumentMetadataDaoImpl implements DocumentMetadataDao {

  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(DocumentMetadataDaoImpl.class);

  private String TIMESTAMP_FORMAT = "YYYY-MM-DD HH24:MI:SS";
  private DateFormat dateFormat = new SimpleDateFormat(TIMESTAMP_FORMAT);

  private SessionFactory sessionFactory;

  /**
   * Constructor
   * 
   * @param sessionFactory the sessionFactory
   */
  @Inject
  public DocumentMetadataDaoImpl(@CmsSessionFactory SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.ca.cwds.dao.cms.DocumentMetadataDao#findByLastJobRunTimeMinusOneMinute(java.util.Date)
   */
  @SuppressWarnings({"unchecked"})
  @Override
  public List<DocumentMetadata> findByLastJobRunTimeMinusOneMinute(Date lastJobRunTime) {
    // TODO - abstract out the transaction management
    Session session = sessionFactory.getCurrentSession();
    Transaction txn = null;
    try {
      txn = session.beginTransaction();
      Query query = session.getNamedQuery("findByLastJobRunTimeMinusOneMinute")
          .setString("lastJobRunTime", dateFormat.format(lastJobRunTime));
      ImmutableList.Builder<DocumentMetadata> documentMetadatas =
          new ImmutableList.Builder<DocumentMetadata>();
      documentMetadatas.addAll(query.list());
      txn.commit();
      return documentMetadatas.build();
    } catch (HibernateException h) {
      if (txn != null) {
        txn.rollback();
      }
      throw new DaoException(h);
    } finally {
      session.close();
    }

  }
}
