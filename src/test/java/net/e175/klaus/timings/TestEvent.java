package net.e175.klaus.timings;

final class TestEvent implements Event {

	final long startTimeMilliseconds;
	final double value;
	final String name;

	public TestEvent() {
		value = 0;
		startTimeMilliseconds = System.currentTimeMillis();
		this.name = "";
	}

	public TestEvent(final long startTimeMilliseconds, final double value) {
		this.startTimeMilliseconds = startTimeMilliseconds;
		this.value = value;
		this.name = "";
	}

	public TestEvent(final long startTimeMilliseconds, final double value, String name) {
		this.startTimeMilliseconds = startTimeMilliseconds;
		this.value = value;
		this.name = name;
	}

	@Override
	public long getTriggerTime() {
		return this.startTimeMilliseconds;
	}

	@Override
	public double getValue() {
		return this.value;
	}

	@Override
	public String getName() {
		return name;
	}

}
