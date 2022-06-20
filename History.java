/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package thecrew;

import java.util.LinkedList;
/**
 *
 * @author Iwan382
 */
public class History {
    
    LinkedList<Card> cardsPlayed = new LinkedList<Card>();
    LinkedList<Integer> playerActing = new LinkedList<Integer>();
    boolean success, finished = false;
    LinkedList<Task> tasksChosen = new LinkedList<Task>();
    LinkedList<Integer> playerChoosing = new LinkedList<Integer>();
    Card[][] startingHands;
    LinkedList<LinkedList<Task>> taskDevision = new LinkedList<LinkedList<Task>>();
    LinkedList<Communication> communicationsMade = new LinkedList<Communication>();
    LinkedList<Integer> roundCommunicated = new LinkedList<Integer>();
    LinkedList<Integer> playerCommunicating = new LinkedList<Integer>();
    long timeTaken;
    
    public History()
    {
        
    }
    
    public History(LinkedList<Card> cP, LinkedList<Integer> pA)
    {
        for(int i = 0; i < cP.size(); i++)
        {
            cardsPlayed.add(cP.get(i));
            playerActing.add(pA.get(i));
        }
    }
    
    public History(LinkedList<Card> cP, LinkedList<Integer> pA, LinkedList<Task> mC, LinkedList<Integer> pC)
    {
        for(int i = 0; i < cP.size(); i++)
        {
            cardsPlayed.add(cP.get(i));
            playerActing.add(pA.get(i));
        }
        
        for(int i = 0; i < mC.size(); i++)
        {
            tasksChosen.add(mC.get(i));
            playerChoosing.add(pC.get(i));
        }
    }
    
    public void communicationMade(Communication comm, int round, int player)
    {
        communicationsMade.add(comm);
        roundCommunicated.add(round);
        playerCommunicating.add(player);
    }
    
    public void update(Card c, int player)
    {
        cardsPlayed.add(c);
        playerActing.add(player);
    }
    
    public void taskUpdate(Task m, int player)
    {
        tasksChosen.add(m);
        playerChoosing.add(player);
    }
    
    public LinkedList<Card> getCardsPlayed()
    {
        return cardsPlayed;
    }
    
    public LinkedList<Integer> getPlayerActing()
    {
        return playerActing;
    }
    
    public LinkedList<Task> getTasksChosen()
    {
        return tasksChosen;
    }
    
    public LinkedList<Integer> getPlayerChoosing()
    {
        return playerChoosing;
    }
    
    public LinkedList<Integer> getPlayerCommunicated()
    {
        return playerCommunicating;
    }
    
    public LinkedList<Integer> getRoundCommunicated()
    {
        return roundCommunicated;
    }
    
    public LinkedList<Communication> getCommunicationsMade()
    {
        return communicationsMade;
    }
    
    public boolean getSuccess()
    {
        return success;
    }
    
    public boolean getFinished()
    {
        return finished;
    }
    
    public Card[][] getStartingHands()
    {
        return startingHands;
    }
    
    public long getTimeTaken()
    {
        return timeTaken;
    }
    
    public void setTimeTaken(long timeTaken)
    {
        this.timeTaken = timeTaken;
    }
    
    public void setStartingHands(Card[][] startingHands)
    {
        this.startingHands = new Card[startingHands.length][startingHands[0].length];
        for(int i = 0; i < startingHands.length; i++)
        {
            for(int j = 0; j < startingHands[i].length; j++)
            {
                this.startingHands[i][j] = startingHands[i][j];
            }
        }
    }
    
    public void setFinished(boolean b)
    {
        finished = b;
    }
    public void setSuccess(boolean b)
    {
        success = b;
    }
    
    @Override
    public String toString()
    {
        String s = "History overview: \n";
        
        for(int i = 0; i < tasksChosen.size(); i++)
        {
            s += "Player " + (playerChoosing.get(i)+1) + " chose task " + tasksChosen.get(i) + "\n";
        }
        
        s += "-------------------------------------- \n";
        
        for(int i = 0; i < cardsPlayed.size(); i++)
        {
            s += "Player " + (playerActing.get(i)+1) + " played a " + cardsPlayed.get(i) + "\n";
        }
        
        return s;
    }
}
