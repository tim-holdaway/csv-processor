/* (C)2023 Tim Holdaway */
package com.timholdaway.accumulators;

import java.util.List;

public interface AccumulatorTypes {
    List<Accumulator<?>> resultsForShard();

    void accumulate(Accumulator<?> accumulator);
}
