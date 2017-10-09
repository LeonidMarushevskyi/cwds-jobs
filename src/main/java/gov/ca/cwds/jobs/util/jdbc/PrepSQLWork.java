package gov.ca.cwds.jobs.util.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

import org.hibernate.jdbc.Work;

/**
 * Execute SQL prior to retrieving records, typically for last change runs. Examples include
 * populating a global temporary table prior to reading from a view.
 * 
 * @author CWDS API Team
 */
public class PrepSQLWork implements Work {

  private final Date lastRunTime;
  private final String sqlInsertLastChange;

  /**
   * Constructor.
   * 
   * @param lastRunTime last successful run time
   * @param sqlInsertLastChange SQL to run
   */
  public PrepSQLWork(Date lastRunTime, String sqlInsertLastChange) {
    this.lastRunTime = lastRunTime != null ? new Date(lastRunTime.getTime()) : null;
    this.sqlInsertLastChange = sqlInsertLastChange;
  }

  @Override
  public void execute(Connection con) throws SQLException {
    con.setSchema(JobJdbcUtils.getDBSchemaName());
    con.setAutoCommit(false);
    JobDB2Utils.enableParallelism(con);

    final StringBuilder buf = new StringBuilder();
    buf.append(JobJdbcUtils.makeTimestampString(lastRunTime));

    final String sql = sqlInsertLastChange.replaceAll("#SCHEMA#", JobJdbcUtils.getDBSchemaName())
        .replaceAll("##TIMESTAMP##", buf.toString());
    JobJdbcUtils.LOGGER.info("Prep SQL: {}", sql);

    try (final PreparedStatement stmt = con.prepareStatement(sql)) {
      JobJdbcUtils.LOGGER.info("Find keys new/changed since {}", lastRunTime);
      final int cntNewChanged = stmt.executeUpdate();
      JobJdbcUtils.LOGGER.info("Total keys new/changed: {}", cntNewChanged);
    }
  }

}