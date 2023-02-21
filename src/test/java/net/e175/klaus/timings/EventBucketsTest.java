package net.e175.klaus.timings;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventBucketsTest {

    @Test
    public void defaultConstructorDoesNotCrash() {
        new EventBuckets();
    }

    @Test
    public void copyConstructorDoesNotCrash() {
        ImmutableBucket b = new ImmutableBucket(0, 1000, null);
        List<EventBucket> buckets = new ArrayList<>();

        buckets.add(b);

        new EventBuckets("foo", buckets);
    }

    @Test
    public void copyConstructorWithMapDoesNotCrash() {
        ImmutableBucket b = new ImmutableBucket(0, 1000, null);
        List<EventBucket> buckets = new ArrayList<>();
        buckets.add(b);

        Map<String, List<EventBucket>> map = new HashMap<>();
        map.put("foo", buckets);

        new EventBuckets(map);
    }

    @Test
    public void defaultConstructorOfInternalWrapperDoesNotCrash() {
        new EventBuckets.EventBucketWrapper();
    }

    private List<EventBucket> createSomeBuckets() {
        ImmutableBucket b1 = new ImmutableBucket(0, 1000, new TestEvent(0, 10, "name1"));
        ImmutableBucket b2 = new ImmutableBucket(1000, 2000, new TestEvent(1000, 10, "name1"));
        ImmutableBucket b3 = new ImmutableBucket(2000, 3000, null);
        List<EventBucket> buckets = new ArrayList<>();

        buckets.add(b1);
        buckets.add(b2);
        buckets.add(b3);
        return buckets;
    }

}
