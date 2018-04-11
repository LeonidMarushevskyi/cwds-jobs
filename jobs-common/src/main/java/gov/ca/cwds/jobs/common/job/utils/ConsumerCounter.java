package gov.ca.cwds.jobs.common.job.utils;

import java.util.concurrent.atomic.AtomicInteger;

public final class ConsumerCounter {

  private static AtomicInteger counter = new AtomicInteger(0);

  private ConsumerCounter() {
  }

  public static void incrementCounter() {
    counter.incrementAndGet();
  }

  public static void addToCounter(int bulkAmount) {
    counter.addAndGet(bulkAmount);
  }

  public static int getCounter() {
    return counter.get();
  }
}