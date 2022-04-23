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
    
    public void update(Card c, int player)
    {
        cardsPlayed.add(c);
        playerActing.add(player);
    }
    
    public LinkedList<Card> getCardsPlayed()
    {
        return cardsPlayed;
    }
    
    public LinkedList<Integer> getPlayerActing()
    {
        return playerActing;
    }
    
    public boolean getSuccess()
    {
        return success;
    }
    
    public boolean getFinished()
    {
        return finished;
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
        
        for(int i = 0; i < cardsPlayed.size(); i++)
        {
            s += "Player " + (playerActing.get(i)+1) + " played a " + cardsPlayed.get(i) + "\n";
        }
        
        return s;
    }
}
