/* (C)2023 Tim Holdaway */
package com.timholdaway;

import com.timholdaway.accumulators.StandardAccumulators;
import java.util.*;
import java.util.function.BiFunction;

public class CsvProcessorApplication {

    BiFunction<FileDownloader, FileProcessor, FileBatchDownloaderProcessor>
            downloaderProcessorOptionFunction;
    FileDownloader downloader;
    FileProcessor processor;

    public CsvProcessorApplication(
            BiFunction<FileDownloader, FileProcessor, FileBatchDownloaderProcessor>
                    downloaderProcessorOptionFunction) {
        this.downloaderProcessorOptionFunction = downloaderProcessorOptionFunction;
        downloader = new FileDownloader();
        processor = new FileProcessor();
    }

    public static void main(String[] args) {
        // Quick-and-dirty command line switch to compare multithreaded with sequential processing
        BiFunction<FileDownloader, FileProcessor, FileBatchDownloaderProcessor>
                downloaderProcessorOptionFunction =
                        (FileDownloader downloader, FileProcessor processor) ->
                                (args.length == 1 && Objects.equals(args[0], "--single-threaded"))
                                        ? new SequentialFileBatchDownloaderProcessor(
                                                downloader, processor)
                                        : new ParallelFileBatchDownloaderProcessor(
                                                downloader, processor);
        List<String> urls = readUrlsFromStdin();

        new CsvProcessorApplication(downloaderProcessorOptionFunction).run(urls);
    }

    public void run(List<String> urls) {

        System.out.println("Downloading and processing " + urls.size() + " urls");

        FileBatchDownloaderProcessor batchProcessor =
                downloaderProcessorOptionFunction.apply(downloader, processor);

        batchProcessor.downloadAndProcess(urls, new StandardAccumulators());
    }

    private static List<String> readUrlsFromStdin() {
        System.out.println("reading urls from stdin");
        List<String> urls = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            urls.add(scanner.nextLine());
        }
        return urls;
    }
}
