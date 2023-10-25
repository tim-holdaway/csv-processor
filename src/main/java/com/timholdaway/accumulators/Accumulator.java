/* (C)2023 Tim Holdaway */
package com.timholdaway.accumulators;

import com.timholdaway.InputRow;

public interface Accumulator<T extends Accumulator<T>> {
    void accumulate(InputRow inputRow);

    void coalesce(T other);

    String reportedResult();
}
