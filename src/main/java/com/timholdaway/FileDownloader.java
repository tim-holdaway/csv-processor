package com.timholdaway;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class FileDownloader {
    public File downloadFile(String url) throws IOException {
        File tempFile = File.createTempFile("CrowdStrikeTakehome-", ".tmp");
        tempFile.deleteOnExit();

        Files.copy(new URL(url).openStream(), tempFile.toPath(), REPLACE_EXISTING);
        return tempFile;
    }
}
