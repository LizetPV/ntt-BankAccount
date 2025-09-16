pipeline {
  agent any
  options { timestamps() }

  stages {
    stage('Checkout') {
      steps { checkout scm }
    }

    stage('Build & Test') {
      parallel {
        stage('customer-ms') {
          steps {
            dir('customer-ms') {
              sh 'chmod +x mvnw || true'
              // 👇 evita conflicto con el 8080 de Jenkins
              sh './mvnw -B -ntp clean verify -Dserver.port=0'
            }
          }
          post { always { junit 'customer-ms/target/surefire-reports/*.xml' } }
        }
        stage('account-ms') {
          steps {
            dir('account-ms') {
              sh 'chmod +x mvnw || true'
              // 👇 evita conflicto con el 8080 de Jenkins
              sh './mvnw -B -ntp clean verify -Dserver.port=0'
            }
          }
          post { always { junit 'account-ms/target/surefire-reports/*.xml' } }
        }
      }
    }

    stage('Archive JARs') {
      steps {
        archiveArtifacts artifacts: 'customer-ms/target/*.jar,account-ms/target/*.jar', fingerprint: true
      }
    }
  }

  post {
    success { echo "✅ Build OK" }
    failure { echo "❌ Build FAILED" }
  }
}
