/* (C)2023 Tim Holdaway */
package com.timholdaway.integrationTest;

import static org.assertj.core.api.Assertions.assertThat;

import com.timholdaway.CsvProcessorApplication;
import com.timholdaway.ParallelFileBatchDownloaderProcessor;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class CsvProcessorApplicationIntegrationTest {

    private File makeTestFile(List<String> content) {
        try {
            File testFile = File.createTempFile("TestFile", ".tmp");
            testFile.deleteOnExit();

            Files.write(testFile.toPath(), content);
            return testFile;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testCsvProcessorApplication() {
        ArrayList<File> testFiles =
                new ArrayList<>(
                        Arrays.asList(
                                makeTestFile(
                                        Arrays.asList(
                                                "fname,lname,age", "Bar,Foo,12", "Baz,qux,13")),
                                makeTestFile(
                                        Arrays.asList(
                                                "fname,lname,age",
                                                "Baz,Foo,24",
                                                "Biz,qux,25",
                                                "Bax,qax,50")),
                                makeTestFile(Arrays.asList("this is a bad file"))));

        File nonExistingFile = makeTestFile(Arrays.asList("foo"));
        nonExistingFile.delete();
        testFiles.add(nonExistingFile);

        List<String> testUrls =
                testFiles.stream()
                        .map(
                                file -> {
                                    try {
                                        return file.toURI().toURL().toString();
                                    } catch (MalformedURLException e) {
                                        throw new RuntimeException(e);
                                    }
                                })
                        .toList();

        CsvProcessorApplication application =
                new CsvProcessorApplication((ParallelFileBatchDownloaderProcessor::new));

        final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(myOut));
        application.run(testUrls);

        final String standardOutput = myOut.toString();

        assertThat(standardOutput).contains("Mean: 24.8");
        assertThat(standardOutput).contains("Median: 24 (Baz Foo)");
        assertThat(standardOutput).containsOnlyOnce("Failed to process file");
        assertThat(standardOutput).containsOnlyOnce("Failed to download file");
    }
}
