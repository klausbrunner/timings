package net.e175.klaus.timings;

import java.util.ArrayList;
import java.util.List;

/**
 * RRD implements a fixed-size "round-robin database" (or circular buffer) of EventBuckets.
 *
 * @ThreadSafe
 */
final class RRD {
    /**
     * Keeps all shared mutable data. Use synchronized(instance) for single and
     * composite operations.
     */
    private static final class RRDStorage {
        private final MutableBucket[] buckets;
        private final long bucketWidthInMilliseconds;

        private int newestBucketIndex = -1;

        private RRDStorage(final int numberOfBuckets, final long bucketWidthInMilliseconds) {
            buckets = new MutableBucket[numberOfBuckets];
            this.bucketWidthInMilliseconds = bucketWidthInMilliseconds;
        }

        private EventBucket updateNewestBucketIndexReturnPreviousBucket(final int targetBucket) {
            EventBucket oldBucket = null;
            if (newestBucketIndex < 0
                    || buckets[targetBucket].getIntervalStart() > buckets[newestBucketIndex].getIntervalStart()) {

                if (newestBucketIndex >= 0) {
                    oldBucket = new ImmutableBucket(buckets[newestBucketIndex]);
                }

                newestBucketIndex = targetBucket;
            }
            return oldBucket;
        }

        private int virtualToRealIndex(final int virtualIndex) {
            assert virtualIndex >= 0 && virtualIndex < buckets.length;
            return (newestBucketIndex + 1 + virtualIndex) % buckets.length;
        }

        private EventBucket at(final int virtualIndex) {
            return buckets[virtualToRealIndex(virtualIndex)];
        }

        private long getLatestTimeForCurrentBuckets() {
            assert newestBucketIndex >= 0;

            return buckets[newestBucketIndex].getIntervalEnd();
        }

        private long getEarliestTimeForCurrentBuckets() {
            assert newestBucketIndex >= 0;

            return getLatestTimeForCurrentBuckets() - bucketWidthInMilliseconds * buckets.length;
        }

        private int getVirtualIndexForTime(final long time) {
            if (newestBucketIndex < 0) {
                return -1;
            }

            final long earliestTime = getEarliestTimeForCurrentBuckets();
            final long latestTime = getLatestTimeForCurrentBuckets();

            if (time < earliestTime || time >= latestTime) {
                return -1;
            }

            return (int) ((time - earliestTime) / bucketWidthInMilliseconds);
        }

        private ImmutableBucket getExportableBucketFrom(final int virtualIndex) {
            final long earliestTime = getEarliestTimeForCurrentBuckets();
            final EventBucket currentBuck = at(virtualIndex);
            if (currentBuck != null && currentBuck.getIntervalStart() >= earliestTime) {
                return new ImmutableBucket(currentBuck);
            } else {
                final long expectedIntervalStart = earliestTime + virtualIndex * bucketWidthInMilliseconds;
                final long expectedIntervalEnd = expectedIntervalStart + bucketWidthInMilliseconds;
                return new ImmutableBucket(expectedIntervalStart, expectedIntervalEnd, null);
            }
        }
    }

    private final RRDStorage data;
    private final int numberOfBuckets;
    private final long bucketWidthInMilliseconds;
    private final String name;
    private final EventBucketListener listener;

    RRD(final int numberOfBuckets, final long bucketWidthInMilliseconds, final String name) {
        assert numberOfBuckets > 0 && bucketWidthInMilliseconds > 0;

        this.numberOfBuckets = numberOfBuckets;
        this.bucketWidthInMilliseconds = bucketWidthInMilliseconds;
        this.name = name;
        listener = null;

        data = new RRDStorage(numberOfBuckets, bucketWidthInMilliseconds);
    }

    RRD(final int numberOfBuckets, final long bucketWidthInMilliseconds, final String name,
        final EventBucketListener listener) {
        assert numberOfBuckets > 0 && bucketWidthInMilliseconds > 0;

        this.numberOfBuckets = numberOfBuckets;
        this.bucketWidthInMilliseconds = bucketWidthInMilliseconds;
        this.name = name;
        this.listener = listener;

        data = new RRDStorage(numberOfBuckets, bucketWidthInMilliseconds);
    }

    void addToBuckets(final Event e) {
        final long eventTime = e.getTriggerTime();

        if (eventTime < 0) {
            throw new IllegalArgumentException(
                    "negative trigger times are not correctly handled by current implementation");
        }

        final int targetBucket = calcTargetBucket(eventTime);

        EventBucket finishedBucket;
        synchronized (data) {
            final MutableBucket oldBucket = data.buckets[targetBucket];
            if (oldBucket != null) {
                if (tooNewForBucket(eventTime, oldBucket)) {
                    data.buckets[targetBucket] = bucketFromScratch(e);
                } else if (tooOldForBucket(eventTime, oldBucket)) {
                    return;
                } else {
                    // bucket interval still fits
                    oldBucket.addEvent(e);
                }
            } else {
                // no bucket yet, create new one
                data.buckets[targetBucket] = bucketFromScratch(e);
            }
            finishedBucket = data.updateNewestBucketIndexReturnPreviousBucket(targetBucket);
        }

        if (finishedBucket != null && listener != null) {
            listener.supersededBucket(name, finishedBucket);
        }

    }

    String getName() {
        return name;
    }

    List<EventBucket> getEventBuckets() {
        final List<EventBucket> bucketlist = new ArrayList<>(numberOfBuckets);

        synchronized (data) {
            if (data.newestBucketIndex < 0) {
                return bucketlist;
            }

            for (int i = 0; i < numberOfBuckets; i++) {
                bucketlist.add(data.getExportableBucketFrom(i));
            }
        }

        return bucketlist;
    }

    List<EventBucket> getEventBuckets(final long untilMilliseconds) {
        final List<EventBucket> bucketlist = new ArrayList<>(numberOfBuckets);

        final long intervalStartOfFirstBucket = intervalStartForTriggerTime(untilMilliseconds)
                + bucketWidthInMilliseconds - numberOfBuckets * bucketWidthInMilliseconds;

        synchronized (data) {
            for (int i = 0; i < numberOfBuckets; i++) {
                final long intervalStartOfThisBucket = intervalStartOfFirstBucket + i * bucketWidthInMilliseconds;
                final int virtualIndex = data.getVirtualIndexForTime(intervalStartOfThisBucket);

                if (virtualIndex >= 0) {
                    bucketlist.add(data.getExportableBucketFrom(virtualIndex));
                } else {
                    final ImmutableBucket emptyBucket = new ImmutableBucket(intervalStartOfThisBucket,
                            intervalStartOfThisBucket + bucketWidthInMilliseconds, null);
                    bucketlist.add(emptyBucket);
                }
            }
        }

        return bucketlist;
    }

    private boolean tooNewForBucket(final long eventTime, final EventBucket oldBucket) {
        return oldBucket.getIntervalEnd() < eventTime;
    }

    private boolean tooOldForBucket(final long eventTime, final EventBucket oldBucket) {
        return oldBucket.getIntervalStart() > eventTime;
    }

    private MutableBucket bucketFromScratch(final Event event) {
        final long startTime = intervalStartForTriggerTime(event.getTriggerTime());
        return new MutableBucket(startTime, startTime + bucketWidthInMilliseconds, event);
    }

    private long intervalStartForTriggerTime(final long triggerTime) {
        return triggerTime / bucketWidthInMilliseconds * bucketWidthInMilliseconds;
    }

    private int calcTargetBucket(final long eventTime) {
        return (int) (eventTime / bucketWidthInMilliseconds % numberOfBuckets);
    }

}
