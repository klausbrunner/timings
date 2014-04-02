package net.e175.klaus.timings;

/**
 * This callback interface must be implemented by listeners interested in
 * EventBucket transitions. Such transitions occur when newer Events have been
 * recorded that the latest EventBucket's interval does not include, which thus
 * is superseded. A typical application would be logging.
 * <p/>
 * <p>
 * Note that there is no guarantee that these EventBuckets are really "finished"
 * in the sense that there will be no more updates to them. EventRecorders do
 * not assume Events to arrive in ascending order of their triggerTimes, so even
 * a superseded EventBucket may be updated later on if such older Events are
 * received. If your Events are recorded in an order not strongly correlated to
 * their trigger times (as could happen when collecting from large, widely
 * distributed systems or with longer buffering of Events), this callback will
 * probably not be useful.
 * </p>
 * <p/>
 * <p>
 * Moreover, there is no guarantee that the last EventBucket used during a
 * process's lifetime is ever reported to this callback. The current
 * implementation will only trigger the callback once a new EventBucket has been
 * created.
 * </p>
 */
public interface EventBucketListener {

    /**
     * Callback method. As this method will be called while recording an event,
     * its implementation should return quickly to prevent blocking the caller.
     * For complex processing, consider e.g. copying to a queue for processing
     * by another thread. It may also be advisable not to throw any runtime
     * exceptions.
     *
     * @param name   The name of the calling RRD (typically the name of the Events
     *               collected in this EventBucket).
     * @param bucket A copy of the finished bucket.
     */
    void supersededBucket(String name, EventBucket bucket);
}
