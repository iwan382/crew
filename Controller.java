
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
    private GameController game;
    
    public Controller(Strategy s, IOInterface io, GameController g)
    {
        ioManager = io;
        strategy = s;
        game = g;
    }

    public Card chooseCard(LinkedList<Card> moves, Player[] players, int pNum, GameController contr) 
    {
        assert(!moves.isEmpty());
        
        Card move = moves.get(r.nextInt(moves.size()));
        Strategy[] s;
        switch(strategy)
        {
            case RANDOM: 
                //System.out.println("Player " + (pNum+1) + " plays a random card");
                return move;
           
            case PLAYER: return ioManager.selectCardToPlay(moves);
            
            case SIMPLESIMCHEAT: 
                s = new Strategy[]{Strategy.RANDOM, Strategy.RANDOM, Strategy.RANDOM, Strategy.RANDOM}; 
                return players[pNum].simulateGame(contr, moves, moves.size()*100, s, false);
                
            case SIMPLESIMHONEST: 
                s = new Strategy[]{Strategy.RANDOM, Strategy.RANDOM, Strategy.RANDOM, Strategy.RANDOM}; 
                return players[pNum].simulateGame(contr, moves, moves.size()*100, s, true);
                
            case COMPETENTSIMHONEST: 
                s = new Strategy[]{Strategy.NOTCOMPLETELYINCOMPETENT, Strategy.NOTCOMPLETELYINCOMPETENT, Strategy.NOTCOMPLETELYINCOMPETENT, Strategy.NOTCOMPLETELYINCOMPETENT}; 
                return players[pNum].simulateGame(contr, moves, moves.size()*100, s, true);
            
            case SMARTSIMHONEST:
                s = new Strategy[]{Strategy.NOTCOMPLETELYINCOMPETENT, Strategy.NOTCOMPLETELYINCOMPETENT, Strategy.NOTCOMPLETELYINCOMPETENT, Strategy.NOTCOMPLETELYINCOMPETENT}; 
                return players[pNum].simulateGame(contr, moves, moves.size()*100, s, true);
                
            case SETLIST:
                move = game.getNextMove();
                if(moves.contains(move))
                {
                    return move;
                }
                else if(move.equals(new Card(Color.BLACK, 7)))
                {
                    return moves.get(r.nextInt(moves.size()));
                }
                else
                {
                    return new Card(Color.BLACK, 9);
                }
                
            case SETLISTNOTDUMB:
                move = game.getNextMove();
                if(game.quickFail(move, pNum) && anotherChoice(moves, pNum))
                {
                    return new Card(Color.BLACK, 9);
                }
                if(moves.contains(move))
                {
                    return move;
                }
                else if(move.equals(new Card(Color.BLACK, 7)))
                {
                    return moves.get(r.nextInt(moves.size()));
                }
                else
                {
                    return new Card(Color.BLACK, 9);
                }
                
            case SETLISTSMART:
                move = game.getNextMove();
                if(game.quickFail(move, pNum) && anotherChoice(moves, pNum))
                {
                    return new Card(Color.BLACK, 9);
                }
                if(moves.contains(move))
                {
                    return move;
                }
                else if(move.equals(new Card(Color.BLACK, 7)))
                {
                    return moves.get(r.nextInt(moves.size()));
                }
                else
                {
                    return new Card(Color.BLACK, 9);
                }
                
            case NOTCOMPLETELYINCOMPETENT:
                int randomInit = r.nextInt(moves.size());
                for(int i = randomInit; i < (moves.size()+randomInit); i++)
                {   
                    if(game.quickFail(move, pNum))
                    {
                        move = moves.get(i%moves.size());
                    }
                }
                return move;
                
            case SMART:
                randomInit = r.nextInt(moves.size());
                for(int i = randomInit; i < (moves.size()+randomInit); i++)
                {   
                    if(game.quickFail(move, pNum))
                    {
                        move = moves.get(i%moves.size());
                    }
                }
                return move;
                
            case BULKSIMHONEST: 
                s = new Strategy[]{Strategy.NOTCOMPLETELYINCOMPETENT, Strategy.NOTCOMPLETELYINCOMPETENT, Strategy.NOTCOMPLETELYINCOMPETENT, Strategy.NOTCOMPLETELYINCOMPETENT}; 
                return players[pNum].simulateGame(contr, moves, moves.size()*1000, s, true);                
                
            default: return move;
        }
    }
    
    public Task chooseTask(LinkedList<Task> missions, Player[] players, int pNum, GameController contr) 
    {
        
        Strategy[] s;
        Task m = missions.get(r.nextInt(missions.size()));
        switch(strategy)
        {
            case RANDOM: 
                missions.remove(m);
                return m;
           
            case PLAYER: 
                m = ioManager.selectTaskToPick(missions, players[pNum].getHand());
                missions.remove(m);
                return m;
            
            case SIMPLESIMCHEAT:
                s = new Strategy[]{Strategy.RANDOM, Strategy.RANDOM, Strategy.RANDOM, Strategy.RANDOM}; 
                m = players[pNum].simulateGameSetUp(contr, missions, missions.size()*100, s, false);
                missions.remove(m);
                return m;
                
            case SIMPLESIMHONEST:
                s = new Strategy[]{Strategy.RANDOM, Strategy.RANDOM, Strategy.RANDOM, Strategy.RANDOM}; 
                m = players[pNum].simulateGameSetUp(contr, missions, missions.size()*100, s, true);
                missions.remove(m);
                return m;
                
            case COMPETENTSIMHONEST:
                s = new Strategy[]{Strategy.NOTCOMPLETELYINCOMPETENT, Strategy.NOTCOMPLETELYINCOMPETENT, Strategy.NOTCOMPLETELYINCOMPETENT, Strategy.NOTCOMPLETELYINCOMPETENT}; 
                m = players[pNum].simulateGameSetUp(contr, missions, missions.size()*100, s, true);
                missions.remove(m);
                return m;
                
            case SMARTSIMHONEST:
                s = new Strategy[]{Strategy.SMART, Strategy.SMART, Strategy.SMART, Strategy.SMART}; 
                m = players[pNum].simulateGameSetUp(contr, missions, missions.size()*100, s, true);
                missions.remove(m);
                return m;
                
            case SETLIST:
                m = game.getNextTask();
                missions.remove(m);
                if(missions.contains(m))
                {
                    return m;
                }
                else if(m.equals(new Task(new Card(Color.BLACK, 7), TaskOrder.NONE)))
                {
                    return missions.get(r.nextInt(missions.size()));
                }
                else
                {
                    return new Task(new Card(Color.BLACK, 9), TaskOrder.NONE);
                }
                
            case SETLISTNOTDUMB:
                m = game.getNextTask();
                missions.remove(m);
                if(false) //bad choice plus another option is available
                {
                    return new Task(new Card(Color.BLACK, 9), TaskOrder.NONE);
                }
                if(missions.contains(m))
                {
                    return m;
                }
                else if(m.equals(new Task(new Card(Color.BLACK, 7), TaskOrder.NONE)))
                {
                    return missions.get(r.nextInt(missions.size()));
                }
                else
                {
                    return new Task(new Card(Color.BLACK, 9), TaskOrder.NONE);
                }
                
            case SETLISTSMART:
                m = game.getNextTask();
                if(m.equals(players[pNum].bestTask(missions))) //not the best choice for the player
                {
                    missions.remove(m);
                    return new Task(new Card(Color.BLACK, 9), TaskOrder.NONE);
                }
                missions.remove(m);
                if(missions.contains(m))
                {
                    return m;
                }
                else if(m.equals(new Task(new Card(Color.BLACK, 7), TaskOrder.NONE)))
                {
                    return missions.get(r.nextInt(missions.size()));
                }
                else
                {
                    return new Task(new Card(Color.BLACK, 9), TaskOrder.NONE);
                }
                                
            case NOTCOMPLETELYINCOMPETENT:
                int randomInit = r.nextInt(missions.size());
                for(int i = randomInit; i < (missions.size()+randomInit); i++)
                {   
                    m = missions.get(i%missions.size());
                }
                missions.remove(m);
                return m;
            
            case SMART:
                m = players[pNum].bestTask(missions);
                missions.remove(m);
                return m;
                
            case BULKSIMHONEST:
                s = new Strategy[]{Strategy.NOTCOMPLETELYINCOMPETENT, Strategy.NOTCOMPLETELYINCOMPETENT, Strategy.NOTCOMPLETELYINCOMPETENT, Strategy.NOTCOMPLETELYINCOMPETENT}; 
                m = players[pNum].simulateGameSetUp(contr, missions, missions.size()*1000, s, true);
                missions.remove(m);
                return m;
                
            default: 
                missions.remove(m);
                return m;
        }
    }

    public Communication chooseCommunication(LinkedList<Communication> communication, Player[] players, int playNum) 
    {
        switch(strategy)
        {
            case RANDOM: return communication.removeFirst();
           
            case PLAYER: return ioManager.selectCommunication(communication, players[playNum].getHand());
            
            case SMARTSIMHONEST: return players[playNum].bestCommunication(communication);
            
            case SMART: return players[playNum].bestCommunication(communication);
           
            default: return communication.removeFirst();
        }
    }
    
    public boolean anotherChoice(LinkedList<Card> moves, int pNum)
    {
        for(Card c: moves)
        {
            if(!game.quickFail(c, pNum))
            {
                return true;
            }
        }
        return false;
    }
    
    public Strategy getStrategy()
    {
        return strategy;
    }
    
    public void setGame(GameController game)
    {
        this.game = game;
    }
    
    public void setStrategy(Strategy s)
    {
        strategy = s;
    }
    
    public Controller cloneController()
    {
        return new Controller(strategy, ioManager, game);
    }
    
    public IOInterface getIOManager()
    {
        return ioManager;
    }
}
