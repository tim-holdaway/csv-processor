package com.timholdaway;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.io.File;
import java.io.IOException;

public class FileProcessor {

    public void processFile(File file) throws IOException {
        CsvMapper mapper = new CsvMapper();
        CsvSchema headerSchema = CsvSchema.emptySchema().withHeader();

        try (MappingIterator<InputRow> it = mapper.readerFor(InputRow.class)
                .with(mapper.schemaFor(InputRow.class))
                .with(headerSchema)
                .readValues(file)) {
            while (it.hasNext()) {
                InputRow current = it.next();
                System.out.println("Read a row: " + current);
            }
        }
    }
}
