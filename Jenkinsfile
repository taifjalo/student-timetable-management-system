// Gravy or Yamal building CI/CD Pipeline script for Testing Stages.

pipeline {
    agent any // environment Win, Linux, etc... (ympäristö tunnista)

    tools {
        maven 'Maven3'
    }

    environment {
          PATH = "C:\\Program Files\\Docker\\Docker\\resources\\bin;${env.PATH}"
          JAVA_HOME = 'C:\\Program Files\\Java\\jdk-21'
          DOCKERHUB_CREDENTIALS_ID = 'Docker_Hub'
          DOCKERHUB_REPO = 'taifjalo1/student-timetable-management-system'
          DOCKER_IMAGE_TAG = 'latest'
      }



    stages {
        stage('Check Docker') {
            steps {
                bat 'docker --version'
            }
        }

        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/taifjalo/student-timetable-management-system.git'
            }
        }

        stage('Build') {
            steps {
                bat 'mvn clean install'
            }
        }

        stage('Code Coverage') {
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

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQubeServer') {
                    bat """
                        mvn sonar:sonar ^
                        -Dsonar.projectKey=taifjalo_student-timetable-management-system ^
                        -Dsonar.projectName=Student Timetable Management System ^
                        -Dsonar.host.url=http://localhost:9000 ^
                        -Dsonar.token=b78e49484120154b6413db4c164336b41300206a ^
                        -Dsonar.java.binaries=target/classes ^
                        -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                    """
                }
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
                          docker.withRegistry('https://index.docker.io/v1/', DOCKERHUB_CREDENTIALS_ID) {
                              docker.image("${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG}").push()
                          }
                      }
                  }
              }
    }
}