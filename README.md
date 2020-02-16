Threadly Test Utilities [![Build status](https://badge.buildkite.com/8bfb74c7efa06fb26fd53f710996951a4e907a8b72d76ae8a6.svg?branch=master)](https://buildkite.com/threadly/threadly-test)
========
Tools for testing concurrent applications.  Helping make normally async designs deterministic and fast to run in unit tests.

Include these tools in your test code by including the maven central artifact: 

```script
<dependency>
	<groupId>org.threadly</groupId>
	<artifactId>threadly-test</artifactId>
	<version>1.0</version>
</dependency>
```
-- Test Tool Overview --

*    `AsyncVerifier` - Used to verify operations which occurred asynchronously.  The AsyncVerifier allows you to assert failures/successes in other threads, and allow the main test thread to throw exceptions if any failures occur.  Thus blocking the main test thread until the sub-threads have completed.

*    `TestCondition` - often times in doing unit test for asynchronous operations you have to wait for a condition to be come true. This class gives a way to easily wait for those conditions to be true, or throw an exception if they do not happen after a given timeout. The implementation of TestRunnable gives a good example of how this can be used.

*    `TestRunnable` - a runnable structure that has common operations already implemented. It gives two functions handleRunStart and handleRunFinish to allow people to optionally override to provide any test specific operation which is necessary. You can see many examples in our own unit test code of how we are using this.

*    `TestableScheduler` - this is very similar to `NoThreadScheduler`, except it allows you to supply the time, thus allowing things to happen faster than real time. If you have an implementation with a recurring task and you want to unit test what happens when it runs the 5th time, while keeping the unit test fast, you can supply a time faster than real time to cause these executions.
