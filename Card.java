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
public class Card {
    
    private final Color suit;
    private final int value;
    
    public Card(Color c, int i)
    {
        suit = c;
        value = i;
    }
    
    public Color getSuit()
    {
        return suit;
    }
    
    public int getValue()
    {
        return value;
    }
    
    @Override
    public String toString()
    {
        return "(" + suit.toString() + ", " + value + ")";
    }
    
    @Override
    public boolean equals(Object o)
    {
        if(o != null)
        {
            if(o.getClass() == Card.class)
            {
                return ((Card) o).getValue() == value && ((Card) o).getSuit() == suit;
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
