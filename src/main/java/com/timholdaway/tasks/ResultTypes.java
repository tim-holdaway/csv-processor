/* (C)2023 Tim Holdaway */
package com.timholdaway.tasks;

import java.util.List;

public interface ResultTypes {
    List<IntermediateResult<?>> resultsForShard();
}
