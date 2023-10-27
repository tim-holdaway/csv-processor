/* (C)2023 Tim Holdaway */
package com.timholdaway;

import com.timholdaway.accumulators.Accumulator;
import com.timholdaway.accumulators.StandardAccumulators;
import java.io.File;
import java.time.Clock;
import java.util.*;

public class SequentialFileBatchDownloaderProcessor implements FileBatchDownloaderProcessor {
    FileDownloader downloader;
    FileProcessor processor;

    public SequentialFileBatchDownloaderProcessor(
            FileDownloader downloader, FileProcessor processor) {
        this.downloader = downloader;
        this.processor = processor;
    }

    public Result<List<Accumulator<?>>> processFileResult(
            Result<File> fileResult, StandardAccumulators standardAccumulators) {
        return fileResult.mapResult(
                file -> processor.processFile(file, standardAccumulators.resultsForShard()));
    }

    @Override
    public void downloadAndProcess(List<String> urls, StandardAccumulators standardAccumulators) {
        long start = Clock.systemUTC().millis();

        List<Result<File>> tempDownloads =
                urls.stream().map(url -> downloader.downloadFile(url)).toList();

        List<Result<List<Accumulator<?>>>> results =
                tempDownloads.stream()
                        .map(fileResult -> processFileResult(fileResult, standardAccumulators))
                        .toList();

        List<Accumulator<?>> successful =
                results.stream()
                        .map(Result::extractOk)
                        .filter(Objects::nonNull)
                        .flatMap(Collection::stream)
                        .toList();

        List<String> errors =
                results.stream().map(Result::extractError).filter(Objects::nonNull).toList();

        for (Accumulator<?> result : successful) {
            standardAccumulators.accumulate(result);
        }

        long end = Clock.systemUTC().millis();

        System.out.println("Processed files in " + ((double) end - start) / 1000 + " seconds");
        System.out.println("Ran tasks on one thread");

        System.out.println();
        System.out.println("Errors: ");
        for (String err : errors) {
            System.out.println("Got Error: " + err);
        }

        System.out.println();
        System.out.println("Results:");
        System.out.println(standardAccumulators.getMean().reportedResult());
        System.out.println(standardAccumulators.getMedian().reportedResult());
    }
}
