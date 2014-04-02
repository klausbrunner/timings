package net.e175.klaus.timings;

/**
 * Mutable implementation of EventBucket.
 * <p/>
 * This mutable implementation is somewhat easier on the garbage collector than
 * ImmutableBucket; very much so under high load (thousands of events per
 * second) and the Hotspot Client VM. On Hotspot server, it doesn't seem to make
 * much of a difference. YMMV.
 * <p/>
 * It has no synchronization whatsoever.
 *
 * @NotThreadSafe
 */
final class MutableBucket implements EventBucket {

    private long intervalStart;
    private long intervalEnd;
    private long count = 0L;

    private double minValue = 0;
    private double meanValue = 0;
    private double maxValue = 0;

    MutableBucket(final long intervalStart, final long intervalEnd, final Event event) {
        setInterval(intervalStart, intervalEnd);

        if (event != null) {
            count = 1;
            maxValue = event.getValue();
            minValue = maxValue;
            meanValue = maxValue;
        }
    }

    void setInterval(final long intervalStart, final long intervalEnd) {
        assert intervalEnd > intervalStart;
        this.intervalStart = intervalStart;
        this.intervalEnd = intervalEnd;
    }

    void addEvent(final Event event) {
        if (event == null) {
            return;
        }

        count++;
        final double value = event.getValue();

        if (value < minValue) {
            minValue = value;
        } else if (value > maxValue) {
            maxValue = value;
        }

        meanValue = meanValue + (value - meanValue) / count;
    }

    @Override
    public long getIntervalStart() {
        return intervalStart;
    }

    @Override
    public long getIntervalEnd() {
        return intervalEnd;
    }

    @Override
    public long getCount() {
        return count;
    }

    @Override
    public double getMaxValue() {
        return maxValue;
    }

    @Override
    public double getMeanValue() {
        return meanValue;
    }

    @Override
    public double getMinValue() {
        return minValue;
    }

    @Override
    public boolean isEmpty() {
        return getCount() == 0;
    }

    @Override
    public String toString() {
        return "MutableBucket [intervalStart=" + intervalStart + ", intervalEnd=" + intervalEnd + ", count=" + count
                + ", minValue=" + minValue + ", meanValue=" + meanValue + ", maxValue=" + maxValue + "]";
    }
}
