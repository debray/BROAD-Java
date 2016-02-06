# BROAD-Java : BROAD Dynamic Experimental Design with Java
========================================================
Java code for Bayesian Rapid Optimal Adaptive Design (BROAD).

# Background:
BROAD is a data-driven adaptive experimental design methodology to test theories (and parameters). BROAD Designs are changed rapidly and adaptively based on responses to experiments. Designs are scored on the basis of the Equivalence-Class Edge-Cutting (EC2) criterion. The EC2 criterion is proved to be Adaptive Submodular in the following paper:

"Bayesian Rapid Optimal Adaptive Design (BROAD): Method and application distinguishing models of risky choice". D. Ray et al.
Link: http://people.hss.caltech.edu/~mshum/reading/Ray2012.pdf

Adaptive Submodularity provides optimality guarantees, i.e. the cost function is within a factor of the (intractable) Bayes-optimal solution. Furthermore, adaptive submodularity leads to lazy evaluations which speeds up (by eliminating unnecessary) computation by an order of magnitude in certain cases.

This instantiation of the code compares Time Preference theories. 
In one instance, the code runs ground truth analyses where the true type and parameters are sampled from a uniform distribution of theories and parameters. The EC2 algorithm is run for 50 rounds, and at the end of the run the posterior probabilities are compared to see whether the MAP classification corresponds to truth. This is run for multiple samples to obtain accuracy curves.

In another instance, the code requests user input after offering choices between two lotteries. This is repeated for N rounds. Data files are generated for each user.

# How to Run the Code:
- Ensure that JAVA is installed on your computer: https://www.java.com/en/download/
- Compile the code to generate .JAR files
- Run the .JAR file from the command line.

# Understanding the code
This section provides a high-level understand of the functions and their dependencies.

