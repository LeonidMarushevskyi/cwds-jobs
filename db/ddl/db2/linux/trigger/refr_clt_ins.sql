DROP TRIGGER CWSINT.trg_refr_clt_ins;

CREATE TRIGGER CWSINT.trg_refr_clt_ins
AFTER UPDATE ON CWSINT.REFR_CLT
REFERENCING NEW AS NROW
FOR EACH ROW MODE DB2SQL
BEGIN ATOMIC
	INSERT INTO CWSRS1.REFR_CLT ( 
		APRVL_NO,
		APV_STC,
		DSP_RSNC,
		DISPSTN_CD,
		RCL_DISPDT,
		SLFRPT_IND,
		STFADD_IND,
		LST_UPD_ID,
		LST_UPD_TS,
		FKCLIENT_T,
		FKREFERL_T,
		DSP_CLSDSC,
		RFCL_AGENO,
		AGE_PRD_CD,
		CNTY_SPFCD,
		MHLTH_IND,
		ALCHL_IND,
		DRUG_IND,
		IBMSNAP_OPERATION,
		IBMSNAP_LOGMARKER
	) VALUES (
		nrow.APRVL_NO,
		nrow.APV_STC,
		nrow.DSP_RSNC,
		nrow.DISPSTN_CD,
		nrow.RCL_DISPDT,
		nrow.SLFRPT_IND,
		nrow.STFADD_IND,
		nrow.LST_UPD_ID,
		nrow.LST_UPD_TS,
		nrow.FKCLIENT_T,
		nrow.FKREFERL_T,
		nrow.DSP_CLSDSC,
		nrow.RFCL_AGENO,
		nrow.AGE_PRD_CD,
		nrow.CNTY_SPFCD,
		nrow.MHLTH_IND,
		nrow.ALCHL_IND,
		nrow.DRUG_IND,
		'I',
		current timestamp
	);
END
