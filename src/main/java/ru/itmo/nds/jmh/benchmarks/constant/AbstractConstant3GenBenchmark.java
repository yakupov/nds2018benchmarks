package ru.itmo.nds.jmh.benchmarks.constant;

import org.openjdk.jmh.annotations.Benchmark;

public abstract class AbstractConstant3GenBenchmark extends AbstractConstantBenchmark {
    AbstractConstant3GenBenchmark() {
        super(3);
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

    @Benchmark
    public int enluTestDataset1() {
        return sortUsingEnlu(1);
    }

    @Benchmark
    public int incJfbFastSweepDataset1() {
        return sortOneGeneration(1, incrementalJFB);
    }

    @Benchmark
    public int incJfbDataset1() {
        return sortOneGeneration(1, jfb2014);
    }

    @Benchmark
    public int jfbyDataset1() {
        return sortUsingJFBY(1);
    }

    @Benchmark
    public int oldJfbDataset1() {
        return sortFullyUsingJfb(1);
    }
    
    @Benchmark
    public int enluTestDataset2() {
        return sortUsingEnlu(2);
    }

    @Benchmark
    public int incJfbFastSweepDataset2() {
        return sortOneGeneration(2, incrementalJFB);
    }

    @Benchmark
    public int incJfbDataset2() {
        return sortOneGeneration(2, jfb2014);
    }

    @Benchmark
    public int jfbyDataset2() {
        return sortUsingJFBY(2);
    }

    @Benchmark
    public int oldJfbDataset2() {
        return sortFullyUsingJfb(2);
    }
}
