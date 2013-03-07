/*
 *  Author: Debajyoti Ray, October 2011.
 *  Copyright: Caltech
 */
package timeprefapp;

/**
 * Definition for Likelihood class
 */
public class Likelihood {
    
    int model;      // Likelihood model
    
    int gridSize;
    
    double [] Params;
    
    double [] Lottery0;
    double [] Lottery1;
    
    double [] MinR;
    
    double [] MaxR;
    
    int [] Response;
    
    // constructor
    Likelihood(int m, int gS, double [] minR, double [] maxR) {
        
        model = m;
        gridSize = gS;
        
        Lottery0 = new double[2];
        Lottery1 = new double[2];
        
        MinR = minR;
        MaxR = maxR;
        
        Params = new double[2];
    }
    
    
    
    double probObs() {
        
        double U1 = 0.0, U2 = 0.0, probObs;
        
        double Lambda = 0.1;
   
        // Exponential model
        if (model == 0) { 
            Utility thisUtil = new Utility(1);
            thisUtil.Params = Params;
            U1 = thisUtil.calcUtilityExp(Lottery0);
            U2 = thisUtil.calcUtilityExp(Lottery1);
        }
        
        // Hyperbolic model
        if (model == 1) { 
            Utility thisUtil = new Utility(1);
            thisUtil.Params = Params;
            U1 = thisUtil.calcUtilityHyp(Lottery0);
            U2 = thisUtil.calcUtilityHyp(Lottery1);
        }
        
        // Quasi-Hyperbolic model
        if (model == 2) { 
            Utility thisUtil = new Utility(2);
            thisUtil.Params = Params;
            U1 = thisUtil.calcUtilityQH(Lottery0);
            U2 = thisUtil.calcUtilityQH(Lottery1);
        }
        
        // Fixed-cost model
        if (model == 3) { 
            Utility thisUtil = new Utility(2);
            thisUtil.Params = Params;
            U1 = thisUtil.calcUtilityFixed(Lottery0);
            U2 = thisUtil.calcUtilityFixed(Lottery1);
        }
        
        // Generalized-Hyperbolic model
        if (model == 4) { 
            Utility thisUtil = new Utility(2);
            thisUtil.Params = Params;
            U1 = thisUtil.calcUtilityGH(Lottery0);
            U2 = thisUtil.calcUtilityGH(Lottery1);
        }
        
        probObs = 1.0 / (1.0 + Math.exp((-U1 + U2) / Lambda));
        
        return probObs;
    }
    
    int MLEresponse() {
        
        double inc0 = 0.0, inc1 = 0.0;
        int count = 0;
        
        // integrate over 1 parameter
        if (model < 2) {
            for (int i=0; i<gridSize; i++) {
                
                inc0 = (MaxR[0]-MinR[0]) / (gridSize-1);
                Params[0] = MinR[0] + i*inc0;
                
                if (probObs() < 0.5) { Response[count] = 1; }
                else { Response[count] = 0; }
                
                count++;
            }
        }
        
        // integrate over 2 parameters
        else {
            
            for (int i=0; i<gridSize; i++) {
                
                inc0 = (MaxR[0]-MinR[0]) / (gridSize-1);
                Params[0] = MinR[0] + i*inc0;
                
                for (int j=0; j<gridSize; j++) {
                    
                    inc1 = (MaxR[1]-MinR[1]) / (gridSize-1);
                    Params[1] = MinR[1] + j*inc1;
                    
                    if (probObs() < 0.5) { Response[count] = 1; }
                    else { Response[count] = 0; }
                    
                    count++;
                }
            }
        }
        
        return 1;
    }
}
