package gov.ca.cwds.jobs.facility;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import gov.ca.cwds.data.model.facility.es.ESFacility;

public class FacilityProcessorTest {

  @Test
  public void type() throws Exception {
    assertThat(FacilityProcessor.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    FacilityProcessor target = new FacilityProcessor();
    assertThat(target, notNullValue());
  }

  // @Test
  public void process_Args__FacilityRow() throws Exception {
    FacilityProcessor target = new FacilityProcessor();
    // given
    FacilityRow item = mock(FacilityRow.class);
    // e.g. : given(mocked.called()).willReturn(1);
    // when
    ESFacility actual = target.process(item);
    // then
    // e.g. : verify(mocked).called();
    ESFacility expected = new ESFacility();
    assertThat(actual, is(equalTo(expected)));
  }

  // @Test
  public void process_Args__FacilityRow_T__Exception() throws Exception {
    FacilityProcessor target = new FacilityProcessor();
    // given
    FacilityRow item = mock(FacilityRow.class);
    // e.g. : given(mocked.called()).willReturn(1);
    try {
      // when
      target.process(item);
      fail("Expected exception was not thrown!");
    } catch (Exception e) {
      // then
    }
  }

}
