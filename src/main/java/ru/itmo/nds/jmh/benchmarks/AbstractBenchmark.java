package ru.itmo.nds.jmh.benchmarks;

import ru.ifmo.nds.IIndividual;
import ru.ifmo.nds.IManagedPopulation;
import ru.ifmo.nds.dcns.sorter.JFB2014;
import ru.itmo.nds.front_storage.DoublesGeneration;
import ru.itmo.nds.front_storage.FrontStorage;
import ru.itmo.nds.jmh.benchmarks.utils.TestData;
import ru.itmo.nds.reference.ENLUSorter;
import ru.itmo.nds.util.RankedPopulation;

import java.util.*;

public abstract class AbstractBenchmark {
    private final JFB2014 jfb2014 = new JFB2014();

    protected abstract Map<Integer, TestData> getPreparedTestData();

    protected DoublesGeneration getGeneration(FrontStorage frontStorage, int generationId) {
        return frontStorage.getRunConfigurations().iterator().next().getGenerations()
                .stream()
                .filter(gen -> gen.getId() == generationId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Generation " + generationId + " not found in Store"));
    }

    protected int sortOneGeneration(int generationId, JFB2014 sorter) {
        final TestData testData = Objects.requireNonNull(getPreparedTestData().get(generationId),
                "no cached test data for generation id " + generationId);
        final RankedPopulation<IIndividual> res = sorter.performIncrementalNds(testData.getRankedPopulation().getPop(),
                testData.getRankedPopulation().getRanks(), testData.getNextAdddend());
        return res.getRanks().length;
    }

    int sortUsingJFBY(int generationId, boolean debug) {
        final TestData testData = Objects.requireNonNull(getPreparedTestData().get(generationId),
                "no cached test data for generation id " + generationId);
        final IManagedPopulation population = testData.getJfbyPopulation();
        final int rs = population.addIndividual(testData.getNextAdddend());
        return rs;
    }

    int sortUsingNdt(int generationId) {
        final TestData testData = Objects.requireNonNull(getPreparedTestData().get(generationId),
                "no cached test data for generation id " + generationId);
        final IManagedPopulation population = testData.getNdtManagedPopulation();
        return population.addIndividual(testData.getNextAdddend());
    }

    protected int sortUsingJFBY(int generationId) {
        return sortUsingJFBY(generationId, false);
    }

    int sortUsingEnlu(int generationId, boolean validate) {
        final TestData testData = Objects.requireNonNull(getPreparedTestData().get(generationId),
                "no cached test data for generation id " + generationId);

        final ENLUSorter sorter = new ENLUSorter(testData.getEnluIndividuals(), testData.getEnluLayers());
        if (validate) {
            sorter.validate();
        }
        final int rs = sorter.addPoint(testData.getNextAdddend().getObjectives());
        if (validate) {
            sorter.validate();
        }
        return rs;
    }

    protected int sortUsingEnlu(int generationId) {
        return sortUsingEnlu(generationId, false);
    }

    protected int sortFullyUsingJfb(int generationId) {
        final TestData testData = Objects.requireNonNull(getPreparedTestData().get(generationId),
                "no cached test data for generation id " + generationId);

        final IIndividual[] oldPop = testData.getRankedPopulation().getPop();
        final IIndividual[] newPop = Arrays.copyOf(oldPop, oldPop.length + 1);
        newPop[newPop.length - 1] = testData.getNextAdddend();

        final int[] ranks = jfb2014.performNds(newPop);
        return ranks[0];
    }

}
