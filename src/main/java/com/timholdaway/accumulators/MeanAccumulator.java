/* (C)2023 Tim Holdaway */
package com.timholdaway.accumulators;

import com.timholdaway.InputRow;

public class MeanAccumulator implements Accumulator<MeanAccumulator> {
    long sum = 0;
    long count = 0;

    int shardsCount;

    public MeanAccumulator() {
        this(1);
    }

    public MeanAccumulator(int shardsCount) {
        this.shardsCount = shardsCount;
    }

    @Override
    public void accumulate(InputRow inputRow) {
        sum += inputRow.age();
        count++;
    }

    @Override
    public void coalesce(MeanAccumulator other) {
        sum += other.sum;
        count += other.count;
        shardsCount += other.shardsCount;
    }

    @Override
    public String reportedResult() {
        return String.format(
                """
                        Mean results for %s shards

                        Sum:
                        %s

                        Count:
                        %s

                        Mean:
                        %s
                        """
                        .trim(),
                shardsCount,
                sum,
                count,
                getMean());
    }

    public double getMean() {
        if (count == 0) {
            return 0;
        }
        return sum / (double) count;
    }
}
