--DROP VIEW CWSRS1.VW_PARENT_CASE_HIST;

CREATE VIEW CWSRS1.VW_PARENT_CASE_HIST AS
SELECT DISTINCT v2.PARENT_ID AS PARENT_PERSON_ID, v1.* 
FROM CWSRS1.ES_CASE_HIST v1 
JOIN CWSRS1.ES_CASE_HIST v2 ON v1.CASE_ID = v2.CASE_ID;