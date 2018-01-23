package ru.itmo.nds.jmh.benchmarks.utils;

import ru.ifmo.nds.IIndividual;
import ru.ifmo.nds.dcns.jfby.JFBYPopulation;
import ru.ifmo.nds.ndt.NdtManagedPopulation;
import ru.itmo.nds.reference.treap2015.TreapPopulation;
import ru.itmo.nds.util.RankedPopulation;

import java.util.List;
import java.util.Set;

public class TestData {
    private final IIndividual nextAdddend;
    private final RankedPopulation<IIndividual> rankedPopulation;
    private final TreapPopulation treapPopulation;
    private final Set<double[]> enluIndividuals;
    private final List<Set<double[]>> enluLayers;
    private final JFBYPopulation jfbyPopulation;
    private final NdtManagedPopulation ndtManagedPopulation;

    /**
     * Test data
     *
     * @param nextAdddend          For all
     * @param rankedPopulation     For all JFB-based except JFBY
     * @param treapPopulation      For Treap2015
     * @param enluIndividuals      For ENLU
     * @param enluLayers           For ENLU
     * @param jfbyPopulation       For JFBY
     * @param ndtManagedPopulation For NDT
     */
    public TestData(IIndividual nextAdddend,
                    RankedPopulation<IIndividual> rankedPopulation,
                    TreapPopulation treapPopulation,
                    Set<double[]> enluIndividuals,
                    List<Set<double[]>> enluLayers,
                    JFBYPopulation jfbyPopulation,
                    NdtManagedPopulation ndtManagedPopulation) {
        this.nextAdddend = nextAdddend;
        this.rankedPopulation = rankedPopulation;
        this.treapPopulation = treapPopulation;
        this.enluIndividuals = enluIndividuals;
        this.enluLayers = enluLayers;
        this.jfbyPopulation = jfbyPopulation;
        this.ndtManagedPopulation = ndtManagedPopulation;
    }

    public IIndividual getNextAdddend() {
        return nextAdddend;
    }

    public RankedPopulation<IIndividual> getRankedPopulation() {
        return rankedPopulation;
    }

    public TreapPopulation getTreapPopulation() {
        return treapPopulation;
    }

    public Set<double[]> getEnluIndividuals() {
        return enluIndividuals;
    }

    public List<Set<double[]>> getEnluLayers() {
        return enluLayers;
    }

    public JFBYPopulation getJfbyPopulation() {
        return jfbyPopulation;
    }

    public NdtManagedPopulation getNdtManagedPopulation() {
        return ndtManagedPopulation;
    }
}
