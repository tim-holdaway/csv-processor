/* (C)2023 Tim Holdaway */
package com.timholdaway;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class FileDownloaderTest {
    @Test
    public void testDownloadsExistingFile() {
        try {
            File testFile = File.createTempFile("TestFile", ".tmp");
            testFile.deleteOnExit();

            Files.write(testFile.toPath(), Arrays.asList("Foo", "Bar"));

            FileDownloader downloader = new FileDownloader();
            File downloadedFile = downloader.downloadFile(testFile.toURI().toURL().toString());

            List<String> lines = Files.readAllLines(downloadedFile.toPath());
            assertThat(lines).containsExactly("Foo", "Bar");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
