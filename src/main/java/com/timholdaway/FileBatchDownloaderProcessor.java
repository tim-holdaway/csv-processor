package com.timholdaway;

import com.timholdaway.accumulators.StandardAccumulators;

import java.util.List;

public interface FileBatchDownloaderProcessor {
    void downloadAndProcess(List<String> urls, StandardAccumulators standardAccumulators);
}
