# graalvm-jfr

## To run

```
$ cd child-module
$ mvn package -Pnative
```

Replicating JfrFeature in small sample to reproduce [build failure](https://github.com/mpeddada1/check-22.1#testing-with-2210-runtime-and-2210-project-pomxml).
https://github.com/oracle/graal/blob/vm-ce-22.1.0/substratevm/src/com.oracle.svm.hosted/src/com/oracle/svm/hosted/jfr/JfrFeature.java. 

So far this sample has been building successfully.
