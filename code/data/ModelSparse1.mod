/*********************************************
 * OPL 12.6.0.0 Model
 * Author: Brian
 * Creation Date: 12/08/2014 at 15.20.28
 *********************************************/

// Data declarations

int m = ...; // size of universe - rows
int n = ...; // number of sets - columns

tuple cover {
  int c1;
  int s1;
}

range universe = 1..m;
range sets = 1..n;
{cover} covers = ...;

int A[cover in covers] = 1;
  
int c[sets] = ...; // Cost vector

// Decision variables

dvar boolean x[sets];

// Objective function

minimize sum(j in sets) c[j]*x[j];

// Constraints

 subject to {
 forall (i in universe)
   sum(<i,s1> in covers) A[<i,s1>]*x[s1] >= 1;
 }