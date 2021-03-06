package do2;

import ilog.cplex.*;
import ilog.opl.*;
import ilog.concert.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class SetCoverLinearProgram {

    private IloOplModel model;
    private int numberOfSets;
    private int numberOfElements;
    private int highestFrequency;
    private HashMap<Integer, Integer> setCosts;
    private HashMap<Integer, Double> setVariables;
    private HashMap<Integer, HashSet<Integer>> setElements;
    private HashMap<Integer, HashSet<Integer>> elementSets;
    private double objValue;

    /** Constructs a SetCoverLinearProgram.
     * IloOplModel parameter is a model for the LP relaxation of the problem. */
    public SetCoverLinearProgram(IloOplModel model) {
        this.model = model;
        this.numberOfSets = model.getElement("n").asInt();
        this.numberOfElements = model.getElement("m").asInt();
        this.setCosts = new HashMap<Integer, Integer>(this.numberOfSets);
        this.setVariables = new HashMap<Integer, Double>(this.numberOfSets);
        this.setElements = new HashMap<Integer, HashSet<Integer>>(this.numberOfSets);
        this.elementSets = new HashMap<Integer, HashSet<Integer>>(this.numberOfElements);
    }

    /** Sets up information, might throw. */
    public void setup() throws IloException {

        // get the obj value of the cplex lp relaxation
        IloCplex cplex = this.model.getCplex();
        if (cplex.getCplexStatus() == IloCplex.CplexStatus.Unknown) {
            cplex.solve();
        }
        this.objValue = cplex.getObjValue();

        IloIntMap costs = this.model.getElement("c").asIntMap();
        IloTupleSet covers = this.model.getElement("covers").asTupleSet();
        IloOplElement xvar = this.model.getElement("x");

        // hack: we don't care about x variables if we are solving by ilp
        // will throw exception if we call asNumMap() on an int map
        IloNumMap xvariables = null;
        if (xvar.getElementType() == IloOplElementType.Type.MAP_NUM) {
            xvariables = xvar.asNumMap();
        }

        // get the highest frequency and fill set-element mappings
        Iterator it = covers.iterator();
        int f = 0, currentElement = 0, currentSet, fTemp = 0;
        while (it.hasNext()) {
            IloTuple cover = (IloTuple)it.next();

            // update most frequent element
            if (currentElement != cover.getIntValue(0)) {
                this.highestFrequency = Math.max(this.highestFrequency, fTemp);
                fTemp = 0;
                currentElement = cover.getIntValue(0);
            }
            fTemp++;

            // update sets mapped to this element
            currentSet = cover.getIntValue(1);
            if (!this.elementSets.containsKey(currentElement)) {
                this.elementSets.put(currentElement, new HashSet<Integer>());
            }
            this.elementSets.get(currentElement).add(currentSet);

            // update elements mapped to this set + set cost + set variables
            if (!this.setElements.containsKey(currentSet)) {
                this.setElements.put(currentSet, new HashSet<Integer>());
                this.setCosts.put(currentSet, costs.get(currentSet));
                if (xvariables != null) {
                    this.setVariables.put(currentSet, xvariables.get(currentSet));
                }
            }
            this.setElements.get(currentSet).add(currentElement);
        }
        this.highestFrequency = Math.max(this.highestFrequency, fTemp);
    }

    /** Gets number of sets (n). */
    public int getNumberOfSets() { return this.numberOfSets; }

    /** Gets number of elements (m). */
    public int getNumberOfElements() { return this.numberOfElements; }

    /** Gets frequency of most frequent element. */
    public int getHighestFrequency() { return this.highestFrequency; }

    /** Gets map of set index to the cost of the set */
    public HashMap<Integer, Integer> getSetCosts() { return this.setCosts; }

    /** Gets map of set index to the decision variable corresponding to the set */
    public HashMap<Integer, Double> getSetVariables() { return this.setVariables; }

    /** Gets map of set index to the indices of elements it covers */
    public HashMap<Integer, HashSet<Integer>> getSetElements() { return this.setElements; }

    /** Gets map of element index to the indices of sets that cover the element */
    public HashMap<Integer, HashSet<Integer>> getElementSets() { return this.elementSets; }

    /** Gets the objective value solution to the relaxation of the linear program */
    public double getObjValue() { return this.objValue; }
}
