package ru.itmo.nds.jmh.benchmarks;

import org.openjdk.jmh.annotations.*;
import ru.ifmo.nds.IIndividual;
import ru.ifmo.nds.INonDominationLevel;
import ru.ifmo.nds.dcns.jfby.JFBYNonDominationLevel;
import ru.ifmo.nds.dcns.jfby.JFBYPopulation;
import ru.ifmo.nds.dcns.sorter.IncrementalJFB;
import ru.ifmo.nds.dcns.sorter.JFB2014;
import ru.ifmo.nds.impl.FitnessOnlyIndividual;
import ru.ifmo.nds.ndt.INode;
import ru.ifmo.nds.ndt.LeafNode;
import ru.ifmo.nds.ndt.NdtManagedPopulation;
import ru.ifmo.nds.ndt.NdtSettings;
import ru.itmo.nds.front_storage.DoublesGeneration;
import ru.itmo.nds.front_storage.Front;
import ru.itmo.nds.front_storage.FrontStorage;
import ru.itmo.nds.jmh.benchmarks.utils.TestData;
import ru.itmo.nds.util.RankedPopulation;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static ru.itmo.nds.util.ComparisonUtils.dominates;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 12)
@Measurement(iterations = 4)
@Fork(value = 2)
public abstract class AbstractDtlzZdtBenchmark extends AbstractBenchmark {
    final IncrementalJFB incrementalJFB = new IncrementalJFB();
    private final JFB2014 jfb2014 = new JFB2014();

    private Map<Integer, TestData> preparedTestData;
    private FrontStorage frontStorage;

    @Override
    protected Map<Integer, TestData> getPreparedTestData() {
        return preparedTestData;
    }

    protected abstract FrontStorage loadFrontsFromResources() throws Exception;

    @SuppressWarnings("WeakerAccess")
    @Setup(Level.Invocation)
    public void prepareTestData() throws Exception {
        if (frontStorage == null)
            frontStorage = loadFrontsFromResources();

        preparedTestData = new HashMap<>();

        for (int i = 0; i <= 10000; i += 1000) {
            final DoublesGeneration generation = getGeneration(frontStorage, i);
            final double[] nextAddend = generation.getNextAddend();
            final RankedPopulation<double[]> rp = generation.getLexSortedRankedPop();
            final IIndividual[] individuals = new IIndividual[rp.getPop().length];
            for (int j = 0; j < individuals.length; ++j) {
                individuals[j] = new FitnessOnlyIndividual(rp.getPop()[j]);
            }
            final RankedPopulation<IIndividual> rp2 = new RankedPopulation<>(individuals, rp.getRanks());

            final JFBYPopulation jfbyPopulation = new JFBYPopulation();
            generation.getFronts().stream()
                    .sorted(Comparator.comparingInt(Front::getId))
                    .map(f -> {
                        final List<IIndividual> members = new ArrayList<>();
                        members.addAll(f.getFitnesses().stream().map(FitnessOnlyIndividual::new).collect(Collectors.toList()));
                        members.sort((o1, o2) -> {
                            for (int objIndex = 0; objIndex < o1.getObjectives().length; ++objIndex) {
                                if (o1.getObjectives()[objIndex] < o2.getObjectives()[objIndex])
                                    return -1;
                                else if (o1.getObjectives()[objIndex] > o2.getObjectives()[objIndex])
                                    return 1;
                            }
                            return 0;
                        });
                        return new JFBYNonDominationLevel(incrementalJFB, members);
                    })
                    .forEach(level -> jfbyPopulation.getLevels().add(level));

            final NdtSettings ndtSettings = new NdtSettings(10, nextAddend.length);
            final Comparator<IIndividual> comparator = (o1, o2) -> dominates(o1.getObjectives(), o2.getObjectives(), ndtSettings.getDimensionsCount());
            final NdtManagedPopulation ndtManagedPopulation = new NdtManagedPopulation(comparator, ndtSettings);
            generation.getFronts().stream()
                    .sorted(Comparator.comparingInt(Front::getId))
                    .forEach(f -> {
                        INode node = new LeafNode(new ArrayList<>(), comparator, ndtSettings, 0);
                        for (double[] doubles : f.getFitnesses()) {
                            node = node.insert(new FitnessOnlyIndividual(doubles));
                        }
                        ndtManagedPopulation.getLevels().add(node);
                    });

            final Set<double[]> enluIndividuals = new HashSet<>();
            final List<Set<double[]>> enluLayers = generation.getFronts().stream()
                    .sorted(Comparator.comparingInt(Front::getId))
                    .map(f -> {
                        final Set<double[]> enluLayer = new HashSet<>();
                        enluLayer.addAll(f.getFitnesses());
                        enluIndividuals.addAll(f.getFitnesses());
                        return enluLayer;
                    })
                    .collect(Collectors.toList());

            preparedTestData.put(i, new TestData(new FitnessOnlyIndividual(nextAddend), rp2, null,
                    enluIndividuals, enluLayers, jfbyPopulation, ndtManagedPopulation));
        }
    }

    @Benchmark
    public int enluGen0() {
        return sortUsingEnlu(0);
    }

    @Benchmark
    public int incJfbFastSweepGen0() {
        return sortOneGeneration(0, incrementalJFB);
    }

    @Benchmark
    public int incJfbGen0() {
        return sortOneGeneration(0, jfb2014);
    }

