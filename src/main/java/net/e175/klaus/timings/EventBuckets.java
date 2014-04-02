package net.e175.klaus.timings;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A convenient wrapper/transfer class for exporting EventBuckets via JAXB to
 * XML/JSON. It should not be used for any other purpose.
 *
 * @NotThreadSafe
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("all")
public final class EventBuckets {

    public NameToBuckets[] buckets;

    /**
     * Create an empty instance.
     */
    public EventBuckets() {
        buckets = new NameToBuckets[0];
    }

    /**
     * Create a new instance based on a list of EventBuckets. The bucket
     * contents are copied to this object; no reference is kept to the original
     * list.
     */
    public EventBuckets(final String name, final List<EventBucket> buckets) {
        assert buckets != null;
        assert name != null;

        final EventBucketWrapper[] bucketArray = toWrapperArray(buckets);
        this.buckets = new NameToBuckets[]{new NameToBuckets(name, bucketArray)};
    }

    /**
     * Create a new instance based on a map of lists of EventBuckets. The bucket
     * contents are copied to this object; no reference is kept to the original
     * list.
     */
    public EventBuckets(final Map<String, List<EventBucket>> buckets) {
        assert buckets != null;

        this.buckets = new NameToBuckets[buckets.keySet().size()];

        int i = 0;
        for (final Map.Entry<String, List<EventBucket>> entry : buckets.entrySet()) {
            final EventBucketWrapper[] bucketArray = toWrapperArray(entry.getValue());
            this.buckets[i++] = new NameToBuckets(entry.getKey(), bucketArray);
        }
    }

    private EventBucketWrapper[] toWrapperArray(final List<EventBucket> buckets) {
        final List<EventBucketWrapper> bucketlist = new ArrayList<EventBucketWrapper>(buckets.size());
        for (final EventBucket source : buckets) {
            bucketlist.add(new EventBucketWrapper(source));
        }
        return bucketlist.toArray(new EventBucketWrapper[bucketlist.size()]);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    static class NameToBuckets {
        @XmlAttribute
        public String name;
        @XmlElement(name = "bucket")
        public EventBucketWrapper[] buckets;

        NameToBuckets() {
        }

        NameToBuckets(final String name, final EventBucketWrapper[] buckets) {
            this.name = name;
            this.buckets = buckets;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    static class EventBucketWrapper {
        public Long intervalStart;
        public Long intervalEnd;

        public Long count;
        public Double minValue;
        public Double meanValue;
        public Double maxValue;

        EventBucketWrapper() {
        }

        EventBucketWrapper(final EventBucket source) {
            intervalStart = source.getIntervalStart();
            intervalEnd = source.getIntervalEnd();

            count = source.getCount();
            if (count > 0) {
                minValue = source.getMinValue();
                meanValue = source.getMeanValue();
                maxValue = source.getMaxValue();
            }
        }
    }
}
