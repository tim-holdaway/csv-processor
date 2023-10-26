/* (C)2023 Tim Holdaway */
package com.timholdaway;

import static com.timholdaway.Result.error;
import static com.timholdaway.Result.ok;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

public class FileDownloader {
    public Result<File> downloadFile(String url) {
        try {
            File tempFile = File.createTempFile("CrowdStrikeTakehome-", ".tmp");
            tempFile.deleteOnExit();
            Files.copy(new URL(url).openStream(), tempFile.toPath(), REPLACE_EXISTING);
            return ok(tempFile);
        } catch (IOException e) {
            return error(String.format("Failed to download file %s (%s)", url, e.getMessage()));
        }
    }
}
