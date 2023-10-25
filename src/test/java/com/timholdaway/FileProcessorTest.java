/* (C)2023 Tim Holdaway */
package com.timholdaway;

import static org.assertj.core.api.Assertions.assertThat;

import com.timholdaway.accumulators.Accumulator;
import com.timholdaway.accumulators.AccumulatorTypes;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class FileProcessorTest {

    public static class TrivialResult implements Accumulator<TrivialResult> {
        int count = 0;

        @Override
        public void accumulate(InputRow inputRow) {
            ++count;
        }

        @Override
        public void coalesce(TrivialResult other) {
            count += other.count;
        }

        @Override
        public String reportedResult() {
            return "trivial result count: " + count;
        }
    }

    public static class TrivialAccumulatorOnly implements AccumulatorTypes {

        @Override
        public List<Accumulator<?>> resultsForShard() {
            return List.of(new TrivialResult());
        }
    }

    @Test
    public void testProcessesValidFileAndReturnsIntermediateResults() {
        try {
            File testFile = File.createTempFile("TestFile", ".tmp");
            testFile.deleteOnExit();

            Files.write(
                    testFile.toPath(),
                    Arrays.asList("fname,lname,age", "Bar,Foo,12", "Baz,qux,13"));

            FileProcessor processor = new FileProcessor();
            List<Accumulator<?>> accumulators =
                    processor.processFile(testFile, new TrivialAccumulatorOnly().resultsForShard());

            assertThat(accumulators).hasSize(1);
            Accumulator<?> result = accumulators.get(0);

            assertThat(result).isInstanceOf(TrivialResult.class);
            assertThat(result.reportedResult()).isEqualTo("trivial result count: 2");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
