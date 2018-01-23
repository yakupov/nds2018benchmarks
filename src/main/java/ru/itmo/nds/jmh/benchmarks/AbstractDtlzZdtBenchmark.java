package ru.itmo.nds.jmh.benchmarks;

import org.openjdk.jmh.annotations.*;
import ru.ifmo.nds.IIndividual;
import ru.ifmo.nds.INonDominationLevel;
import ru.ifmo.nds.dcns.lppsn.LPPSNNonDominationLevel;
import ru.ifmo.nds.dcns.lppsn.LPPSNPopulation;
import ru.ifmo.nds.dcns.sorter.IncrementalPPSN;
import ru.ifmo.nds.dcns.sorter.PPSN2014;
import ru.ifmo.nds.impl.FitnessOnlyIndividual;
import ru.ifmo.nds.ndt.INode;
import ru.ifmo.nds.ndt.LeafNode;
import ru.ifmo.nds.ndt.NdtManagedPopulation;
import ru.ifmo.nds.ndt.NdtSettings;
import ru.itmo.nds.front_storage.DoublesGeneration;
import ru.itmo.nds.front_storage.Front;
import ru.itmo.nds.front_storage.FrontStorage;
import ru.itmo.nds.jmh.benchmarks.utils.PpsnTestData;
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
    final IncrementalPPSN incrementalPPSN = new IncrementalPPSN();
    private final PPSN2014 ppsn2014 = new PPSN2014();

    private Map<Integer, PpsnTestData> preparedTestData;
    private FrontStorage frontStorage;

    @Override
    protected Map<Integer, PpsnTestData> getPreparedTestData() {
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

            final LPPSNPopulation lppsnPopulation = new LPPSNPopulation();
            generation.getFronts().stream()
                    .sorted(Comparator.comparingInt(Front::getId))
                    .map(f -> {
                        final INonDominationLevel level = new LPPSNNonDominationLevel(incrementalPPSN);
                        level.getMembers().addAll(f.getFitnesses().stream().map(FitnessOnlyIndividual::new).collect(Collectors.toList()));

                        (level.getMembers()).sort((o1, o2) -> {
                            for (int objIndex = 0; objIndex < o1.getObjectives().length; ++objIndex) {
                                if (o1.getObjectives()[objIndex] < o2.getObjectives()[objIndex])
                                    return -1;
                                else if (o1.getObjectives()[objIndex] > o2.getObjectives()[objIndex])
                                    return 1;
                            }
                            return 0;
                        });

                        return level;
                    })
                    .forEach(level -> lppsnPopulation.getLevels().add(level));

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

            preparedTestData.put(i, new PpsnTestData(new FitnessOnlyIndividual(nextAddend), rp2, null,
                    enluIndividuals, enluLayers, lppsnPopulation, ndtManagedPopulation));
        }
    }

    @Benchmark
    public int enluGen0() {
        return sortUsingEnlu(0);
    }

    @Benchmark
    public int incPpsnFastSweepGen0() {
        return sortOneGeneration(0, incrementalPPSN);
    }

    @Benchmark
    public int incPpsnGen0() {
        return sortOneGeneration(0, ppsn2014);
    }

    @Benchmark
    public int levelPpsnGen0() {
        return sortUsingLevelPPSN(0);
    }

    @Benchmark
    public int ndtGen0() {
        return sortUsingNdt(0);
    }

    @Benchmark
    public int oldPpsnGen0() {
        return sortFullyUsingPpsn(0);
    }

    @Benchmark
    public int enluGen1000() {
        return sortUsingEnlu(1000);
    }

    @Benchmark
    public int incPpsnFastSweepGen1000() {
        return sortOneGeneration(1000, incrementalPPSN);
    }

    @Benchmark
    public int incPpsnGen1000() {
        return sortOneGeneration(1000, ppsn2014);
    }

    @Benchmark
    public int levelPpsnGen1000() {
        return sortUsingLevelPPSN(1000);
    }

    @Benchmark
    public int oldPpsnGen1000() {
        return sortFullyUsingPpsn(1000);
    }

    @Benchmark
    public int enluGen2000() {
        return sortUsingEnlu(2000);
    }

    @Benchmark
    public int incPpsnFastSweepGen2000() {
        return sortOneGeneration(2000, incrementalPPSN);
    }

    @Benchmark
    public int incPpsnGen2000() {
        return sortOneGeneration(2000, ppsn2014);
    }

    @Benchmark
    public int levelPpsnGen2000() {
        return sortUsingLevelPPSN(2000);
    }

    @Benchmark
    public int oldPpsnGen2000() {
        return sortFullyUsingPpsn(2000);
    }

    @Benchmark
    public int enluGen3000() {
        return sortUsingEnlu(3000);
    }

    @Benchmark
    public int incPpsnFastSweepGen3000() {
        return sortOneGeneration(3000, incrementalPPSN);
    }

    @Benchmark
    public int incPpsnGen3000() {
        return sortOneGeneration(3000, ppsn2014);
    }

    @Benchmark
    public int levelPpsnGen3000() {
        return sortUsingLevelPPSN(3000);
    }

    @Benchmark
    public int oldPpsnGen3000() {
        return sortFullyUsingPpsn(3000);
    }

    @Benchmark
    public int enluGen4000() {
        return sortUsingEnlu(4000);
    }

    @Benchmark
    public int incPpsnFastSweepGen4000() {
        return sortOneGeneration(4000, incrementalPPSN);
    }

    @Benchmark
    public int incPpsnGen4000() {
        return sortOneGeneration(4000, ppsn2014);
    }

    @Benchmark
    public int levelPpsnGen4000() {
        return sortUsingLevelPPSN(4000);
    }

    @Benchmark
    public int oldPpsnGen4000() {
        return sortFullyUsingPpsn(4000);
    }

    @Benchmark
    public int enluGen5000() {
        return sortUsingEnlu(5000);
    }

    @Benchmark
    public int incPpsnFastSweepGen5000() {
        return sortOneGeneration(5000, incrementalPPSN);
    }

    @Benchmark
    public int incPpsnGen5000() {
        return sortOneGeneration(5000, ppsn2014);
    }

    @Benchmark
    public int levelPpsnGen5000() {
        return sortUsingLevelPPSN(5000);
    }

    @Benchmark
    public int oldPpsnGen5000() {
        return sortFullyUsingPpsn(5000);
    }

    @Benchmark
    public int enluGen6000() {
        return sortUsingEnlu(6000);
    }

    @Benchmark
    public int incPpsnFastSweepGen6000() {
        return sortOneGeneration(6000, incrementalPPSN);
    }

    @Benchmark
    public int incPpsnGen6000() {
        return sortOneGeneration(6000, ppsn2014);
    }

    @Benchmark
    public int levelPpsnGen6000() {
        return sortUsingLevelPPSN(6000);
    }

    @Benchmark
    public int oldPpsnGen6000() {
        return sortFullyUsingPpsn(6000);
    }

    @Benchmark
    public int enluGen7000() {
        return sortUsingEnlu(7000);
    }

    @Benchmark
    public int incPpsnFastSweepGen7000() {
        return sortOneGeneration(7000, incrementalPPSN);
    }

    @Benchmark
    public int incPpsnGen7000() {
        return sortOneGeneration(7000, ppsn2014);
    }

    @Benchmark
    public int levelPpsnGen7000() {
        return sortUsingLevelPPSN(7000);
    }

    @Benchmark
    public int oldPpsnGen7000() {
        return sortFullyUsingPpsn(7000);
    }

    @Benchmark
    public int enluGen8000() {
        return sortUsingEnlu(8000);
    }

    @Benchmark
    public int incPpsnFastSweepGen8000() {
        return sortOneGeneration(8000, incrementalPPSN);
    }

    @Benchmark
    public int incPpsnGen8000() {
        return sortOneGeneration(8000, ppsn2014);
    }

    @Benchmark
    public int levelPpsnGen8000() {
        return sortUsingLevelPPSN(8000);
    }

    @Benchmark
    public int oldPpsnGen8000() {
        return sortFullyUsingPpsn(8000);
    }

    @Benchmark
    public int enluGen9000() {
        return sortUsingEnlu(9000);
    }

    @Benchmark
    public int incPpsnFastSweepGen9000() {
        return sortOneGeneration(9000, incrementalPPSN);
    }

    @Benchmark
    public int incPpsnGen9000() {
        return sortOneGeneration(9000, ppsn2014);
    }

    @Benchmark
    public int levelPpsnGen9000() {
        return sortUsingLevelPPSN(9000);
    }

    @Benchmark
    public int oldPpsnGen9000() {
        return sortFullyUsingPpsn(9000);
    }

}
