/* (C)2023 Tim Holdaway */
package com.timholdaway;

import static com.timholdaway.Result.error;

import com.timholdaway.accumulators.Accumulator;
import com.timholdaway.accumulators.StandardAccumulators;
import java.io.File;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

public class ParallelFileBatchDownloaderProcessor implements FileBatchDownloaderProcessor {
    FileDownloader downloader;
    FileProcessor processor;

    public ParallelFileBatchDownloaderProcessor(
            FileDownloader downloader, FileProcessor processor) {
        this.downloader = downloader;
        this.processor = processor;
    }

    public Result<List<Accumulator<?>>> processFileResult(
            Result<File> fileResult, StandardAccumulators standardAccumulators) {
        return fileResult.mapResult(
                file -> processor.processFile(file, standardAccumulators.resultsForShard()));
    }

    private static void printCurrentThread(String operation) {
        System.out.println(operation + " running on " + Thread.currentThread().getName());
    }

    //    public Runnable downloadThenProcess(ExecutorService executorService, String url) {
    //        CompletableFuture<String> urlFuture = CompletableFuture.supplyAsync(() -> url);
    //        CompletableFuture<Result<File>> fileFuture = urlFuture.thenApplyAsync(value -> {
    //            printCurrentThread("Download " + url);
    //            return downloader.downloadFile(value);
    //        });
    //        fileFuture.thenApplyAsync(fileResult -> processFileResult(fileResult, standar))
    //    }

    private static Result<List<Accumulator<?>>> getFutureOrError(
            CompletableFuture<Result<List<Accumulator<?>>>> future) {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            return error(e.toString());
        }
    }

    @Override
    public void downloadAndProcess(List<String> urls, StandardAccumulators standardAccumulators) {
        long start = Clock.systemUTC().millis();

        List<CompletableFuture<Result<List<Accumulator<?>>>>> shardResultFutures =
                new ArrayList<>();
        for (String url : urls) {
            CompletableFuture<String> urlFuture = CompletableFuture.supplyAsync(() -> url);
            CompletableFuture<Result<File>> fileFuture =
                    urlFuture.thenApplyAsync(
                            value -> {
                                printCurrentThread("Download " + url);
                                return downloader.downloadFile(value);
                            });
            CompletableFuture<Result<List<Accumulator<?>>>> resultCompletableFuture =
                    fileFuture.thenApplyAsync(
                            fileResult -> {
                                printCurrentThread("Process " + url);
                                return processFileResult(fileResult, standardAccumulators);
                            });
            shardResultFutures.add(resultCompletableFuture);
        }

        List<Accumulator<?>> successful =
                shardResultFutures.stream()
                        .map(ParallelFileBatchDownloaderProcessor::getFutureOrError)
                        .map(Result::extractOk)
                        .filter(Objects::nonNull)
                        .flatMap(Collection::stream)
                        .toList();

        List<String> errors =
                shardResultFutures.stream()
                        .map(ParallelFileBatchDownloaderProcessor::getFutureOrError)
                        .map(Result::extractError)
                        .filter(Objects::nonNull)
                        .toList();

        for (Accumulator<?> result : successful) {
            standardAccumulators.accumulate(result);
        }
        long end = Clock.systemUTC().millis();

        System.out.println("Processed files in " + ((double) end - start) / 1000 + " seconds");
        System.out.println(
                "Ran tasks on thread pool ForkJoinPool.commonPool with size: "
                        + ForkJoinPool.commonPool().getPoolSize());

        System.out.println();
        System.out.println("Errors: ");
        for (String err : errors) {
            System.out.println(err);
        }

        System.out.println();
        System.out.println("Results:");
        System.out.println(standardAccumulators.getMean().reportedResult());
        System.out.println(standardAccumulators.getMedian().reportedResult());
    }
}
