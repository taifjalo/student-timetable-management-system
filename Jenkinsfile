// Gravy or Yamal building CI/CD Pipeline script for Testing Stages.

pipeline {
    agent any // environment Win, Linux, etc... (ympäristö tunnista)

    tools {
        maven 'Maven3'
    }

    environment {
          PATH = "C:\\Program Files\\Docker\\Docker\\resources\\bin;${env.PATH}"
          DOCKERHUB_CREDENTIALS_ID = 'docker_hub'
          DOCKERHUB_REPO = 'taifjalo1/student-timetable-management-system'
          DOCKER_IMAGE_TAG = 'v0'
      }



    stages {

        stage('Checkout') {
            steps {
                git 'https://github.com/taifjalo/student-timetable-management-system.git'
            }
        }

        stage('Build') {
            steps {
                bat 'mvn clean install'
            }
        }

        stage('Generate Report') {
            steps {
                bat 'mvn jacoco:report'
            }
        }

        stage('Publish Test Results') {
            steps {
                junit '**/target/surefire-reports/*.xml'
            }
        }

        stage('Publish Coverage Report') {
            steps {
                jacoco()
            }
        }

         stage('Build Docker Image') {
              steps {
                 script {
                     docker.build("${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG}")
                 }
              }
         }

         stage('Push Docker Image to Docker Hub') {
                  steps {
                      script {
                          docker.withRegistry('https://index.docker.io/v0/', DOCKERHUB_CREDENTIALS_ID) {
                              docker.image("${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG}").push()
                          }
                      }
                  }
              }
    }
}