/* (C)2023 Tim Holdaway */
package com.timholdaway;

import com.timholdaway.accumulators.StandardAccumulators;
import java.util.Arrays;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {
        // Press Opt+Enter with your caret at the highlighted text to see how
        // IntelliJ IDEA suggests fixing it.
        System.out.printf("Hello and welcome!");

        FileDownloader downloader = new FileDownloader();
        FileProcessor processor = new FileProcessor();

        FileBatchDownloaderProcessor batcher =
                new FileBatchDownloaderProcessor(downloader, processor);

        batcher.downloadAndProcess(
                Arrays.asList(
                        "file:///Users/tim/Downloads/DetPlatCloudHomework_main/data/file1.csv",
                        "file:///Users/tim/Downloads/DetPlatCloudHomework_main/data/file2.csv",
                        "file:///Users/tim/Downloads/DetPlatCloudHomework_main/data/file3.csv",
                        "file:///Users/tim/Downloads/DetPlatCloudHomework_main/data/file4.csv",
                        "file:///Users/tim/Downloads/DetPlatCloudHomework_main/data/bogus.csv",
                        "file:///Users/tim/Downloads/DetPlatCloudHomework_main/data/file5.csv"),
                new StandardAccumulators());
    }
}
