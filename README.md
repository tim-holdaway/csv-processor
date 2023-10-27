# CrowdStrike Detections Platform Cloud team homework assignment
Author: Tim Holdaway

# Design
The CsvProcessorApplication is a java application that downloads and processes files and reports the aggregate results for median and mode across all the data downloaded.

### Input
The program reads urls from `stdin` until EOF. In practice, this allows us to send it urls by piping the output of another command or redirecting a file of urls to the invocation of the program.

### Processing flow
For each url specified, the program downloads the file contents into a temp file, collects intermediate results for the median and mean, and then aggregates the intermediate results into final results for the entire logical dataset:
* For mean, the count and sum are collected for each file, and are each summed over the collection of files.
* For median, the histogram of ages is collected for each file. The histograms are summed over the collection of files
* This distributed median algorithm relies on the fact that there are a limited number of discrete ages we expect to encounter.
* **WARNING** This distributed mean histogram assumes that no humans will live beyond 200 years old, for the purposes of counting ages
* Once the combined age histogram is produced for all files, the median is found by traversing the histogram by age order until the midpoint of the total count has been reached.
	* If the median age corresponds to a nonexistent value falling between two existing records, the record with the larger age is returned.
* The application is structured such that additional sharded aggregation tasks could be added in the future. These could implement the `Accumulator` interface, and be added to the set of `Accumulator`s that are run per-file and aggregated.

### Error handling
We may encounter errors in the steps of downloading or processing a file. This may happen due to a file not existing, errors during download, or a CSV that is somehow malformed. Errors encountered in a download or processing step are encapsulated in a `Result` class, which either contains the result of a step (e.g a downloaded file or an accumulated intermediate result for a file), or a description of an error that occurred (and subsequent steps can see that the `Result` is an error and forego any further processing).
Errors are

### Concurrency
* By default, the CsvProcessorApplication runs the download and file processing steps in `CompletableFuture`s that run in the java `ForkJoinPool.commonPool`. The size of this thread pool is system dependent. On my system, it contained between 7 and 9 executors. The size of the executing thread pool is printed in the program output, along with the wall clock time for the entire execution.
* For comparison sake, you can pass the `--single-threaded` option to run the downloads and processing sequentially. The wall clock time is printed in this output as well
* The intermediate-results-and-aggregate pattern used in this program allows each unit of work to occur in isolation and avoids any synchronization issues. It also would scale up well to a distributed system, with many worker nodes picking up tasks before their intermediate results are collected and aggregated (this is basically map-reduce).

### Dependencies
* Junit and AssertJ for tests
* Jackson Dataformat CSV for csv parsing (I used this library because csv parsing has a lot of edge cases that established libraries have solved, so that I could focus on the collect-and-aggregate pattern and the multi-threaded runner)

# Execution

Requires Java 19 (may also work with Java 17, as it doesn't use many dependencies or language features.)

Program can be compiled with gradle:
```
./gradlew build
```

Program can be invoked with gradle, in this example redirecting stdin from a file of urls of test data hosted on my personal site timholdaway.com:
```
./gradlew run < exampleUrls
```

Program can be invoked single-threaded with gradle:
```
./gradlew run --args='--single-threaded' < exampleUrls

```

Tests can be run with gradle:
```
./gradlew test
```

The linter can be run with gradle:
```
./gradlew spotlessApply
```

# Deliverables
1. What assumptions did you make in your design? Why?
	* I assumed that no human will live to age 200. By assuming that there are discrete and bounded ages, we're able to fit the median calculation nicely into a collect-and-aggregate pattern by bucketing counts by age, and don't have to worry about combining the entire dataset together and sorting it to find the median.
	* I assumed that the entirety of the files would fit into temp files on disk. Downloading to temp files may not have been strictly necessary, but allowed decoupling download errors from processing errors more cleanly.
	* I assumed that the number of file handles we can have open is greater than the number of threads we might have running tasks downloading or processing files. Download and processing tasks should clean up their file handles, but if the jvm somehow ended up configured with a very low allowed number of open files, this could fail.
2. How would you change your program if it had to process many files where each file was over 10M records?
	* I believe that the file-processing step is reading files line-by-line and not trying to read the entirety of a file into memory (depends on library implementation of the csv row iterator I'm using). I'd want to confirm that this doesn't load up the entire file into memory if we were processing a lot of giant files.
	* We might be able to do some clever pre-splitting of CSV files after we download them, to allow us to process them more parallelly in a larger number of shards. This would probably be more important if we were doing more intensive calculations in the processing stage, and especially if we were running in a setting with more cpus or workers available to work on tasks.
3. How would you change your program if it had to process data from more than 20K URLs?
	* This might be a good case for vastly upping the parallelism by extending the task-running code to run in a distributed environment. This would allow us to scale up the parallelism vastly more than multithreading on a single machine. If we were going to extend this functionality, the download steps could download the files to some shared file system, or the tasks could have some concept of data locality so that processing for a given file happened on the worker node that had downloaded that file already.
4. How would you test your code for production use at scale?
	* While there are basic unit tests and an integration test in this repository, these simple tests come nowhere near the sort of tests we would want to perform for production use at scale. To test at scale, we could create an estimate of production data volumes. We could produce data of similar scale to expected production data (or maybe an order of magnitude larger, as a safety buffer for when demand grows in the future), and run the program against the large test dataset in a test environment that is configured and specced similarly to our production environment. We might generate the test data based on real data if there are idiosyncrasies in real data that we care about, or we might generate random data for the load test if we determine that's sufficient. We would want to instrument the application with monitoring and logging, and observe errors and resource metrics during the test execution.
	* We might also want to keep an eye on the data sources we're downloading from. We don't want to DDOS our data sources.
	* We might also be able to soft-launch the code in production -- that is, run it separately from other applications, and turn it on to consume data and produce results, and monitor its performance and output against real data before we start using its output in downstream services.


# Future improvements wish list
If I had more time to work on this, I'd improve the following:
* replace `System.out.println` calls with logging framework, and centralize the output logic.
* Build a more robust set of command line options using an option parsing library. Options I'd like to support include specifying parallelism and error handling behaviors.
* Restructure code to use a dependency injection framework, to support better unit tests. I've sort of used this pattern in places in the code, but rigorously applying it everywhere would make it easier to test components of the sytem,.
* Additional calculations beyond mean and median, and a more extensible way of specifying sets of calculations to run and aggregate.
* Stretch goal: build out distributed support for massively parallel processing and aggregation.