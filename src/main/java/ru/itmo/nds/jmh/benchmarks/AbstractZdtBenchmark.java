package ru.itmo.nds.jmh.benchmarks;

import org.openjdk.jmh.annotations.*;
import ru.ifmo.nds.IIndividual;
import ru.ifmo.nds.INonDominationLevel;
import ru.ifmo.nds.dcns.lppsn.LPPSNNonDominationLevel;
import ru.ifmo.nds.dcns.lppsn.LPPSNPopulation;
import ru.ifmo.nds.impl.FitnessOnlyIndividual;
import ru.ifmo.nds.ndt.INode;
import ru.ifmo.nds.ndt.LeafNode;
import ru.ifmo.nds.ndt.NdtManagedPopulation;
import ru.ifmo.nds.ndt.NdtSettings;
import ru.itmo.nds.front_storage.DoublesGeneration;
import ru.itmo.nds.front_storage.Front;
import ru.itmo.nds.front_storage.FrontStorage;
import ru.itmo.nds.jmh.benchmarks.utils.PpsnTestData;
import ru.itmo.nds.reference.treap2015.Double2DIndividual;
import ru.itmo.nds.reference.treap2015.TreapPopulation;
import ru.itmo.nds.util.RankedPopulation;

import java.util.*;
import java.util.stream.Collectors;

import static ru.itmo.nds.util.ComparisonUtils.dominates;

public abstract class AbstractZdtBenchmark extends AbstractDtlzZdtBenchmark {
    private Map<Integer, PpsnTestData> preparedTestData;
    private FrontStorage frontStorage;

    @Override
    protected Map<Integer, PpsnTestData> getPreparedTestData() {
        return preparedTestData;
    }

    @SuppressWarnings("WeakerAccess")
    @Setup(Level.Invocation)
    @Override
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

            final TreapPopulation treapPopulation = new TreapPopulation();
            for (Set<double[]> layer : enluLayers) {
                for (double[] ind: layer) {
                    treapPopulation.addPoint(new Double2DIndividual(ind));
                }
            }

            preparedTestData.put(i, new PpsnTestData(new FitnessOnlyIndividual(nextAddend), rp2, treapPopulation,
                    enluIndividuals, enluLayers, lppsnPopulation, ndtManagedPopulation));
        }
    }

    private int sortUsingTreap2015(int generationId, boolean validate) {
        final PpsnTestData testData = Objects.requireNonNull(getPreparedTestData().get(generationId),
                "no cached test data for generation id " + generationId);

        final TreapPopulation tp = testData.getTreapPopulation();
        tp.addPoint(new Double2DIndividual(testData.getNextAdddend().getObjectives()));

        if (validate)
            tp.validate();

        return tp.size();
    }

    private int sortUsingTreap2015(int generationId) {
        return sortUsingTreap2015(generationId, false);
    }

    @Benchmark
    public int treap2015Gen0() {
        return sortUsingTreap2015(0);
    }

    @Benchmark
    public int treap2015Gen1000() {
        return sortUsingTreap2015(1000);
    }

    @Benchmark
    public int treap2015Gen2000() {
        return sortUsingTreap2015(2000);
    }

    @Benchmark
    public int treap2015Gen3000() {
        return sortUsingTreap2015(3000);
    }

    @Benchmark
    public int treap2015Gen4000() {
        return sortUsingTreap2015(4000);
    }

    @Benchmark
    public int treap2015Gen5000() {
        return sortUsingTreap2015(5000);
    }

    @Benchmark
    public int treap2015Gen6000() {
        return sortUsingTreap2015(6000);
    }

    @Benchmark
    public int treap2015Gen7000() {
        return sortUsingTreap2015(7000);
    }

    @Benchmark
    public int treap2015Gen8000() {
        return sortUsingTreap2015(8000);
    }

    @Benchmark
    public int treap2015Gen9000() {
        return sortUsingTreap2015(9000);
    }
}
