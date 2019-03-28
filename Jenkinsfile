@Library('jenkins-pipeline-utils') _

GITHUB_CREDENTIALS_ID = '433ac100-b3c2-4519-b4d6-207c029a103b'

node ('dora-slave'){
   def artifactVersion = "3.3-SNAPSHOT"
   def serverArti = Artifactory.server 'CWDS_DEV'
   def rtGradle = Artifactory.newGradleBuild()
   def tagPrefixes = ['audit-events', 'cap-users', 'facilities-cws', 'facilities-lis']
   def newTag, tagPrefix, newVersion
   if (env.BUILD_JOB_TYPE == 'master' || env.BUILD_JOB_TYPE == 'hotfix') {
     triggerProperties = pullRequestMergedTriggerProperties('cwds-jobs-master')
     properties([buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '25')),
     pipelineTriggers([triggerProperties]), disableConcurrentBuilds(), [$class: 'RebuildSettings', autoRebuild: false, rebuildDisabled: false],
     parameters([
        booleanParam(defaultValue: true, description: '', name: 'USE_NEWRELIC'),
        string(defaultValue: 'latest', description: '', name: 'APP_VERSION'),
        string(defaultValue: 'master', description: '', name: 'branch'),
        booleanParam(defaultValue: true, description: 'Default release version template is: <majorVersion>_<buildNumber>-RC', name: 'RELEASE_PROJECT'),
        string(defaultValue: "", description: 'Fill this field if need to specify custom version ', name: 'OVERRIDE_VERSION'),
        string(defaultValue: 'inventories/tpt2dev/hosts.yml', description: '', name: 'inventory')])
        ])
   } else {
      properties([disableConcurrentBuilds(), [$class: 'RebuildSettings', autoRebuild: false, rebuildDisabled: false],
      parameters([
        string(defaultValue: 'master', description: '', name: 'branch'),
        booleanParam(defaultValue: true, description: 'Default release version template is: <majorVersion>_<buildNumber>-RC', name: 'RELEASE_PROJECT'),
        string(defaultValue: 'inventories/tpt2dev/hosts.yml', description: '', name: 'inventory')])])
   }

  try {
   if (env.BUILD_JOB_TYPE == 'hotfix' && OVERRIDE_VERSION == '') {
     error('OVERRIDE_VERSION parameter is mandatory for hotfix builds')
   }
   stage('Preparation') {
       cleanWs()
       git branch: '$branch', credentialsId: GITHUB_CREDENTIALS_ID, url: 'git@github.com:ca-cwds/cwds-jobs.git'
       rtGradle.tool = "Gradle_35"
       rtGradle.resolver repo:'repo', server: serverArti
       rtGradle.deployer.mavenCompatible = true
       rtGradle.deployer.deployMavenDescriptors = true
       rtGradle.useWrapper = true
   }
   if (env.BUILD_JOB_TYPE == 'master' || env.BUILD_JOB_TYPE == 'hotfix') {
     if(env.BUILD_JOB_TYPE == 'master') {
       stage('Increment Tag') {
         newTag = newSemVer('', tagPrefixes)
         (tagPrefix, newVersion) = (newTag =~ /^(.+)\-(\d+\.\d+\.\d+)/).with { it[0][1,2] }
       }
     }
     stage('Build'){
         def buildInfo = rtGradle.run buildFile: "jobs-${tagPrefix}/build.gradle".toString(), tasks: "jar shadowJar -DRelease=true -D build=${BUILD_NUMBER} -DnewVersion=${newVersion}".toString()
     }
   } else {
     stage('Check for Labels') {
       checkForLabel('cwds-jobs', tagPrefixes)
     }
   }
   stage('Tests and Coverage') {
       buildInfo = rtGradle.run buildFile: 'build.gradle', switches: '--info', tasks: 'test jacocoMergeTest'
   }
   stage('SonarQube analysis'){
       lint(rtGradle)
   }
   if (env.BUILD_JOB_TYPE == 'master' || env.BUILD_JOB_TYPE == 'hotfix') {
        stage('Update License Report') {
          updateLicenseReport('master', GITHUB_CREDENTIALS_ID, [runtimeGradle: rtGradle])
        }
        stage('Tag Repo') {
            tagGithubRepo(newTag, GITHUB_CREDENTIALS_ID)
        }
        stage('Push to artifactory') {
            rtGradle.deployer.deployArtifacts = true
            buildInfo = rtGradle.run buildFile: "jobs-${tagPrefix}/build.gradle".toString(), tasks: "publish -DRelease=\$RELEASE_PROJECT -DBuildNumber=\$BUILD_NUMBER -DCustomVersion=\$OVERRIDE_VERSION -DnewVersion=${newVersion}".toString()
            rtGradle.deployer.deployArtifacts = false
        }
        stage('Clean WorkSpace') {
            archiveArtifacts artifacts: '**/jobs-*.jar,readme.txt,DocumentIndexerJob-*.jar', fingerprint: true
            sh ('docker-compose down -v')
            publishHTML([allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: '**/build/reports/tests/', reportFiles: 'index.html', reportName: 'JUnitReports', reportTitles: ''])
        }
    }
 } catch(Exception e) {
       publishHTML([allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: '**/build/reports/tests/', reportFiles: 'index.html', reportName: 'JUnitReports', reportTitles: ''])
       sh ('docker-compose down -v')
       emailext attachLog: true, body: "Failed: ${e}", recipientProviders: [[$class: 'DevelopersRecipientProvider']],
       subject: "Jobs failed with ${e.message}", to: "Alex.Serbin@osi.ca.gov"
       slackSend channel: "#cals-api", baseUrl: 'https://hooks.slack.com/services/', tokenCredentialId: 'slackmessagetpt2', message: "Build Falled: ${env.JOB_NAME} ${env.BUILD_NUMBER}"
       currentBuild.result = "FAILURE"
       throw e
    } finally {
        cleanWs()
    }
}
