@Library('gematik-jenkins-shared-library') _

def CREDENTIAL_ID_NEXUS = "Nexus"

pipeline {
    options {
        disableConcurrentBuilds()
        ansiColor('xterm')
        copyArtifactPermission('*')
    }

    agent { label 'k8-android' }

    stages {
        stage("Download Gradle Caches") {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    nexusFileDownload("E-Rezept-App/Android-Cache/gradle-caches.zip", "gradle-caches.zip")
                    sh("mkdir ${GRADLE_USER_HOME}/caches")
                    unzip(zipFile: "gradle-caches.zip", dir: "${GRADLE_USER_HOME}/caches/")
                    sh("chmod -R +x ${GRADLE_USER_HOME}/caches")
                }
            }
        }

        stage('Build') {
            steps {
                gradleNexusCredentials(
                {
                    sh label: "starting build...", script: "./gradlew assembleGoogleTuInternalDebug -Pbuildkonfig.flavor=googleTuInternal"
                },
                CREDENTIAL_ID_NEXUS)
            }
        }

        stage('AppCenter Upload') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    withCredentials([[
                        $class          : "UsernamePasswordMultiBinding",
                        credentialsId   : "AppCenter-eRp-Android-Develop_u_p",
                        usernameVariable: "UNUSED",
                        passwordVariable: "APPCENTER_API_TOKEN"]])
                        {
                            appCenter apiToken: APPCENTER_API_TOKEN,
                                ownerName: "Gematik",
                                appName: 'eRezept-Android-Develop',
                                pathToApp: 'android/build/outputs/apk/**/android-googleTuInternal-debug.apk',
                                distributionGroups: 'Collaborators'
                        }
                }
            }
        }

        stage('UnitTests') {
            steps {
                sh './gradlew testGoogleTuInternaDebug -Pbuildkonfig.flavor=googleTuInternal'
            }
        }

        stage('Gradle Cache Upload') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    sh("./gradlew --stop")
                    zip(zipFile: "gradle-caches.zip", archive: false, dir: "${GRADLE_USER_HOME}/caches/", overwrite: true)
                    nexusFileUpload("gradle-caches.zip", "E-Rezept-App/Android-Cache/gradle-caches.zip")
                }
            }
        }
    }

    post {
        success {
            emailext attachLog: true,
                to: "vl_ti_erp_app_android@gematik.de",
                subject: "Job ${env.JOB_NAME} build ${env.BUILD_NUMBER}: ${currentBuild.currentResult}",
                body: "${currentBuild.currentResult}: Job ${env.JOB_NAME} build ${env.BUILD_NUMBER}"
        }
        failure {
            emailext attachLog: true,
                to: "vl_ti_erp_app_android@gematik.de",
                subject: "Job ${env.JOB_NAME} build ${env.BUILD_NUMBER}: ${currentBuild.currentResult}",
                body: ""
        }
        always {
            archiveArtifacts artifacts: '**/*-debug.apk', allowEmptyArchive: true
            junit testResults: '**/testGoogleTuInternalDebugUnitTest/*.xml,**/desktopTest/*.xml', allowEmptyResults: true
        }
    }
}