package gov.ca.cwds.jobs.cals.rfa.inject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import gov.ca.cwds.cals.Constants;
import gov.ca.cwds.cals.inject.MappingModule;
import gov.ca.cwds.cals.service.rfa.RFA1aFormsCollectionService;
import gov.ca.cwds.inject.NsSessionFactory;
import gov.ca.cwds.jobs.cals.CalsElasticJobWriter;
import gov.ca.cwds.jobs.cals.facility.FacilityJob;
import gov.ca.cwds.jobs.cals.facility.inject.NsDataAccessModule;
import gov.ca.cwds.jobs.cals.rfa.ChangedRFA1aFormDTO;
import gov.ca.cwds.jobs.cals.rfa.ChangedRFA1aFormsService;
import gov.ca.cwds.jobs.cals.rfa.RFA1aJob;
import gov.ca.cwds.jobs.cals.rfa.RFA1aJobConfiguration;
import gov.ca.cwds.jobs.common.BaseJobConfiguration;
import gov.ca.cwds.jobs.common.ElasticSearchIndexerDao;
import gov.ca.cwds.jobs.common.config.JobOptions;
import gov.ca.cwds.jobs.common.identifier.ChangedIdentifiersService;
import gov.ca.cwds.jobs.common.inject.AbstractBaseJobModule;
import gov.ca.cwds.jobs.common.job.ChangedEntitiesService;
import gov.ca.cwds.jobs.common.job.Job;
import gov.ca.cwds.jobs.common.job.JobWriter;
import io.dropwizard.hibernate.UnitOfWorkAwareProxyFactory;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Alexander Serbin on 3/4/2018.
 */
public class RFA1aJobModule extends AbstractBaseJobModule {

    private static final Logger LOG = LoggerFactory.getLogger(RFA1aJobModule.class);

    public RFA1aJobModule(String[] args) {
        super(args);
    }

    @Override
    protected void configure() {
        super.configure();
        install(new MappingModule());
        install(new NsDataAccessModule());
        bind(Job.class).to(FacilityJob.class);
        bind(JobWriter.class).to(RFA1aFormElasticJobWriter.class);
        bind(ChangedIdentifiersService.class).toProvider(ChangedRFAIdentifiersProvider.class);
        bind(ChangedEntitiesService.class).toProvider(ChangedRFAServiceProvider.class);
        bind(RFA1aFormsCollectionService.class);
        bind(ChangedRFA1aFormsService.class);
        bind(Job.class).to(RFA1aJob.class).in(Singleton.class);
    }

    @Provides
    @Override
    @Inject
    public RFA1aJobConfiguration getJobsConfiguration(JobOptions jobOptions) {
        RFA1aJobConfiguration jobConfiguration = BaseJobConfiguration.getJobsConfiguration(RFA1aJobConfiguration.class,
                jobOptions.getEsConfigLoc());
        jobConfiguration.setDocumentMapping("rfa.mapping.json");
        jobConfiguration.setIndexSettings("rfa.settings.json");
        return jobConfiguration;
    }

    @Provides
    @Inject
    UnitOfWorkAwareProxyFactory provideUnitOfWorkAwareProxyFactory(@NsSessionFactory SessionFactory nsSessionFactory) {
       return new UnitOfWorkAwareProxyFactory(Constants.UnitOfWork.CALSNS, nsSessionFactory);
    }

    static class RFA1aFormElasticJobWriter extends CalsElasticJobWriter<ChangedRFA1aFormDTO> {

        /**
         * Constructor.
         *
         * @param elasticsearchDao ES DAO
         * @param objectMapper Jackson object mapper
         */
        @Inject
        public RFA1aFormElasticJobWriter(ElasticSearchIndexerDao elasticsearchDao,
                                         ObjectMapper objectMapper) {
            super(elasticsearchDao, objectMapper);
        }
    }

}
