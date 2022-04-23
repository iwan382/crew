
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package thecrew;

import java.util.Random;
import java.util.LinkedList;
import java.util.*;
/**
 *
 * @author Iwan382
 */
public class GameController {
    
    private LinkedList<Card> deck = new LinkedList();
    private LinkedList<Mission> missionOverview = new LinkedList();
    private Card[][] startingHands;
    private Player[] players;
    private Random r = new Random();
    private int round;
    private Player leadPlayer;
    private int player;
    private IOInterface ioManager = new IOInterface();
    private boolean trickPhase = false;
    private boolean feedback = true;
    private History history = new History();
    
    private Strategy[] strats;
    private int mNum;
    
    public GameController(int nPlayers, Player[] newPlayers, int round, Player lead, int player, boolean b, LinkedList<Mission> overview, History h)
    {
        players = newPlayers;
        history = new History(h.getCardsPlayed(), h.getPlayerActing());
        /*
        System.out.println("The first player is: " + players[0]);
        for(int i = 0; i < nPlayers; i++)
        {
            
            System.out.println("Player " + i + " is: " + players[i]);
            System.out.println("This is " + this);
            this.players[i] = players[i].clonePlayer(this);
        }
        */
        deck = new LinkedList();
        missionOverview = overview;
        r = new Random();
        this.round = round;
        leadPlayer = lead;
        this.player = player;
        //System.out.println("This simulation was made in player: " + (this.player+1) + "'s turn");
        ioManager = new IOInterface();
        trickPhase = b;
        feedback = false;
    }
    
    public GameController(int nPlayers, boolean b, Strategy[] s)
    {
        Color[] colors = {Color.BLUE, Color.GREEN, Color.YELLOW, Color.PINK, Color.BLACK};
        int j = 0, value = 1;
        strats = s;
        feedback = b;
        //Create the deck of cards
        for(int i = 0; i < 40; i++)
        {
            deck.add(new Card(colors[j], value));
            if(value==9)
            {
                j++;
            }
            value = value % 9 + 1;
            //System.out.print(deck[i].toString() + "\n"); debug info
        }
        
        //Devide the deck of cards (as equally as possible) into a  number of hands equal to the number of players
        startingHands = new Card[nPlayers][40/nPlayers+1];
        for(int i = 0; i < 40; i++)
        {
            startingHands[i%nPlayers][i/nPlayers] = deck.remove(r.nextInt(40-i));
        }
        
        /* information
        for(int i = 0; i < nPlayers; i++)
        {
            System.out.print("Player: " + (i+1) + " has in his hand\n");
            for(j = 0; j < startingHands[i].length; j++)
            {
                System.out.print(startingHands[i][j] + "\n");
            }
        }
        */
        
        
        //create a list of the players
        players = new Player[nPlayers];
        Strategy strat;
        for(int i = 0; i < nPlayers; i++)
        {
            if(feedback)
            {
                strat = ioManager.selectStrategy(i);
            }
            else
            {
                strat = strats[i];
            }
            players[i] = new Player(new LinkedList(Arrays.asList(startingHands[i])), nPlayers, i, strat, ioManager, feedback, this);
        }
        //determine which player has the captain role
        for(Player p: players)
        {
            if(p.isCaptain())
            {
                leadPlayer = p;
            }
        }
        
        player = leadPlayer.getPlayNum();
        if(feedback)
        {
            System.out.println("Preparation done");
        }
        
    }
    
    public double calculateSuccesRate(int m)
    {
        mNum = m;
        feedback = false;
        gameSetUp();
        gameStart((40/players.length));
        if(results())
        {
            return 1.00;
        }
        return 0.00;
    }
    
    public void gameSetUp()
    {
        //ask which mission
        int mission;
        if(feedback)
        {
            mission = ioManager.selectMission();
        }
        else
        {
            mission = mNum;
        }
        Color[] colors = {Color.BLUE, Color.GREEN, Color.YELLOW, Color.PINK};
        int value = 1;
        for(Color c: colors)
        {
            while(value < 10)
            {
                deck.add(new Card(c, value));
                value++;
            }
            value = 1;
        }
        //System.out.print(deck.toString() + "\n"); //debug info
        //assign missions
        
        assignMissions(mission, deck);
        
        
        player = leadPlayer.getPlayNum();
        /*                    //debug info
        double[][][] info = players[0].getGameInfo();
        
        System.out.print("("); //debug info
        for(double[][] twodarray: info)
        {
            System.out.print("(");
            for(double[] array: twodarray)
            {
                System.out.print("(");
                for(double d: array)
                {
                    System.out.print(d + ", ");
                }
                System.out.println(")");
            }
            System.out.println(")");
        }
        System.out.println(")");
        */
        
    }
    
