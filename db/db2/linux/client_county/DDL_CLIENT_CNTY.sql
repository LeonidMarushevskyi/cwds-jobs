-- CREATE CLIENT COUNTY TABLE
CREATE TABLE CWSRS1.CLIENT_CNTY (
	CLIENT_ID CHAR(10)  NOT NULL,
	GVR_ENTC SMALLINT NOT NULL WITH DEFAULT,
	LST_UPD_OP CHAR (1) NOT NULL,
	LST_UPD_TS TIMESTAMP NOT NULL,
	CONSTRAINT CLIENT_ID PRIMARY KEY
		(CLIENT_ID, GVR_ENTC)
);


CREATE INDEX CWSRS1.CLIENTCNTY
	ON CWSRS1.CLIENT_CNTY
	(LST_UPD_TS	ASC)
;