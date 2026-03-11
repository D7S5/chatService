pipeline {
    agent any

    environment {
        REMOTE_HOST = '43.202.136.248'
        REMOTE_USER = 'ubuntu'
        REMOTE_APP_DIR = '/home/ubuntu/chatService/app'
        JAR_NAME = 'app.jar'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Jar') {
            steps {
                sh '''
                    chmod +x gradlew
                    ./gradlew clean bootJar -x test
                '''
            }
        }

        stage('Upload App Files') {
            steps {
                sshagent(credentials: ['chat-prod-ssh']) {
                    sh '''
                        scp -o StrictHostKeyChecking=no \
                          build/libs/app.jar \
                          ${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_APP_DIR}/${JAR_NAME}
                    '''
                }
            }
        }

        stage('Write .env on Remote') {
            steps {
                withCredentials([
                    string(credentialsId: 'DB_URL', variable: 'DB_URL'),
                    string(credentialsId: 'DB_USERNAME', variable: 'DB_USERNAME'),
                    string(credentialsId: 'DB_PASSWORD', variable: 'DB_PASSWORD'),
                    string(credentialsId: 'JWT_SECRET', variable: 'JWT_SECRET'),
                    string(credentialsId: 'GOOGLE_CLIENT_ID', variable: 'GOOGLE_CLIENT_ID'),
                    string(credentialsId: 'GOOGLE_CLIENT_SECRET', variable: 'GOOGLE_CLIENT_SECRET'),
                    string(credentialsId: 'NAVER_CLIENT_ID', variable: 'NAVER_CLIENT_ID'),
                    string(credentialsId: 'NAVER_CLIENT_SECRET', variable: 'NAVER_CLIENT_SECRET'),
                    string(credentialsId: 'KAKAO_CLIENT_ID', variable: 'KAKAO_CLIENT_ID'),
                    string(credentialsId: 'KAKAO_CLIENT_SECRET', variable: 'KAKAO_CLIENT_SECRET')
                ]) {
                    sshagent(credentials: ['chat-prod-ssh']) {
                        sh '''
                            ssh -o StrictHostKeyChecking=no ${REMOTE_USER}@${REMOTE_HOST} << EOF
                              mkdir -p ${REMOTE_APP_DIR}
                              cat > ${REMOTE_APP_DIR}/.env << EOT
DB_URL=${DB_URL}
DB_USERNAME=${DB_USERNAME}
DB_PASSWORD=${DB_PASSWORD}
JWT_SECRET=${JWT_SECRET}
GOOGLE_CLIENT_ID=${GOOGLE_CLIENT_ID}
GOOGLE_CLIENT_SECRET=${GOOGLE_CLIENT_SECRET}
NAVER_CLIENT_ID=${NAVER_CLIENT_ID}
NAVER_CLIENT_SECRET=${NAVER_CLIENT_SECRET}
KAKAO_CLIENT_ID=${KAKAO_CLIENT_ID}
KAKAO_CLIENT_SECRET=${KAKAO_CLIENT_SECRET}
EOT
                              chmod 600 ${REMOTE_APP_DIR}/.env
EOF
                        '''
                    }
                }
            }
        }



        stage('Restart App on Remote') {
            steps {
                sshagent(credentials: ['chat-prod-ssh']) {
                    sh '''
                        ssh -o StrictHostKeyChecking=no ${REMOTE_USER}@${REMOTE_HOST} << EOF
                          cd ${REMOTE_APP_DIR}
                          pkill -f 'java.*app.jar' || true
                          nohup java -Dspring.profiles.active=prod -jar ${JAR_NAME} > app.log 2>&1 < /dev/null &
EOF
                    '''
                }
            }
        }

        stage('Check App Log') {
            steps {
                sshagent(credentials: ['chat-prod-ssh']) {
                    sh '''
                        ssh -o StrictHostKeyChecking=no ${REMOTE_USER}@${REMOTE_HOST} << EOF
                          sleep 8
                          tail -n 100 ${REMOTE_APP_DIR}/app.log
EOF
                    '''
                }
            }
        }
    }
}