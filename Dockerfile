# ===============================
# Etapa 1: Build (Compilação)
# ===============================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copia os arquivos de configuração do Maven primeiro (para aproveitar cache)
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Baixa as dependências sem copiar o código fonte ainda (cache inteligente)
# O comando 'go-offline' garante que tudo seja baixado
RUN ./mvnw dependency:go-offline

# Agora copia o código fonte do projeto
COPY src ./src

# Compila o projeto e gera o arquivo .jar (pula os testes para ser mais rápido no deploy)
RUN ./mvnw clean package -DskipTests

# ===============================
# Etapa 2: Runtime (Execução)
# ===============================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Cria um usuário sem privilégios para segurança (boa prática)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copia apenas o .jar gerado na etapa anterior
# O asterisco *.jar pega qualquer nome que o Maven tiver gerado
COPY --from=build /app/target/*.jar app.jar

# Informa qual porta a aplicação usa (Spring Boot padrão é 8080)
EXPOSE 8080

# Comando que roda quando o container inicia
ENTRYPOINT ["java", "-jar", "app.jar"]