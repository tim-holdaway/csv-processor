/* (C)2023 Tim Holdaway */
package com.timholdaway.tasks;

import com.timholdaway.tasks.meanTask.MeanResult;
import com.timholdaway.tasks.medianTask.MedianResult;
import java.util.Arrays;
import java.util.List;

public class StandardResults implements ResultTypes {
    @Override
    public List<IntermediateResult<?>> resultsForShard() {
        return Arrays.asList(new MeanResult(), new MedianResult());
    }
}
