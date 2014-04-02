package net.e175.klaus.timings;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Test class to generate lots of events in a separate thread. Uses a cyclic
 * barrier to synchronize starting and stopping of all threads.
 * 
 * Sample usage:
 * 
 * <pre>
 * 
 * public void canCountManyEventsFromMultipleThreads() throws Exception {
 * 	final int eventCount = 900000;
 * 	final int threadCount = 10;
 * 	SimpleCountingEventRecorder recorder = new SimpleCountingEventRecorder();
 * 
 * 	EventMassProducer.runThreads(eventCount, threadCount, recorder);
 * 
 * 	assertEquals(eventCount * threadCount, recorder.getEventCount());
 * }
 * 
 * </pre>
 * 
 */
public class EventMassProducer implements Runnable {

	final protected EventRecorder recorder;
	final protected long maxEvents;
	final protected String eventName;
	final CyclicBarrier barrier;

	EventMassProducer(final EventRecorder recorder, final long maxEvents, final String eventName,
			final CyclicBarrier barrier) {
		this.recorder = recorder;
		this.maxEvents = maxEvents;
		this.barrier = barrier;
		this.eventName = eventName;
	}

	@Override
	public void run() {
		try {
			barrier.await();
			generateEvents();
			barrier.await();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	protected void generateEvents() {
		for (long i = 0; i < maxEvents; i++) {
			TimerStart start = new TimerStart();
			if (i % 100 == 0) {
				Thread.yield();
			}
			TimedEvent e = start.stop(eventName);
			recorder.record(e);
		}
	}

	public static void runThreads(final int eventCount, final int threadCount, EventRecorder recorder)
			throws InterruptedException, BrokenBarrierException {
		final CyclicBarrier barrier = new CyclicBarrier(threadCount + 1);
		final ExecutorService pool = Executors.newCachedThreadPool();
		for (int i = 0; i < threadCount; i++) {
			pool.execute(new EventMassProducer(recorder, eventCount, "T" + (i % 2), barrier));
		}
		barrier.await();
		barrier.await();
	}

	public String getEventName() {
		return eventName;
	}

}
