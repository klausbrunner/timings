package net.e175.klaus.timings;

import org.junit.Test;

import static org.junit.Assert.*;

public class MutableBucketTest {

    public static final double MAXDELTA = 1e-6;

    @Test
    public void knowsItsInterval() {
        MutableBucket b = new MutableBucket(10, 1000, null);

        assertEquals(10, b.getIntervalStart());
        assertEquals(1000, b.getIntervalEnd());
    }

    @Test
    public void handlesNullEventGracefully() {
        MutableBucket b = new MutableBucket(0, 1000, null);
        assertEquals(0, b.getCount());

        b.addEvent(null);
        assertEquals(0, b.getCount());
        assertTrue(b.isEmpty());
    }

    @Test
    public void countsRight() {
        final TestEvent e = new TestEvent(0, 4711);
        MutableBucket b = new MutableBucket(0, 1000, e);
        assertEquals(1, b.getCount());

        for (int i = 0; i < 100; i++) {
            b.addEvent(e);
        }

        assertEquals(101, b.getCount());
        assertFalse(b.isEmpty());
        assertEquals(4711, b.getMeanValue(), MAXDELTA);
        assertEquals(4711, b.getMinValue(), MAXDELTA);
        assertEquals(4711, b.getMaxValue(), MAXDELTA);
    }

    @Test
    public void calculatesProperAverageMinMax() {
        TestEvent e = new TestEvent(0, 0);
        MutableBucket b = new MutableBucket(0, 1000, e);

        for (int i = 0; i < 100000; i++) {
            e = new TestEvent(0, i);
            b.addEvent(e);
            e = new TestEvent(0, -i);
            b.addEvent(e);
        }

        assertEquals(200001, b.getCount());

        assertEquals(0, b.getMeanValue(), MAXDELTA);
        assertEquals(-99999, b.getMinValue(), MAXDELTA);
        assertEquals(99999, b.getMaxValue(), MAXDELTA);
    }

    @Test
    public void returnsSomethingOnToString() {
        MutableBucket b = new MutableBucket(0, 1000, null);
        assertNotNull(b.toString());
    }

}
