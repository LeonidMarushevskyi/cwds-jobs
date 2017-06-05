package gov.ca.cwds.jobs.util;

/**
 * 
 * @param <I> input type
 * @param <O> output type
 * @author CWDS Elasticsearch Team
 */
@FunctionalInterface
public interface JobProcessor<I, O> {

  /**
   * Transform item I into O.
   * 
   * @param item input
   * @return an O
   * @throws Exception on ... whatever
   */
  O process(I item) throws Exception;

}
