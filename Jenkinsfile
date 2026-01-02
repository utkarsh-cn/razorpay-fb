pipeline {
    agent any

    tools {
        // Make sure you've configured Maven in Jenkins Global Tools
        maven 'Maven-3.9.12'
        // Java should be available as a tool or via system PATH
    }

    environment {
        // Optional: You can set environment variables here
        JAVA_OPTS = "-Xmx1024m"
    }

    stages {

        stage('Checkout Code') {
            steps {
                githubCheckout()
            }
        }

        stage('Build') {
            steps {
                // Run maven build
                sh "mvn clean package"
            }
        }

        stage('Archive Artifact') {
            steps {
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
    }

    post {
        success {
            echo "Build completed successfully!"
        }
        failure {
            echo "Oops! Build failed!"
        }
    }
}
