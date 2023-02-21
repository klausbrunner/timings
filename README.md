Timings
=======

The idea of timings is that you can easily record usage and performance measurements in your application and retrieve them anytime, without the need to write huge log files or set up a separate monitoring infrastructure, and without headaches about memory consumption or additional load.
The project was inspired by perf4j (http://perf4j.codehaus.org/), ostrich (https://github.com/twitter/ostrich), parts of x-ray (http://java.net/projects/x-ray), and a little bit of RRDTool (http://oss.oetiker.ch/rrdtool/). And it's apparently quite similar to the core of JAMon (http://jamonapi.sourceforge.net/).

Usage
-----

Basic usage of instrumentation, counting and timing a piece of code with an event name of "success":

```java
TimerStart stopwatch = new TimerStart();

// do some work here

TimedEvent jobFinished = stopwatch.stop("success");
recorder.record(jobFinished);
```

The recorder object is an instance of EventRecorder (and you should use this interface for recording); the only implementation available so far is RrdEventRecorder. To create an instance that records events in 60 minute-sized time intervals ("buckets"):
```java
EventRecorder recorder = new RrdEventRecorder(60, RrdEventRecorder.MINUTE);
```

This Recorder will always keep data for the past hour (more accurately, one hour before the time of the last received event) and forget everything that happened before. You may want to use your preferred dependency injection framework to access an application-wide instance, or just wrap it into a plain Singleton, or whatever fits your application's design to get Events into the Recorder. You could even batch them for a short while or send them via JMS, as ordering of messages is irrelevant and only the Event's trigger time is considered, not the time of its arrival in the Recorder. The library doesn't wire anything automatically and doesn't hook itself into any frameworks (as opposed to perf4j, which uses the available logging framework).

To look at the data, you can get either all buckets for all event names, or ask for a specific name:
```java
List<EventBucket> buckets = recorder.getEventBuckets("success");
```
EventBucket is a simple class with getters for the covered time interval and recorded count, minimum, mean, and maximum value. The list returned by getEventBuckets() is always in chronological order, ending with the latest recorded events (or, if preferred, another point in time).

Characteristics
---------------

* Limited memory usage. There are no unbounded queues or lists or anything like that, just fixed buffers whose size you can configure on startup.
* Timings does not start any threads and does not run periodic “consolidation” jobs or the like. Its code only executes when recording a new event (essentially in O(1) time, regardless of how many events have already been recorded) and when retrieving the recorded data.
* It’s very small, currently well below 1000 LOC.
* No logging framework needed.
* No external storage needed (neither filesystem nor a DBMS).
* No runtime dependencies at all, just plain Java SE 8.
* Performance: I can easily record several million events per second from several threads, all on a modest Core i-3 machine with default JVM settings.
* It’s covered by a fairly good base of test cases, including multi-threaded stress tests. And it’s regularly checked with static analyzers such as FindBugs. However, it hasn’t seen heavy long-term production use yet.
