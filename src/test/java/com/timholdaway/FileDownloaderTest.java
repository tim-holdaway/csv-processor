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
            Result<File> downloadedFile =
                    downloader.downloadFile(testFile.toURI().toURL().toString());

            assertThat(downloadedFile.isOk()).isTrue();

            List<String> lines = Files.readAllLines(downloadedFile.extractOk().toPath());
            assertThat(lines).containsExactly("Foo", "Bar");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testDownloadsNonExistingFile() {
        try {
            File testFile = File.createTempFile("TestFile", ".tmp");
            testFile.delete();

            FileDownloader downloader = new FileDownloader();
            Result<File> downloadedFile =
                    downloader.downloadFile(testFile.toURI().toURL().toString());

            assertThat(downloadedFile.isError()).isTrue();
            assertThat(downloadedFile.extractError()).startsWith("Failed to download file");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
