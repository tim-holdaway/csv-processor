/* (C)2023 Tim Holdaway */
package com.timholdaway.accumulators;

import java.util.Arrays;
import java.util.List;

public class StandardAccumulators implements AccumulatorTypes {
    @Override
    public List<Accumulator<?>> resultsForShard() {
        return Arrays.asList(new MeanAccumulator(), new MedianAccumulator());
    }
}
