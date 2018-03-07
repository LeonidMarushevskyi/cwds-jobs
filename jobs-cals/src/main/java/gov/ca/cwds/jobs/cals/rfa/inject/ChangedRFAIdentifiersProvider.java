package gov.ca.cwds.jobs.cals.rfa.inject;

import com.google.inject.Inject;
import com.google.inject.Injector;
import gov.ca.cwds.cals.inject.AbstractInjectProvider;
import gov.ca.cwds.jobs.cals.rfa.ChangedRFA1aFormIdentifiersService;
import io.dropwizard.hibernate.UnitOfWorkAwareProxyFactory;

/**
 * Created by Alexander Serbin on 3/6/2018.
 */
public class ChangedRFAIdentifiersProvider extends AbstractInjectProvider<ChangedRFA1aFormIdentifiersService> {

    @Inject
    public ChangedRFAIdentifiersProvider(Injector injector,
                                          UnitOfWorkAwareProxyFactory unitOfWorkAwareProxyFactory) {
        super(injector, unitOfWorkAwareProxyFactory);
    }

    @Override
    public Class<ChangedRFA1aFormIdentifiersService> getServiceClass() {
        return ChangedRFA1aFormIdentifiersService.class;
    }
}
