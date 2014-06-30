/*
 *  Author: Debajyoti Ray, October 2011.
 *  Copyright: Caltech
 *
 *  This is the main class that implements the 
 *  Equivalent Class Edge Cutting (EC^2) objective function
 */
package timeprefapp;

/**
 * Definition of Equivalence Class Edge Cutting functions
 */
public class EC2 {
 
    int num_Designs;
    double [][] AllDesigns0;
    double [][] AllDesigns1;
    
    int numM;       // number of models being tested
    
    int numN;               // number of noise levels
    double [] noiseProb;    // prob of observation error
    int [] maxErr;          // max no of errors to tolerate
    
    double [] design_Score; // array of design scores
    
    double [][] best_Design;  // best Design
    
    double [] probModel;    // posterior prob of each Model
    
    double [][] WtHT;       // weights for (H, Theta)
    int [] ErrHT;         // number of errors for (H, Theta)
    
    // constructor
    EC2(int m, int n, int hyp, int numD) {
        
        numM = m;
        numN = n;
        
        noiseProb = new double[numN];
        maxErr = new int[numN];
        
        // No of hypothesis = 2 * 11 + 3 * 11^2 = 385
        WtHT = new double[hyp][numN];
        ErrHT = new int[hyp];
        
        probModel = new double[numM];
        
        num_Designs = numD;
        design_Score = new double[numD];
        
        best_Design = new double[2][2];
    }
    
    public int designEC2(Likelihood likExp, Likelihood likHyp,
            Likelihood likQH, Likelihood likFix, Likelihood likGH,
            int [] UsedD, int numR) {
        
        double Score = 0.0, minScore = 100.0;
        int bestD = 0;
        
        int [] numHT = new int[numM];
        numHT[0] = likExp.gridSize;
        numHT[1] = likHyp.gridSize;
        numHT[2] = likQH.gridSize * likQH.gridSize;
        numHT[3] = likFix.gridSize * likFix.gridSize;
        numHT[4] = likGH.gridSize * likGH.gridSize;
        
        // get all responses from the models into a vector
        int [] ResponseHT;
        
        // assign scores to designs
        for (int d=0; d<num_Designs; d++) {
            
            // create ResponseHT - table of MLE responses:
            // what does each hypotheses choose for each design (test)
            ResponseHT = tableHT(likExp, likHyp, likQH, likFix, likGH, d, numHT);
            
            // score of this particular design
            Score = scoreEC2(ResponseHT, numHT);
            //System.err.println("Design: " + d + ", Score: " + Score);
            
            design_Score[d] = Score;
            
            if (Score < minScore) {
                
                // has this design been used already?
                boolean isUsed = false;
                for (int r=0; r<numR; r++) {
                    if (d==UsedD[r]) { isUsed = true; }
                }
                
                if (!isUsed) {
                    minScore = Score;
                    best_Design[0] = AllDesigns0[d];
                    best_Design[1] = AllDesigns1[d];
                    bestD = d;
                }
            }
        }
        
        return bestD;
    }
    
    // What (binary) choice does each hypothesis with each model select
    // for a given design (test) d:
    public int[] tableHT (Likelihood likExp, Likelihood likHyp,
            Likelihood likQH, Likelihood likFix, Likelihood likGH,
            int d, int [] numHT) {
        
        int numH = 0;
        for (int h=0; h<numM; h++) { numH += numHT[h]; }
        int [] ResponseHT = new int[numH];
        
        Likelihood tempExp; Likelihood tempHyp; Likelihood tempQH;
        Likelihood tempFix; Likelihood tempGH;
        tempExp = likExp; tempHyp = likHyp; tempQH = likQH;
        tempFix = likFix; tempGH = likGH;
        
        tempExp.Lottery0 = AllDesigns0[d]; tempExp.Lottery1 = AllDesigns1[d];
        tempHyp.Lottery0 = AllDesigns0[d]; tempHyp.Lottery1 = AllDesigns1[d];
        tempQH.Lottery0 = AllDesigns0[d]; tempQH.Lottery1 = AllDesigns1[d];
        tempFix.Lottery0 = AllDesigns0[d]; tempFix.Lottery1 = AllDesigns1[d];
        tempGH.Lottery0 = AllDesigns0[d]; tempGH.Lottery1 = AllDesigns1[d];
        
        // initialize attributes of Likelihood objects
        tempExp.Response = new int[numHT[0]];
        tempHyp.Response = new int[numHT[1]];
        tempQH.Response = new int[numHT[2]];
        tempFix.Response = new int[numHT[3]];
        tempGH.Response = new int[numHT[4]];
        
        tempExp.MLEresponse(); 
        tempHyp.MLEresponse();
        tempQH.MLEresponse();
        tempFix.MLEresponse();
        tempGH.MLEresponse();
        
        // Aggregate responses for all models into a vector (array)
        int ind = 0;
        for (int th=0; th<numHT[0]; th++) {
            ResponseHT[ind]=tempExp.Response[th]; ind++; }
        for (int th=0; th<numHT[1]; th++) {
            ResponseHT[ind]=tempHyp.Response[th]; ind++; }
        for (int th=0; th<numHT[2]; th++) {
            ResponseHT[ind]=tempQH.Response[th]; ind++; }
        for (int th=0; th<numHT[3]; th++) {
            ResponseHT[ind]=tempFix.Response[th]; ind++; }
        for (int th=0; th<numHT[4]; th++) {
            ResponseHT[ind]=tempGH.Response[th]; ind++; }
        
        return ResponseHT;
    }
    
