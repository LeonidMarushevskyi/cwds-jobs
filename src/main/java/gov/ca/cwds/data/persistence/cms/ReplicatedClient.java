package gov.ca.cwds.data.persistence.cms;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.NamedNativeQueries;
import org.hibernate.annotations.NamedNativeQuery;
import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import gov.ca.cwds.dao.cms.BaseCmsReplicated;
import gov.ca.cwds.dao.cms.CmsReplicatedEntity;
import gov.ca.cwds.dao.cms.CmsReplicationOperation;
import gov.ca.cwds.data.persistence.PersistentObject;

/**
 * {@link PersistentObject} representing a Client as a {@link CmsReplicatedEntity}.
 * 
 * @author CWDS API Team
 */
@NamedNativeQueries({@NamedNativeQuery(
    name = "gov.ca.cwds.data.persistence.cms.ReplicatedClient.findAllUpdatedAfter",
    query = "select z.IDENTIFIER, z.ADPTN_STCD, z.ALN_REG_NO, z.BIRTH_DT, "
        + "trim(z.BR_FAC_NM) as BR_FAC_NM, z.B_STATE_C, z.B_CNTRY_C, z.CHLD_CLT_B, "
        + "trim(z.COM_FST_NM) as COM_FST_NM, trim(z.COM_LST_NM) as COM_LST_NM, "
        + "trim(z.COM_MID_NM) as COM_MID_NM, z.CONF_EFIND, z.CONF_ACTDT, z.CREATN_DT, "
        + "z.DEATH_DT, trim(z.DTH_RN_TXT) as DTH_RN_TXT, trim(z.DRV_LIC_NO) as DRV_LIC_NO, "
        + "z.D_STATE_C, z.GENDER_CD, z.I_CNTRY_C, z.IMGT_STC, z.INCAPC_CD, "
        + "z.LITRATE_CD, z.MAR_HIST_B, z.MRTL_STC, z.MILT_STACD, z.NMPRFX_DSC, "
        + "z.NAME_TPC, z.OUTWRT_IND, z.P_ETHNCTYC, z.P_LANG_TPC, z.RLGN_TPC, "
        + "z.S_LANG_TC, z.SENSTV_IND, z.SNTV_HLIND, z.SS_NO, z.SSN_CHG_CD, "
        + "trim(z.SUFX_TLDSC) as SUFX_TLDSC, z.UNEMPLY_CD, z.LST_UPD_ID, z.LST_UPD_TS, "
        + "trim(z.COMMNT_DSC) as COMMNT_DSC, z.EST_DOB_CD, z.BP_VER_IND, z.HISP_CD, "
        + "z.CURRCA_IND, z.CURREG_IND, z.COTH_DESC, z.PREVCA_IND, z.PREREG_IND, "
        + "trim(z.POTH_DESC) as POTH_DESC, z.HCARE_IND, z.LIMIT_IND, "
        + "trim(z.BIRTH_CITY) as BIRTH_CITY, trim(z.HEALTH_TXT) as HEALTH_TXT, "
        + "z.MTERM_DT, z.FTERM_DT, z.ZIPPY_IND, trim(z.DEATH_PLC) as DEATH_PLC, "
        + "z.TR_MBVRT_B, z.TRBA_CLT_B, z.SOC158_IND, z.DTH_DT_IND, "
        + "trim(z.EMAIL_ADDR) as EMAIL_ADDR, z.ADJDEL_IND, z.ETH_UD_CD, "
        + "z.HISP_UD_CD, z.SOCPLC_CD, z.CL_INDX_NO, z.IBMSNAP_OPERATION, z.IBMSNAP_LOGMARKER "
        + "from {h-schema}CLIENT_T z WHERE z.IBMSNAP_LOGMARKER >= :after FOR READ ONLY",
    resultClass = ReplicatedClient.class)})
@Entity
@Table(name = "CLIENT_T")
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReplicatedClient extends Client implements CmsReplicatedEntity {

  /**
   * 
   */
  private static final long serialVersionUID = 6160989831851057517L;

  @Transient
  private BaseCmsReplicated replicated = new BaseCmsReplicated();

  @Enumerated(EnumType.STRING)
  @Column(name = "IBMSNAP_OPERATION", updatable = false)
  @Override
  public CmsReplicationOperation getReplicationOperation() {
    return replicated.getReplicationOperation();
  }

  @Type(type = "timestamp")
  @Column(name = "IBMSNAP_LOGMARKER", updatable = false)
  @Override
  public Date getReplicationDate() {
    return replicated.getReplicationDate();
  }

}
