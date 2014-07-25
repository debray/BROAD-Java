/*
 *  Author: Debajyoti Ray, October 2011.
 *  Copyright: Caltech
 */
package timeprefapp;

/**
 * Contains the Main function to interact with subject
 * Takes as input the subject ID and max num of Rounds
 * Output: subject responses and model posteriors.
 */
public class testSubject {
    
    int id;             // unique subject ID
    
    int maxRounds;      // maximum number of rounds to run
    
    testSubject(int ID, int maxR) {
        
        id = ID;

        maxRounds = maxR;
    }
    
    public int testThisSubject() {
        
        // Set up file input and output
        boolean dirExists = (new java.io.File("Results")).exists();
        if (!dirExists) { (new java.io.File("Results")).mkdir(); }
        
        String strPath = "Results/Subject-" + id + ".txt";
        //java.io.FileWriter file = null;
        java.io.BufferedWriter out = null;
        try {
            //file = new java.io.FileWriter(strPath);
            //out = new java.io.BufferedWriter(file);
            out = new java.io.BufferedWriter(new java.io.FileWriter(strPath));
            //out.write("Subject " + id);
            out.write("Subject "+id);
            out.newLine();
        }
        catch (Exception e) { System.err.println("Error Opening File"); }
        
        // Initialize Design object
        // Creates all possible designs in design space
        DesignFunctions TP_Designs = new DesignFunctions();
        TP_Designs.makeAllDesigns();
        
        // specify number of models
        int numM = 5;
        
        // specify noise model:
        // Requires noise level and max number of Errors before hypothesis is eliminated
        int numN = 4;
        double [] noiseProb = { 0.0, 0.05, 0.10, 0.30 };
        int [] maxErr = { 0, 4, 7, 15 };
        
        // specify grid
        int gridSize = 11;
        
        // initialize likelihood objects and grid
        // For each dimension, specify range: min and max values of parameter
        double [] Exp_minR = {0.0, 0}, Exp_maxR = {0.02, 0};
        double [] Hyp_minR = {0.0, 0}, Hyp_maxR = {0.2, 0};
        double [] QH_minR = {0.0, 0.5}, QH_maxR = {0.02, 1.0};
        double [] Fix_minR = {0.0, 0.0}, Fix_maxR = {0.02, 20.0};
        double [] GH_minR = {0.02, 0.5}, GH_maxR = {0.22, 1.0};
        
        // Initialize Likelihood objects:
        // specify Likelihood model id, and parameter specifications.
        Likelihood LikExp = new Likelihood(0, gridSize, Exp_minR, Exp_maxR);
        Likelihood LikHyp = new Likelihood(1, gridSize, Hyp_minR, Hyp_maxR);
        Likelihood LikQH = new Likelihood(2, gridSize, QH_minR, QH_maxR);
        Likelihood LikFix = new Likelihood(3, gridSize, Fix_minR, Fix_maxR);
        Likelihood LikGH = new Likelihood(4, gridSize, GH_minR, GH_maxR);
        
        // initially all (H, Theta) are valid
        // numHT is the number of hypotheses for each model.
        // for e.g. exponential has 1 parameter, so number of hypotheses
        // is equal to grid size
        int [] numHT = new int[numM];
        numHT[0] = LikExp.gridSize;
        numHT[1] = LikHyp.gridSize;
        numHT[2] = LikQH.gridSize * LikQH.gridSize;
        numHT[3] = LikFix.gridSize * LikFix.gridSize;
        numHT[4] = LikGH.gridSize * LikGH.gridSize;
        
        // numH is all the hypotheses (across all models)
        int numH = 0;
        for (int m=0; m<numM; m++) { numH += numHT[m]; }
        
        // initialize EC2:
        // no of models, no of noise models, no of hypotheses, no of designs
        EC2 testEC = new EC2(numM, numN, numH, TP_Designs.num_Designs);
        
        // Specify EC2 object parameters:
        // All designs in lottery 0 and lottery 1, max error rates and noise prob.
        testEC.AllDesigns0 = TP_Designs.Design0;
        testEC.AllDesigns1 = TP_Designs.Design1;
        
        testEC.maxErr = maxErr;
        testEC.noiseProb = noiseProb;
        
        // IMPORTANT: Prior probability for each model
        // here we assume each model is equally likely
        for (int m=0; m<numM; m++) { testEC.probModel[m] = 1.0 / numM; }
        
        // initialize parameters and create grid
        LikExp.Params[0] = LikExp.MinR[0];
        LikHyp.Params[0] = LikHyp.MinR[0];
        for (int i=0; i<2; i++) {
            LikQH.Params[i] = LikQH.MinR[i];
            LikFix.Params[i] = LikFix.MinR[i];
            LikGH.Params[i] = LikGH.MinR[i];
        }
        
        // initialize weights WtHT
        // IMPORTANT - Prior: Each hypotheses within a model has equal probability
        // WtHT is the weight (evidence) for a specific hypothesis for each test
        // Makes sense to output or store WtHT after every round for diagnosis.
        int ind;
        for (int n=0; n<testEC.numN; n++) {
            ind = 0;
            for (int m=0; m<testEC.numM; m++) {
                for (int h=0; h<numHT[m]; h++) {
                    testEC.WtHT[ind][n] = 1.0/(numM*numHT[m]); ind++;
                }
            }
        }
        
        // counts for number of errors
        for (int h=0; h<numH; h++) { testEC.ErrHT[h] = 0; }
        
        //try { file.close(); } catch (Exception e) { System.err.println("Error 1"); }
        
        System.err.println("Starting Experiment");
        
        int [] UsedD = new int[maxRounds];
        
        // Loop over the best designs
        for (int numRound=0; numRound < maxRounds; numRound++) {
            
            try {out.write(numRound + "\n");} catch(Exception e) {}
            
            // testEC.designEC2 returns the best test (design) given likelihood objects
            UsedD[numRound] = testEC.designEC2(LikExp, LikHyp, LikQH, LikFix, 
                            LikGH, UsedD, numRound);
            
            double [] Lottery0, Lottery1;
            
            Lottery0 = testEC.best_Design[0];
            Lottery1 = testEC.best_Design[1];
            
            // Following is a rudimentary UI for testing
            // This should be replaced by a UI of choice - the only parts that are 
            // important are recording the subject responses and passing them to the program
            // Clear the screen (PITA in JAVA)
            try {
                if ((System.getProperty( "os.name")).startsWith(" Window ")) {
                    Runtime.getRuntime().exec("cls"); }
                else { Runtime.getRuntime().exec("clear"); }
            }
            catch(Exception e) {
                for (int i=0; i<1000; i++) { System.err.println(); }
            }
            
            
            System.err.println("\n\n CHOOSE: \n\n");
            System.err.println(" Option 1: \n\n");
            System.err.println(" Amount:    $" + (int) Lottery0[0] + " after " + 
                    (int) Lottery0[1] + " Days.\n\n");
            System.err.println("    OR\n\n");
            System.err.println(" Option 2: \n\n");
            System.err.println(" Amount:    $" + (int) Lottery1[0] + " after " + 
                    (int) Lottery1[1] + " Days.\n\n");
            
            // write choices to file
            try {
                out.write(Lottery0[0] + "," + Lottery0[1] + "\n");
                out.write(Lottery1[0] + "," + Lottery1[1] + "\n");
            }
            catch (Exception e) { System.err.println("Error writing to file"); }
            
            // assign design to objects
            LikExp.Lottery0=testEC.best_Design[0]; LikExp.Lottery1=testEC.best_Design[1];
            LikHyp.Lottery0=testEC.best_Design[0]; LikHyp.Lottery1=testEC.best_Design[1];
            LikQH.Lottery0=testEC.best_Design[0]; LikQH.Lottery1=testEC.best_Design[1];
            LikFix.Lottery0=testEC.best_Design[0]; LikFix.Lottery1=testEC.best_Design[1];
            LikGH.Lottery0=testEC.best_Design[0]; LikGH.Lottery1=testEC.best_Design[1];
            
            // Set up response timer
            long StartTime = System.currentTimeMillis();
                    
            // Record observation from keyboard input
            int ynow;
            java.util.Scanner kbIn = new java.util.Scanner(System.in);
            System.err.println("Enter choice: 1 or 2\n\n");
            ynow = Integer.parseInt(kbIn.next());
            ynow -= 1;
            
            long RT = System.currentTimeMillis() - StartTime; 
            
            // input must be valid
            boolean invalid = true;
            if (ynow==0 || ynow==1) { invalid=false; }
            while (invalid) {
                System.err.println("Invalid Input!");
                System.err.println("Enter choice: 1 or 2 \n\n");
                ynow = Integer.parseInt(kbIn.next());
                ynow -= 1;
                if (ynow==0 || ynow==1) { invalid=false; }
            }
            
            try{
                out.write(ynow + "\n");
            } catch(Exception e) {}
            
            // update which (H, Theta) are valid after observation
            testEC.updateHT(LikExp, LikHyp, LikQH, LikFix, LikGH, 
                            UsedD[numRound], ynow);
            
            // The key variable of interest is the WtHT matrix,
            // which is a public variable of the testEC object.
            // Output the testEC.WtHT file to disk by calling a public function
            String outputWt = "";
            
            testEC.outputWeights(outputWt);
            // calculate model posteriors from valid (H, Theta)
            // The model posteriors are just computed by "compressing", i.e.
            // summing over all hypothesis of WtHT variable.
            testEC.updateModelPost(numHT);
            
            // File Output
            //System.err.println("Model Posteriors: " + testEC.probModel[0] +
            //        ", " + testEC.probModel[1] + ", " + testEC.probModel[2] + 
            //        ", " + testEC.probModel[3] + ", " + testEC.probModel[4]);
            try {
                out.write(testEC.probModel[0] + "," + testEC.probModel[1] + 
                        "," + testEC.probModel[2] + "," + testEC.probModel[3] + 
                        "," + testEC.probModel[4]);
                out.newLine();
            }
            catch (Exception e) { System.err.println("File Output Error"); }
            
            // output response time to file
            try {out.write(RT + "\n"); } catch(Exception e) { }
            
            System.err.println(" REST \n\n\n");
        }
        
        try {out.close();} catch(Exception e) {System.err.println("Error Closing");}
        
        return 1;
    }
}
