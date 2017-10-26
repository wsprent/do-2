package do2;

import ilog.cplex.*;
import ilog.opl.*;
import ilog.concert.*;

import java.util.Iterator;
import java.util.Random;
import java.util.HashMap;
import java.util.HashSet;

public class Main {

    public static void rounding(IloOplFactory fac, IloOplModel mod, Boolean randomized) throws IloException {
        IloCplex cplex = mod.getCplex();

        // Constraints
        IloIntMap c = mod.getElement("c").asIntMap();

        // Number of Sets
        int n = mod.getElement("n").asInt();
        // Size of the Universe
        int m = mod.getElement("m").asInt();


        // Objective Values
        IloNumMap x = mod.getElement("x").asNumMap();

        IloTupleSet covers = mod.getElement("covers").asTupleSet();
        Iterator it = covers.iterator();
        int f = 0;
        int currentElement = 0;
        int currentSet;
        int fTemp = 0;
        IloTuple cover;
        HashMap<Integer, HashSet> coveredBy = new HashMap<Integer, HashSet>(m);

        // find frequency of most frequent element;
        // and set indices for when covers start in the list
        // we assume the cover tuples are sorted by set
        for (int i = 0; i < covers.getSize(); i++) {
            // (elem, set)
            cover = covers.makeTuple(i);

            if (currentElement != cover.getIntValue(0)) {
                if (f < fTemp) {
                    f = fTemp;
                }

                fTemp = 0;
                currentElement = cover.getIntValue(0);
            }
            fTemp++;

            if (randomized) {
                currentSet = cover.getIntValue(1);

                if (!coveredBy.containsKey(currentSet)) {
                    coveredBy.put(currentSet, new HashSet<Integer>());
                }
                coveredBy.get(currentSet).add(currentElement);
            }
        }
        if (f < fTemp) f = fTemp;

        double limit = 1.0 / f;

        // Do Rounding

        int[] rounded_x = new int[n];

        if (!randomized) {
            for (int i = 0; i < n; i++) {
                if (x.get(i + 1) > limit) rounded_x[i] = 1;
                else rounded_x[i] = 0;
            }

        } else {
            Random rand = new Random();
            HashSet covered = new HashSet<Integer>(m);
            int j, start;
            while (covered.size() < m) {
                for (int i = 0; i < n; i++) {
                    // If we have already picked x_i continue
                    if (rounded_x[i] == 1) continue;


                    if (rand.nextDouble() < x.get(i + 1)) {
                        // select x and mark more of the universe as covered
                        rounded_x[i] = 1;

                        covered.addAll(coveredBy.get(i+1));

                    }
                    else rounded_x[i] = 0;

                }

            }
        }

        double totalCost = 0.0;

        for (int i = 0; i < n; i++) {
            totalCost += rounded_x[i] * c.get(i+1);
        }
        System.out.printf("LP Relaxation Cost is: %.2f\n", mod.getCplex().getObjValue());
        System.out.printf("Rounded Cost Is: %.2f\n", totalCost);
    }

    public static void rounding (IloOplFactory fac, IloOplModel mod) throws IloException {
        rounding(fac, mod, false);
    }
    public static void main(String[] args){

        String modelPath = args[0];
        String dataPath = args[1];

        IloOplFactory oplF = new IloOplFactory();

        IloOplModelSource modelSource = oplF.createOplModelSource(modelPath);
        IloOplDataSource dataSource = oplF.createOplDataSource(dataPath);
        IloOplErrorHandler errHandler = oplF.createOplErrorHandler();
        IloOplModelDefinition def = oplF.createOplModelDefinition(modelSource, errHandler);

        long start = System.nanoTime();
        try {
            IloCplex cplex = new IloCplex();
            IloOplModel mod = oplF.createOplModel(def, cplex);
            mod.addDataSource(dataSource);
            mod.generate();
            cplex.solve();

            if (args[2].contains("-round")) {
                rounding(oplF, mod);
            } else if (args[2].contains("-rand")) {
                rounding(oplF, mod, true);
            } else if (args[2].contains("-cplex")) {
                System.out.println("Solved ILP by CPLEX");
                System.out.printf("Cost Is: %.2f\n", cplex.getObjValue());
            }
        } catch (IloException e) {
            System.out.println("Something bad happened when loading model.");
            e.printStackTrace(System.out);
            System.exit(1);
        }

        long end = System.nanoTime();
        System.out.printf("Took %.2fms\n",(end-start)/1000000.0);


    }
}
