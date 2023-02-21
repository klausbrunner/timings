package net.e175.klaus.timings;

/**
 * Immutable implementation of EventBucket.
 * <p>
 * This implementation has the usual nice properties of immutable objects
 * (thread safety, safe publication), but may not be as memory-efficient for
 * frequent updates.
 *
 * @Immutable
 */
final class ImmutableBucket implements EventBucket {

    private final long intervalStart;
    private final long intervalEnd;
    private final long count;

    private final double minValue;
    private final double meanValue;
    private final double maxValue;

    ImmutableBucket(final EventBucket oldBucket, final Event newEvent) {
        assert oldBucket.getIntervalStart() <= newEvent.getTriggerTime();
        assert oldBucket.getIntervalEnd() > newEvent.getTriggerTime();

        intervalStart = oldBucket.getIntervalStart();
        intervalEnd = oldBucket.getIntervalEnd();
        count = oldBucket.getCount() + 1;

        minValue = Math.min(newEvent.getValue(), oldBucket.getMinValue());
        maxValue = Math.max(newEvent.getValue(), oldBucket.getMaxValue());
        meanValue = oldBucket.getMeanValue() + (newEvent.getValue() - oldBucket.getMeanValue()) / count;
    }

    ImmutableBucket(final long intervalStart, final long intervalEnd, final Event event) {
        assert intervalEnd > intervalStart;

        this.intervalStart = intervalStart;
        this.intervalEnd = intervalEnd;

        if (event != null) {
            count = 1;
            maxValue = event.getValue();
            minValue = maxValue;
            meanValue = maxValue;
        } else {
            count = 0;
            maxValue = 0;
            minValue = 0;
            meanValue = 0;
        }
    }

    /**
     * Copy constructor.
     */
    ImmutableBucket(final EventBucket sourceBucket) {
        assert sourceBucket != null;

        intervalStart = sourceBucket.getIntervalStart();
        intervalEnd = sourceBucket.getIntervalEnd();
        count = sourceBucket.getCount();
        minValue = sourceBucket.getMinValue();
        meanValue = sourceBucket.getMeanValue();
        maxValue = sourceBucket.getMaxValue();
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
        return "ImmutableBucket [intervalStart=" + intervalStart + ", intervalEnd=" + intervalEnd + ", count=" + count
                + ", minValue=" + minValue + ", meanValue=" + meanValue + ", maxValue=" + maxValue + "]";
    }
}