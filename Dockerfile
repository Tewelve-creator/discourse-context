# 第一阶段：使用 Maven 编译打包
FROM maven:3.8.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
# 预先下载依赖，利用缓存加速
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# 第二阶段：运行环境
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
# 暴露你 Spring Boot 的端口 (假设是 8080，请根据实际情况修改)
EXPOSE 8080
# 启动命令 (如果配置文件在外部，可以在这里指定路径)
ENTRYPOINT ["java", "-jar", "app.jar"]