echo on
set project_root=D:\alberta0714\sw\
cd /d %project_root%

rem 构建kafka插件
cd apm-sniffer\apm-sdk-plugin\kafka-plugin
call mvn clean install -DskipTests
rem 回到了根目录 
cd /d %project_root%
rem 删除原kafka插件
del skywalking-agent\plugins\apm-kafka-plugin-8.0.0.jar
rem 复制编译好的插件至指定目录 
copy apm-sniffer\apm-sdk-plugin\kafka-plugin\target\apm-kafka-plugin-8.0.0.jar skywalking-agent\plugins\


rem 构建elasticserch6.3.1插件
cd /d %project_root%
cd apm-sniffer\apm-sdk-plugin\elasticsearch-6.x-plugin
call mvn clean install -DskipTests -Dmaven.test.skip=true
rem 回到了根目录 
cd /d %project_root%
rem 删除原kafka插件
del skywalking-agent\plugins\apm-elasticsearch-6.x-plugin-8.0.0.jar
rem 复制编译好的插件至指定目录 
copy apm-sniffer\apm-sdk-plugin\elasticsearch-6.x-plugin\target\apm-elasticsearch-6.x-plugin-8.0.0.jar skywalking-agent\plugins\


rem 构建canal插件
cd /d %project_root%
cd apm-sniffer\apm-sdk-plugin\canal-1.x-plugin
call mvn clean install -DskipTests -Dmaven.test.skip=true
rem 回到了根目录 
cd /d %project_root%
rem 删除原kafka插件
del skywalking-agent\plugins\apm-canal-1.x-plugin-8.0.0.jar
rem 复制编译好的插件至指定目录 
copy apm-sniffer\apm-sdk-plugin\canal-1.x-plugin\target\apm-canal-1.x-plugin-8.0.0.jar skywalking-agent\plugins\

rem 