package ru.itmo.nds.jmh.benchmarks.constant;

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
import ru.itmo.nds.jmh.benchmarks.AbstractBenchmark;
import ru.itmo.nds.jmh.benchmarks.utils.PpsnTestData;
import ru.itmo.nds.util.RankedPopulation;

import java.io.IOException;
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
public abstract class AbstractConstantBenchmark extends AbstractBenchmark {
    final IncrementalPPSN incrementalPPSN = new IncrementalPPSN();
    final PPSN2014 ppsn2014 = new PPSN2014();

    private final int numberOfGenerations;

    private Map<Integer, PpsnTestData> preparedTestData;
    private FrontStorage frontStorage;

    AbstractConstantBenchmark(int numberOfGenerations) {
        this.numberOfGenerations = numberOfGenerations;
    }

    @Override
    protected Map<Integer, PpsnTestData> getPreparedTestData() {
        return preparedTestData;
    }

    protected abstract FrontStorage loadFrontsFromResources() throws IOException;

    @SuppressWarnings("WeakerAccess")
    @Setup(Level.Invocation)
    public void prepareTestData() throws Exception {
        if (frontStorage == null) {
            frontStorage = loadFrontsFromResources();
        }

        preparedTestData = new HashMap<>();

        for (int i = 0; i < numberOfGenerations; i++) {
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

            final NdtSettings ndtSettings = new NdtSettings(5, nextAddend.length);
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
}