/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package timeprefapp;

/**
 *
 * @author debray
 */
public class runTimePrefApp {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        // number of rounds
        int maxRounds = 50;
        
        //System.err.println("\f");       // flush the screen
        // Clear the screen (PITA in JAVA)
        try {
            if ((System.getProperty( "os.name")).startsWith(" Window ")) {
                Runtime.getRuntime().exec("cls"); }
            else { Runtime.getRuntime().exec("clear"); }
        }
        catch(Exception e) {
            for (int i=0; i<1000; i++) { System.err.println(); }
        }
        System.err.println("Enter Subject Id: \n\n\n");
        
        int id = 0;
        
        java.util.Scanner kbIn = new java.util.Scanner(System.in);
        id = Integer.parseInt(kbIn.next());

        testSubject subj = new testSubject(id, maxRounds);
        
        subj.testThisSubject();
    }
}
