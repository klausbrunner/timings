package net.e175.klaus.timings;

/**
 * An Event models something that can be recorded. Events have no identity, as
 * they are expected to be aggregated anyway.
 * <p/>
 * Implementations should take care of thread safety: immutability is
 * recommended.
 */
public interface Event {

    /**
     * The time instant associated with this event. Value is assumed to be the
     * customary "milliseconds since 1970", as returned by
     * System.currentTimeMillis() and other date/time methods in the Java API.
     *
     * @see java.lang.System#currentTimeMillis()
     */
    long getTriggerTime();

    /**
     * Identifier for a kind/type/category of Event. Example: "OK" for
     * successful events, "FAIL" for failed ones.
     */
    String getName();

    /**
     * Value attached with this Event. Some scalar measurement, e.g. an
     * execution time.
     */
    double getValue();

}