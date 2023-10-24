/* (C)2023 Tim Holdaway */
package com.timholdaway;

import static org.assertj.core.api.Assertions.assertThat;

import com.timholdaway.tasks.IntermediateResult;
import com.timholdaway.tasks.ResultTypes;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class FileProcessorTest {

    public static class TrivialResult implements IntermediateResult<TrivialResult> {
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

    public static class TrivialResultOnly implements ResultTypes {

        @Override
        public List<IntermediateResult<?>> resultsForShard() {
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
            List<IntermediateResult<?>> intermediateResults =
                    processor.processFile(testFile, new TrivialResultOnly().resultsForShard());

            assertThat(intermediateResults).hasSize(1);
            IntermediateResult<?> result = intermediateResults.get(0);

            assertThat(result).isInstanceOf(TrivialResult.class);
            assertThat(result.reportedResult()).isEqualTo("trivial result count: 2");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
