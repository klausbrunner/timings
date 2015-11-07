package net.e175.klaus.timings;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ImmutableBucketTest {

    public static final double MAXDELTA = 1e-6;

    @Test
    public void handlesNullEventGracefully() {
        ImmutableBucket b = new ImmutableBucket(0, 1000, null);
        assertEquals(0, b.getCount());
    }

    @Test
    public void countsRight() {
        final TestEvent e = new TestEvent(0, 4711);
        ImmutableBucket b = new ImmutableBucket(0, 1000, e);
        assertEquals(1, b.getCount());

        for (int i = 0; i < 100; i++) {
            b = new ImmutableBucket(b, e);
        }

        assertEquals(101, b.getCount());
        assertEquals(4711, b.getMeanValue(), MAXDELTA);
        assertEquals(4711, b.getMinValue(), MAXDELTA);
        assertEquals(4711, b.getMaxValue(), MAXDELTA);
    }

    @Test
    public void calculatesProperAverageMinMax() {
        TestEvent e = new TestEvent(0, 0);
        ImmutableBucket b = new ImmutableBucket(0, 1000, e);

        for (int i = 0; i < 100000; i++) {
            e = new TestEvent(0, i);
            b = new ImmutableBucket(b, e);
            e = new TestEvent(0, -i);
            b = new ImmutableBucket(b, e);
        }

        assertEquals(200001, b.getCount());

        assertEquals(0, b.getMeanValue(), MAXDELTA);
        assertEquals(-99999, b.getMinValue(), MAXDELTA);
        assertEquals(99999, b.getMaxValue(), MAXDELTA);
    }

    @Test
    public void canConstructCopy() {
        TestEvent e = new TestEvent(10, 20);
        ImmutableBucket original = new ImmutableBucket(100, 1000, e);

        ImmutableBucket copy = new ImmutableBucket(original);

        assertEquals(original.getIntervalStart(), copy.getIntervalStart());
        assertEquals(original.getIntervalEnd(), copy.getIntervalEnd());
        assertEquals(original.getMinValue(), copy.getMinValue(), MAXDELTA);
        assertEquals(original.getMeanValue(), copy.getMeanValue(), MAXDELTA);
        assertEquals(original.getMaxValue(), copy.getMaxValue(), MAXDELTA);

    }

}
