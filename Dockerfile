FROM openjdk:11-slim

ENV TINI_VERSION v0.18.0
ADD https://github.com/krallin/tini/releases/download/${TINI_VERSION}/tini /tini
RUN chmod +x /tini

RUN groupadd -g 1100 app \
 && useradd -u 1100 -g app -M app \
 && mkdir /app \
 && chown -R app:app /app

ARG JAR
COPY target/${JAR} /app/app.jar

COPY idx /app/idx

USER app

WORKDIR /app

ENTRYPOINT ["/tini", "--", "java", "-jar", "app.jar"]