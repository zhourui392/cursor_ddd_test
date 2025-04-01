# 部署规范（Docker & Kubernetes）

- 提供标准化的Dockerfile模板，如：
  ```dockerfile
  FROM openjdk:21-jdk-slim
  COPY target/app.jar /app.jar
  EXPOSE 8080
  ENTRYPOINT ["java", "-jar", "/app.jar"]
  ```
- 部署文档需明确应用的端口、环境变量、资源请求与限制配置。
- 提供CI/CD流水线示例，以自动化构建、测试与部署过程。