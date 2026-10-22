FROM artifacts.developer.gov.bc.ca/docker-remote/maven:3.9.9-amazoncorretto-21-alpine AS maven

# Use the timezone-patched JDK for both compilation and the final runtime.
FROM artifacts.developer.gov.bc.ca/docker-remote/amazoncorretto:21.0.12-alpine3.23 AS build
COPY --from=maven /usr/share/maven /usr/share/maven
ENV PATH="/usr/share/maven/bin:${PATH}"
WORKDIR /workspace/app

COPY api/pom.xml .
COPY api/src src
RUN mvn package -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

FROM artifacts.developer.gov.bc.ca/docker-remote/amazoncorretto:21.0.12-alpine3.23
RUN adduser -D -h /home/spring -s /bin/sh spring && mkdir -p /logs && chown -R spring:spring /logs && chmod 755 /logs
USER spring
VOLUME /tmp
ARG DEPENDENCY=/workspace/app/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-Duser.name=EDUC_GRAD_GRADUATION_API","-Xms1300m","-Xmx1300m",\
            "-XX:TieredStopAtLevel=1","-XX:+UseParallelGC","-XX:MinHeapFreeRatio=20",\
            "-XX:MaxHeapFreeRatio=40","-XX:GCTimeRatio=4","-XX:AdaptiveSizePolicyWeight=90",\
            "-XX:MaxMetaspaceSize=300m","-XX:ParallelGCThreads=1",\
            "-Djava.util.concurrent.ForkJoinPool.common.parallelism=1","-XX:CICompilerCount=2",\
            "-XX:+ExitOnOutOfMemoryError","-Djava.security.egd=file:/dev/./urandom",\
            "-Dspring.backgroundpreinitializer.ignore=true","-cp","app:app/lib/*",\
            "ca.bc.gov.educ.api.graduation.EducGraduationApiApplication"]
