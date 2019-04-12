import groovy.transform.Field

@Library('jenkins-pipeline-utils-FIT-525') _

@Field
def GITHUB_CREDENTIALS_ID = '433ac100-b3c2-4519-b4d6-207c029a103b'

@Field
def tagPrefixes = ['audit-events', 'cap-users', 'facilities-cws', 'facilities-lis']


def githubConfig() {
    githubConfigProperties('https://github.com/ca-cwds/cwds-jobs')
}

node ('dora-slave'){
    // for master pipeline set the branch specifier on config UI to: master
    def triggerProperties = pullRequestMergedTriggerProperties('cwds-jobs-master')
    properties([
            [$class: 'RebuildSettings', autoRebuild: false, rebuildDisabled: true],
            githubConfig(),
            disableConcurrentBuilds(),
            pipelineTriggers([triggerProperties]),
            buildDiscarderDefaults('master')
    ])
    try {
        stage('Preparation') {
            cleanWs()
            git branch: '$branch', credentialsId: GITHUB_CREDENTIALS_ID, url: 'git@github.com:ca-cwds/cwds-jobs.git'
            checkout scm
        }
        if (env.BUILD_JOB_TYPE == 'master') {
            stage('Run release jobs') {
                def labels = getPRLabels('cwds-jobs')
                def foundTagPrefixes = labels.findAll { label -> tagPrefixes.contains(label) }
                def versionIncrement = versionIncrement(labels)

                def jobBackLink = "http://jenkins.dev.cwds.io:8080/job/cwds-jobs-pull-request/${env.BUILD_ID}/"
                for(String tagPrefix in foundTagPrefixes) {
                    withCredentials([usernamePassword(credentialsId: $ {
                        GITHUB_CREDENTIALS_ID
                    }, usernameVariable: 'JENKINS_USER', passwordVariable: 'JENKINS_API_TOKEN')]) {
                        def jobParams = "token=${JENKINS_TRIGGER_TOKEN}&versionIncrement=${versionIncrement}&triggered_by=${jobBackLink}&tagPrefix=${tagPrefix}"
                        def jobLink = "http://jenkins.dev.cwds.io:8080/job/cwds-jobs-test/buildWithParameters?${jobParams}"
                        debug("Calling cwds-jobs release job")
                        sh "curl -v -u '${JENKINS_USER}:${JENKINS_API_TOKEN}' '${jobLink}'"
                    }
                }
            }
        }
    } catch(Exception e) {
        echo e.message
        publishHTML([allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: '**/build/reports/tests/', reportFiles: 'index.html', reportName: 'JUnitReports', reportTitles: ''])
        sh ('docker-compose down -v')
        emailext attachLog: true, body: "Failed: ${e}", recipientProviders: [[$class: 'DevelopersRecipientProvider']],
                subject: "Jobs failed with ${e.message}", to: "Alex.Serbin@osi.ca.gov"
        currentBuild.result = 'FAILURE'
        throw e
    }finally {
        cleanWs()
    }
}
