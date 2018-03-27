@echo off
call gradle clean shadowJar
call mkdir out
java -Dlog4j.configuration=file:log4j.properties -cp build/libs/cals-jobs-0.6.2-SNAPSHOT.jar gov.ca.cwds.jobs.cals.facility.FacilityJobRunner ^
     -c config/cals/facility/facility-job.yaml -l ./out/ ^
     > ./out/out_Facility.txt 2>&1
