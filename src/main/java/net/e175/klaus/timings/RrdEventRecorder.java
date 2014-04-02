package net.e175.klaus.timings;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * RrdEventRecorder is an EventRecorder that aggregates Events by name into
 * fixed-size circular buffers, providing a constantly updated view of a time
 * window reaching from "now" (date of newest recorded Event) into the past (as
 * defined by the number of EventBuckets and their interval width).
 *
 * @ThreadSafe
 */
public class RrdEventRecorder implements EventRecorder {
    public static final int DEFAULT_NUM_BUCKETS = 24;
    public static final long DEFAULT_BUCKET_WIDTH = RrdEventRecorder.HOUR;

    private final int numberOfBuckets;
    private final long bucketWidthMilliseconds;
    private final EventBucketListener listener;

    private final ConcurrentMap<String, RRD> rrdMap = new ConcurrentHashMap<String, RRD>();

    public static final long SECOND = 1000l;
    public static final long MINUTE = 60 * SECOND;
    public static final long HOUR = 60 * MINUTE;
    public static final long DAY = 24 * HOUR;

    /**
     * Create an RrdEventRecorder with the default number of buckets and bucket
     * width.
     *
     * @see #DEFAULT_NUM_BUCKETS
     * @see #DEFAULT_BUCKET_WIDTH
     */
    public RrdEventRecorder() {
        numberOfBuckets = DEFAULT_NUM_BUCKETS;
        bucketWidthMilliseconds = DEFAULT_BUCKET_WIDTH;
        listener = null;
    }

    /**
     * Create an RrdEventRecorder.
     *
     * @param numberOfBuckets         Must be greater than 0.
     * @param bucketWidthMilliseconds Must be greater than 0.
     */
    public RrdEventRecorder(final int numberOfBuckets, final long bucketWidthMilliseconds) {
        checkParams(numberOfBuckets, bucketWidthMilliseconds);

        this.numberOfBuckets = numberOfBuckets;
        this.bucketWidthMilliseconds = bucketWidthMilliseconds;
        listener = null;
    }

    /**
     * Create an RrdEventRecorder with a callback listener for bucket
     * transitions.
     *
     * @param numberOfBuckets         Must be greater than 0.
     * @param bucketWidthMilliseconds Must be greater than 0.
     * @param listener                See {@link EventBucketListener} for details. Ignored if null.
     */
    public RrdEventRecorder(final int numberOfBuckets, final long bucketWidthMilliseconds,
                            final EventBucketListener listener) {
        checkParams(numberOfBuckets, bucketWidthMilliseconds);

        this.numberOfBuckets = numberOfBuckets;
        this.bucketWidthMilliseconds = bucketWidthMilliseconds;
        this.listener = listener;
    }

    private void checkParams(final int numberOfBuckets, final long bucketWidthMilliseconds) {
        if (numberOfBuckets <= 0 || bucketWidthMilliseconds <= 0) {
            throw new IllegalArgumentException("numberOfBuckets and bucketWidthMilliseconds must be greater than 0");
        }
    }

    /**
     * Record an event. Events are kept separated based on their names.
     *
     * @param event Event object. Null values are silently ignored.
     * @see RrdEventRecorder#getEventBuckets(String)
     */
    @Override
    public void record(final Event event) {
        if (event == null) {
            return;
        }

        final String name = event.getName();

        RRD target = rrdMap.get(name);

        while (target == null) {
            rrdMap.putIfAbsent(name, new RRD(numberOfBuckets, bucketWidthMilliseconds, name, listener));
            target = rrdMap.get(name);
        }

        target.addToBuckets(event);
    }

    /**
     * Get a list of event buckets in chronological order, with the last bucket
     * corresponding to the last recorded event's trigger time.
     *
     * @param name Event name for which to retrieve the list.
     * @return List of EventBuckets. If the name was not found (no events
     * recorded for it), an empty list is returned. Otherwise, the list
     * size is always the numberOfBuckets given to this object's
     * constructor.
     */
    public List<EventBucket> getEventBuckets(final String name) {
        final RRD target = rrdMap.get(name);
        if (target != null) {
            return target.getEventBuckets();
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Get a list of event buckets in chronological order, with the last bucket
     * corresponding to the untilMillis parameter. Any buckets not covered by
     * the current RRD buffer are "extrapolated" as empty buckets.
     * <p/>
     * This is useful to display a sliding window ending with the current time,
     * even if no events have been recorded recently.
     *
     * @param name        Event name for which to retrieve the list.
     * @param untilMillis Time (milliseconds-since-epoch) which should be in the last
     *                    bucket's interval.
     * @return List of EventBuckets. If the name was not found (no events
     * recorded for it), an empty list is returned. Otherwise, the list
     * size is always the numberOfBuckets given to this object's
     * constructor.
     */
    public List<EventBucket> getEventBuckets(final String name, final long untilMillis) {
        final RRD target = rrdMap.get(name);
        if (target != null) {
            return target.getEventBuckets(untilMillis);
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Get a map of all lists of (used) event buckets, for all names, with the
     * last bucket corresponding to the untilMillis parameter. Any buckets not
     * covered by the current RRD buffer are "extrapolated" as empty buckets.
     * <p/>
     * This is effectively a convenience method combining {@link #getNames()}
     * and {@link #getEventBuckets(String, long)}.
     *
     * @param untilMillis Time (milliseconds-since-epoch) which should be in the last
     *                    bucket's interval.
     * @see #getEventBuckets(String, long)
     */
    public Map<String, List<EventBucket>> getEventBuckets(final long untilMillis) {
        final Map<String, List<EventBucket>> result = new TreeMap<String, List<EventBucket>>();
        for (final Map.Entry<String, RRD> entry : rrdMap.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getEventBuckets(untilMillis));
        }
        return result;
    }

    /**
     * Get a map of all lists of (used) event buckets, for all names.
     * <p/>
     * This is effectively a convenience method combining {@link #getNames()}
     * and {@link #getEventBuckets(String)}.
     *
     * @see #getEventBuckets(String)
     */
    public Map<String, List<EventBucket>> getEventBuckets() {
        final Map<String, List<EventBucket>> result = new TreeMap<String, List<EventBucket>>();
        for (final Map.Entry<String, RRD> entry : rrdMap.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getEventBuckets());
        }
        return result;
    }

    /**
     * @return All names that have been used in recorded events so far (since
     * creation or the last clear() call).
     */
    public List<String> getNames() {
        final List<String> result = new ArrayList<String>(rrdMap.keySet());
        Collections.sort(result);
        return result;
    }

    /**
     * Clear history of events; forget everything recorded so far.
     */
    public void clear() {
        rrdMap.clear();
    }

}