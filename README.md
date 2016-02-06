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

- Download the code (gitpull) to local folder, e.g. ~/BroadRisky/

- Compile all the .java code files to generate .class files:

> ~/BroadRisky/javac *.java

Note: Part of the configuration process for setting up the Java platform is setting the class path. The class path can be set using either the -classpath option with the javac compiler command and java interpreter command, or by setting the CLASSPATH environment variable. You need to set the class path to point to the directory where the EC2 class is so the compiler and interpreter commands can find it.

- Create a directory with the package name timeprefapp and move all .class files to it:

> ~/BroadRisky/mkdir timeprefapp; mv *.class timeprefapp/.

- Create a manifest.txt file with the following in it:

Main-Class: timeprefapp.runTimePrefApp

Note: make sure there is a newline\carriage return after the first line.

- Create an executable .jar file:

> ~/BroadRisky/jar cvfm runTimePrefApp.jar manifest.txt timeprefapp/*.class

- Run the .JAR file from the command line
    
> ~/BroadRisky/java -jar runTimePrefApp.jar

Once the executable file has been created, the program can be run on different computers with the .jar file. 

# Understanding the code
This section provides a high-level understand of the functions and their dependencies.

- runTimePrefApp.java contains the main function. The max number of rounds is set here with the maxRounds (=50) variable.
- testSubject.java contains:
    variable strPath to specify where results are stored.

    variable numM specifies number of models.
    
    variable numN specifies the number of noise levels.
    
    array noiseProb specifies the noise levels ({ 0.0, 0.05, 0.10, 0.30 }). 
    
    array maxErr specifies maximum allowable errors from model ({ 0, 4, 7, 15 }). 
    
    variable gridSize specifies the gridSize (=11) per parameter.
    
    The function testThisSubject() instantiates the likelihood objects for the models, parameter ranges, and testEC object.
    DesignFunctions() is also instantiated in this function.
    
- Likelihood.java integrates the posterior probabilities over model parameters to generate a likelihood for each model.

- EC2.java calculates the EC^2 objective function given the posterior probabilities over models and parameters and the space of all possible designs. Also includes the function updateHT() to update parameter posterior probabilities based on user responses.

- DesignFunctions.java generates the space of all designs and stores them into 2 multidimensional arrays: Design0 and Design1.

    The variables Xlo and T specifies the monetary amounts and time delay in the Temporal choice experiment.

- utility.java contains specifications of different utility models being compared.
    
