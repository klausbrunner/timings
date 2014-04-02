package net.e175.klaus.timings;

import java.io.Serializable;

/**
 * TimedEvent is a type of Event that records a completion time as well as
 * elapsed time.
 *
 * @Immutable
 */
public final class TimedEvent implements Event, Serializable {
    private static final long serialVersionUID = 1L;
    public static final String DEFAULT_NAME = "";
    private static final double NANOS_TO_MILLIS = 1E6;

    private final long triggerMilliseconds;
    private final long elapsedNanoseconds;
    private final double elapsedMilliseconds;
    private final String name;

    /**
     * Create a new TimedEvent based on a TimerStart instance and a name.
     *
     * @param startNanoseconds time at start of event, as measured by System.nanoTime()
     * @param name             Name for this event; must not be null.
     * @see java.lang.System#nanoTime()
     */
    public TimedEvent(final long startNanoseconds, final String name) {
        elapsedNanoseconds = System.nanoTime() - startNanoseconds;
        triggerMilliseconds = System.currentTimeMillis();
        elapsedMilliseconds = elapsedNanoseconds / NANOS_TO_MILLIS;
        this.name = name;
    }

    /**
     * Creates a new TimedEvent based on a TimerStart instance and the default
     * name.
     *
     * @param start TimerStart instance.
     * @see #DEFAULT_NAME
     */
    public TimedEvent(final TimerStart start) {
        elapsedNanoseconds = System.nanoTime() - start.getStartNanoseconds();
        triggerMilliseconds = System.currentTimeMillis();
        elapsedMilliseconds = elapsedNanoseconds / NANOS_TO_MILLIS;
        name = DEFAULT_NAME;
    }

    long getElapsedNanoseconds() {
        return elapsedNanoseconds;
    }

    double getElapsedMilliseconds() {
        return elapsedMilliseconds;
    }

    /**
     * @return The completion time of this event.
     */
    @Override
    public long getTriggerTime() {
        return triggerMilliseconds;
    }

    /**
     * @return The elapsed time for this event, in milliseconds.
     */
    @Override
    public double getValue() {
        return getElapsedMilliseconds();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "TimedEvent [triggerMilliseconds=" + triggerMilliseconds + ", elapsedMilliseconds="
                + elapsedMilliseconds + ", name=" + name + "]";
    }
}
