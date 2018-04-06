package gov.ca.cwds.jobs.cals.facility.lis;

import com.google.inject.Inject;
import gov.ca.cwds.cals.inject.FasSessionFactory;
import gov.ca.cwds.cals.inject.LisSessionFactory;
import gov.ca.cwds.jobs.cals.facility.ChangedFacilityDTO;
import gov.ca.cwds.jobs.common.inject.JobImpl;
import org.hibernate.SessionFactory;

/**
 * Created by Alexander Serbin on 3/5/2018.
 */
public class LisFacilityJob extends JobImpl<ChangedFacilityDTO> {

  @Inject
  @FasSessionFactory
  private SessionFactory fasSessionFactory;

  @Inject
  @LisSessionFactory
  private SessionFactory lisSessionFactory;

  @Override
  public void close() {
    super.close();
    fasSessionFactory.close();
    lisSessionFactory.close();
  }

}
