
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
    private int round = 0;
    private Player leadPlayer;
    private int player;
    private IOInterface ioManager = new IOInterface();
    private boolean trickPhase = false;
    private boolean feedback = true;
    private History history = new History();
    private LinkedList<Card> preSelectedMoves = new LinkedList();
    private LinkedList<Mission> preSelectedMissions = new LinkedList();
    
    private Strategy[] strats;
    private int mNum;
    
    public GameController(int nPlayers, Player[] newPlayers, int round, Player lead, int player, boolean b, LinkedList<Mission> overview, History h)
    {
        players = newPlayers;
        for(Player p: players)
        {
            p.setGame(this);
        }
        history = new History(h.getCardsPlayed(), h.getPlayerActing(), h.getMissionsChosen(), h.getPlayerChoosing());
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
        
        /*information
        for(int i = 0; i < nPlayers; i++)
        {
            System.out.print("Player: " + (i+1) + " has in his hand\n");
            for(j = 0; j < startingHands[i].length; j++)
            {
                System.out.print(startingHands[i][j] + "\n");
            }
        }
        */
        
        history.setStartingHands(startingHands);
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
    
    /*
    Used to run a game, it then returns a 1 if the game was won, a 0 if the game was lost.
    No output will be given during the time the game runs.
    
    */
    public double calculateSucces(int m)
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
    
    
    /*
    Simulate part of a game with the given strategy, which should make use of the preselected moves.
    It returns true when the preselected moves were able to be played, otherwise it returns false.
    
    */
    public boolean historyCheck(Strategy[] s)   
    {
        History h = simulateGameStart(preSelectedMoves.size()/players.length-1, s);
        if(h.getMissionsChosen().contains(new Mission(new Card(Color.BLACK, 9), MissionOrder.NONE)))
        {
            return false;
        }
        //h = simulateGame(preSelectedMoves.size()/players.length-1, s);
        return !h.getCardsPlayed().contains(new Card(Color.BLACK, 9));
    }
    
    /*
    Simulate a game including the deviding the missions, then return a History object containing the relevant information about it.
    
    */
    public History simulateGameStart(int maxRounds, Strategy[] strats)
    {
        reassignStrategy(strats);
        simulateDevideNormally(missionOverview, strats);
        gameStart(maxRounds);
        results();
        return history;
    }
    
    /*
    Ensure that the missions associated with the wanted mission are divided properly
    
    */
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
        //System.out.println("The lead player is: )" + player);
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
    
    /*
    Make player pNum add the mission mission.
    Then update which player has to take their turn.
    
    */
    public void chooseMission(int pNum, Mission mission)
    {
        players[pNum].forceChooseMission(mission);
        //System.out.println("Assign the mission " + mission.toString() + " to player " + (pNum+1));
        missionOverview.get(missionOverview.indexOf(mission)).setPlayerNum(pNum);
        //System.out.println("Now the mission is assigned to player " + (mission.getPlayerNum()+1));
        history.missionUpdate(mission, player);
        player++;
        player %= players.length;
    }
    
    /*
    Make player pNum play card card.
    Then update which player has to take their turn.
    
    */
    public void makeMove(int pNum, Card card)
    {
        players[pNum].forcePlayCard(card);
        history.update(card, pNum);
        player++;
        player %= players.length;
    }
    
    /*
    Simulate a game, then return a History object containing the relevant information about it.
    
    */
    public History simulateGame(int maxRounds, Strategy[] strats)
    {
        boolean gameFinished = false;
        //player++;
        //player %= players.length;
        //System.out.println("The current player is" + + "" +);
        reassignStrategy(strats);
        if(trickPhase)
        {
            //System.out.println("The lead player is: " + leadPlayer.getPlayNum());
            //System.out.println("The current player is: " + player);
            //System.out.println("We start a trick phase");
            int check = trick();
            //System.out.println("We end a trick phase");
            if(check == 1)
            {
                return history;
            }
            round++;
            //System.out.println("We start a communication phase");
            gameFinished = communication(maxRounds);
            //System.out.println("We finish a communication phase");
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
            //System.out.println("start the remainder of the game");
            gameStart(maxRounds);
            //System.out.println("finished the remainder of the game");
        }
        //System.out.println("Calculate the results");
        results();
        //System.out.println("Finished calculating the results");
        return history;
    }
    
    /*
        Start with performing a communication phase then loop a trick phase followed by a communication phase
        until either the communication phase function signals that the game has come to an end,
        or you determine that the result of the game is already known
        (if (at least) one mission cannot be completed anymore, or all missions already have been completed)
    */
    public void gameStart(int maxRounds)
    {
        if(feedback)
        {
            ioManager.commPhase();
        }
        //System.out.println("We start a communication phase");
        boolean gameFinished = communication(maxRounds);
        //System.out.println("We finish a communication phase");
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
                System.out.println("The currect round is " + round);
                ioManager.commPhase();
            }
            //System.out.println("We start a communication phase");
            gameFinished = communication(maxRounds);
            //System.out.println("We finish a communication phase");
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

    /*
        Return true if and only if all of the missions have been completed succesfully.
        
    */
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
    
    /*
        Changes the game state to a communication state,
        then proceed to ask all players, starting with the lead, to communicate.
        continue to do this until all players have refused (in a row).
        Then return true if and only if the last player in the array (who will always have the smallest hand)
        has run out of cards.
    */
    private boolean communication(int maxRounds) 
    {
        if(players[players.length-1].handEmpty())
        {
            return true;
        }
        if(round > maxRounds)
        {
           return true; 
        }
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
                history.communicationMade(players[player].getComm(), round, player);
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

    /*
        Changes the game state to a trick state,
        then proceed to ask all players, starting with the lead, to communicate.
        continue to do this until all players have refused (in a row).
        Then return true if and only if the last player in the array (who will always have the smallest hand)
        has run out of cards.
    */
    private int trick() 
    {
        trickPhase = true;
        Card[] cardsPlayed = new Card[players.length];
        //System.out.println("(");
        for(int i = 0; i < players.length; i++)
        {
            if(players[i].getCardPlayed() != null)
            {
                cardsPlayed[i] = players[i].getCardPlayed();
                for(Player p: players)
                {
                    p.updateInfoCard(players[i], cardsPlayed[i]);
                }
                //System.out.println("there will be a mismatch");
            }
        }
        //debug info
        //System.out.println("The lead player is: " + leadPlayer.getPlayNum());
        //System.out.println("The current player is: " + player);
        while(!cardsPlayed())
        {
            cardsPlayed[player] = players[player].playCard(leadPlayer.getPlayNum(), players);
            history.update(cardsPlayed[player], player);
            if(cardsPlayed[player].equals(new Card(Color.BLACK, 9)))
            {
                return 1;
            }
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
            //System.out.println("there was no card uncovered");
        }
        //System.out.println(")");
        
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
        
        return 0;
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
    
    public boolean quickFail(Card move, int playerNumber) 
    {
        LinkedList<Card> cardsOnTheTable = new LinkedList();
        for(Player p: players)
        {
            if(p.getCardPlayed() != null)
            {
                cardsOnTheTable.add(p.getCardPlayed());
            }
        }
        
        if(playerNumber == leadPlayer.getPlayNum())
        {
            
        }
        else if(leadPlayer.getCardPlayed() == null)
        {
            return false;
        }
        else
        {
            Color leadColor = leadPlayer.getCardPlayed().getSuit();
            for(Card c: cardsOnTheTable)
            {
                if(isInAMission(c))
                {
                    if(playerHasMission(c, players[playerNumber]))
                    {
                        return !winningCard(leadColor, move);
                    }
                    else
                    {
                        return winningCard(leadColor, move);
                    }
                }
            }
            
            for(Player p: players)
            {
                if(playerHasMission(move, p))
                {
                    if(p.getCardPlayed() != null)
                    {
                        players[playerNumber].setCardPlayed(move);
                        boolean wins = winningCard(leadColor, p.getCardPlayed());
                        players[playerNumber].removeCardPlayed();
                        return !wins;
                    }
                    else
                    {
                        Card strongest = move;
                        for(Card c: cardsOnTheTable)
                        {
                            if(c.getValue() > move.getValue() && (c.getSuit() == Color.BLACK || c.getSuit() == leadColor))
                            {
                                if((c.getSuit() == Color.BLACK && strongest.getSuit() != Color.BLACK) || (c.getSuit() == leadColor && strongest.getSuit() != leadColor && strongest.getSuit() != Color.BLACK))
                                {
                                    strongest = c;
                                }
                            }
                        }
                        return !couldWin(strongest, leadColor);
                    }
                }
            }

        }
        
        
        return false;
    }
    
    /*
    Returns a boolean that answers the question "Can card c still be won?"
    
    */
    public boolean couldWin(Card c, Color leadColor)
    {
        int cardPosibilities = 13;
        if(leadColor == Color.BLACK)
        {
            cardPosibilities = 4;
        }
        LinkedList<Card> strongerCards = new LinkedList<Card>();
        Color[] colors = {leadColor, Color.BLACK};
        for(int i = 0; i < cardPosibilities; i++)
        {
            strongerCards.add(new Card(colors[i/9],(i%9)+1));
        }
        
        LinkedList<Card> tooWeak = new LinkedList();
        for(Card card: strongerCards)
        {
            if(card.getValue() <= c.getValue() && card.getSuit() != Color.BLACK)
            {
                tooWeak.add(card);
            }
        }
        strongerCards.removeAll(tooWeak);
        strongerCards.removeAll(history.getCardsPlayed());
        
        return !strongerCards.isEmpty();
    }
    
    public boolean winningCard(Color leadingColor, Card card)
    {
        Card winningCard;
        if(card.getSuit() == leadingColor || card.getSuit() == Color.BLACK)
        {
            winningCard = card;
        }
        else
        {
            return false;
        }
        
        LinkedList<Card> cardsPlayed = new LinkedList();
        for(Player p: players)
        {
            if(p.getCardPlayed() != null)
            {
                cardsPlayed.add(p.getCardPlayed());
            }
        }
        
        for(Card c: cardsPlayed)//y9,y3,y5
        {
            if(c.getSuit() == Color.BLACK)
            {
                if(c.getValue() > winningCard.getValue() || winningCard.getSuit() != Color.BLACK)
                {
                    return false;
                }
            }
            else if(c.getSuit() == leadingColor)
            {
                if(c.getValue() > winningCard.getValue() && winningCard.getSuit() != Color.BLACK)
                {
                    return false;
                }
            }
        }
        return true;
    }
    
    public boolean isInAMission(Card c)
    {
        for(Player p: players)
        {
            if(playerHasMission(c,p))
            {
                return true;
            }
        }
        return false;
    }
    
    public boolean playerHasMission(Card c, Player p)
    {
        for(Mission m: p.getPersonalMissions())
        {
            if(m.getMissionCard().equals(c))
            {
                return true;
            }
        }
        return false;
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
            missions.add(new Mission(deck.remove(r.nextInt(36-i)), MissionOrder.values()[conditions[i]]));
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
    
    public void simulateDevideNormally(LinkedList<Mission> missions, Strategy[] strats)
    {
        LinkedList<Mission> copyMissions = new LinkedList();
        
        for(Mission m: missions)
        {
            copyMissions.add(missionOverview.get(missionOverview.indexOf(m)));
        }        
        this.reassignStrategy(strats);
        while(!missions.isEmpty())
        {
            //System.out.println("The previous number should not be 0");
            missions = players[player].chooseMission(copyMissions, ioManager, players);
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
    
    public Mission getNextMission()
    {
        if(preSelectedMissions.isEmpty())
        {
            return new Mission(new Card(Color.BLACK, 7), MissionOrder.NONE);
        }
        else
        {
            return preSelectedMissions.remove();
        }
    }
    
    public Card getNextMove()
    {
        if(preSelectedMoves.isEmpty())
        {
            return new Card(Color.BLACK, 7);
        }
        else
        {
            return preSelectedMoves.remove();
        }
    }
    
    public LinkedList<Mission> getMissionOverview()
    {
        return missionOverview;
    }
    
    public int getPlayer()
    {
        return player;
    }
    
    public History getHistory()
    {
        return history;
    }
    
    public void setGame(GameController g)
    {
        for(Player p: players)
        {
            p.setGame(g);
        }
    }
    
    public void setLeadPlayer()
    {
        for(Player p: players)
        {
            if(p.isCaptain())
            {
                leadPlayer = p;
            }
        }
    }
    
    public void setPreSelectedMissions(LinkedList<Mission> missions)
    {
        this.preSelectedMissions = missions;
    }
    
    public void setPreSelectedMoves(LinkedList<Card> moves)
    {
        this.preSelectedMoves = moves;
    }
    
    public void setHands(LinkedList<LinkedList<Card>> hands)
    {
        for(int i = 0; i < players.length; i++)
        {
            players[i].setHand(hands.get(i));
        }
    }
    
    public void reassignStrategy(Strategy[] newStrats)
    {
        for(int i = 0; i < players.length; i++)
        {
            players[i].setStrategy(newStrats[i]);
        }
    }
}
