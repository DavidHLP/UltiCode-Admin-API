1. 判题异常，导致判题终止
```shell
2025-07-31T21:21:34.584+08:00 ERROR 314762 --- [ntainer#0-0-C-1] o.s.k.l.KafkaMessageListenerContainer    : Error while stopping the container

java.util.concurrent.CompletionException: java.lang.NoClassDefFoundError: com/david/service/impl/DockerExecuteServiceImpl$1
	at java.base/java.util.concurrent.CompletableFuture.encodeThrowable(CompletableFuture.java:315) ~[na:na]
	at java.base/java.util.concurrent.CompletableFuture.completeThrowable(CompletableFuture.java:320) ~[na:na]
	at java.base/java.util.concurrent.CompletableFuture$AsyncRun.run(CompletableFuture.java:1807) ~[na:na]
	at java.base/java.lang.Thread.run(Thread.java:840) ~[na:na]
Caused by: java.lang.NoClassDefFoundError: com/david/service/impl/DockerExecuteServiceImpl$1
	at com.david.service.impl.DockerExecuteServiceImpl.executeCode(DockerExecuteServiceImpl.java:30) ~[classes/:na]
	at com.david.consumer.SandboxConsumer.consume(SandboxConsumer.java:22) ~[classes/:na]
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[na:na]
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77) ~[na:na]
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Method.java:569) ~[na:na]
	at org.springframework.messaging.handler.invocation.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:169) ~[spring-messaging-6.1.5.jar:6.1.5]
	at org.springframework.messaging.handler.invocation.InvocableHandlerMethod.invoke(InvocableHandlerMethod.java:119) ~[spring-messaging-6.1.5.jar:6.1.5]
	at org.springframework.kafka.listener.adapter.HandlerAdapter.invoke(HandlerAdapter.java:56) ~[spring-kafka-3.1.3.jar:3.1.3]
	at org.springframework.kafka.listener.adapter.MessagingMessageListenerAdapter.invokeHandler(MessagingMessageListenerAdapter.java:376) ~[spring-kafka-3.1.3.jar:3.1.3]
	at org.springframework.kafka.listener.adapter.RecordMessagingMessageListenerAdapter.onMessage(RecordMessagingMessageListenerAdapter.java:92) ~[spring-kafka-3.1.3.jar:3.1.3]
	at org.springframework.kafka.listener.adapter.RecordMessagingMessageListenerAdapter.onMessage(RecordMessagingMessageListenerAdapter.java:53) ~[spring-kafka-3.1.3.jar:3.1.3]
	at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.doInvokeOnMessage(KafkaMessageListenerContainer.java:2881) ~[spring-kafka-3.1.3.jar:3.1.3]
	at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.invokeOnMessage(KafkaMessageListenerContainer.java:2859) ~[spring-kafka-3.1.3.jar:3.1.3]
	at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.lambda$doInvokeRecordListener$55(KafkaMessageListenerContainer.java:2777) ~[spring-kafka-3.1.3.jar:3.1.3]
	at io.micrometer.observation.Observation.observe(Observation.java:499) ~[micrometer-observation-1.12.0.jar:1.12.0]
	at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.doInvokeRecordListener(KafkaMessageListenerContainer.java:2776) ~[spring-kafka-3.1.3.jar:3.1.3]
	at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.doInvokeWithRecords(KafkaMessageListenerContainer.java:2625) ~[spring-kafka-3.1.3.jar:3.1.3]
	at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.invokeRecordListener(KafkaMessageListenerContainer.java:2511) ~[spring-kafka-3.1.3.jar:3.1.3]
	at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.invokeListener(KafkaMessageListenerContainer.java:2153) ~[spring-kafka-3.1.3.jar:3.1.3]
	at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.invokeIfHaveRecords(KafkaMessageListenerContainer.java:1493) ~[spring-kafka-3.1.3.jar:3.1.3]
	at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.pollAndInvoke(KafkaMessageListenerContainer.java:1458) ~[spring-kafka-3.1.3.jar:3.1.3]
	at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.run(KafkaMessageListenerContainer.java:1328) ~[spring-kafka-3.1.3.jar:3.1.3]
	at java.base/java.util.concurrent.CompletableFuture$AsyncRun.run(CompletableFuture.java:1804) ~[na:na]
	... 1 common frames omitted
Caused by: java.lang.ClassNotFoundException: com.david.service.impl.DockerExecuteServiceImpl$1
	at java.base/jdk.internal.loader.BuiltinClassLoader.loadClass(BuiltinClassLoader.java:641) ~[na:na]
	at java.base/jdk.internal.loader.ClassLoaders$AppClassLoader.loadClass(ClassLoaders.java:188) ~[na:na]
	at java.base/java.lang.ClassLoader.loadClass(ClassLoader.java:525) ~[na:na]
	... 25 common frames omitted

```

- 错误原因：VM 无法找到名为 com.david.service.impl.DockerExecuteServiceImpl$1 的类,打包异常重新打包即可