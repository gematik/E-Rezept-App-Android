import org.jenkinsci.plugins.pipeline.modeldefinition.Utils

pipeline {
    triggers {
        gitlab(
            triggerOnNoteRequest: true,
            noteRegex: "[jJ]enkins"
        )
    }

    environment {
        NEXUS_CREDENTIALS = credentials('Nexus')
        GITHUB_API_TOKEN  = credentials('GITHUB.API.Token.Publish')
    }

    parameters {
	string(name: 'NEXUS_ARTEFACT_PATH', defaultValue: '', description: 'Artefact path in the Nexus repository. e.g. de/gematik/OpenSSL-Swift/4.3.2/OpenSSL_6_f647ffd.xcframework.zip')
	string(name: 'TAG', defaultValue: '', description: 'Tag of the GitHub release (draft) to attach the xcframework to')

    }

    options {
        ansiColor('xterm')
        copyArtifactPermission('*')
    }

    stages {
        stage('Download apk from Nexus and attach to GitHub release') {
            steps {
                sh label: 'Download apk from Nexus and attach to GitHub release', script: '''#!/bin/bash -l
                    source /etc/profile.d/android.sh

                    bundle exec fastlane download_apk_from_nexus_and_attach_to_github_release
                    '''
            }
        }
    }
}