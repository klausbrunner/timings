package net.e175.klaus.timings;

import java.lang.management.ManagementFactory;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * This is not a JUnit test, but a simple application intended for memory leak
 * checking. It runs in an infinite loop, putting a moderate load on the Timings
 * classes.
 * 
 */
public final class LongrunningTestApp {

	static class EternalProducer extends EventMassProducer {
		private static final Logger LOG = Logger.getLogger(EternalProducer.class.toString());

		EternalProducer(EventRecorder recorder, String eventName, CyclicBarrier barrier) {
			super(recorder, Long.MAX_VALUE, eventName, barrier);
		}

		@Override
		protected void generateEvents() {
			LOG.info("thread " + Thread.currentThread().getName() + " starting generation for name "
					+ this.getEventName());
			while (!Thread.interrupted()) {
				TimerStart start = new TimerStart();

				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					return;
				}

				TimedEvent e = start.stop(eventName);
				recorder.record(e);
			}
		}
	}

	private static final int THREAD_COUNT = 100;
	private static final RrdEventRecorder recorder = new RrdEventRecorder(24, RrdEventRecorder.HOUR);
	private static final Logger LOG = Logger.getLogger(LongrunningTestApp.class.toString());

	public static void runThreads() throws InterruptedException, BrokenBarrierException {
		final CyclicBarrier barrier = new CyclicBarrier(THREAD_COUNT + 1);
		final ExecutorService pool = Executors.newCachedThreadPool();
		for (int i = 0; i < THREAD_COUNT; i++) {
			pool.execute(new EternalProducer(recorder, "T" + (i % 2), barrier));
		}
		barrier.await();

		queryRecorderPeriodically();

	}

	private static void queryRecorderPeriodically() throws InterruptedException {
		while (!Thread.interrupted()) {
			Thread.sleep(5 * RrdEventRecorder.MINUTE);
			for (String name : recorder.getNames()) {
				recorder.getEventBuckets(name);
			}
			LOG.info("still running");
		}
	}

	public static void main(String[] args) throws Exception {
		LOG.info("started " + ManagementFactory.getRuntimeMXBean().getName());
		runThreads();
	}

}
