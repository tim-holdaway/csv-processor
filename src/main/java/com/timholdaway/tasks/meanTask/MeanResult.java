/* (C)2023 Tim Holdaway */
package com.timholdaway.tasks.meanTask;

import com.timholdaway.InputRow;
import com.timholdaway.tasks.IntermediateResult;

public class MeanResult implements IntermediateResult<MeanResult> {
  long sum = 0;
  long count = 0;

  int shardsCount;

  public MeanResult() {
    this(1);
  }

  public MeanResult(int shardsCount) {
    this.shardsCount = shardsCount;
  }

  @Override
  public void accumulate(InputRow inputRow) {
    sum += inputRow.age();
    count++;
  }

  @Override
  public void coalesce(MeanResult other) {
    sum += other.sum;
    count += other.count;
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

  private double getMean() {
    if (count == 0) {
      return 0;
    }
    return sum / (double) count;
  }
}
