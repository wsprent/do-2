package do2;

import ilog.cplex.*;
import ilog.opl.*;
import ilog.concert.*;

import java.util.Iterator;
import java.util.Random;

public class Main {

    public static void rounding(IloOplFactory fac, IloOplModel mod, Boolean randomized) throws IloException {
        IloCplex cplex = mod.getCplex();

        // Constraints
        IloIntMap c = mod.getElement("c").asIntMap();

        // Number of Sets
        int n = mod.getElement("n").asInt();
        // Number of Covers
        int m = mod.getElement("m").asInt();


        // Objective Values
        IloNumMap x = mod.getElement("x").asNumMap();

        IloTupleSet covers = mod.getElement("covers").asTupleSet();
        Iterator it = covers.iterator();
        int f = 0;  
        int cur = 0;
        int fTemp = 0;
        IloTuple cover;
        
        // find frequency of most frequent element;
        for (int i = 0; i < covers.getSize(); i++) {
            cover = covers.makeTuple(i);
            //covers.setAt(indices, cover);
            if (cur != cover.getIntValue(0)) {
                if (f < fTemp) {
                    f = fTemp;
                }
                fTemp = 0;
                cur = cover.getIntValue(0);
            }
            fTemp++;
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
            for (int i = 0; i < n; i++) {
                if (rand.nextDouble() < x.get(i + 1)) rounded_x[i] = 1;
                else rounded_x[i] = 0;
            }
            // TODO VERIFY AND REPEAT IF NECCESSARY
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

