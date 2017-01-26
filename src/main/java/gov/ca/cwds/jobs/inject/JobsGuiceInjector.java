package gov.ca.cwds.jobs.inject;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import gov.ca.cwds.dao.DocumentMetadataDao;
import gov.ca.cwds.dao.cms.DocumentMetadataDaoImpl;
import gov.ca.cwds.dao.elasticsearch.ElasticsearchConfiguration;
import gov.ca.cwds.dao.elasticsearch.ElasticsearchDao;
import gov.ca.cwds.data.CmsSystemCodeSerializer;
import gov.ca.cwds.data.cms.ClientDao;
import gov.ca.cwds.data.persistence.cms.CmsSystemCodeCacheService;
import gov.ca.cwds.data.persistence.cms.ISystemCodeCache;
import gov.ca.cwds.data.persistence.cms.ISystemCodeDao;
import gov.ca.cwds.data.persistence.cms.SystemCodeDaoFileImpl;
import gov.ca.cwds.inject.CmsSessionFactory;
import gov.ca.cwds.inject.NsSessionFactory;
import gov.ca.cwds.jobs.JobsException;

public class JobsGuiceInjector extends AbstractModule {

  private File esConfig;
  private String lastJobRunTimeFilename;

  public JobsGuiceInjector() {}

  public JobsGuiceInjector(File esConfigFileLoc, String lastJobRunTimeFilename) {
    this.esConfig = esConfigFileLoc;
    this.lastJobRunTimeFilename = lastJobRunTimeFilename;
  }

  /**
   * {@inheritDoc}
   * 
   * @see com.google.inject.AbstractModule#configure()
   */
  @Override
  protected void configure() {

    bind(SessionFactory.class).annotatedWith(CmsSessionFactory.class)
        .toInstance(new Configuration().configure("cms-hibernate.cfg.xml").buildSessionFactory());
    bind(DocumentMetadataDao.class).to(DocumentMetadataDaoImpl.class);
    bind(ClientDao.class);

    // Must declare as singleton, otherwise Guice creates a new instance each time.
    bind(ObjectMapper.class).asEagerSingleton();

    if (!StringUtils.isBlank(this.lastJobRunTimeFilename)) {
      bindConstant().annotatedWith(LastRunFile.class).to(this.lastJobRunTimeFilename);
    }

    // Register CMS system code translator.
    bind(ISystemCodeDao.class).to(SystemCodeDaoFileImpl.class);
    bind(ISystemCodeCache.class).to(CmsSystemCodeCacheService.class).asEagerSingleton();
    bind(CmsSystemCodeSerializer.class).asEagerSingleton();
  }

  @Provides
  ElasticsearchDao esDao() {
    if (esConfig != null) {
      try {
        final ElasticsearchConfiguration configuration = new ObjectMapper(new YAMLFactory())
            .readValue(esConfig, ElasticsearchConfiguration.class);
        return new ElasticsearchDao(configuration);
      } catch (Exception e) {
        throw new JobsException(e);
      }
    }

    return null;
  }

  // @Provides
  // @CmsSessionFactory
  // SessionFactory cmsSessionFactory() {
  // return new Configuration().configure("cms-hibernate.cfg.xml").buildSessionFactory();
  // }

  @Provides
  @NsSessionFactory
  SessionFactory nsSessionFactory() {
    return new Configuration().configure("ns-hibernate.cfg.xml").buildSessionFactory();
  }

}
