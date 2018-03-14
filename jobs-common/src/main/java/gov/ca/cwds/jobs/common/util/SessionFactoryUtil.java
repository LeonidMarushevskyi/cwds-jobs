package gov.ca.cwds.jobs.common.util;

import com.google.common.collect.ImmutableList;
import io.dropwizard.db.DataSourceFactory;
import org.apache.commons.lang3.Validate;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import java.util.Map;
import java.util.function.Function;

/**
 * Created by Alexander Serbin on 3/2/2018.
 */
public final class SessionFactoryUtil {

    private SessionFactoryUtil() {
    }

    public static SessionFactory buildSessionFactory(DataSourceFactory dataSourceFactory,
                                                     String dataSourceName,
                                                     ImmutableList<Class<?>> entityClasses,
                                                     Function<Configuration, Configuration> function) {
        Validate.notNull(dataSourceFactory, String.format("%s data source configuration is empty", dataSourceName));
        Configuration configuration = new Configuration();
        for (Map.Entry<String, String> property : dataSourceFactory.getProperties().entrySet()) {
            configuration.setProperty(property.getKey(), property.getValue());
        }

        configuration.setProperty("hibernate.current_session_context_class", "managed");
        configuration.setProperty("hibernate.c3p0.min_size", "1");

        ServiceRegistry serviceRegistry
                = new StandardServiceRegistryBuilder()
                .applySettings(configuration.getProperties()).build();

        entityClasses.forEach(configuration::addAnnotatedClass);
        function.apply(configuration);
        return configuration.buildSessionFactory(serviceRegistry);
    }

    public static SessionFactory buildSessionFactory(DataSourceFactory dataSourceFactory,
                                                     String dataSourceName,
                                                     ImmutableList<Class<?>> entityClasses) {
        Function<Configuration, Configuration> emptyFunction = configuration -> configuration;
        return buildSessionFactory(dataSourceFactory, dataSourceName, entityClasses, emptyFunction);
    }

}