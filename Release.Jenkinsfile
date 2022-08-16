@Library('gematik-jenkins-shared-library') _

def CREDENTIAL_ID_GEMATIK_GIT = "GITLAB.tst_tt_build.Username_Password"
def CREDENTIAL_ID_NEXUS = "Nexus"
def REPO_URL = "https://gitlab.prod.ccs.gematik.solutions/git/erezept/app/erp-app-android.git"
def GRADLE_PARAMS = ""
def BUILD_TASK = ""
def FLAVOR = ""
def CHECKOUT_BRANCH = "master"

def appCenterUpload(credentialsId, appName, pathToApp, distributionGroups) {
    withCredentials([[
        $class          : "UsernamePasswordMultiBinding",
        credentialsId   : credentialsId,
        usernameVariable: "UNUSED",
        passwordVariable: "APPCENTER_API_TOKEN"]])
        {
            appCenter apiToken: APPCENTER_API_TOKEN,
                ownerName: "Gematik",
                appName: appName,
                pathToApp: pathToApp,
                distributionGroups: distributionGroups
        }
}

pipeline {
    options {
        skipDefaultCheckout()
        disableConcurrentBuilds()
        ansiColor('xterm')
        copyArtifactPermission('*')
    }
    agent { label 'k8-android' }

    parameters {
        choice(name: 'Packaging', choices: ['assemble', 'bundle'], description: 'Type of packaging, APK = assemble for e.g. manual installation on devices, AAB = bundle for Store distribution')
        choice(name: 'Store', choices: ['Google', 'Huawei', 'Konnektathon'], description: 'Which store to build for')
        choice(name: 'Environment', choices: ['Pu', 'Tu', 'Ru', 'Devru'], description: 'Build type')
        choice(name: 'Build_Type', choices: ['Release', 'Debug'], description: 'Build type')
        string(name: 'VERSION_CODE', defaultValue: '', description: 'App version code (Versioning in Play/AppGallery)')
        string(name: 'VERSION_NAME', defaultValue: '', description: 'App version name (User-facing app version)')
        string(name: 'BRANCH_OVERRIDE', defaultValue: '', description: 'Branch specifier to use for checkout. Default is Master if no value is specified.')
        booleanParam(name: 'Unit_Test', defaultValue: true, description: 'Run Unit Tests')
        booleanParam(name: 'OWASP_Dep_Check', defaultValue: true, description: 'Run OWASP Dependency Check')
        booleanParam(name: 'Archive_Build_Log', defaultValue: false, description: 'Archive Jenkins Build Log to Git')
    }

    stages {
        stage('Process Parameters') {
            steps {
                script {
                    if (!params.BRANCH_OVERRIDE.isEmpty()) {
                        CHECKOUT_BRANCH = params.BRANCH_OVERRIDE
                    }
                    if (!params.VERSION_CODE.isEmpty() && !params.VERSION_NAME.isEmpty()) {
                        GRADLE_PARAMS +=
                                " -PVERSION_CODE=${params.VERSION_CODE}" +
                                        " -PVERSION_NAME=${params.VERSION_NAME}"
                    }
                    BUILD_TASK += params.Packaging + params.Store + params.Environment
                    if (params.Build_Type == "Release") {
                        BUILD_TASK += "ExternalRelease"
                    } else {
                        BUILD_TASK += "InternalDebug"
                    }
                    FLAVOR += params.Store.toLowerCase() + params.Environment
                    if (params.Build_Type == "Release") {
                        FLAVOR += "External"
                    } else {
                        FLAVOR += "Internal"
                    }
                    echo "CHECKOUT_BRANCH: ${CHECKOUT_BRANCH}"
                    echo "params.Packaging: ${params.Packaging}"
                    echo "params.Store: ${params.Store}"
                    echo "params.Environment: ${params.Environment}"
                    echo "params.Build_Type: ${params.Build_Type}"
                    echo "BUILD_TASK: ${BUILD_TASK}"
                    echo "FLAVOR: ${FLAVOR}"
                }
            }
        }
        stage('Checkout') {
            steps {
                checkout([
                    $class: 'GitSCM', branches: [[name: "*/${CHECKOUT_BRANCH}"]],
                    userRemoteConfigs: [[url: REPO_URL, credentialsId: CREDENTIAL_ID_GEMATIK_GIT]]
                ])
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
        stage('Setup app signing') {
            steps {
                echo 'Moving Play signing keystore into place...'
                withCredentials([file(credentialsId: 'erp-android-keystore-play', variable: 'KEYSTORE')]) {
                    sh "cp \$KEYSTORE erp-release-keystore-play.jks"
                }
                echo 'Moving Huawei signing keystore into place...'
                withCredentials([file(credentialsId: 'erp-android-keystore-huawei', variable: 'KEYSTORE')]) {
                    sh "cp \$KEYSTORE erp-release-keystore-huawei.jks"
                }
                echo 'Moving signing.properties into place...'
                withCredentials([file(credentialsId: 'android-release-signing-props-google-huawei', variable: 'PROPS')]) {
                    sh "cp \$PROPS signing.properties"
                }
            }
        }
        stage('Build') {
            steps {
                // collect info for logging
                sh 'printenv'
                sh 'dpkg-query -l'

                gradleNexusCredentials(
                {
                    sh label: "starting build...", script: "./gradlew $BUILD_TASK $GRADLE_PARAMS -Pbuildkonfig.flavor=$FLAVOR"
                    echo 'Finished building BUILD_TASK $BUILD_TASK GRADLE_PARAMS $GRADLE_PARAMS FLAVOR $FLAVOR.'
                },
                CREDENTIAL_ID_NEXUS)
            }
        }

        stage('AppCenter Upload') {
            steps {
                script {
                    switch(BUILD_TASK) {
                        case "assembleGooglePuExternalRelease":
                            appCenterUpload(
                                'AC-eRezept-Android-Release-Google',
                                'eRezept-Android-Release-Google',
                                'android/build/outputs/apk/googlePuExternal/release/android-googlePuExternal-release.apk',
                                'Collaborators'
                            )
                        break
                        case "bundleGooglePuExternalRelease":
                            appCenterUpload(
                                'AC-eRezept-Android-Release-Google-AAB',
                                'eRezept-Android-Release-Google-AAB',
                                'android/build/outputs/bundle/googlePuExternalRelease/android-googlePuExternal-release.aab',
                                'Collaborators'
                            )
                            break
                        case "assembleHuaweiPuExternalRelease":
                            appCenterUpload(
                                'AC-eRezept-Android-Release-Huawei',
                                'eRezept-Android-Release-Huawei',
                                'android/build/outputs/apk/huaweiPuExternal/release/android-huaweiPuExternal-release.apk',
                                'Collaborators'
                            )
                            break
                        case "bundleHuaweiPuExternalRelease":
                            appCenterUpload(
                                'AC-eRezept-Android-Release-Huawei-AAB',
                                'eRezept-Android-Release-Huawei-AAB',
                                'android/build/outputs/bundle/huaweiPuExternalRelease/android-huaweiPuExternal-release.aab',
                                'Collaborators'
                            )
                            break
                        case "assembleGoogleTuExternalRelease":
                            appCenterUpload(
                                'AC-eRezept-Android-Release-TU-Google',
                                'eRezept-Android-Release-TU',
                                'android/build/outputs/apk/googleTuExternal/release/android-googleTuExternal-release.apk',
                                'Collaborators'
                            )
                            break
                        case "assembleKonnektathonRuInternalDebug":
                            appCenterUpload(
                                'AC-eRezept-Android-Konnektathon-Release-RU',
                                'eRezept-Android-Konnektathon',
                                'android/build/outputs/apk/**/android-konnektathonRuInternal-debug.apk',
                                'Collaborators, Public'
                            )
                        default:
                            echo "No AppCenter upload for Build Task '${BUILD_TASK}'!"
                            break
                    }
                }
            }
        }

        stage('UnitTests') {
            when {
                expression {
                    return params.Unit_Test
                }
            }
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    echo 'Running Unit Tests...'
                    sh './gradlew testGoogleTuInternaDebug -Pbuildkonfig.flavor=googleTuInternal'
                    echo 'Done running unit tests.'
                }
            }
        }
        stage('OWASP DepsCheck') {
            when {
                expression {
                    return params.OWASP_Dep_Check
                }
            }
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    echo 'Running OWASP DepCheck...'
                    gradleNexusCredentials(
                    {
                        sh './gradlew dependencyCheckAnalyze'
                    },
                    CREDENTIAL_ID_NEXUS)
                    echo 'Done with OWASP DepCheck.'
                }
            }
        }
        stage('Archive Build Log') {
            when {
                expression {
                    return params.Archive_Build_Log
                }
            }
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    build job: 'eRp-Android-Archive-Buildlog',
                        parameters: [
                            string(name: 'BUILDNUMBER', value: env.BUILD_NUMBER),
                            string(name: 'BRANCH_NAME', value: ""),
                            string(name: 'COMMIT_MESSAGE', value: "Buildlog ${CHECKOUT_BRANCH} - ${env.BUILD_NUMBER}")
                    ],
                    wait: true
                }
            }
        }
    }

    post {
        success {
            emailext attachLog: false,
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
            archiveArtifacts artifacts: '**/*-release.apk,**/*-release.aab,**/*-debug.apk,**/dependency-check-report.*', allowEmptyArchive: true
            junit testResults: '**/testGoogleTuInternalDebugUnitTest/*.xml,**/desktopTest/*.xml', allowEmptyResults: true
        }
    }
}