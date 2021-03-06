package gov.ca.cwds.jobs.cals.facility.cws.inject;

import com.google.inject.Inject;
import com.google.inject.Injector;
import gov.ca.cwds.cals.inject.AbstractInjectProvider;
import gov.ca.cwds.jobs.cals.facility.cws.savepoint.CwsTimestampSavePointService;
import io.dropwizard.hibernate.UnitOfWorkAwareProxyFactory;

/**
 * Created by Alexander Serbin on 6/26/2018.
 */
public class CwsTimestampSavePointServiceProvider extends
    AbstractInjectProvider<CwsTimestampSavePointService> {

  @Inject
  public CwsTimestampSavePointServiceProvider(Injector injector,
      UnitOfWorkAwareProxyFactory unitOfWorkAwareProxyFactory) {
    super(injector, unitOfWorkAwareProxyFactory);
  }

  @Override
  public Class<CwsTimestampSavePointService> getServiceClass() {
    return CwsTimestampSavePointService.class;
  }

}



