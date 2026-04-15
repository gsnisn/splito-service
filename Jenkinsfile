pipeline {
    agent any

    environment {
        AWS_REGION = "ap-south-1"
        ECR_REPO = "712163226335.dkr.ecr.ap-south-1.amazonaws.com/splito"
        IMAGE_TAG = "latest"
    }

    stages {

        stage('Debug Structure') {
            steps {
                sh 'pwd && ls -R'
            }
        }
	
//         stage('Clone Repo') {
//             steps {
//                 git branch: 'main', url: 'https://github.com/gsnisn/splito.git',
//                 credentialsId: 'github-token'
//             }
//         }

        stage('Build All Modules') {
            steps {
                sh '''
                chmod +x splito/mvnw
                cd splito
                ./mvnw clean install -U -DskipTests
                '''
            }
        }

        stage('Build Docker Image') {
            steps {
                sh 'cd splito && docker build -t $ECR_REPO:$IMAGE_TAG .'
            }
        }

        stage('Login to ECR') {
            steps {
                withCredentials([[
                    $class: 'AmazonWebServicesCredentialsBinding',
                    credentialsId: 'aws-creds'
                ]]) {
                    sh """
                    aws ecr get-login-password --region $AWS_REGION | \
                    docker login --username AWS --password-stdin $ECR_REPO
                    """
                }
            }
        }

        stage('Push to ECR') {
            steps {
                sh """
                docker push $ECR_REPO:$IMAGE_TAG
                """
            }
        }
    }
}