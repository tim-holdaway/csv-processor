/* (C)2023 Tim Holdaway */
package com.timholdaway;

import com.timholdaway.tasks.IntermediateResult;
import com.timholdaway.tasks.meanTask.MeanResult;
import com.timholdaway.tasks.medianTask.MedianResult;
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

    public void downloadAndProcess(List<String> urls) {
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

        List<IntermediateResult<?>> results =
                tempDownloads.stream()
                        .flatMap(
                                file -> {
                                    try {
                                        System.out.printf(
                                                "Processing file %s%n", file.getAbsolutePath());
                                        return processor.processFile(file).stream();
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                })
                        .toList();

        MeanResult aggregateMean = new MeanResult(0);
        MedianResult aggreagteMedian = new MedianResult(0);

        results.forEach(
                result -> {
                    if (result instanceof MeanResult shardMean) {
                        aggregateMean.coalesce(shardMean);
                    } else if (result instanceof MedianResult shardMedian) {
                        aggreagteMedian.coalesce(shardMedian);
                    }
                });

        System.out.println(aggregateMean.reportedResult());
        System.out.println(aggreagteMedian.reportedResult());
    }
}
