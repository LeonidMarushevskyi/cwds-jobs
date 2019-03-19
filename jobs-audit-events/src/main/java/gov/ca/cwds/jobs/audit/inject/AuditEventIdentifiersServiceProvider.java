package gov.ca.cwds.jobs.audit.inject;

import com.google.inject.Inject;
import com.google.inject.Injector;
import gov.ca.cwds.jobs.audit.identifier.AuditEventIdentifiersService;
import gov.ca.cwds.jobs.audit.identifier.IncrementalModeAuditEventIdentifiersService;
import gov.ca.cwds.jobs.audit.identifier.InitialModeAuditEventIdentifiersService;
import gov.ca.cwds.jobs.common.inject.AbstractInjectProvider;
import gov.ca.cwds.jobs.common.mode.DefaultJobMode;
import gov.ca.cwds.jobs.common.mode.LocalDateTimeDefaultJobModeService;
import io.dropwizard.hibernate.UnitOfWorkAwareProxyFactory;

public class AuditEventIdentifiersServiceProvider extends
    AbstractInjectProvider<AuditEventIdentifiersService> {

  @Inject
  private LocalDateTimeDefaultJobModeService jobModeService;

  @Inject
  public AuditEventIdentifiersServiceProvider(Injector injector,
      UnitOfWorkAwareProxyFactory unitOfWorkAwareProxyFactory) {
    super(injector, unitOfWorkAwareProxyFactory);
  }

  @Override
  public Class<? extends AuditEventIdentifiersService> getServiceClass() {
    if (jobModeService.getCurrentJobMode() == DefaultJobMode.INITIAL_LOAD) {
      return InitialModeAuditEventIdentifiersService.class;
    } else {
      return IncrementalModeAuditEventIdentifiersService.class;
    }

  }

}
