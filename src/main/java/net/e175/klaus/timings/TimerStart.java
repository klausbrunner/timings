package net.e175.klaus.timings;

import java.io.Serializable;

/**
 * TimerStart simply records the time at its creation.
 *
 * @Immutable
 */
public final class TimerStart implements Serializable {
    private static final long serialVersionUID = 1L;

    private final long startNanoseconds;

    /**
     * The default constructor records the time at this object's construction.
     */
    public TimerStart() {
        startNanoseconds = System.nanoTime();
    }

    /**
     * @return Object's creation time as measured by System.nanoTime().
     * @see java.lang.System#nanoTime()
     */
    public long getStartNanoseconds() {
        return startNanoseconds;
    }

    /**
     * Constructs a new TimedEvent representing the timespan from this object's
     * creation to the instant of this call.
     *
     * @param eventName name for the Event object
     * @return new TimedEvent object
     * @see TimedEvent
     */
    public TimedEvent stop(final String eventName) {
        return new TimedEvent(startNanoseconds, eventName);
    }

    @Override
    public String toString() {
        return "TimerStart [startNanoseconds=" + startNanoseconds + "]";
    }
}
