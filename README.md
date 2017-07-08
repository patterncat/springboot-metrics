sigar-demo
metrics-demo

## Summary
The default spring boot actuator is good, but for some situation, it may not be good enough for production. That's why i enhanced it.

## metrics layer
- system os
- network
- application server
- jvm metrics

# changelog

## version 0.0.8

fix tomcat datasource class not found if not use jdbc
add LogSlowQueryReport for slow sql log error
add LogResetAbandonedTimer for reset and slow sql log error

## version 0.0.7
add tomcat jdbc metrics

## version 0.0.6
remove mvc filter

## version 0.0.5
change mvc filter to conditional

## version 0.0.4
- fix tcp stat collect interval and log

## version 0.0.3
- add @Profile("debug") for ConsoleReporter
- add tomcat config endpoint
- fix AdvancedTomcatMetrics cast exception

## version 0.0.2
change dependency scope to provided

## version 0.0.1
first version

## how to use
```
		<dependency>
			<groupId>cn.patterncat</groupId>
			<artifactId>springboot-metrics</artifactId>
			<version>0.0.8</version>
		</dependency>
```
