package net.e175.klaus.timings;

/**
 * An EventRecorder accepts Event objects.
 */
public interface EventRecorder {

    /**
     * Record an event.
     *
     * @param event Event object.
     */
    void record(final Event event);

}