    public void makeMove(int pNum, Card card)
    {
        players[pNum].forcePlayCard(card);
        history.update(card, pNum);
        player++;
        player %= players.length;
    }
    
    public History simulateGame(int maxRounds, Strategy[] strats)
    {
        boolean gameFinished = false;
        //player++;
        //player %= players.length;
        reassignStrategy(strats);
        if(trickPhase)
        {
            trick();
            round++;
            gameFinished = communication();
            boolean won = true, lost = false;
            for(Mission m: missionOverview)
            {
                won &= m.MissionSuccess(players);
                lost |= m.PrematureFailure(players);
            }
            gameFinished |= won;
            gameFinished |= lost;
        }
        
        if(!gameFinished)
        {
            gameStart(maxRounds-1);
        }
        results();
        return history;
    }
    
    public void gameStart(int maxRounds)
    {
        if(feedback)
        {
            ioManager.commPhase();
        }
        
        boolean gameFinished = communication();
        if(feedback)
        {
            ioManager.trickPhase();
        }
        while(!gameFinished)
        {
            trick();
            round++;
            if(feedback)
            {
                ioManager.commPhase();
            }
            gameFinished = communication();
            boolean won = true, lost = false;
            for(Mission m: missionOverview)
            {
                won &= m.MissionSuccess(players);
                lost |= m.PrematureFailure(players);
            }
            gameFinished |= won;
            gameFinished |= lost;
        }
    }

    
    
    public boolean results()
    {
        boolean gameWon = true;
        boolean missionSucces;
        for(Mission m: missionOverview)
        {
            missionSucces = m.MissionSuccess(players);
            gameWon &= missionSucces;
            if(missionSucces && feedback)
            {
                ioManager.missionSucces(m, m.getPlayerNum());
            }
            else if(feedback)
            {
                ioManager.missionFailed(m, m.getPlayerNum());
            }
            
        }
        if(feedback)
        {
            ioManager.result(gameWon);
        }
        
        //System.out.println(history.toString());
        
        
        if(missionOverview == null)
        {
            System.out.println("oi");
        }
        /*                    //debug info
        double[][][] info = players[0].getGameInfo();
        
        System.out.print("(");
        for(double[][] twodarray: info)
        {
            System.out.print("(");
            for(double[] array: twodarray)
            {
                System.out.print("(");
                for(double d: array)
                {
                    System.out.print(d + ", ");
                }
                System.out.println(")");
            }
            System.out.println(")");
        }
        System.out.println(")");
        */
        history.setFinished(true);
        history.setSuccess(gameWon);
        return gameWon;
    }
    
    private boolean communication() 
    {
        trickPhase = false;
        int[] commChance = new int[players.length];
        for(int i = 0; i < players.length; i++)
        {
            commChance[i] = players[i].getDecision();
        }
        int n;
        boolean finished = false;
        while(!finished)
        {
            n = 0;
            commChance[player] = players[player].makeCommDecision(leadPlayer.getPlayNum(), players);
            for(int i: commChance)
            {
                n+=i;
            }
            if(n==players.length*2)
            {
                finished = true;
            }
            if(feedback)
            {
                ioManager.commCard(commChance[player], player, players[player]);
            }
            
            if(commChance[player] == 1)
            {
                for(Player p: players)
                {
                    if(p.getPlayNum() != player)
                    {
                        p.updateInfoCommunication(players[player], players[player].getComm());
                    }
                }
            }
            player++;
            player = player % players.length;
        }
        
        for(Player p: players)
        {
            p.setDecision(0);
        }
        player = leadPlayer.getPlayNum();
        return players[players.length-1].handEmpty();
    }

