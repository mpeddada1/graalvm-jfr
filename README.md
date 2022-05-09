# graalvm-jfr

## To run

```
$ cd child-module
$ mvn package -Pnative
```

Replicating [JfrFeature](https://github.com/oracle/graal/blob/vm-ce-22.1.0/substratevm/src/com.oracle.svm.hosted/src/com/oracle/svm/hosted/jfr/JfrFeature.java) in small sample to reproduce [build failure](https://github.com/mpeddada1/check-22.1#testing-with-2210-runtime-and-2210-project-pomxml).

Surprisingly, this sample built successfully.

**Update**

Adding a test class now results in the following issue at image build time when `mvn test -Pnative` or `mvn package` is called:

```
Error: ImageSingletons.add must not overwrite existing key com.oracle.svm.core.jdk.ProtectionDomainSupport
Existing value: com.oracle.svm.core.jdk.ProtectionDomainSupport@3e587920
New value: com.oracle.svm.core.jdk.ProtectionDomainSupport@2ef8a8c3
Error: Use -H:+ReportExceptionStackTraces to print stacktrace of underlying exception
```

However, calling `mvn package -Dskiptests` works successfully. 
