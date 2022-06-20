/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package thecrew;
import java.lang.*;
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
        
        GameController game;
        long startTime, endTime;
        IOInterface Io = new IOInterface();
        Strategy[] s = new Strategy[players];//{Strategy.RANDOM, Strategy.RANDOM, Strategy.RANDOM, Strategy.RANDOM};
        for(int i = 0; i < players; i++)
        {
            s[i] = Io.selectStrategy(i);
        }
        
        if(Io.manyGames())
        {
            int mission = Io.selectMission();
            //ExcelExport export = new ExcelExport("3PlayersBULKSIMHONESTmis42");
            Double successes = 0.00, tries = (double) Io.howManyGames(); //Strategy.RANDOM
            for(int d = 0; d < tries; d++)
            {   
                startTime = System.currentTimeMillis(); 
                game = new GameController(players, false, s);
                successes += game.runGame(mission);
                endTime = System.currentTimeMillis();
                long gameTime = endTime-startTime;
                History h = game.getHistory();
                h.setTimeTaken(gameTime);
                //export.exportHistory(h, d);
                if(d % (tries/100) == 0)
                {
                    System.out.println(d);
                }
            }
            
            System.out.println("The mission succeeded " + (100*(successes/tries)) + " % of the time");
        }
        else
        {
            game = new GameController(players, true, s);
            game.gameSetUp();
            game.gameStart(40);
            game.results();
        }        
    }
    
}
