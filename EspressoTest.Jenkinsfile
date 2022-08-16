@Library('gematik-jenkins-shared-library') _

pipeline {
    options {
        disableConcurrentBuilds()
        buildDiscarder logRotator(artifactNumToKeepStr: '1', numToKeepStr: '5')
    }
    agent { label 'k8-android' }

    triggers { cron('@midnight') }

    environment {
        UNIQUE_UPLOAD_NAME = "reports_${getTimestamp()}"
        PATH = "/home/jenkins/agent/workspace/eRp-Android-Testautomation/google-cloud-sdk/bin:${env.PATH}"
    }

    stages {
        stage('Prepare python libs') {
            steps {
                sh('pip install junitparser')
            }
        }
        stage('Load & extract gcloud cli tool') {
            steps {
                sh('curl -O https://dl.google.com/dl/cloudsdk/channels/rapid/downloads/google-cloud-cli-394.0.0-linux-x86_64.tar.gz')
                sh('tar -xf google-cloud-cli-394.0.0-linux-x86_64.tar.gz')
                sh('./google-cloud-sdk/install.sh -q')
            }
        }
        stage('Authenticate with gcloud & set project') {
            steps {
                withCredentials([file(credentialsId: 'gematik-erx-app-testenv-secret-file', variable: 'CLOUD_ACCESS_JSON')]) {
                    sh('gcloud auth activate-service-account --key-file=$CLOUD_ACCESS_JSON')
                }
                sh('gcloud config set project gematik-erx-app-testenv')
                sh('gcloud firebase test android models list')
            }
        }
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
                sh("./gradlew android:assembleGoogleTuInternalDebug -Pbuildkonfig.flavor=googleTuInternal")
                sh("./gradlew android:assembleGoogleTuInternalDebugAndroidTest -Pbuildkonfig.flavor=googleTuInternal")
            }
        }
        stage('Archive') {
            steps {
                archiveArtifacts(artifacts: 'android/build/outputs/apk/**/*.apk')
            }
        }
//         stage('Upload & Start Test') {
//             steps {
//                 withCredentials([string(credentialsId: 'MCD_exklusive', variable: 'CLOUD_ACCESS_KEY')]) {
//                     sh label:'Push Android build to cloud', script:'''
//                     curl --location --request POST "https://mobiledevicecloud.t-systems-mms.eu/api/v1/test-run/execute-test-run?deviceQueries=@os='android'" \
//                     -H "Authorization: Bearer $CLOUD_ACCESS_KEY" \
//                     -F "executionType=espresso" \
//                     -F "runningType=coverage" \
//                     -F "useTestOrchestrator=true" \
//                     -F "clearPackageData=true" \
//                     -F "testApp=@./android/build/outputs/apk/androidTest/googleTuInternal/debug/android-googleTuInternal-debug-androidTest.apk" \
//                     -F "app=@./android/build/outputs/apk/googleTuInternal/debug/android-googleTuInternal-debug.apk" > test-result.json
//                     '''
//                 }
//                 sh('cat test-result.json')
//             }
//         }
        stage('Upload & Start Test') {
            steps {
                sh('''
                gcloud firebase test android run \
                --type=instrumentation \
                --app=./android/build/outputs/apk/googleTuInternal/debug/android-googleTuInternal-debug.apk \
                --test=./android/build/outputs/apk/androidTest/googleTuInternal/debug/android-googleTuInternal-debug-androidTest.apk \
                --device=model=redfin,version=30 \
                --use-orchestrator \
                --environment-variables=clearPackageData=true \
                --results-dir=$UNIQUE_UPLOAD_NAME \
                --results-bucket=test-results-fe06447a10a2 || true
                ''')
            }
        }
        stage('Download reporting') {
            steps {
                sh('gsutil cp gs://test-results-fe06447a10a2/$UNIQUE_UPLOAD_NAME/redfin-30-en-portrait/test_result_1.xml ./test_result_1.xml')
            }
        }
    }

    post {
//         always {
//             stage('De-Authenticate with gcloud') {
//                 steps {
//                     sh('./google-cloud-sdk/bin/gcloud auth ')
//                 }
//             }
//         }
//         success {
//             script {
//                 def result = readJSON file: 'test-result.json'
//
//                 if (result['data'].containsKey('Error reason')) {
//                     emailext(
//                         subject: "Android Test Run - TSys Cloud Failure",
//                         body: '${FILE, path="test-result.json"}',
//                         to: "vl_ti_erp_app_android@gematik.de,marcel.basquitt@gematik.de,tanja.rahn@gematik.de,christian.lange@gematik.de"
//                     )
//                 } else {
//                     def totalNumberOfTest = result['data']['Total number of tests'] as Integer
//                     def numberOfPassed = result['data']['Number of passed tests'] as Integer
//                     def successRate = ((numberOfPassed / totalNumberOfTest) * 100) as Integer
//
//                     emailext(
//                         subject: "Android Test Run - Passed ${successRate}% of ${totalNumberOfTest} tests",
//                         body: '${FILE, path="test-result.json"}',
//                         to: 'tobias.schwerdtfeger@gematik.de'
// //                         to: "vl_ti_erp_app_android@gematik.de,marcel.basquitt@gematik.de,tanja.rahn@gematik.de,christian.lange@gematik.de"
//                     )
//                 }
//             }
        success {
            script {
                sh('python ./ci/junit-report.py test_result_1.xml > report.txt')

                def results = (readFile('report.txt')).split("\n\n")
                def subject = results[0]
                def body = results[1]

                emailext(
                    subject: subject,
                    body: body,
                    to: "vl_ti_erp_app_android@gematik.de,marcel.basquitt@gematik.de,tanja.rahn@gematik.de,christian.lange@gematik.de,patrick.dargel@gematik.de, daniel.storl@gematik.de"
                )
            }
        }
        failure {
            emailext to: "vl_ti_erp_app_android@gematik.de",
                subject: "Espresso Test Run - Failed",
                body: ""
        }
    }
}

def getTimestamp() {
    def now = new Date()
    return now.format("yyyyMMddHHmm");
}
