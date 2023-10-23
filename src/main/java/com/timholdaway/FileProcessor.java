/* (C)2023 Tim Holdaway */
package com.timholdaway;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.timholdaway.tasks.IntermediateResult;
import com.timholdaway.tasks.meanTask.MeanResult;
import com.timholdaway.tasks.medianTask.MedianResult;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class FileProcessor {

    public List<IntermediateResult<?>> processFile(File file) throws IOException {
        CsvMapper mapper = new CsvMapper();
        CsvSchema headerSchema = CsvSchema.emptySchema().withHeader();

        MeanResult meanResult = new MeanResult();
        MedianResult medianResult = new MedianResult();

        try (MappingIterator<InputRow> it =
                mapper.readerFor(InputRow.class)
                        .with(mapper.schemaFor(InputRow.class))
                        .with(headerSchema)
                        .readValues(file)) {
            while (it.hasNext()) {
                InputRow current = it.next();

                meanResult.accumulate(current);
                medianResult.accumulate(current);
            }
        }

        return Arrays.asList(meanResult, medianResult);
    }
}
