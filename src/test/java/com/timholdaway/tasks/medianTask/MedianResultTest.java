/* (C)2023 Tim Holdaway */
package com.timholdaway.tasks.medianTask;

import static org.junit.jupiter.api.Assertions.*;

import com.timholdaway.InputRow;
import org.junit.jupiter.api.Test;

public class MedianResultTest {
    @Test
    public void testResultWithEmptyValues() {
        MedianResult result = new MedianResult();
        assertNull(result.getMedian());
    }

    @Test
    public void testResultWithMultipleExistingMedianRecords() {
        MedianResult result = new MedianResult();
        result.accumulate(new InputRow("Foo", "Bar", 28));
        result.accumulate(new InputRow("Foo2", "Bar", 29));
        result.accumulate(new InputRow("Foo3", "Bar", 29));
        result.accumulate(new InputRow("Foo4", "Bar", 50));

        assertEquals(4, result.count);
        assertEquals(1, result.shardsCount);
        MedianResult.HistogramValue v = result.getMedian();
        assertEquals(29, v.age);
        assertEquals(2, v.count);
        assertEquals(new InputRow("Foo2", "Bar", 29), v.representativeRow);
    }

    @Test
    public void testMedianWithNonexistingRecordsPicksNextExistingRow() {
        MedianResult result = new MedianResult();
        result.accumulate(new InputRow("Foo", "Bar", 1));
        result.accumulate(new InputRow("Foo2", "Bar", 1));
        result.accumulate(new InputRow("Foo3", "Bar", 10));
        result.accumulate(new InputRow("Foo4", "Bar", 11));

        MedianResult.HistogramValue v = result.getMedian();
        assertEquals(10, v.age);
        assertEquals(1, v.count);
        assertEquals(new InputRow("Foo3", "Bar", 10), v.representativeRow);
    }

    @Test
    public void testCoalesce() {
        MedianResult result1 = new MedianResult();
        result1.accumulate(new InputRow("Foo2", "Bar", 20));
        MedianResult result2 = new MedianResult();
        result2.accumulate(new InputRow("Foo3", "Bar", 30));
        MedianResult result3 = new MedianResult();
        result3.accumulate(new InputRow("Foo4", "Bar", 31));

        result2.coalesce(result3);
        result1.coalesce(result2);

        assertEquals(3, result1.count);
        assertEquals(30, result1.getMedian().age);
        assertEquals(3, result1.shardsCount);
    }
}
