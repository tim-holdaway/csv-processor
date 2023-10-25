/* (C)2023 Tim Holdaway */
package com.timholdaway;

import com.timholdaway.accumulators.Accumulator;
import com.timholdaway.accumulators.MeanAccumulator;
import com.timholdaway.accumulators.MedianAccumulator;
import com.timholdaway.accumulators.StandardAccumulators;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class FileBatchDownloaderProcessor {
    FileDownloader downloader;
    FileProcessor processor;

    public FileBatchDownloaderProcessor(FileDownloader downloader, FileProcessor processor) {
        this.downloader = downloader;
        this.processor = processor;
    }

    public void downloadAndProcess(List<String> urls, StandardAccumulators standardAccumulators) {
        List<File> tempDownloads =
                urls.stream()
                        .map(
                                url -> {
                                    try {
                                        System.out.printf("Downloading file %s%n", url);
                                        return downloader.downloadFile(url);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                })
                        .toList();

        List<Accumulator<?>> results =
                tempDownloads.stream()
                        .flatMap(
                                file -> {
                                    try {
                                        System.out.printf(
                                                "Processing file %s%n", file.getAbsolutePath());
                                        return processor
                                                .processFile(
                                                        file,
                                                        standardAccumulators.resultsForShard())
                                                .stream();
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                })
                        .toList();

        MeanAccumulator aggregateMean = new MeanAccumulator(0);
        MedianAccumulator aggreagteMedian = new MedianAccumulator(0);

        results.forEach(
                result -> {
                    if (result instanceof MeanAccumulator shardMean) {
                        aggregateMean.coalesce(shardMean);
                    } else if (result instanceof MedianAccumulator shardMedian) {
                        aggreagteMedian.coalesce(shardMedian);
                    }
                });

        System.out.println(aggregateMean.reportedResult());
        System.out.println(aggreagteMedian.reportedResult());
    }
}
