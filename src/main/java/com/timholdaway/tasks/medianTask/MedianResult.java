/* (C)2023 Tim Holdaway */
package com.timholdaway.tasks.medianTask;

import com.timholdaway.InputRow;
import com.timholdaway.tasks.IntermediateResult;

public class MedianResult implements IntermediateResult<MedianResult> {
  static final int MAX_AGE = 200;
  HistogramValue[] histogram = new HistogramValue[MAX_AGE];
  long count = 0;

  int shardsCount;

  public MedianResult() {
    this(1);
  }

  public MedianResult(int shardsCount) {
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
  public void coalesce(MedianResult other) {
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
  }

  @Override
  public String reportedResult() {
    return String.format(
        """
            Median results for %s shards

            Histogram:
            %s

            Count:
            %s

            Median:
            %s
            """
            .trim(),
        shardsCount,
        printHistogram(),
        count,
        getMedian());
  }

  public HistogramValue getMedian() {
    long halfCount = count / 2;
    long currentCount = 0;
    for (HistogramValue v : histogram) {
      currentCount += v.count;
      if (currentCount > halfCount) {
        return v;
      }
    }
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
    private long count = 0;
    private final long age;
    private InputRow representativeRow = null;

    public HistogramValue(int age) {
      this.age = age;
    }

    @Override
    public String toString() {
      return String.format("%s: %s (%s)", age, count, representativeRow);
    }
  }
}
