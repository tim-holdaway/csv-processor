/* (C)2023 Tim Holdaway */
package com.timholdaway;

import com.timholdaway.accumulators.StandardAccumulators;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import picocli.CommandLine;

public class CsvProcessorApplication {

    public static void main(String[] args) {
        new CsvProcessorApplication().run();
    }

    public void run() {

        List<String> urls = readUrlsFromStdin();

        System.out.println("Urls: ");
        System.out.println(urls);

        FileDownloader downloader = new FileDownloader();
        FileProcessor processor = new FileProcessor();

        FileBatchDownloaderProcessor batcher =

                new SequentialFileBatchDownloaderProcessor(downloader, processor);

        batcher.downloadAndProcess(urls, new StandardAccumulators());
    }

    private List<String> readUrlsFromStdin() {
        List<String> urls = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            urls.add(scanner.nextLine());
        }
        return urls;
    }
}
