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
public class Mission {
    public Card missionCard;
    public MissionOrder order;
    public int playerNum = -37;
    
    public Mission(Card card, MissionOrder order)
    {
        missionCard = card;
        this.order = order;
    }
    
    public Mission(Card card, MissionOrder order, int n)
    {
        missionCard = card;
        this.order = order;
        playerNum = n;
    }
    
    public void setPlayerNum(int n)
    {
        playerNum = n;
    }
    
    public boolean MissionSuccess(Player[] players)
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
                            //System.out.println(missionCard); //debug info
                            if(c != null && c.equals(missionCard))
                            {
                                //System.out.println("Yeah mission succes"); //debug info
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
                                if(c != null && c.equals(missionCard))
                                {
                                    //System.out.println("Mission card found"); //debug info
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
    
    public Card getMissionCard()
    {
        return missionCard;
    }
    
    public MissionOrder getOrder()
    {
        return order;
    }
    
    public int getPlayerNum()
    {
        return playerNum;
    }
    
    public Mission cloneMission()
    {
        return new Mission(missionCard, order, playerNum);
    }
    
    
    @Override
    public String toString()
    {
        String s = "Win " + missionCard;
        
        return s;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if(o != null)
        {
            if(o.getClass() == Mission.class)
            {
                return ((Mission) o).getMissionCard().equals(missionCard) && ((Mission) o).getOrder() == order;
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
