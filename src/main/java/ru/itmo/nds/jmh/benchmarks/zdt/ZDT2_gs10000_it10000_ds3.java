package ru.itmo.nds.jmh.benchmarks.zdt;

import ru.itmo.nds.front_storage.FrontStorage;
import ru.itmo.nds.jmh.benchmarks.AbstractDtlzZdtBenchmark;
import ru.itmo.nds.jmh.benchmarks.AbstractZdtBenchmark;

import java.io.InputStream;
import java.util.Objects;

/**
 * Perform all the same tests as in {@link AbstractDtlzZdtBenchmark}
 * but on another dataset
 */
public class ZDT2_gs10000_it10000_ds3 extends AbstractZdtBenchmark {
    @Override
    protected FrontStorage loadFrontsFromResources() throws Exception {
        final FrontStorage frontStorage = new FrontStorage();
        try (InputStream is = ZDT2_gs10000_it10000_ds3.class
                .getResourceAsStream("zdt2_gen10000_iter10000_dataset3.json")) {
            Objects.requireNonNull(is, "Test data not found");
            frontStorage.deserialize(is);
        }
        return frontStorage;
    }
}