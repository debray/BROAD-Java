/*
 *  Author: Debajyoti Ray, October 2011.
 *  Copyright: Caltech
 */
package timeprefapp;

/**
 * Enumerating the design space
 */
public class DesignFunctions {
    
    double [][] Design0, Design1;
    int num_Designs;
    
    // class constructor
    void DesignFunctions() {
        Design0 = new double[1][2]; 
        Design1 = new double[1][2];
        
        num_Designs = 0;
    }
    
    void makeAllDesigns() {
        
        int num_X = 10;
        int num_T = 25;
        
        // Create temporary design arrays with max_D elements
        // later we'll copy to Design0 and Design1 once number
        // of elements are known. Each design has 1x2 lottery.
        int max_D = 20000;
        
        double [][] tempDes0, tempDes1;
        
        tempDes0 = new double[max_D][2];
        tempDes1 = new double[max_D][2];
        
        // Minimum X and increment (upto 50)
        double Xlo = 5.0, inc = 5.0;
        
        // outcomes of lottery
        double [] X = new double[num_X];
        
        for (int i=0; i<num_X; i++) {
            
            X[i] = Xlo + inc*i;
        }
        
        double [] T = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                12, 14, 16, 21, 28, 30, 35,
                42, 49, 56, 60, 70, 80, 90};
        
        int numD = 0;   // number of designs
        
        for (int i0=0; i0<num_X-1; i0++) {
            
            for (int j0=0; j0<num_T-1; j0++) {
                
                for (int i1=i0+1; i1<num_X; i1++) {
                    
                    for (int j1=j0+1; j1<num_T; j1++) {
                        
                        double [] Lottery0 = new double[2];
                        double [] Lottery1 = new double[2];
                        
                        Lottery0[0] = X[i0]; Lottery0[1] = T[j0];
                        Lottery1[0] = X[i1]; Lottery1[1] = T[j1];
                        
                        tempDes0[numD] = Lottery0;
                        tempDes1[numD] = Lottery1;
                        
                        numD++;
                    }
                }
            }
        }
        
        // copy to truncated matrix
        Design0 = new double[numD][2];
        Design1 = new double[numD][2];
        
        for (int i=0; i<numD; i++) {
            
            Design0[i] = tempDes0[i];
            Design1[i] = tempDes1[i];
        }
        
        num_Designs = numD;
        
        System.err.println("Number of Designs: " + num_Designs);
    }
}
