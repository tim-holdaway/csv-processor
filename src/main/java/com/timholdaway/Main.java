/* (C)2023 Tim Holdaway */
package com.timholdaway;

import java.io.File;
import java.io.IOException;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {
        // Press Opt+Enter with your caret at the highlighted text to see how
        // IntelliJ IDEA suggests fixing it.
        System.out.printf("Hello and welcome!");

        FileDownloader downloader = new FileDownloader();
        FileProcessor processor = new FileProcessor();

        try {
            File f =
                    downloader.downloadFile(
                            "file:///Users/tim/Downloads/DetPlatCloudHomework_main/data/file1.csv");
            System.out.printf("Retrieved a file %s", f.getAbsolutePath());

            processor.processFile(f);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
