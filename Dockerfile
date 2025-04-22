# 사용할 베이스 이미지를 선택 (OpenJDK 17 이미지를 사용)
FROM openjdk:17-jdk-slim

# 작업 디렉토리 생성
WORKDIR /app

# app/store 디렉토리 및 서브 디렉토리 생성
RUN mkdir -p /app/store/basicTemplate /app/store/dashboard /app/store/dashboardImage /app/store/deploy /app/store/sharedTemplate /app/store/sharedTemplateImage

# 빌드된 JAR 파일을 컨테이너로 복사
COPY build/libs/*.jar app.jar

# 컨테이너 시작 시 실행될 명령어 지정 (애플리케이션 실행)
CMD ["java", "-jar", "app.jar"]

# 애플리케이션이 사용할 포트 노출
EXPOSE 8080