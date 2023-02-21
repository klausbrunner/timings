package net.e175.klaus.timings;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RrdTest {

    static class SimpleListener implements EventBucketListener {
        public long count = 0;
        public final List<EventBucket> events = new ArrayList<>();

        @Override
        public synchronized void supersededBucket(String name, EventBucket bucket) {
            System.out.println("listener got: " + bucket);
            events.add(bucket);
            count++;
        }
    }

    @Test
    public void canInstantiate() {
        new RRD(60, RrdEventRecorder.SECOND, "");
    }

    @Test
    public void knowsItsName() {
        RRD rrd = new RRD(60, RrdEventRecorder.SECOND, "fooblabb");
        assertEquals("fooblabb", rrd.getName());
    }

    @Test
    public void canRecordAnEvent() {
        RRD recorder = new RRD(60, RrdEventRecorder.SECOND, "");
        TimedEvent e = new TimedEvent(System.nanoTime(), "");
        recorder.addToBuckets(e);
    }

    @Test
    public void canCountSingleBucket() {
        RRD recorder = new RRD(60, RrdEventRecorder.SECOND, "");
        TestEvent e = new TestEvent();

        recorder.addToBuckets(e);
        recorder.addToBuckets(e);
        recorder.addToBuckets(e);

        List<EventBucket> events = recorder.getEventBuckets();

        assertEquals(60, events.size());

        EventBucket buck = events.get(events.size() - 1);
        assertEquals(3, buck.getCount());
    }

    @Test
    public void bucketIntervalsAreContiguous() {
        RRD recorder = new RRD(60, RrdEventRecorder.SECOND, "");
        TestEvent e = new TestEvent();
        recorder.addToBuckets(e);

        List<EventBucket> events = recorder.getEventBuckets();

        checkThatEventBucketsAreContiguous(events, RrdEventRecorder.SECOND);

    }

    private void checkThatEventBucketsAreContiguous(final List<EventBucket> events, final long bucketWidth) {
        long prevEnd = events.get(0).getIntervalStart();
        for (EventBucket b : events) {
            assertEquals(prevEnd, b.getIntervalStart());
            assertEquals(prevEnd + bucketWidth, b.getIntervalEnd());
            prevEnd = b.getIntervalEnd();
        }
    }

    @Test
    public void ignoresTooOldEvent() {
        RRD recorder = new RRD(60, RrdEventRecorder.SECOND, "");
        TestEvent e = new TestEvent();
        recorder.addToBuckets(e);

        TestEvent eOld = new TestEvent(e.getTriggerTime() - 60 * 1000 * 1000, 1);
        recorder.addToBuckets(eOld);
        List<EventBucket> events = recorder.getEventBuckets();

        for (int i = 0; i < events.size() - 1; i++) {
            assertTrue(events.get(0).isEmpty());
        }

        assertEquals(1, events.get(events.size() - 1).getCount());
    }

    @Test
    public void returnsEmptyListIfEmpty() {
        RRD recorder = new RRD(60, RrdEventRecorder.SECOND, "");
        assertEquals(0, recorder.getEventBuckets().size());
    }

    @Test
    public void canCountSeveralBuckets() {
        RRD recorder = new RRD(60, RrdEventRecorder.SECOND, "");
        TestEvent e = new TestEvent();
        recorder.addToBuckets(e);
        recorder.addToBuckets(e);

        TestEvent e2 = new TestEvent(e.getTriggerTime() + 1000, 1);
        recorder.addToBuckets(e2);

        List<EventBucket> events = recorder.getEventBuckets();

        assertEquals(60, events.size());

        EventBucket buck = events.get(events.size() - 2);
        assertEquals(2, buck.getCount());

        EventBucket buck2 = events.get(events.size() - 1);
        assertEquals(1, buck2.getCount());

        for (EventBucket b : events) {
            System.out.println(b);
        }

    }

    @Test
    public void forgetsOldEvents() {
        RRD recorder = new RRD(60, RrdEventRecorder.SECOND, "");
        TestEvent e = new TestEvent();
        recorder.addToBuckets(e);
        recorder.addToBuckets(e);

        TestEvent e2 = new TestEvent(e.getTriggerTime() + 61 * 1000, 1);
        recorder.addToBuckets(e2);

        List<EventBucket> events = recorder.getEventBuckets();

        for (int i = 0; i < events.size() - 1; i++) {
            assertTrue(events.get(0).isEmpty());
        }
        assertEquals(1, events.get(events.size() - 1).getCount());
    }

    @Test
    public void overwritesOldEvents() {
        RRD recorder = new RRD(60, RrdEventRecorder.SECOND, "");
        TestEvent e = new TestEvent();
        recorder.addToBuckets(e);
        recorder.addToBuckets(e);

        TestEvent e2 = new TestEvent(e.getTriggerTime() + 60 * 1000, 1);
        recorder.addToBuckets(e2);

        List<EventBucket> events = recorder.getEventBuckets();

        for (int i = 0; i < events.size() - 1; i++) {
            assertTrue(events.get(0).isEmpty());
        }
        assertEquals(1, events.get(events.size() - 1).getCount());
    }

    @Test
    public void handlesHourlyEvents() {
        RRD recorder = new RRD(24, RrdEventRecorder.HOUR, "");

        List<Event> hourlyEvents = createShuffledHourlyEvents();

        for (Event e : hourlyEvents) {
            recorder.addToBuckets(e);
        }

        List<EventBucket> buckets = recorder.getEventBuckets();
        assertEquals(24, buckets.size());

        for (EventBucket b : buckets) {
            System.out.println(b);
        }

        assertEquals(1, buckets.get(0).getCount());
        assertEquals(24, buckets.get(23).getCount());

    }

    @Test
    public void triggersCallbacks() {

        SimpleListener listener = new SimpleListener();

        RRD recorder = new RRD(24, RrdEventRecorder.HOUR, "quaxi", listener);

        List<Event> hourlyEvents = createHourlyEvents();

        for (Event e : hourlyEvents) {
            recorder.addToBuckets(e);
        }

        assertEquals(23, listener.count);
    }

    @Test
    public void triggersCallbacksInExpectedOrder() {

        SimpleListener listener = new SimpleListener();

        RRD recorder = new RRD(24, RrdEventRecorder.HOUR, "quaxi", listener);

        List<Event> hourlyEvents = createHourlyEvents();

        for (Event e : hourlyEvents) {
            recorder.addToBuckets(e);
        }

        checkThatEventBucketsAreContiguous(listener.events, RrdEventRecorder.HOUR);
    }

    @Test(expected = IllegalArgumentException.class)
    public void stopsOnNegativeEventTimes() {
        RRD recorder = new RRD(24, RrdEventRecorder.HOUR, "");
        Event e = new TestEvent(-1, 1);
        recorder.addToBuckets(e);
    }

    @Test
    public void extrapolatesDisjunctBuckets() {
        RRD recorder = new RRD(10, RrdEventRecorder.SECOND, "");
        Event e = new TestEvent(9500, 1);
        recorder.addToBuckets(e);

        List<EventBucket> buckets = recorder.getEventBuckets(100000);

        assertEquals(10, buckets.size());
        long prevEndTime = 100000 - 9 * 1000;
        for (EventBucket b : buckets) {
            assertEquals(prevEndTime, b.getIntervalStart());
            assertTrue(b.isEmpty());
            prevEndTime += 1000;
        }
    }

    @Test
    public void extrapolatesWhenNoBuckets() {
        RRD recorder = new RRD(10, RrdEventRecorder.SECOND, "");

        List<EventBucket> buckets = recorder.getEventBuckets(100000);

        assertEquals(10, buckets.size());
        long prevEndTime = 100000 - 9 * 1000;
        for (EventBucket b : buckets) {
            assertEquals(prevEndTime, b.getIntervalStart());
            assertTrue(b.isEmpty());
            prevEndTime += 1000;
        }
    }

    @Test
    public void extrapolatesIntoFuture() {
        RRD recorder = new RRD(10, RrdEventRecorder.SECOND, "");
        recorder.addToBuckets(new TestEvent(9500, 1));
        recorder.addToBuckets(new TestEvent(3500, 1));

        List<EventBucket> buckets = recorder.getEventBuckets(12500);

        assertEquals(10, buckets.size());

        assertEquals(0, buckets.get(9).getCount());
        assertEquals(0, buckets.get(8).getCount());
        assertEquals(0, buckets.get(7).getCount());
        assertEquals(1, buckets.get(6).getCount());
        assertEquals(0, buckets.get(5).getCount());
        assertEquals(0, buckets.get(4).getCount());
        assertEquals(0, buckets.get(3).getCount());
        assertEquals(0, buckets.get(2).getCount());
        assertEquals(0, buckets.get(1).getCount());
        assertEquals(1, buckets.get(0).getCount());
    }

    @Test
    public void extrapolatesIntoPast() {
        RRD recorder = new RRD(10, RrdEventRecorder.SECOND, "");
        recorder.addToBuckets(new TestEvent(9500, 1));
        recorder.addToBuckets(new TestEvent(3500, 1));

        List<EventBucket> buckets = recorder.getEventBuckets(4700);

        assertEquals(10, buckets.size());

        assertEquals(0, buckets.get(9).getCount());
        assertEquals(1, buckets.get(8).getCount());
        assertEquals(0, buckets.get(7).getCount());
        assertEquals(0, buckets.get(6).getCount());
        assertEquals(0, buckets.get(5).getCount());
        assertEquals(0, buckets.get(4).getCount());
        assertEquals(0, buckets.get(3).getCount());
        assertEquals(0, buckets.get(2).getCount());
        assertEquals(0, buckets.get(1).getCount());
        assertEquals(0, buckets.get(0).getCount());
    }

    List<Event> createShuffledHourlyEvents() {
        List<Event> events = createHourlyEvents();

        Collections.shuffle(events); // shuffle, because order should not matter

        return events;
    }

    List<Event> createHourlyEvents() {
        List<Event> events = new ArrayList<>();

        for (int i = 0; i < 24; i++) {
            for (int v = 0; v <= i; v++) {
                Event e = new TestEvent(RrdEventRecorder.DAY + i * RrdEventRecorder.HOUR + 1, i);
                events.add(e);
            }
        }

        Event e = new TestEvent(RrdEventRecorder.DAY - 1, 25);
        events.add(e);
        return events;
    }

}
