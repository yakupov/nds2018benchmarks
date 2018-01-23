package ru.itmo.nds.jmh.benchmarks.utils;

import ru.ifmo.nds.IIndividual;
import ru.ifmo.nds.dcns.lppsn.LPPSNPopulation;
import ru.ifmo.nds.ndt.NdtManagedPopulation;
import ru.itmo.nds.reference.treap2015.TreapPopulation;
import ru.itmo.nds.util.RankedPopulation;

import java.util.List;
import java.util.Set;

public class PpsnTestData {
    private final IIndividual nextAdddend;
    private final RankedPopulation<IIndividual> rankedPopulation;
    private final TreapPopulation treapPopulation;
    private final Set<double[]> enluIndividuals;
    private final List<Set<double[]>> enluLayers;
    private final LPPSNPopulation lppsnPopulation;
    private final NdtManagedPopulation ndtManagedPopulation;

    /**
     * Test data
     *
     * @param nextAdddend          For all
     * @param rankedPopulation     For all PPSN-based except LevelPPSN
     * @param treapPopulation      For Treap2015
     * @param enluIndividuals      For ENLU
     * @param enluLayers           For ENLU
     * @param lppsnPopulation      For Level PPSN
     * @param ndtManagedPopulation For NDT
     */
    public PpsnTestData(IIndividual nextAdddend,
                        RankedPopulation<IIndividual> rankedPopulation,
                        TreapPopulation treapPopulation,
                        Set<double[]> enluIndividuals,
                        List<Set<double[]>> enluLayers,
                        LPPSNPopulation lppsnPopulation,
                        NdtManagedPopulation ndtManagedPopulation) {
        this.nextAdddend = nextAdddend;
        this.rankedPopulation = rankedPopulation;
        this.treapPopulation = treapPopulation;
        this.enluIndividuals = enluIndividuals;
        this.enluLayers = enluLayers;
        this.lppsnPopulation = lppsnPopulation;
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

    public LPPSNPopulation getLppsnPopulation() {
        return lppsnPopulation;
    }

    public NdtManagedPopulation getNdtManagedPopulation() {
        return ndtManagedPopulation;
    }
}
