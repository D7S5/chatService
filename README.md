ChatService BE코드입니다.

ChatService FE https://github.com/D7S5/chatService-FE


의존성 Kafka, Redis가 있어 도커 설치가 필요합니다.

sudo docker compose up -d

application-demo.yml DB 설정 이후 

./gradlew bootRun --args='--spring.profiles.active=demo'
