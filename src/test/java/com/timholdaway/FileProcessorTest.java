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

        @Override
        public void accumulate(Accumulator<?> accumulator) {}
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
            Result<List<Accumulator<?>>> accumulators =
                    processor.processFile(testFile, new TrivialAccumulatorOnly().resultsForShard());

            assertThat(accumulators.isOk()).isTrue();

            assertThat(accumulators.extractOk()).hasSize(1);
            Accumulator<?> result = accumulators.extractOk().get(0);

            assertThat(result).isInstanceOf(TrivialResult.class);
            assertThat(result.reportedResult()).isEqualTo("trivial result count: 2");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testProcessesColumnsInDifferentOrder() {
        try {
            File testFile = File.createTempFile("TestFile", ".tmp");
            testFile.deleteOnExit();

            Files.write(
                    testFile.toPath(),
                    Arrays.asList("lname,age,fname", "Foo,12,Bar", "qux,13,Baz"));

            FileProcessor processor = new FileProcessor();
            Result<List<Accumulator<?>>> accumulators =
                    processor.processFile(testFile, new TrivialAccumulatorOnly().resultsForShard());

            assertThat(accumulators.isOk()).isTrue();

            assertThat(accumulators.extractOk()).hasSize(1);
            Accumulator<?> result = accumulators.extractOk().get(0);

            assertThat(result).isInstanceOf(TrivialResult.class);
            assertThat(result.reportedResult()).isEqualTo("trivial result count: 2");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testFailsToProcessCsvWithExtraValues() {
        try {
            File testFile = File.createTempFile("TestFile", ".tmp");
            testFile.deleteOnExit();

            Files.write(
                    testFile.toPath(),
                    Arrays.asList("fname,lname,age", "Bar,Foo,12,extra", "Baz,qux,13"));

            FileProcessor processor = new FileProcessor();
            Result<List<Accumulator<?>>> accumulators =
                    processor.processFile(testFile, new TrivialAccumulatorOnly().resultsForShard());

            assertThat(accumulators.isError()).isTrue();
            assertThat(accumulators.extractError()).contains("Failed to process file");
            assertThat(accumulators.extractError()).contains("Too many entries");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testFailsToProcessCsvWithExtraHeadersAndColumns() {
        try {
            File testFile = File.createTempFile("TestFile", ".tmp");
            testFile.deleteOnExit();

            Files.write(
                    testFile.toPath(),
                    Arrays.asList("fname,lname,age,foo", "Bar,Foo,12,extra", "Baz,qux,13,extra"));

            FileProcessor processor = new FileProcessor();
            Result<List<Accumulator<?>>> accumulators =
                    processor.processFile(testFile, new TrivialAccumulatorOnly().resultsForShard());

            assertThat(accumulators.isError()).isTrue();
            assertThat(accumulators.extractError()).contains("Failed to process file");
            assertThat(accumulators.extractError()).contains("Unrecognized field \"foo\"");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testFailsToProcessCsvWithMissingColumns() {
        try {
            File testFile = File.createTempFile("TestFile", ".tmp");
            testFile.deleteOnExit();

            Files.write(testFile.toPath(), Arrays.asList("fname,lname", "Bar,Foo", "Baz,qux"));

            FileProcessor processor = new FileProcessor();
            Result<List<Accumulator<?>>> accumulators =
                    processor.processFile(testFile, new TrivialAccumulatorOnly().resultsForShard());

            assertThat(accumulators.isError()).isTrue();
            assertThat(accumulators.extractError()).contains("Failed to process file");
            assertThat(accumulators.extractError()).contains("Missing 1 header column");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testFailsToProcessExampleBadFile() {
        try {
            File testFile = File.createTempFile("TestFile", ".tmp");
            testFile.deleteOnExit();

            Files.write(testFile.toPath(), Arrays.asList("This is not a csv file at all"));

            FileProcessor processor = new FileProcessor();
            Result<List<Accumulator<?>>> accumulators =
                    processor.processFile(testFile, new TrivialAccumulatorOnly().resultsForShard());

            assertThat(accumulators.isError()).isTrue();
            assertThat(accumulators.extractError()).contains("Failed to process file");
            assertThat(accumulators.extractError()).containsPattern("Missing . header columns");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
