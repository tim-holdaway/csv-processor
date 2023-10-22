/* (C)2023 Tim Holdaway */
package com.timholdaway.tasks;

import com.timholdaway.InputRow;

public interface IntermediateResult<T extends IntermediateResult<T>> {
    void accumulate(InputRow inputRow);

    void coalesce(T other);

    String reportedResult();
}
