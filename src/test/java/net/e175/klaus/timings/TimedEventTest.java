package net.e175.klaus.timings;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.Assert.*;

public class TimedEventTest {
    public static final double MAXDELTA = 1e-6;

    @Test
    public void canBeInstantiated() {
        new TimedEvent(System.nanoTime(), "");
    }

    @Test
    public void canBeInstantiatedViaTimerStart() {
        TimedEvent e = new TimerStart().stop("foo");
        assertTrue(e.getElapsedNanoseconds() >= 0);
    }

    @Test
    public void knowsItsName() {
        Event e = new TimedEvent(System.nanoTime(), "hopsiflopsi");
        assertEquals("hopsiflopsi", e.getName());
    }

    @Test
    public void knowsItsDefaultName() {
        Event e = new TimedEvent(new TimerStart());
        assertEquals(TimedEvent.DEFAULT_NAME, e.getName());
    }

    @Test
    public void knowsEndTime() {
        long myEndTime = System.currentTimeMillis();
        TimedEvent e = new TimedEvent(System.nanoTime(), "");
        assertTrue(myEndTime <= e.getTriggerTime());
    }

    @Test
    public void knowsElapsedTime() {
        TimedEvent e = new TimedEvent(System.nanoTime(), "");
        assertTrue(e.getElapsedNanoseconds() >= 0);
        assertEquals(e.getElapsedNanoseconds() / 1000000d, e.getElapsedMilliseconds(), MAXDELTA);
    }

    @Test
    public void returnsSomethingForToString() {
        TimedEvent e = new TimedEvent(System.nanoTime(), "");
        assertNotNull(e.toString());
    }

    @Test
    public void canBeSerialized() throws Exception {
        TimedEvent e = new TimedEvent(System.nanoTime(), "quaxi");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(e);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        TimedEvent resurrected = (TimedEvent) ois.readObject();

        assertEquals(e.getName(), resurrected.getName());
    }
}
