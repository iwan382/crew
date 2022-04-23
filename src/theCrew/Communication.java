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
public class Communication {
    
    private Card card;
    private Position pos;
    
    public Communication(Card c, Position p)
    {
        card = c;
        pos = p;
    }
    
    public Card getCard()
    {
        return card;
    }
    
    public Position getPos()
    {
        return pos;
    }
            
    @Override
    public String toString()
    {
        if(pos == Position.TOP)
        {
            return card.toString() + " as their highest card";
        }
        else if(pos == Position.BOTTOM)
        {
            return card.toString() + " as their lowest card";
        }
        else
        {
            return card.toString() + " as their only card";
        }
    }
}
