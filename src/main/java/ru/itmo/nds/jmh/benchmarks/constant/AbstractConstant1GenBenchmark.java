package ru.itmo.nds.jmh.benchmarks.constant;

import org.openjdk.jmh.annotations.Benchmark;

public abstract class AbstractConstant1GenBenchmark extends AbstractConstantBenchmark {
    AbstractConstant1GenBenchmark() {
        super(1);
    }

    @Benchmark
    public int enluTestDataset0() {
        return sortUsingEnlu(0);
    }

    @Benchmark
    public int incJfbFastSweepDataset0() {
        return sortOneGeneration(0, incrementalJFB);
    }

    @Benchmark
    public int incJfbDataset0() {
        return sortOneGeneration(0, jfb2014);
    }

    @Benchmark
    public int jfbyDataset0() {
        return sortUsingJFBY(0);
    }

    @Benchmark
    public int oldJfbDataset0() {
        return sortFullyUsingJfb(0);
    }
}
