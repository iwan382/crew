/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package thecrew;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Scanner;

/**
 *
 * @author Iwan382
 */
public class IOInterface 
{
    Scanner scan;
    LinkedList<Integer> supportedMissions = new LinkedList<>(Arrays.asList(1,2,4,10,42,47));;
    
    public IOInterface()
    {
        scan = new Scanner(System.in);
        
    }
    
    public boolean manyGames()
    {
        System.out.println("Please indicate whether you want to run one game, or run a lot of games.\n Write true for many games, false for one game.");
        while(true)
        {
            if(scan.hasNextBoolean())
            {
                return scan.nextBoolean();
            }
        }
    }
    
    public int howManyGames()
    {
        System.out.println("Please indicate the number of games you want to run.");
        while(true)
        {
            if(scan.hasNextInt())
            {
                return scan.nextInt();
            }
        }
    }
    
    public String sheetName() 
    {
        System.out.println("Please indicate how you want to name the sheet the data is written to.");
        System.out.println("(Ensure that it does not exist already)");
        while(true)
        {
            if(scan.hasNext())
            {
                return scan.next();
            }
        }
    }
    
    public void cardPlayed(int player, Card card)
    {
        System.out.println("Player " + (player+1) + ", played a " + card.toString());
    }
    
    public void trickPhase()
    {
        System.out.println("A trick phase starts.");        
    }
    
    public void commPhase()
    {
        System.out.println("A communication phase starts.");        
    }
    
    public void commCard(int communicated, int playerNum, Player player)
    {
        if(communicated == 2)
        {
            System.out.println("Player " + (playerNum+1) + " has decided not to communicate");
        }
        else if(communicated == 1)
        {
            System.out.println("Player " + (playerNum+1) + " has decided to communicated that they have a " + player.getComm().toString());
        }
    }
    
    public Strategy selectStrategy(int p)
    {
        System.out.println("Please indicate which strategy player " + (p+1) + " should employ");
        System.out.println("The choices are: R, for random; P, for player controlled; SSC, for simple simulations (with universal knowledge)"
                + "; SSH for simple simulations (with information fog);\n CSH for simulations where a bit of competense is assumed (with infor"
                + "mation fog); SMH for simulations where significantly more competence is assumed (with information fog);\n BSH for a lot more" 
                + "simulations where a bit of competense is assumed (with information fog)");
        while(true)
        {
            if(scan.hasNext())
            {
                switch(scan.next())
                {
                    case "R": return Strategy.RANDOM;
                    
                    case "P": return Strategy.PLAYER;
                    
                    case "SSC": return Strategy.SIMPLESIMCHEAT;
                    
                    case "SSH": return Strategy.SIMPLESIMHONEST;
                    
                    case "CSH": return Strategy.COMPETENTSIMHONEST;
                    
                    case "SMH": return Strategy.SMARTSIMHONEST;
                    
                    case "BSH": return Strategy.BULKSIMHONEST;
                    
                    default: System.out.println("The input is not recognised");
                }
            }
        }
    }
    
    public Card selectCardToPlay(LinkedList<Card> hand)
    {
        System.out.println("Please indicate which card you want to play: ");
        System.out.println("The cards you can play are: " + hand);
        System.out.println("Please indicate which card with a number corresponding to its position");
        while(true)
        {
            if(scan.hasNextInt())
            {
                int i = scan.nextInt();
                return hand.get(i-1);
            }
        }
    }
    
    public int selectMission()
    {
        System.out.println("Please indicate which mission you would like to play:");
        System.out.println("The currently supported missions are: " + supportedMissions);
        int mission = 1;
        while(true)
        {
            if(scan.hasNextInt())
            {
                mission = scan.nextInt();
                if(supportedMissions.contains(mission))
                {
                    return mission;
                }
                else
                {
                    System.out.println("This mission is currently not supported.");
                }
            }
        }
    }

    public Task selectTaskToPick(LinkedList<Task> tasks, LinkedList<Card> hand)
    {
        System.out.println("Please indicate which task you want to perform: ");
        System.out.println("The tasks you can pick are: " + tasks);
        System.out.println("You currently have: " + hand + " in your hand");
        System.out.println("Please indicate which task with a number corresponding to its position");
        while(true)
        {
            if(scan.hasNextInt())
            {
                int i = scan.nextInt();
                return tasks.remove(i-1);
            }
            else
            {
                if(scan.hasNext())
                {
                    scan.next();
                }
            }
        }
    }
    
    public Communication selectCommunication(LinkedList<Communication> communication, LinkedList<Card> hand) 
    {
        System.out.println("Please indicate if you want to communicate (true of false): ");
        if(!scan.nextBoolean())
        {
            return null;
        }
        System.out.println("Please incicate what you want to communicate");
        System.out.println("The possibilities are: " + communication);
        System.out.println("You currently have: " + hand + " in your hand");
        System.out.println("Please indicate what you want to communicate with a number corresponding to its position");
        while(true)
        {
            if(scan.hasNextInt())
            {
                int i = scan.nextInt();
                return communication.remove(i-1);
            }
        }
    }
    
    public void taskSelected(Task m, int player)
    {
        System.out.println("Player " + (player+1) + " has chosen the task: \"" + m.toString() + "\"");
    }
    
    public void taskSucces(Task m, int player)
    {
        System.out.println("Player " + (player+1) + " has completed the task: \"" + m.toString() + "\"");
    }
    
    public void taskFailed(Task m, int player)
    {
        System.out.println("Player " + (player+1) + " has not completed the task: \"" + m.toString() + "\"");
    }    
    
    public void result(boolean won)
    {
        if(won)
        {
            System.out.println("This task has been completed successfully.");
        }
        else
        {
            System.out.println("This task was unsuccessfull.");
        }
    }

    
}
