package ru.itmo.nds.jmh.benchmarks.constant;

import org.openjdk.jmh.annotations.*;
import ru.ifmo.nds.IIndividual;
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
import ru.itmo.nds.jmh.benchmarks.AbstractBenchmark;
import ru.itmo.nds.jmh.benchmarks.utils.TestData;
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
    final IncrementalJFB incrementalJFB = new IncrementalJFB();
    final JFB2014 jfb2014 = new JFB2014();

    private final int numberOfGenerations;

    private Map<Integer, TestData> preparedTestData;
    private FrontStorage frontStorage;

    AbstractConstantBenchmark(int numberOfGenerations) {
        this.numberOfGenerations = numberOfGenerations;
    }

    @Override
    protected Map<Integer, TestData> getPreparedTestData() {
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

            preparedTestData.put(i, new TestData(new FitnessOnlyIndividual(nextAddend), rp2, null,
                    enluIndividuals, enluLayers, jfbyPopulation, ndtManagedPopulation));
        }
    }
}