    // Compute score of design d from samples
    // The main computations for the EC^2 objective function
    public double scoreEC2 (int [] ResponseHT, int [] numHT) {
        
        int numH = 0;
        for (int h=0; h<numM; h++) { numH += numHT[h]; }
        
        // get aggregate weights
        double [] AggWtHT = new double[numH];
        for (int h=0; h<numH; h++) { AggWtHT[h] = 0.0; }
        
        // get aggregate likelihood by marginalizing over all noise levels
        for (int h=0; h<numH; h++) {
            for (int n=0; n<numN; n++) {
                
                AggWtHT[h] += WtHT[h][n]; 
            }
        }
        
        // Calculate conditional, P(x_d = 0 | x_A)
        double MCond0 = 0.0, MCondAll = 0.0;
        double PCond0 = 0.0;
        // Conditional count for each hypothesis
        for (int h=0; h<numH; h++) {
            
            MCondAll += AggWtHT[h];
            if (ResponseHT[h]==0) { MCond0 += AggWtHT[h]; }
        }
        PCond0 = MCond0 / MCondAll;
        
        // Calculate joint, P(x_A, x_d=0)
        double MJ0 = 0.0, MJ1 = 0.0, MAll = 0.0;
        double PJoint0 = 0.0, PJoint1 = 0.0;
        // joint count
        for (int h=0; h<numH; h++) {
            
            MAll += AggWtHT[h];
            
            if (ResponseHT[h]==0) { MJ0 += AggWtHT[h]; }
            else if (ResponseHT[h]==1) { MJ1 += AggWtHT[h]; }
        }
        PJoint0 = MJ0 / MAll;
        PJoint1 = MJ1 / MAll;
        
        // Calculate joint with hypothesis, P(H, x_A, x_d=0)
        double [] MH0 = new double[numM]; double [] MH1 = new double[numM];
        double [] PH0 = new double[numM]; double [] PH1 = new double[numM];
        
        for (int m=0; m<numM; m++) { 
            MH0[m]=0.0; MH1[m]=0.0; PH0[m]=0.0; PH1[m]=0.0; }
        
        // add mass for each hypothesis
        int ind = 0;
        for (int m=0; m<numM; m++) {
            for (int n=0; n<numHT[m]; n++) {
                
                if (ResponseHT[ind]==0) { MH0[m] += AggWtHT[ind]; }
                else if (ResponseHT[ind]==1) { MH1[m] += AggWtHT[ind]; }
                
                ind++;
            }
            PH0[m] = MH0[m] / MAll;
            PH1[m] = MH1[m] / MAll;
        }
        
        // sum square of P(H, x_A, x_d = 0)
        double PH0all2 = 0.0, PH1all2 = 0.0;
        for (int m=0; m<numM; m++) {
            PH0all2 += PH0[m]*PH0[m];
            PH1all2 += PH1[m]*PH1[m];
        }
        
        double Score = 0.0;
        Score = PCond0*(PJoint0*PJoint0 - PH0all2) + 
                (1-PCond0)*(PJoint1*PJoint1 - PH1all2);
        
        return Score;
    }
    
