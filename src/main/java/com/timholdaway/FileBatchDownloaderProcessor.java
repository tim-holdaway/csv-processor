/* (C)2023 Tim Holdaway */
package com.timholdaway;

import com.timholdaway.accumulators.AccumulatorTypes;
import java.util.List;

public interface FileBatchDownloaderProcessor {
    void downloadAndProcess(List<String> urls, AccumulatorTypes accumulatorTypes);
}
