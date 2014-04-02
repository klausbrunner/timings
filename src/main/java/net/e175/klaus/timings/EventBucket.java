package net.e175.klaus.timings;

/**
 * An EventBucket aggregates Events falling into a certain (half-closed) time
 * interval.
 */
public interface EventBucket {

    /**
     * Time instant that begins this bucket's interval (inclusive). Value is
     * assumed to be the customary "milliseconds since 1970", as returned by
     * System.currentTimeMillis() and other date/time methods in the Java API.
     *
     * @see java.lang.System#currentTimeMillis()
     */
    long getIntervalStart();

    /**
     * Time instant that ends this bucket's interval (exclusive). Value is
     * assumed to be the customary "milliseconds since 1970", as returned by
     * System.currentTimeMillis() and other date/time methods in the Java API.
     *
     * @see java.lang.System#currentTimeMillis()
     */
    long getIntervalEnd();

    /**
     * Count of events recorded within this interval.
     */
    long getCount();

    /**
     * Maximum value recorded within this interval. Only valid if getCount() >
     * 0.
     */
    double getMaxValue();

    /**
     * Arithmetic mean of all values recorded in this interval. Only valid if
     * getCount() > 0.
     */
    double getMeanValue();

    /**
     * Minimum value recorded within this interval. Only valid if getCount() >
     * 0.
     */
    double getMinValue();

    /**
     * @return getCount() == 0
     */
    boolean isEmpty();

}
