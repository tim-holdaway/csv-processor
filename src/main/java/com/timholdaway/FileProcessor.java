/* (C)2023 Tim Holdaway */
package com.timholdaway;

import static com.timholdaway.Result.error;
import static com.timholdaway.Result.ok;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.timholdaway.accumulators.Accumulator;
import java.io.File;
import java.util.List;

public class FileProcessor {

    public Result<List<Accumulator<?>>> processFile(File file, List<Accumulator<?>> resultTypes) {
        CsvMapper mapper = new CsvMapper();
        mapper.configure(CsvParser.Feature.FAIL_ON_MISSING_COLUMNS, true);
        mapper.configure(CsvParser.Feature.FAIL_ON_MISSING_HEADER_COLUMNS, true);

        CsvSchema schema = mapper.schemaFor(InputRow.class).withHeader().withColumnReordering(true);

        try (MappingIterator<InputRow> it =
                mapper.readerFor(InputRow.class).with(schema).readValues(file)) {
            boolean rowsRead = false;
            while (it.hasNext()) {
                InputRow current = it.next();
                rowsRead = true;
                resultTypes.forEach(result -> result.accumulate(current));
            }
            if (!rowsRead
                    && !((CsvSchema) it.getParserSchema())
                            .getColumnNames()
                            .equals(mapper.schemaFor(InputRow.class).getColumnNames())) {
                throw new IllegalArgumentException("Bad header row with single-line file");
            }
        } catch (Exception e) {
            return error(
                    String.format("Failed to process file (%s)", e.getMessage()), file.toString());
        }

        return ok(resultTypes);
    }
}
