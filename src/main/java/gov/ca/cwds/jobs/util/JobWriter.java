package gov.ca.cwds.jobs.util;

import java.util.List;

/**
 * @author CWDS Elasticsearch Team
 */
public interface JobWriter<T> extends JobComponent {

    void write(List<T> items) throws Exception;

}