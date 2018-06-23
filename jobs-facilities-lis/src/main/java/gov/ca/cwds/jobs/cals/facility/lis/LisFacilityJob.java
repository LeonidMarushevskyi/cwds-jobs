package gov.ca.cwds.jobs.cals.facility.lis;

import com.google.inject.Inject;
import gov.ca.cwds.cals.inject.CalsnsSessionFactory;
import gov.ca.cwds.cals.inject.FasSessionFactory;
import gov.ca.cwds.cals.inject.LisSessionFactory;
import gov.ca.cwds.jobs.cals.facility.ChangedFacilityDto;
import gov.ca.cwds.jobs.common.core.JobImpl;
import org.hibernate.SessionFactory;

/**
 * Created by Alexander Serbin on 3/5/2018.
 */
public class LisFacilityJob extends JobImpl<ChangedFacilityDto> {

  @Inject
  @FasSessionFactory
  private SessionFactory fasSessionFactory;

  @Inject
  @LisSessionFactory
  private SessionFactory lisSessionFactory;

  @Inject
  @CalsnsSessionFactory
  private SessionFactory calsnsSessionFactory;

  @Override
  public void close() {
    super.close();
    fasSessionFactory.close();
    lisSessionFactory.close();
    calsnsSessionFactory.close();
  }
}
