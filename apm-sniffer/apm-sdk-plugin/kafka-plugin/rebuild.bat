call mvn clean install -DskipTests
del ..\skywalking-agent\plugins\apm-kafka-plugin-8.0.0.jar
copy target\apm-kafka-plugin-8.0.0.jar ..\..\..\skywalking-agent\plugins\
