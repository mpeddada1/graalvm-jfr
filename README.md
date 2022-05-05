# graalvm-jfr

## To run

```
$ cd child-module
$ mvn package -Pnative
```

Replicating [JfrFeature](https://github.com/oracle/graal/blob/vm-ce-22.1.0/substratevm/src/com.oracle.svm.hosted/src/com/oracle/svm/hosted/jfr/JfrFeature.java) in small sample to reproduce [build failure](https://github.com/mpeddada1/check-22.1#testing-with-2210-runtime-and-2210-project-pomxml).

So far this sample has been building successfully.