    @Benchmark
    public int jfbyGen0() {
        return sortUsingJFBY(0);
    }

    @Benchmark
    public int ndtGen0() {
        return sortUsingNdt(0);
    }

    @Benchmark
    public int oldJfbGen0() {
        return sortFullyUsingJfb(0);
    }

    @Benchmark
    public int enluGen1000() {
        return sortUsingEnlu(1000);
    }

    @Benchmark
    public int incJfbFastSweepGen1000() {
        return sortOneGeneration(1000, incrementalJFB);
    }

    @Benchmark
    public int incJfbGen1000() {
        return sortOneGeneration(1000, jfb2014);
    }

    @Benchmark
    public int jfbyGen1000() {
        return sortUsingJFBY(1000);
    }

    @Benchmark
    public int oldJfbGen1000() {
        return sortFullyUsingJfb(1000);
    }

    @Benchmark
    public int enluGen2000() {
        return sortUsingEnlu(2000);
    }

    @Benchmark
    public int incJfbFastSweepGen2000() {
        return sortOneGeneration(2000, incrementalJFB);
    }

    @Benchmark
    public int incJfbGen2000() {
        return sortOneGeneration(2000, jfb2014);
    }

    @Benchmark
    public int jfbyGen2000() {
        return sortUsingJFBY(2000);
    }

    @Benchmark
    public int oldJfbGen2000() {
        return sortFullyUsingJfb(2000);
    }

    @Benchmark
    public int enluGen3000() {
        return sortUsingEnlu(3000);
    }

    @Benchmark
    public int incJfbFastSweepGen3000() {
        return sortOneGeneration(3000, incrementalJFB);
    }

    @Benchmark
    public int incJfbGen3000() {
        return sortOneGeneration(3000, jfb2014);
    }

    @Benchmark
    public int jfbyGen3000() {
        return sortUsingJFBY(3000);
    }

    @Benchmark
    public int oldJfbGen3000() {
        return sortFullyUsingJfb(3000);
    }

    @Benchmark
    public int enluGen4000() {
        return sortUsingEnlu(4000);
    }

    @Benchmark
    public int incJfbFastSweepGen4000() {
        return sortOneGeneration(4000, incrementalJFB);
    }

    @Benchmark
    public int incJfbGen4000() {
        return sortOneGeneration(4000, jfb2014);
    }

    @Benchmark
    public int jfbyGen4000() {
        return sortUsingJFBY(4000);
    }

    @Benchmark
    public int oldJfbGen4000() {
        return sortFullyUsingJfb(4000);
    }

    @Benchmark
    public int enluGen5000() {
        return sortUsingEnlu(5000);
    }

    @Benchmark
    public int incJfbFastSweepGen5000() {
        return sortOneGeneration(5000, incrementalJFB);
    }

    @Benchmark
    public int incJfbGen5000() {
        return sortOneGeneration(5000, jfb2014);
    }

    @Benchmark
    public int jfbyGen5000() {
        return sortUsingJFBY(5000);
    }

    @Benchmark
    public int oldJfbGen5000() {
        return sortFullyUsingJfb(5000);
    }

    @Benchmark
    public int enluGen6000() {
        return sortUsingEnlu(6000);
    }

    @Benchmark
    public int incJfbFastSweepGen6000() {
        return sortOneGeneration(6000, incrementalJFB);
    }

    @Benchmark
    public int incJfbGen6000() {
        return sortOneGeneration(6000, jfb2014);
    }

    @Benchmark
    public int jfbyGen6000() {
        return sortUsingJFBY(6000);
    }

    @Benchmark
    public int oldJfbGen6000() {
        return sortFullyUsingJfb(6000);
    }

    @Benchmark
    public int enluGen7000() {
        return sortUsingEnlu(7000);
    }

    @Benchmark
    public int incJfbFastSweepGen7000() {
        return sortOneGeneration(7000, incrementalJFB);
    }

    @Benchmark
    public int incJfbGen7000() {
        return sortOneGeneration(7000, jfb2014);
    }

    @Benchmark
    public int jfbyGen7000() {
        return sortUsingJFBY(7000);
    }

    @Benchmark
    public int oldJfbGen7000() {
        return sortFullyUsingJfb(7000);
    }

    @Benchmark
    public int enluGen8000() {
        return sortUsingEnlu(8000);
    }

    @Benchmark
    public int incJfbFastSweepGen8000() {
        return sortOneGeneration(8000, incrementalJFB);
    }

    @Benchmark
    public int incJfbGen8000() {
        return sortOneGeneration(8000, jfb2014);
    }

    @Benchmark
    public int jfbyGen8000() {
        return sortUsingJFBY(8000);
    }

    @Benchmark
    public int oldJfbGen8000() {
        return sortFullyUsingJfb(8000);
    }

    @Benchmark
    public int enluGen9000() {
        return sortUsingEnlu(9000);
    }

    @Benchmark
    public int incJfbFastSweepGen9000() {
        return sortOneGeneration(9000, incrementalJFB);
    }

    @Benchmark
    public int incJfbGen9000() {
        return sortOneGeneration(9000, jfb2014);
    }

    @Benchmark
    public int jfbyGen9000() {
        return sortUsingJFBY(9000);
    }

    @Benchmark
    public int oldJfbGen9000() {
        return sortFullyUsingJfb(9000);
    }

}
