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
public class Task {
    public Card taskCard;
    public TaskOrder order;
    public int playerNum = -37;
    
    public Task(Card card, TaskOrder order)
    {
        taskCard = card;
        this.order = order;
    }
    
    public Task(Card card, TaskOrder order, int n)
    {
        taskCard = card;
        this.order = order;
        playerNum = n;
    }
    
    public void setPlayerNum(int n)
    {
        playerNum = n;
    }
    
    public boolean TaskSuccess(Player[] players)
    {
        //System.out.println(this);
        Card[][] winnings = players[playerNum].getWinPile();
        switch(order)
        {
            case NONE:
                for(Card[] array: winnings)
                {
                    if(array != null)  
                    {
                        for(Card c: array)
                        {
                            //System.out.println(c); //debug info
                            //System.out.println(taskCard); //debug info
                            if(c != null && c.equals(taskCard))
                            {
                                //System.out.println("Yeah task succes"); //debug info
                                return true;
                            }
                        }
                    }
                }
                break;
            default:
                return false;
        }
        return false;
    }
    
    public boolean PrematureFailure(Player[] players)
    {
        Card[][][] winningsCollection = new Card[players.length][players.length][40/players.length];
        for(int i = 0; i < players.length; i++)
        {
            winningsCollection[i] = players[i].getWinPile();
        }
        switch(order)
        {
            case NONE:
                for(int i = 0; i < players.length; i++)
                {
                    Card[][] winnings = winningsCollection[i];
                    if(winnings != null)
                    {
                        for(Card[] array: winnings)
                        {
                            for(Card c: array)
                            {
                                if(c != null && c.equals(taskCard))
                                {
                                    //System.out.println("Task card found"); //debug info
                                    return i != playerNum;
                                }
                            }
                        }
                    }
                }
                break;
            default:
                return false;
        }
        return false;
    }
    
    public Card getTaskCard()
    {
        return taskCard;
    }
    
    public TaskOrder getOrder()
    {
        return order;
    }
    
    public int getPlayerNum()
    {
        return playerNum;
    }
    
    public Task cloneTask()
    {
        return new Task(taskCard, order, playerNum);
    }
    
    
    @Override
    public String toString()
    {
        String s = "Win " + taskCard;
        
        return s;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if(o != null)
        {
            if(o.getClass() == Task.class)
            {
                return ((Task) o).getTaskCard().equals(taskCard) && ((Task) o).getOrder() == order;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }
    
}
