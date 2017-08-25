DROP VIEW CWDSDSM.VW_MQT_REFERRAL_HIST;

CREATE VIEW CWDSDSM.VW_MQT_REFERRAL_HIST (
	CLIENT_ID,
	CLIENT_SENSITIVITY_IND,
	REFERRAL_ID,
	START_DATE,
	END_DATE,
	REFERRAL_RESPONSE_TYPE,
	LIMITED_ACCESS_CODE,
	LIMITED_ACCESS_DATE,
	LIMITED_ACCESS_DESCRIPTION,
	LIMITED_ACCESS_GOVERNMENT_ENT,
	REFERRAL_LAST_UPDATED,
	REPORTER_ID,
	REPORTER_FIRST_NM,
	REPORTER_LAST_NM,
	REPORTER_LAST_UPDATED,
	WORKER_ID,
	WORKER_FIRST_NM,
	WORKER_LAST_NM,
	WORKER_LAST_UPDATED,
	PERPETRATOR_ID,
	PERPETRATOR_SENSITIVITY_IND,
	PERPETRATOR_FIRST_NM,
	PERPETRATOR_LAST_NM,
	PERPETRATOR_LAST_UPDATED,
	VICTIM_ID,
	VICTIM_SENSITIVITY_IND,
	VICTIM_FIRST_NM,
	VICTIM_LAST_NM,
	VICTIM_LAST_UPDATED,
	REFERRAL_COUNTY,
	ALLEGATION_ID,
	ALLEGATION_DISPOSITION,
	ALLEGATION_TYPE,
	ALLEGATION_LAST_UPDATED,
	LAST_CHG
) AS 
SELECT 
	RC.FKCLIENT_T		  AS CLIENT_ID,
	RC.SENSTV_IND		  AS CLIENT_SENSITIVITY_IND,
	RFL.IDENTIFIER        AS REFERRAL_ID,
	RFL.REF_RCV_DT        AS START_DATE,
	RFL.REFCLSR_DT        AS END_DATE,
	RFL.RFR_RSPC          AS REFERRAL_RESPONSE_TYPE,
	RFL.LMT_ACSSCD        AS LIMITED_ACCESS_CODE,
	RFL.LMT_ACS_DT        AS LIMITED_ACCESS_DATE,
	TRIM(RFL.LMT_ACSDSC)  AS LIMITED_ACCESS_DESCRIPTION,
	RFL.L_GVR_ENTC        AS LIMITED_ACCESS_GOVERNMENT_ENT,
	RFL.LST_UPD_TS        AS REFERRAL_LAST_UPDATED,
	RPT.FKREFERL_T        AS REPORTER_ID,
	TRIM(RPT.RPTR_FSTNM)  AS REPORTER_FIRST_NM,
	TRIM(RPT.RPTR_LSTNM)  AS REPORTER_LAST_NM,
	RPT.LST_UPD_TS        AS REPORTER_LAST_UPDATED,
	STP.IDENTIFIER        AS WORKER_ID,
	TRIM(STP.FIRST_NM)    AS WORKER_FIRST_NM,
	TRIM(STP.LAST_NM)     AS WORKER_LAST_NM,
	STP.LST_UPD_TS        AS WORKER_LAST_UPDATED,
	CLP.IDENTIFIER        AS PERPETRATOR_ID,
	CLP.SENSTV_IND        AS PERPETRATOR_SENSITIVITY_IND,
	TRIM(CLP.COM_FST_NM)  AS PERPETRATOR_FIRST_NM,
	TRIM(CLP.COM_LST_NM)  AS PERPETRATOR_LAST_NM,
	CLP.LST_UPD_TS        AS PERPETRATOR_LAST_UPDATED,
	CLV.IDENTIFIER        AS VICTIM_ID,
	CLV.SENSTV_IND        AS VICTIM_SENSITIVITY_IND,
	TRIM(CLV.COM_FST_NM)  AS VICTIM_FIRST_NM,
	TRIM(CLV.COM_LST_NM)  AS VICTIM_LAST_NM,
	CLV.LST_UPD_TS        AS VICTIM_LAST_UPDATED,
	RFL.GVR_ENTC          AS REFERRAL_COUNTY,
	ALG.IDENTIFIER        AS ALLEGATION_ID,
	ALG.ALG_DSPC          AS ALLEGATION_DISPOSITION,
	ALG.ALG_TPC           AS ALLEGATION_TYPE,
	ALG.LST_UPD_TS        AS ALLEGATION_LAST_UPDATED,
	CURRENT TIMESTAMP     AS LAST_CHG
FROM CWDSDSM.GT_REFR_CLT 	  RC
JOIN CWSRSQ.REFERL_T          RFL  ON RFL.IDENTIFIER = RC.FKREFERL_T
JOIN CWDSDSM.VICTIM_ALLGTN    ALG  ON ALG.FKREFERL_T = RFL.IDENTIFIER
JOIN CWDSDSM.CMP_CLIENT       CLV  ON CLV.IDENTIFIER = ALG.FKCLIENT_T
LEFT JOIN CWDSDSM.CMP_CLIENT  CLP  ON CLP.IDENTIFIER = ALG.FKCLIENT_0
LEFT JOIN CWSRSQ.REPTR_T      RPT  ON RPT.FKREFERL_T = RFL.IDENTIFIER
LEFT JOIN CWSRSQ.STFPERST     STP  ON RFL.FKSTFPERST = STP.IDENTIFIER
;