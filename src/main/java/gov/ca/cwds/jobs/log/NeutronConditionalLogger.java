package gov.ca.cwds.jobs.log;

import java.util.function.Supplier;

import org.slf4j.Logger;

public interface NeutronConditionalLogger extends Logger {

  void trace(String format, Supplier<Object>... args);

  void debug(String format, Supplier<Object>... args);

  void info(String format, Supplier<Object>... args);

  void warn(String format, Supplier<Object>... args);

  void error(String format, Supplier<Object>... args);

}
