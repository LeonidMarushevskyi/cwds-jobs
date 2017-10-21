package gov.ca.cwds.jobs;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Ignore;
import org.junit.Test;

import gov.ca.cwds.data.cms.SystemCodeDao;
import gov.ca.cwds.data.cms.SystemMetaDao;
import gov.ca.cwds.rest.api.domain.cms.SystemCodeCache;

public class SystemCodesLoaderModuleTest {

  @Test
  public void type() throws Exception {
    assertThat(SystemCodesLoaderModule.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    SystemCodesLoaderModule target = new SystemCodesLoaderModule();
    assertThat(target, notNullValue());
  }

  @Test
  @Ignore
  public void configure_Args__() throws Exception {
    SystemCodesLoaderModule target = new SystemCodesLoaderModule();
    target.configure();
  }

  @Test
  public void provideSystemCodeCache_Args__SystemCodeDao__SystemMetaDao() throws Exception {
    SystemCodesLoaderModule target = new SystemCodesLoaderModule();
    SystemCodeDao systemCodeDao = mock(SystemCodeDao.class);
    SystemMetaDao systemMetaDao = mock(SystemMetaDao.class);
    SystemCodeCache actual = target.provideSystemCodeCache(systemCodeDao, systemMetaDao);
    assertThat(actual, is(notNullValue()));
  }

}
