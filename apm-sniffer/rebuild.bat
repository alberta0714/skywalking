echo on
set project_root=D:\alberta0714\sw\
cd /d %project_root%

rem ����kafka���
cd apm-sniffer\apm-sdk-plugin\kafka-plugin
call mvn clean install -DskipTests
rem �ص��˸�Ŀ¼ 
cd /d %project_root%
rem ɾ��ԭkafka���
del skywalking-agent\plugins\apm-kafka-plugin-8.0.0.jar
rem ���Ʊ���õĲ����ָ��Ŀ¼ 
copy apm-sniffer\apm-sdk-plugin\kafka-plugin\target\apm-kafka-plugin-8.0.0.jar skywalking-agent\plugins\


rem ����elasticserch6.3.1���
cd /d %project_root%
cd apm-sniffer\apm-sdk-plugin\elasticsearch-6.x-plugin
call mvn clean install -DskipTests -Dmaven.test.skip=true
rem �ص��˸�Ŀ¼ 
cd /d %project_root%
rem ɾ��ԭkafka���
del skywalking-agent\plugins\apm-elasticsearch-6.x-plugin-8.0.0.jar
rem ���Ʊ���õĲ����ָ��Ŀ¼ 
copy apm-sniffer\apm-sdk-plugin\elasticsearch-6.x-plugin\target\apm-elasticsearch-6.x-plugin-8.0.0.jar skywalking-agent\plugins\


rem ����canal���
cd /d %project_root%
cd apm-sniffer\apm-sdk-plugin\canal-1.x-plugin
call mvn clean install -DskipTests -Dmaven.test.skip=true
rem �ص��˸�Ŀ¼ 
cd /d %project_root%
rem ɾ��ԭkafka���
del skywalking-agent\plugins\apm-canal-1.x-plugin-8.0.0.jar
rem ���Ʊ���õĲ����ָ��Ŀ¼ 
copy apm-sniffer\apm-sdk-plugin\canal-1.x-plugin\target\apm-canal-1.x-plugin-8.0.0.jar skywalking-agent\plugins\

rem 