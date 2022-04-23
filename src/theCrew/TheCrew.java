/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package thecrew;

/**
 *
 * @author Iwan382
 */
public class TheCrew {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int players = 3;
        
        /*
        TODO user input here
        */
        GameController game;
        
        Strategy[] s = {Strategy.SIMPLESIMCHEAT, Strategy.SIMPLESIMCHEAT, Strategy.SIMPLESIMCHEAT, Strategy.RANDOM};
        /*
        Double successes = 0.00, tries = 1000.00; //Strategy.RANDOM
        for(double d = 0; d < tries; d++)
        {   
            game = new GameController(players, false, s);
            successes += game.calculateSuccesRate(1);
            if(d % (tries/100) == 0)
            {
                System.out.println(d);
            }
        }
        
        
        System.out.println("The mission succeeded " + (100*(successes/tries)) + " % of the time");
        */
        ///*
        //normal run 
        game = new GameController(players, true, s);
        game.gameSetUp();
        game.gameStart(40);
        game.results();
          // */
        
    }
    
}
