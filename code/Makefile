SRC=src/
LIB=libs/
TARGET=bin/
ILPMODEL=data/ModelSparse1.mod
LPMODEL=data/LPRelax.mod
ILPDATA?=data/scpc3.dat  # run with ILPDATA=<data> make run-<type> to use diff data
# directory which contains cplex library
CPLEX=/opt/ibm/ILOG/CPLEX_Studio1271/cplex/bin/x86-64_linux/
OPL=/opt/ibm/ILOG/CPLEX_Studio1271/opl/bin/x86-64_linux/

default: compile

compile:
	mkdir -p $(TARGET)
	javac -cp $(SRC):$(LIB)* $(SRC)do2/Main.java -d $(TARGET)

run-rounding:
	LD_LIBRARY_PATH=$(OPL) java -cp $(TARGET):$(LIB)* -Djava.library.path=$(CPLEX):$(OPL)	do2.Main $(LPMODEL) $(ILPDATA) -round

run-rounding-rand:
	LD_LIBRARY_PATH=$(OPL) java -cp $(TARGET):$(LIB)* -Djava.library.path=$(CPLEX):$(OPL)	do2.Main $(LPMODEL) $(ILPDATA) -rand

run-cplex:
	LD_LIBRARY_PATH=$(OPL) java -cp $(TARGET):$(LIB)* -Djava.library.path=$(CPLEX):$(OPL)	do2.Main $(ILPMODEL) $(ILPDATA) -cplex

run-primal-dual:
	LD_LIBRARY_PATH=$(OPL) java -cp $(TARGET):$(LIB)* -Djava.library.path=$(CPLEX):$(OPL)	do2.Main $(ILPMODEL) $(ILPDATA) -primal-dual-schema

clean:
	rm -rf $(TARGET)

.PHONY: clean compile run
