#!groovy
pipeline {
  environment {
    PROJECT_NAME = "keyple-integration-java-test"
    PROJECT_BOT_NAME = "Eclipse Keyple Bot"
  }
  agent { kubernetes { yaml javaBuilder('2.0') } }
  stages {
    stage('Build and Test') {
      steps { container('java-builder') {
        sh './gradlew clean spotlessCheck test --no-build-cache --info --stacktrace'
        junit testResults: 'build/test-results/test/*.xml', allowEmptyResults: true
      } }
    }
  }
  post { always { container('java-builder') {
    archiveArtifacts artifacts: 'build*/reports/tests/**', allowEmptyArchive: true
  } } }
}
