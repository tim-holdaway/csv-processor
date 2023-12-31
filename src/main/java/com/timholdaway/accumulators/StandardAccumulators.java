/* (C)2023 Tim Holdaway */
package com.timholdaway.accumulators;

import java.util.Arrays;
import java.util.List;

public class StandardAccumulators implements AccumulatorTypes {

    private MeanAccumulator mean = new MeanAccumulator(0);
    private MedianAccumulator median = new MedianAccumulator(0);

    @Override
    public List<Accumulator<?>> resultsForShard() {
        return Arrays.asList(new MeanAccumulator(), new MedianAccumulator());
    }

    @Override
    public void accumulate(Accumulator<?> accumulator) {
        if (accumulator instanceof MeanAccumulator shardMean) {
            mean.coalesce(shardMean);
        } else if (accumulator instanceof MedianAccumulator shardMedian) {
            median.coalesce(shardMedian);
        }
    }

    @Override
    public void printResults() {
        System.out.println(mean.reportedResult());
        System.out.println(median.reportedResult());
    }
}
