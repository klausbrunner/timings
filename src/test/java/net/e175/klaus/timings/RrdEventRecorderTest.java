package net.e175.klaus.timings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class RrdEventRecorderTest {

	class SimpleListener implements EventBucketListener {
		public long count = 0;
		public final List<EventBucket> events = new ArrayList<EventBucket>();

		@Override
		public synchronized void supersededBucket(String name, EventBucket bucket) {
			System.out.println("listener got: " + name + " " + bucket);
			events.add(bucket);
			count++;
		}
	}

	public static final double MAXDELTA = 1e-6;

	@Test(expected = IllegalArgumentException.class)
	public void rejectsSillyNumberOfBuckets() {
		new RrdEventRecorder(-1, 100);
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsSillyBucketWidth() {
		new RrdEventRecorder(10, 0);
	}

	@Test
	public void returnsEmptyListForNonexistingName() {
		RrdEventRecorder recorder = new RrdEventRecorder();
		List<EventBucket> list = recorder.getEventBuckets("quaxi");

		assertNotNull(list);
		assertEquals(0, list.size());
	}

	@Test
	public void silentlyIgnoresNullEvents() {
		EventRecorder recorder = new RrdEventRecorder(24, RrdEventRecorder.HOUR);

		recorder.record(null);
	}

	@Test
	public void canListNames() {
		RrdEventRecorder recorder = new RrdEventRecorder(24, RrdEventRecorder.HOUR);

		recorder.record(new TestEvent(0, 0, "A"));
		recorder.record(new TestEvent(0, 0, "B"));

		assertEquals(2, recorder.getNames().size());
		assertEquals("A", recorder.getNames().get(0));
		assertEquals("B", recorder.getNames().get(1));
	}

	@Test
	public void canReturnAllBuckets() {
		RrdEventRecorder recorder = new RrdEventRecorder(24, RrdEventRecorder.HOUR);

		recorder.record(new TestEvent(0, 0, "A"));
		recorder.record(new TestEvent(0, 0, "B"));

		Map<String, List<EventBucket>> set = recorder.getEventBuckets();

		assertEquals(2, set.size());
	}

	@Test
	public void canReturnAllBucketsWithUntilTime() {
		RrdEventRecorder recorder = new RrdEventRecorder(24, RrdEventRecorder.HOUR);

		recorder.record(new TestEvent(0, 0, "A"));
		recorder.record(new TestEvent(0, 0, "B"));

		Map<String, List<EventBucket>> set = recorder.getEventBuckets(1000);

		assertEquals(2, set.size());
	}

	@Test
	public void canClearEvents() {
		List<Event> events1 = createShuffledHourlyEvents("1");

		RrdEventRecorder recorder = new RrdEventRecorder(24, RrdEventRecorder.HOUR);

		for (Event e : events1) {
			recorder.record(e);
		}

		assertTrue(recorder.getEventBuckets("1").size() > 0);
		recorder.clear();
		assertTrue(recorder.getEventBuckets("1").size() == 0);
	}

	@Test
	public void canRecordSeparateNames() {
		List<Event> events1 = createShuffledHourlyEvents("1");
		List<Event> events2 = createShuffledHourlyEvents("2");

		events1.addAll(events2);
		Collections.shuffle(events1);

		RrdEventRecorder recorder = new RrdEventRecorder(24, RrdEventRecorder.HOUR);

		for (Event e : events1) {
			recorder.record(e);
		}

		List<EventBucket> buckets1 = recorder.getEventBuckets("1");
		List<EventBucket> buckets2 = recorder.getEventBuckets("2");

		assertEquals(24, buckets1.size());
		assertEquals(24, buckets2.size());

		assertEquals(1, buckets1.get(0).getCount());
		assertEquals(23001, buckets1.get(23).getCount());
		assertEquals(0, buckets1.get(22).getMeanValue(), MAXDELTA);

		assertEquals(1, buckets2.get(0).getCount());
		assertEquals(23001, buckets2.get(23).getCount());
		assertEquals(0, buckets2.get(22).getMeanValue(), MAXDELTA);
	}

	@Test
	public void triggersCallbacks() {
		List<Event> events1 = createHourlyEvents("1");
		List<Event> events2 = createHourlyEvents("2");

		SimpleListener listener = new SimpleListener();
		RrdEventRecorder recorder = new RrdEventRecorder(24, RrdEventRecorder.HOUR, listener);

		for (int i = 0; i < events1.size(); i++) {
			recorder.record(events1.get(i));
			recorder.record(events2.get(i));
		}

		assertEquals(2 * 23, listener.count);

	}

	List<Event> createShuffledHourlyEvents(final String name) {
		List<Event> events = createHourlyEvents(name);

		Collections.shuffle(events); // shuffle, because order should not matter

		return events;
	}

	List<Event> createHourlyEvents(final String name) {
		List<Event> events = new ArrayList<Event>();

		for (int i = 0; i < 24; i++) {
			for (int v = 0; v <= i * 1000; v++) {
				Event e = new TestEvent(RrdEventRecorder.DAY + i * RrdEventRecorder.HOUR + 1, 0, name);
				events.add(e);
			}
		}

		Event e = new TestEvent(RrdEventRecorder.DAY - 1, 25);
		events.add(e);

		return events;
	}

}
