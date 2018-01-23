package ru.itmo.nds.jmh.benchmarks.dtlz3d;

import ru.itmo.nds.front_storage.FrontStorage;
import ru.itmo.nds.jmh.benchmarks.AbstractDtlzZdtBenchmark;

import java.io.InputStream;
import java.util.Objects;

/**
 * Perform all the same tests as in {@link AbstractDtlzZdtBenchmark}
 * but on another dataset
 */
public class DTLZ3_dim3_gs10000_it10000_ds2 extends AbstractDtlzZdtBenchmark {
    @Override
    protected FrontStorage loadFrontsFromResources() throws Exception {
        final FrontStorage frontStorage = new FrontStorage();
        try (InputStream is = DTLZ3_dim3_gs10000_it10000_ds2.class
                .getResourceAsStream("dtlz3_dim3_gen10000_iter10000_dataset2.json")) {
            Objects.requireNonNull(is, "Test data not found");
            frontStorage.deserialize(is);
        }
        return frontStorage;
    }
}
