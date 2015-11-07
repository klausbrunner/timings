package net.e175.klaus.timings;

import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RrdEventRecorderThreadsTest {

    @Test
    @Ignore("can take a long time to run with coverage tools enabled")
    public void canRecordManyEventsFromMultipleThreads() throws Exception {
        final int eventCount = 10 * 1000 * 1000;
        final int threadCount = 20;
        RrdEventRecorder recorder = new RrdEventRecorder(1, RrdEventRecorder.DAY * 2);

        EventMassProducer.runThreads(eventCount, threadCount, recorder);

        long totalCount = 0;
        for (String name : recorder.getNames()) {
            List<EventBucket> bucks = recorder.getEventBuckets(name);

            assertTrue(bucks.get(0).getMeanValue() >= 0);
            totalCount += bucks.get(0).getCount();
        }

        assertEquals(eventCount * threadCount, totalCount);

    }

}
