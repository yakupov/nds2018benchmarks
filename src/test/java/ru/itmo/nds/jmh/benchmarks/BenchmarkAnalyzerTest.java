package ru.itmo.nds.jmh.benchmarks;

import org.junit.Ignore;
import org.junit.Test;
import ru.itmo.nds.jmh.benchmarks.dtlz3d.DTLZ7_dim3_gs10000_it10000_ds2;
import ru.itmo.nds.jmh.benchmarks.dtlzNd.DTLZ2_dim10_gs10000_it10000_ds1;

/**
 * Playground for obtaining debug data from benchmarks on a single run.
 */
public class BenchmarkAnalyzerTest {
    @Test
    @Ignore
    public void obtainStatisticsLevelJfbDtlz7_1() throws Exception {
        final AbstractDtlzZdtBenchmark testClass = new DTLZ7_dim3_gs10000_it10000_ds2();
        testClass.prepareTestData();

        System.out.println(testClass.sortUsingJFBY(0, true));
        System.out.println();
        System.out.println(testClass.sortUsingJFBY(4000, true));
        System.out.println();
        System.out.println(testClass.sortUsingJFBY(5000, true));
        System.out.println();
        System.out.println(testClass.sortUsingJFBY(6000, true));
        System.out.println();
        System.out.println(testClass.sortUsingJFBY(7000, true));
        System.out.println();
        System.out.println(testClass.sortUsingJFBY(8000, true));
        System.out.println();
        System.out.println(testClass.sortUsingJFBY(9000, true));
        System.out.println();
    }

    @Test
    //@Ignore
    public void obtainStatisticsLevelJfbDtlz2_dim10() throws Exception {
        final AbstractDtlzZdtBenchmark testClass = new DTLZ2_dim10_gs10000_it10000_ds1();
        testClass.prepareTestData();

        System.out.println(testClass.sortUsingJFBY(0, true));
        System.out.println();
        System.out.println(testClass.sortUsingJFBY(3000, true));
        System.out.println();
        System.out.println(testClass.sortUsingJFBY(6000, true));
        System.out.println();
        System.out.println(testClass.sortUsingJFBY(9000, true));
        System.out.println();
    }


    @Test
    @Ignore
    public void spinLevelJfbDtlz7_1() throws Exception {
        final AbstractDtlzZdtBenchmark testClass = new DTLZ7_dim3_gs10000_it10000_ds2();
        testClass.prepareTestData();

        for (int i = 0; i < 100000; ++i) {
            if (testClass.sortUsingJFBY(1000, false) == 42) {
                break;
            }
        }
    }

    @Test
    @Ignore
    public void spinLevelJfbDtlz7_2() throws Exception {
        final AbstractDtlzZdtBenchmark testClass = new DTLZ7_dim3_gs10000_it10000_ds2();
        testClass.prepareTestData();

        for (int i = 0; i < 100000; ++i) {
            if (testClass.jfbyGen6000() == 42) {
                break;
            }
        }
    }

    @Test
    @Ignore
    public void spinIncJfbDtlz7_2() throws Exception {
        final AbstractDtlzZdtBenchmark testClass = new DTLZ7_dim3_gs10000_it10000_ds2();
        testClass.prepareTestData();

        for (int i = 0; i < 100000; ++i) {
            if (testClass.incJfbGen6000() == 42) {
                break;
            }
        }
    }
}
