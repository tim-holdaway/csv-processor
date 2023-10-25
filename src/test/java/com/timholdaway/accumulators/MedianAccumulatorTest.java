/* (C)2023 Tim Holdaway */
package com.timholdaway.accumulators;

import static org.junit.jupiter.api.Assertions.*;

import com.timholdaway.InputRow;
import org.junit.jupiter.api.Test;

public class MedianAccumulatorTest {
    @Test
    public void testResultWithEmptyValues() {
        MedianAccumulator result = new MedianAccumulator();
        assertNull(result.getMedian());
    }

    @Test
    public void testResultWithMultipleExistingMedianRecords() {
        MedianAccumulator result = new MedianAccumulator();
        result.accumulate(new InputRow("Foo", "Bar", 28));
        result.accumulate(new InputRow("Foo2", "Bar", 29));
        result.accumulate(new InputRow("Foo3", "Bar", 29));
        result.accumulate(new InputRow("Foo4", "Bar", 50));

        assertEquals(4, result.count);
        assertEquals(1, result.shardsCount);
        MedianAccumulator.HistogramValue v = result.getMedian();
        assertEquals(29, v.age);
        assertEquals(2, v.count);
        assertEquals(new InputRow("Foo2", "Bar", 29), v.representativeRow);
    }

    @Test
    public void testMedianWithNonexistingRecordsPicksNextExistingRow() {
        MedianAccumulator result = new MedianAccumulator();
        result.accumulate(new InputRow("Foo", "Bar", 1));
        result.accumulate(new InputRow("Foo2", "Bar", 1));
        result.accumulate(new InputRow("Foo3", "Bar", 10));
        result.accumulate(new InputRow("Foo4", "Bar", 11));

        MedianAccumulator.HistogramValue v = result.getMedian();
        assertEquals(10, v.age);
        assertEquals(1, v.count);
        assertEquals(new InputRow("Foo3", "Bar", 10), v.representativeRow);
    }

    @Test
    public void testCoalesce() {
        MedianAccumulator result1 = new MedianAccumulator();
        result1.accumulate(new InputRow("Foo2", "Bar", 20));
        MedianAccumulator result2 = new MedianAccumulator();
        result2.accumulate(new InputRow("Foo3", "Bar", 30));
        MedianAccumulator result3 = new MedianAccumulator();
        result3.accumulate(new InputRow("Foo4", "Bar", 31));

        result2.coalesce(result3);
        result1.coalesce(result2);

        assertEquals(3, result1.count);
        assertEquals(30, result1.getMedian().age);
        assertEquals(3, result1.shardsCount);
    }
}