    // update valid (H, Theta) after observation
    public void updateHT (Likelihood likExp, Likelihood likHyp,
            Likelihood likQH, Likelihood likFix, Likelihood likGH,
            int d, int Xobs) {
        
        // allocate temporary variables
        Likelihood tempExp; Likelihood tempHyp; Likelihood tempQH;
        Likelihood tempFix; Likelihood tempGH;
        tempExp = likExp; tempHyp = likHyp; tempQH = likQH;
        tempFix = likFix; tempGH = likGH;
        
        // find X_nA consistent with this design d
        tempExp.Lottery0 = AllDesigns0[d]; tempExp.Lottery1 = AllDesigns1[d];
        tempHyp.Lottery0 = AllDesigns0[d]; tempHyp.Lottery1 = AllDesigns1[d];
        tempQH.Lottery0 = AllDesigns0[d]; tempQH.Lottery1 = AllDesigns1[d];
        tempFix.Lottery0 = AllDesigns0[d]; tempFix.Lottery1 = AllDesigns1[d];
        tempGH.Lottery0 = AllDesigns0[d]; tempGH.Lottery1 = AllDesigns1[d];
        
        // number of parameters for each model
        int [] numHT = new int[numM];
        numHT[0] = likExp.gridSize;
        numHT[1] = likHyp.gridSize;
        numHT[2] = likQH.gridSize * likQH.gridSize;
        numHT[3] = likFix.gridSize * likFix.gridSize;
        numHT[4] = likGH.gridSize * likGH.gridSize;
        
        int numH = 0;
        for (int h=0; h<numM; h++) { numH += numHT[h]; }
        int [] X_HT = new int[numH];
        
        // what is the MLE response of (H, Theta) to design d?
        tempExp.MLEresponse(); 
        tempHyp.MLEresponse();
        tempQH.MLEresponse();
        tempFix.MLEresponse();
        tempGH.MLEresponse();
        
        int ind = 0;
        for (int th=0; th<numHT[0]; th++) {
            X_HT[ind]=tempExp.Response[th]; ind++; }
        for (int th=0; th<numHT[1]; th++) {
            X_HT[ind]=tempHyp.Response[th]; ind++; }
        for (int th=0; th<numHT[2]; th++) {
            X_HT[ind]=tempQH.Response[th]; ind++; }
        for (int th=0; th<numHT[3]; th++) {
            X_HT[ind]=tempFix.Response[th]; ind++; }
        for (int th=0; th<numHT[4]; th++) {
            X_HT[ind]=tempGH.Response[th]; ind++; }
        
        // Reweight (H, Theta) based on observation
        // If noise prob = 0 then each hypothesis is eliminated 
        // if not consistent with test outcome. But for noisy responses,
        // the weight WtHT is multiplied by the noise probablity
        for (int h=0; h<numH; h++) {
            
            if (X_HT[h] != Xobs) {
                
                ErrHT[h] = ErrHT[h] + 1;
                
                for (int n=0; n<numN; n++) {
                    WtHT[h][n] = WtHT[h][n] * noiseProb[n];
                }
            }
            else {
                for (int n=0; n<numN; n++) {
                    WtHT[h][n] = WtHT[h][n] * (1-noiseProb[n]);
                }
            }
            
            // Eliminate (H, Theta) if more than maxErr errors made
            for (int n=0; n<numN; n++) {
                if (ErrHT[h] > maxErr[n]) { WtHT[h][n] = 0.0; }
            }
        }
        
    }
    
    // Update the model posterior
    // Note: in addition to the Model posterior, one should output the
    // hypotheses-test weights (WtHT) after each test for diagnosis.
    public void updateModelPost(int [] numHT) {
        
        double [] MassH = new double[numM];
        double TotalM = 0.0;
        int ind = 0;
        
        for (int m=0; m<numM; m++) { MassH[m]=0.0; }
        
        for (int m=0; m<numM; m++) {
            for (int n=0; n<numHT[m]; n++) {
                for (int p=0; p<numN; p++) {
                    
                    // The probability mass over a hypothesis is the sum
                    // of the weights over all the noise models.
                    MassH[m] += WtHT[ind][p];
                }
                ind++;
            }
        }
        
        for (int m=0; m<numM; m++) { TotalM += MassH[m]; }
        
        for (int m=0; m<numM; m++) {
            
            probModel[m] = MassH[m] / TotalM;
        }
    }
    
    // choose a design at random
    public int randomDesign(int [] UsedD, int numR) {
        
        boolean notdone = true;
        int low = 0, high = num_Designs-1;
        int range = high-low;
        int d = 0;
        
        while (notdone) {
            
            d = low + (int)(range*Math.random());
            
            best_Design[0] = AllDesigns0[d];
            best_Design[1] = AllDesigns1[d];
            
            // check if it has already been used
            boolean isUsed = false;
            for (int r=0; r<numR; r++) {
                
                if (d==UsedD[r]) { isUsed = true; }
            }
            if (isUsed) { notdone = true; } else { notdone = false; }
        }
        
        return d;
    }
}
