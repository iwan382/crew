/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package thecrew;

import java.util.LinkedList;
import java.util.Random;

/**
 *
 * @author Iwan382
 */
public class Controller {
    private Strategy strategy;
    private IOInterface ioManager;
    private Random r = new Random();
    
    public Controller(Strategy s, IOInterface io)
    {
        ioManager = io;
        strategy = s;
    }

    public Card chooseCard(LinkedList<Card> moves, Player[] players, int pNum, GameController contr) 
    {
        switch(strategy)
        {
            case RANDOM: 
                //System.out.println("Player " + (pNum+1) + " plays a random card");
                return moves.get(r.nextInt(moves.size()));
           
            case PLAYER: return ioManager.selectCardToPlay(moves);
            
            case SIMPLESIMCHEAT: 
                Strategy[] s = {Strategy.RANDOM, Strategy.RANDOM, Strategy.RANDOM, Strategy.RANDOM}; 
                return players[pNum].simulateGame(contr, moves, moves.size()*100, s);
           
            default: return moves.getFirst();
        }
    }
    
    public Mission chooseMission(LinkedList<Mission> missions, Player[] players, int pNum) 
    {
        switch(strategy)
        {
            case RANDOM: return missions.removeFirst();
           
            case PLAYER: return ioManager.selectMissionToPick(missions, players[pNum].getHand());
           
            default: return missions.removeFirst();
        }
    }

    public Communication chooseCommunication(LinkedList<Communication> communication, Player[] players, int playNum) 
    {
        switch(strategy)
        {
            case RANDOM: return communication.removeFirst();
           
            case PLAYER: return ioManager.selectCommunication(communication, players[playNum].getHand());
           
            default: return communication.removeFirst();
        }
    }
    
    
    public Strategy getStrategy()
    {
        return strategy;
    }
    
    public void setStrategy(Strategy s)
    {
        strategy = s;
    }
    
    public Controller cloneController()
    {
        return new Controller(strategy, ioManager);
    }
    
    public IOInterface getIOManager()
    {
        return ioManager;
    }
}
