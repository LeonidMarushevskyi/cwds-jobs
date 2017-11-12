package gov.ca.cwds.neutron.vox.jmx;

import gov.ca.cwds.data.std.ApiMarker;
import gov.ca.cwds.jobs.exception.NeutronException;
import gov.ca.cwds.neutron.jetpack.ConditionalLogger;
import gov.ca.cwds.neutron.jetpack.JetPackLogger;
import gov.ca.cwds.neutron.jetpack.JobLogs;
import gov.ca.cwds.neutron.vox.VoxCommandInstruction;

public class VoxCommandFactory implements ApiMarker {

  private static final long serialVersionUID = 1L;
  private static final ConditionalLogger LOGGER = new JetPackLogger(VoxCommandFactory.class);

  private VoxCommandFactory() {
    // Static class
  }

  public static VoxJMXCommandClient build(final VoxCommandType cmdType,
      final VoxCommandInstruction cmd) throws NeutronException {
    VoxJMXCommandClient ret;
    try {
      ret = (VoxJMXCommandClient) cmdType.getKlass().newInstance();
    } catch (Exception e) {
      throw JobLogs.runtime(LOGGER, e, "JMX ERROR! host: {}, port: {}, rocket: {}", cmd.getHost(),
          cmd.getPort(), cmd.getRocket());
    }

    return ret;
  }

  public static VoxJMXCommandClient build(String[] args) throws NeutronException {
    final VoxCommandInstruction cmd = VoxCommandInstruction.parseCommandLine(args);
    final VoxCommandType cmdType = VoxCommandType.lookup(cmd.getCommand());
    return build(cmdType, cmd);
  }

  public static void main(String[] args) throws Exception {
    build(args).run();
  }

}
