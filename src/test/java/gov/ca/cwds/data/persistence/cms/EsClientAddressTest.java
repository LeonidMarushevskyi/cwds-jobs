package gov.ca.cwds.data.persistence.cms;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import gov.ca.cwds.data.persistence.cms.rep.CmsReplicationOperation;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;

public class EsClientAddressTest {

  private static EsClientAddress emptyTarget;

  @Mock
  private ResultSet rs;

  @BeforeClass
  public static void setupClass() throws Exception {
    emptyTarget = EsClientAddress.extract(Mockito.mock(ResultSet.class));
  }

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    when(rs.first()).thenReturn(true);
    when(rs.getString("CLT_ADJDEL_IND")).thenReturn("Y");

    final Short shortZero = Short.valueOf((short) 0);
    when(rs.getShort("ADR_GVR_ENTC")).thenReturn(shortZero);
    when(rs.getShort("ADR_ST_SFX_C")).thenReturn(shortZero);
    when(rs.getShort("ADR_STATE_C")).thenReturn(shortZero);
    when(rs.getShort("ADR_UNT_DSGC")).thenReturn(shortZero);
    when(rs.getShort("ADR_ZIP_SFX_NO")).thenReturn(shortZero);
    when(rs.getShort("CLA_ADDR_TPC")).thenReturn(shortZero);
    when(rs.getShort("CLT_B_CNTRY_C")).thenReturn(shortZero);
    when(rs.getShort("CLT_B_STATE_C")).thenReturn(shortZero);
    when(rs.getShort("CLT_D_STATE_C")).thenReturn(shortZero);
    when(rs.getShort("CLT_I_CNTRY_C")).thenReturn(shortZero);
    when(rs.getShort("CLT_IMGT_STC")).thenReturn(shortZero);
    when(rs.getShort("CLT_MRTL_STC")).thenReturn(shortZero);
    when(rs.getShort("CLT_NAME_TPC")).thenReturn(shortZero);
    when(rs.getShort("CLT_P_ETHNCTYC")).thenReturn(shortZero);
    when(rs.getShort("CLT_P_LANG_TPC")).thenReturn(shortZero);
    when(rs.getShort("CLT_RLGN_TPC")).thenReturn(shortZero);
    when(rs.getShort("CLT_S_LANG_TC")).thenReturn(shortZero);
  }

  @Test
  public void type() throws Exception {
    assertThat(EsClientAddress.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    EsClientAddress target = new EsClientAddress();
    assertThat(target, notNullValue());
  }

  @Test
  public void strToRepOp_Args__String() throws Exception {
    // given
    String op = null;
    // e.g. : given(mocked.called()).willReturn(1);
    // when
    CmsReplicationOperation actual = EsClientAddress.strToRepOp(op);
    // then
    // e.g. : verify(mocked).called();
    CmsReplicationOperation expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void extract_Args__ResultSet() throws Exception {
    // given
    final ResultSet rs = Mockito.mock(ResultSet.class);
    // e.g. : given(mocked.called()).willReturn(1);
    // when
    final EsClientAddress actual = EsClientAddress.extract(rs);
    // then
    // e.g. : verify(mocked).called();
    final EsClientAddress expected = emptyTarget;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void extract_Args__ResultSet_read() throws Exception {
    // given
    // e.g. : given(mocked.called()).willReturn(1);
    // when
    final EsClientAddress actual = EsClientAddress.extract(rs);
    // then
    // e.g. : verify(mocked).called();
    final EsClientAddress expected = new EsClientAddress();
    expected.setCltAdjudicatedDelinquentIndicator("Y");
    assertThat(actual, is(equalTo(expected)));
  }

  // @Test
  // public void extract_Args__ResultSet_T__SQLException() throws Exception {
  // // given
  // final ResultSet rs = mock(ResultSet.class);
  // // e.g. : given(mocked.called()).willReturn(1);
  // try {
  // // when
  // EsClientAddress.extract(rs);
  // fail("Expected exception was not thrown!");
  // } catch (SQLException e) {
  // // then
  // }
  // }

  @Test
  public void getNormalizationClass_Args__() throws Exception {
    EsClientAddress target = new EsClientAddress();
    // given
    // e.g. : given(mocked.called()).willReturn(1);
    // when
    final Class<ReplicatedClient> actual = target.getNormalizationClass();
    // then
    // e.g. : verify(mocked).called();
    final Class<ReplicatedClient> expected = ReplicatedClient.class;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void normalize_Args__Map() throws Exception {
    final EsClientAddress target = new EsClientAddress();
    // given
    final Map<Object, ReplicatedClient> map = new HashMap<Object, ReplicatedClient>();
    // e.g. : given(mocked.called()).willReturn(1);
    // when
    final ReplicatedClient actual = target.normalize(map);
    // then
    // e.g. : verify(mocked).called();
    final ReplicatedClient expected = new ReplicatedClient();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getNormalizationGroupKey_Args__() throws Exception {
    final EsClientAddress target = new EsClientAddress();
    // given
    // e.g. : given(mocked.called()).willReturn(1);
    // when
    final Object actual = target.getNormalizationGroupKey();
    // then
    // e.g. : verify(mocked).called();
    Object expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getPrimaryKey_Args__() throws Exception {
    final EsClientAddress target = new EsClientAddress();
    // given
    // e.g. : given(mocked.called()).willReturn(1);
    // when
    final Serializable actual = target.getPrimaryKey();
    // then
    // e.g. : verify(mocked).called();
    final Serializable expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void hashCode_Args__() throws Exception {
    final EsClientAddress target = new EsClientAddress();
    // given
    // e.g. : given(mocked.called()).willReturn(1);
    // when
    final int actual = target.hashCode();
    // then
    // e.g. : verify(mocked).called();
    final int expected = 337958661;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void equals_Args__Object() throws Exception {
    final EsClientAddress target = new EsClientAddress();
    // given
    Object obj = null;
    // e.g. : given(mocked.called()).willReturn(1);
    // when
    final boolean actual = target.equals(obj);
    // then
    // e.g. : verify(mocked).called();
    final boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

}