    private void trick() 
    {
        trickPhase = true;
        Card[] cardsPlayed = new Card[players.length];
        for(int i = 0; i < players.length; i++)
        {
            if(players[i].getCardPlayed() != null)
            {
                cardsPlayed[i] = players[i].getCardPlayed();
                for(Player p: players)
                {
                    p.updateInfoCard(players[i], cardsPlayed[i]);
                }
            }
        }
        while(!cardsPlayed())
        {
            cardsPlayed[player] = players[player].playCard(leadPlayer.getPlayNum(), players);
            history.update(cardsPlayed[player], player);
            if(feedback)
            {
                ioManager.cardPlayed(player, cardsPlayed[player]);
            }
            
            for(Player p: players)
            {
                p.updateInfoCard(players[player], cardsPlayed[player]);
            }
            player++;
            player %= players.length;
            //System.out.println("player " + (i+1) + " now plays a card"); //debug info
        }
        
        
        //Determine the winner of this trick
        int i = leadPlayer.getPlayNum();
        int winner = i;
        Card winningCard = cardsPlayed[i];
        Color leadingColor = winningCard.getSuit();//y
        i = 0;
        for(Card c: cardsPlayed)//y9,y3,y5
        {
            if(c.getSuit() == Color.BLACK)
            {
                if(c.getValue() > winningCard.getValue() || winningCard.getSuit() != Color.BLACK)
                {
                    winner = i;
                    winningCard = c;
                }
            }
            else if(c.getSuit() == leadingColor)
            {
                if(c.getValue() > winningCard.getValue() && winningCard.getSuit() != Color.BLACK)
                {
                    winner = i;
                    winningCard = c;
                }
            }
            leadingColor = winningCard.getSuit();
            i++;
            i = i % players.length;
        }
        leadPlayer = players[winner];
        //System.out.println("The winner is player: " + (leadPlayer.getPlayNum()+1));//debug info
        players[winner].setWinPile(round, cardsPlayed);
        player = leadPlayer.getPlayNum();
        for(Player p: players)
        {
            p.removeCardPlayed();
        }
    }
    
    public boolean cardsPlayed()
    {
        for(Player p: players)
        {
            if(p.getCardPlayed() == null)
            {
                return false;
            }
        }
        return true;
        
    }
    
    public void assignMissions(int mission, LinkedList<Card> missionCards)
    {
        LinkedList<Mission> missions = new LinkedList();
        int conditions[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        switch(mission)
        {
            case 1: 
                missions.add(new Mission(deck.remove(r.nextInt(36)), MissionOrder.NONE));
                devideNormally(missions);
                break;
            case 2:
                missions = createMissions(missionCards, 2,  conditions);
                //missions.add(new Mission(deck.remove(r.nextInt(36)), MissionOrder.NONE));
                //missions.add(new Mission(deck.remove(r.nextInt(35)), MissionOrder.NONE));
                devideNormally(missions);
                break;
            case 4:
                missions = createMissions(missionCards, 3,  conditions);
                devideNormally(missions);
                break;
            case 10:
                missions = createMissions(missionCards, 4,  conditions);
                devideNormally(missions);
                break;
            case 42:
                missions = createMissions(missionCards, 9,  conditions);
                devideNormally(missions);
                break;
            case 47:
                missions = createMissions(missionCards, 10,  conditions);
                devideNormally(missions);
                break;
            default: 
                break;
        }
    }
    
    public LinkedList<Mission> createMissions(LinkedList<Card> cards, int n, int[] conditions)
    {
        LinkedList<Mission> missions = new LinkedList<>();
        for(int i = 0; i < n; i++)
        {
            missions.add(new Mission(deck.remove(r.nextInt(36-i)), MissionOrder.values()[conditions[i]])); //untested
        }
        return missions;
    }

    public void devideNormally(LinkedList<Mission> missions) 
    {
        missionOverview = new LinkedList();
        missionOverview.addAll(missions);
        while(!missions.isEmpty())
        {
            missions = players[player].chooseMission(missions, ioManager, players);
            player++;
            player = player % players.length;
        }
    }
    
    //public GameController(int nPlayers, Player[] players, int round, Player lead, int player, boolean b)
    public GameController cloneGameController()
    {
        //System.out.println("The first player is: " + players[0]);//debug info
        Player[] newPlayers = new Player[players.length];
        for(int i = 0; i < players.length; i++)
        {
            //System.out.println("Player " + i + " is: " + players[i]);//debug info
            //System.out.println("This is " + this); //debug info
            newPlayers[i] = players[i].clonePlayer(this);
            //System.out.println(players[i]); //debug info
            //System.out.println(newPlayers[i]);//debug info
        }
        //System.out.println(this);//debug info
        LinkedList<Mission> missionCopy = new LinkedList();
        for(int i = 0; i < missionOverview.size(); i++)
        {
            missionCopy.add(missionOverview.get(i).cloneMission());
        }
        
        return new GameController(players.length, newPlayers, round, leadPlayer, player, trickPhase, missionCopy, history);
    }
    
    public History getHistory()
    {
        return history;
    }
    
    public void reassignStrategy(Strategy[] newStrats)
    {
        for(int i = 0; i < players.length; i++)
        {
            players[i].setStrategy(newStrats[i]);
        }
    }
    
    public void cleanUp()
    {
        
    }
}
