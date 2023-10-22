/* (C)2023 Tim Holdaway */
package com.timholdaway.tasks.meanTask;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.timholdaway.InputRow;
import org.junit.jupiter.api.Test;

public class MeanResultTest {
    @Test
    public void testResultWithEmptyValues() {
        MeanResult result = new MeanResult();
        assertEquals(0, result.getMean());
    }

    @Test
    public void testResultWithValues() {
        MeanResult result = new MeanResult();
        result.accumulate(new InputRow("Foo", "Bar", 28));
        result.accumulate(new InputRow("Foo2", "Bar", 29));

        assertEquals(2, result.count);
        assertEquals(57, result.sum);
        assertEquals(28.5, result.getMean());
        assertEquals(1, result.shardsCount);
    }

    @Test
    public void testCoalesce() {
        MeanResult result1 = new MeanResult();
        result1.accumulate(new InputRow("Foo", "Bar", 28));
        result1.accumulate(new InputRow("Foo2", "Bar", 29));
        MeanResult result2 = new MeanResult();
        result2.accumulate(new InputRow("Foo3", "Bar", 30));
        MeanResult result3 = new MeanResult();
        result3.accumulate(new InputRow("Foo4", "Bar", 31));

        result2.coalesce(result3);
        result1.coalesce(result2);

        assertEquals(4, result1.count);
        assertEquals(118, result1.sum);
        assertEquals(29.5, result1.getMean());
        assertEquals(3, result1.shardsCount);
    }
}
