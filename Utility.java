/*
 * Author: Debajyoti Ray, October 2011.
 * Copyright: Caltech
 */
package timeprefapp;

/**
 * Contains definitions of Utility functions
 * 
 */
public class Utility {
    
    double [] Params;
    
    // Constructor
    Utility(int numParams) { 
        
        Params = new double[numParams];
        
        for (int i=0; i<numParams; i++) {
            
            Params[i] = 0;
        }
    }
    
    // Exponential discounting
    double calcUtilityExp(double [] Lottery) {
        
        double U = 0;
        double discF = 0;
        
        discF = Math.exp(-Params[0] * Lottery[1]);
        
        U = discF * Lottery[0];
        
        return U;
    }
    
    // Hyperbolic discounting 
    double calcUtilityHyp(double [] Lottery) {
        
        double U = 0;
        double discF = 0;
        
        discF = 1.0 / (1.0 + Params[0] * Lottery[1]);
        
        U = discF * Lottery[0];
        
        return U;
    }
    
    // Quasi-Hyperbolic discounting
    double calcUtilityQH(double [] Lottery) {
        
        double U = 0;
        double discF = 0;
        
        // fixed cost of time is magnitude independent
        if (Lottery[1]==0) { discF = 1; }
        else { 
            discF = Params[1] * Math.exp(-Params[0] * Lottery[1]);
        }
        
        U = discF * Lottery[0];
        
        return U;
    }
    
    // Fixed cost of waiting
    double calcUtilityFixed(double [] Lottery) {
        
        double U = 0;
        double discF = 0;
        
        if (Lottery[1]==0) { discF = 1; }
        else { 
            discF = Math.exp(-Params[0] * Lottery[1]) -
                    (Params[1] / Lottery[0]);
        }
        
        U = discF * Lottery[0];
        
        return U;
    }
    
    // Generalized Hyperbolic discounting
    double calcUtilityGH(double [] Lottery) {
        
        double U = 0;
        double discF = 0;
        
        // if alpha = 0, D = exp(-beta t)
        if (Lottery[1]==0) { 
            discF = Params[1] * Math.exp(-Params[0] * Lottery[1]);
        }
        // if alpha > 0
        else { 
            double tempD = - (Params[0] / Params[1]) * 
                    Math.log(1 + Params[0] * Lottery[1]);
            
            discF = Math.exp(tempD);
        }
        
        U = discF * Lottery[0];
        
        return U;
    }
}
