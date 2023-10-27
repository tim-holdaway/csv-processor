/* (C)2023 Tim Holdaway */
package com.timholdaway.accumulators;

import com.timholdaway.InputRow;

public class MedianAccumulator implements Accumulator<MedianAccumulator> {
    static final int MAX_AGE = 200;
    HistogramValue[] histogram = new HistogramValue[MAX_AGE];
    long count = 0;

    int shardsCount;

    public MedianAccumulator() {
        this(1);
    }

    public MedianAccumulator(int shardsCount) {
        for (int i = 0; i < MAX_AGE; ++i) {
            histogram[i] = new HistogramValue(i);
        }
        this.shardsCount = shardsCount;
    }

    @Override
    public void accumulate(InputRow inputRow) {
        int age = inputRow.age();
        if (age >= MAX_AGE) {
            throw new IllegalArgumentException(
                    String.format("Age %s in input data exceeded max age %s", age, MAX_AGE));
        }

        HistogramValue currentHistogramValue = histogram[age];
        if (currentHistogramValue.representativeRow == null) {
            currentHistogramValue.representativeRow = inputRow;
        }
        currentHistogramValue.count++;
        count++;
    }

    @Override
    public void coalesce(MedianAccumulator other) {
        count += other.count;
        for (int i = 0; i < MAX_AGE; ++i) {
            HistogramValue currentHistogramValue = histogram[i];
            HistogramValue otherHistogramValue = other.histogram[i];
            assert (currentHistogramValue.age == otherHistogramValue.age);
            currentHistogramValue.count += otherHistogramValue.count;
            if (currentHistogramValue.representativeRow == null) {
                currentHistogramValue.representativeRow = otherHistogramValue.representativeRow;
            }
        }
        shardsCount += other.shardsCount;
    }

    @Override
    public String reportedResult() {
        return String.format("Median: %s", getMedian());
    }

    public HistogramValue getMedian() {
        if (count == 0) {
            return null;
        }
        long halfCount = count / 2;
        long currentCount = 0;
        for (HistogramValue v : histogram) {
            currentCount += v.count;
            if (currentCount > halfCount) {
                return v;
            }
        }
        // We should never reach this exception, because the counts within histogram should add up
        // to the full count
        throw new IllegalStateException("Median histogram did not contain enough values");
    }

    public String printHistogram() {
        StringBuilder builder = new StringBuilder();
        for (HistogramValue v : histogram) {
            builder.append(v.toString()).append("\n");
        }
        return builder.toString();
    }

    public static class HistogramValue {
        long count = 0;
        final long age;
        InputRow representativeRow = null;

        public HistogramValue(int age) {
            this.age = age;
        }

        @Override
        public String toString() {
            return String.format(
                    "%s (%s %s)", age, representativeRow.fname(), representativeRow.lname());
        }
    }
}
