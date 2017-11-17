package gov.ca.cwds.jobs.util.elastic;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.junit.Ignore;
import org.junit.Test;

public class XPackUtilsTest {

  @Test
  public void type() throws Exception {
    assertThat(XPackUtils.class, notNullValue());
  }

  @Test
  @Ignore
  public void secureClient_Args__String__String__SettingsBuilder() throws Exception {
    String user = null;
    String password = null;
    Settings.Builder settings = mock(Settings.Builder.class);

    // when(settings.)

    TransportClient actual = XPackUtils.secureClient(user, password, settings);
    TransportClient expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

}
