/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package thecrew;
import java.util.LinkedList;
import java.lang.Enum;
import java.util.Random;

/**
 *
 * @author Iwan382
 */
public class Player {
    
    private LinkedList<Card> hand = new LinkedList();
    private LinkedList<Mission> personalMissions = new LinkedList();
    private LinkedList<History> simulationStorage = new LinkedList();
    private double[][][] cardInfo;  //player, color, number
    private Card[][] winPile;
    private Card cardPlayed;
    private Communication comm;
    private Controller controller;
    private int playNum;
    private int nOfPlayers;
    private Random r = new Random();
    private boolean hasCommunicated = false;
    private boolean feedback = false;
    private GameController game;
    private int hasMadeDecision = 0; //0 communicates that no decision was made yet, 
                                     //1 communicates that a decision was made to communicate,
                                     //2 communicates that a decision was made to not communicate.
    
    public Player(LinkedList<Card> h, int players, int pn, Controller c, Communication comm, Card played, double[][][] info, GameController contr, Card[][] winnings, boolean hasComm, LinkedList<Mission> pMissions)
    {
        for(Card card: h)
        {
            hand.add(card);
        }
        personalMissions = new LinkedList();
        for(Mission m: pMissions)
        {
            personalMissions.add(m);
        }
        
        cardPlayed = played;
        this.comm = comm;
        controller = c;
        playNum = pn;
        nOfPlayers = players;
        hasCommunicated = hasComm;
        hasMadeDecision = 0;
        game = contr;
        
        winPile = new Card[winnings.length][winnings[0].length];
        for(int i = 0; i < winnings.length; i++)
        {
            for(int j = 0; j < winnings[0].length; j++)
            {
                winPile[i][j] = winnings[i][j];
            }
        }
        
        cardInfo = new double[info.length][info[0].length][info[0][0].length];
        for(int i = 0; i < info.length; i++)
        {
            for(int j = 0; j < info[0].length; j++)
            {
                for(int k = 0; k < info[0][0].length; k++)
                {
                    cardInfo[i][j][k] = info[i][j][k]; 
                }
            }
        }
        
    }
    
    
    
    public Player(LinkedList<Card> h, int players, int pn, Strategy s, IOInterface io, boolean feed, GameController contr)
    {
        controller = new Controller(s, io);
        hand = h;
        feedback = feed;
        h.remove(null);
        winPile = new Card[40/players][players];
        playNum = pn;
        game = contr;
        cardInfo = new double[players][5][9];
        for(int i = 0; i < players; i++)
        {
            for(int j = 0; j < 5; j++)
            {
                for(int k = 0; k < 9; k++)
                {
                    if(playNum == i)
                    {
                        if(hand.contains(new Card(Color.values()[j],k+1)))
                        {
                            cardInfo[i][j][k] = 1.00;
                        }
                        else
                        {
                            cardInfo[i][j][k] = 0.00;
                        }
                    }
                    else
                    {
                        if(hand.contains(new Card(Color.values()[j],k+1)))
                        {
                            cardInfo[i][j][k] = 0.00;
                        }
                        else
                        {
                            if(j != 4 || (k+1) < 5)
                            {
                                cardInfo[i][j][k] = 1.00/(players-1);
                            }
                            else
                            {
                                cardInfo[i][j][k] = 0.00;
                            }
                        }
                    }
                }
            }
        }
        nOfPlayers = players;
    }
    
    /*
    Input: The information about the current game state
    Internal changes: if the player decides to communicate, the comm, hasCommunicated and hasMadeDecision variables are updates
    Output: a boolean that communicates if this player has decided to communicate
    */
    public int makeCommDecision(int leadPLayer, Player[] players)
    {
        if(hasCommunicated)
        {
            //System.out.println("Already communicated");// debug info
            hasMadeDecision = 2;
            return 2;
        }
        else
        {
            LinkedList<Communication> communication = legalComm();
            Communication wantComm = controller.chooseCommunication(communication, players, playNum);//put decision function here, where there is true
            
            //System.out.println("Choose a communication"); //debug info
            if(wantComm != null)
            {
                comm = wantComm; //replacement for decision making function
                hasCommunicated = true;
                hasMadeDecision = 1;
                return 1;
            }
            else
            {
                hasMadeDecision = 2;
                return 2;
            }
        }
    }
    
    public boolean lastPredictionEqual(History general, History simulated, int moveNumber)
    {
        int i = 0;
        if(general.getCardsPlayed().size() > simulated.getCardsPlayed().size())
        {
            return false;
        }
        while(moveNumber > i && general.getPlayerActing().get(moveNumber-i) != playNum)
        {
            if(!general.getCardsPlayed().get(moveNumber-i).equals(simulated.getCardsPlayed().get(moveNumber-i)))
            {
                return false;
            }
            i++;
        }
        
        return general.getCardsPlayed().get(moveNumber-i).equals(simulated.getCardsPlayed().get(moveNumber-i));
    }
    
    public Card simulateGame(GameController contr, LinkedList<Card> moves, int simulations, Strategy[] strats)
    {
        GameController game;
        int moveNumber = contr.getHistory().getPlayerActing().size();
        System.out.println("There have been: " + moveNumber + " moves made");
        double[] successes = new double[moves.size()];
        int[] attempts = new int[moves.size()];
        int[] timesWon = new int[moves.size()];
        // prune storage
        
        LinkedList<History> wrongSimulations = new LinkedList();
        for(History h: simulationStorage)
        {
            if(moveNumber-1 >= contr.getHistory().getPlayerActing().size() )//debug info
            {
                System.out.println("how");
            }
            if(!lastPredictionEqual(contr.getHistory(), h, (moveNumber-1)))
            {
                wrongSimulations.add(h);
            }
        }
        simulationStorage.removeAll(wrongSimulations);
        for(int i = 0; i < simulations; i++)
        {
            game = contr.cloneGameController();
            game.makeMove(playNum, moves.get(i % moves.size()));
            History results = game.simulateGame(40/nOfPlayers, strats);
            simulationStorage.add(results);
            
        }
        
        for(History h: simulationStorage)
        {
            int i = moves.indexOf(h.getCardsPlayed().get(moveNumber));
            if(i == -1)
            {
                System.out.println("Something went wrong this move:");
                System.out.println(h.getCardsPlayed().get(moveNumber));
                System.out.println("is not present here:");
                for(Card c: moves)
                {
                    System.out.println(c);
                }
                System.out.println("The complete history:");
                System.out.println(h.toString());
            }
            if(h.getFinished() && h.getSuccess())
            {
                timesWon[i]++;
            }
            attempts[i]++;
        }
        
        for(int i = 0; i < successes.length; i++)
        {
            successes[i] = (double) timesWon[i] / (double) attempts[i];
        }
        int highestPosition = 0;
        double highest = 0.00;
        
        for(int i = 0; i < moves.size(); i++)
        {
            if(successes[i] > highest)
            {
                highest = successes[i];
                highestPosition = i;
            }
        }
        
        
        ///*
        if(feedback)
        {
            for(int i = 0; i < moves.size(); i++)
            {
                System.out.println("Playing a " + moves.get(i) + " won a total of " + successes[i] + " times");
            } 
        }
        
        
        
        //*/
        
        return moves.get(highestPosition);
    }
    
    public LinkedList<Communication> legalComm() 
    {
        LinkedList<Communication> communication = new LinkedList();
        for(Card c: hand)
        {
            if(c.getSuit() == Color.BLACK)
            {
                
            }
            else
            {
                boolean largest = true, smallest = true;
                for(Card h: hand)
                {
                    if(h.getSuit() == c.getSuit())
                    {
                        if(h.getValue() <= c.getValue())
                        {
                            largest &= true;
                        }
                        else
                        {
                            largest = false;
                        }
                    }
                    
                    if(h.getSuit() == c.getSuit())
                    {
                        if(h.getValue() >= c.getValue())
                        {
                            smallest &= true;
                        }
                        else
                        {
                            smallest = false;
                        }
                    }
                }
                //System.out.println("A Communication added");//debug info                  
                if(largest && smallest)
                {
                    
                    communication.add(new Communication(c, Position.MIDDLE));
                }
                else if(largest)
                {
                    communication.add(new Communication(c, Position.TOP));
                }
                else if(smallest)
                {
                    communication.add(new Communication(c, Position.BOTTOM));
                }
            }
        }
        
        return communication;
    }

    public void updateInfoCard(Player p, Card c)
    {
        for(int i = 0; i < nOfPlayers; i++)
        {
            int j = c.getSuit().ordinal(), k = c.getValue();
            if(p.getPlayNum() == i)
            {
                cardInfo[i][j][k-1] = 1.00;
            }
            else
            {
                cardInfo[i][j][k-1] = 0.00;
            }
        }
    }
    
    
    
    public void updateInfoCommunication(Player player, Communication comm)
    {
        Card c = comm.getCard();
        Position pos = comm.getPos();
        int j = c.getSuit().ordinal(), k = c.getValue();
        for(int i = 0; i < nOfPlayers; i++)
        {
            if(player.getPlayNum() == i)
            {
                cardInfo[i][j][k-1] = 1.00;
                if(pos.ordinal() >= 1)
                {
                    for(int n = 1; n < k; n++)
                    {
                        if(cardInfo[i][j][n-1] != 1.00)
                        {
                            if(cardInfo[i][j][n-1] != 0.00)
                            {
                                cardInfo[i][j][n-1] = 0.00;
                                int eligiblePlayers = 0;
                                for(int m = 0; m < nOfPlayers; m++)
                                {
                                    if(cardInfo[m][j][n-1] != 0.00 && cardInfo[m][j][n-1] != 1.00)
                                    {
                                        eligiblePlayers++;
                                    }
                                    
                                }
                                for(int m = 0; m < nOfPlayers; m++)
                                {
                                    if(cardInfo[m][j][n-1] != 0.00 && cardInfo[m][j][n-1] != 1.00)
                                    {
                                        cardInfo[m][j][n-1] = 1.00/eligiblePlayers;
                                    }
                                }
                            }
                            
                        }
                    }
                }
                
                if(pos.ordinal() <= 1)
                {
                    for(int n = 9; n > k; n--)
                    {
                        if(cardInfo[i][j][n-1] != 1.00)
                        {
                            if(cardInfo[i][j][n-1] != 0.00)
                            {
                                cardInfo[i][j][n-1] = 0.00;
                                int eligiblePlayers = 0;
                                for(int m = 0; m < nOfPlayers; m++)
                                {
                                    if(cardInfo[m][j][n-1] != 0.00 && cardInfo[m][j][k-1] != 1.00)
                                    {
                                        eligiblePlayers++;
                                    }
                                    
                                }
                                for(int m = 0; m < nOfPlayers; m++)
                                {
                                    if(cardInfo[m][j][n-1] != 0.00 && cardInfo[m][j][n-1] != 1.00)
                                    {
                                        cardInfo[m][j][n-1] = 1.00/eligiblePlayers;
                                    }
                                }
                            }
                            
                        }
                    }
                }
            }
        }
    }
    
    public boolean isCaptain() 
    {
        for(Card c: hand)
        {
            
            if(c.getValue() == 4 && c.getSuit() == Color.BLACK)
            {
                return true;
            }
            //System.out.println(c.toString());
        }
        return false;
    }
    
    public boolean handEmpty()
    {
        return hand.isEmpty();
    }
    
    public Card playCard(int leadPlayer, Player[] players) 
    {
        LinkedList<Card> moves = legalMoves(leadPlayer, players);
        
        //System.out.println(game); //debug info
        Card playing = controller.chooseCard(moves, players, playNum, game);
        
        cardPlayed = hand.remove(hand.indexOf(playing));
        return playing;
    }
    
    public void forcePlayCard(Card c)
    {
        cardPlayed = hand.remove(hand.indexOf(c));
    }
    
    public LinkedList<Card> legalMoves(int leadPlayer, Player[] players)
    {
        boolean noLeadColor = true;
        if(leadPlayer == playNum)
        {
            return hand;
        }
        else
        {
            LinkedList<Card> legalMoves = new LinkedList();
            for(Card c: hand)
            {
                if(c.getSuit() == Color.BLACK || c.getSuit() == players[leadPlayer].getCardPlayed().getSuit())
                {
                    legalMoves.add(c);
                    if(c.getSuit() == players[leadPlayer].getCardPlayed().getSuit())
                    {
                        noLeadColor = false;
                    }
                }
            }
            
            if(noLeadColor)
            {
                return hand;
            }
            
            return legalMoves;
        }
        
    }
    
    public LinkedList<Mission> chooseMission(LinkedList<Mission> missions, IOInterface f, Player[] players)
    {
        Mission m = controller.chooseMission(missions, players, playNum);
        personalMissions.add(m);
        m.setPlayerum(playNum);
        if(feedback)
        {
            f.missionSelected(m, playNum);
        }
        
        return missions;
    }
    
    public Card getCardPlayed()
    {
        return cardPlayed;
    }
    
    public Communication getComm()
    {
        return comm;
    }
    
    public int getPlayNum()
    {
        return playNum;
    }
    
    public int getDecision()
    {
        return hasMadeDecision;
    }

    public Card[][] getWinPile()
    {
        return winPile;
    }
    
    public LinkedList<Card> getHand()
    {
        return hand;
    }
    
    public void removeCardPlayed()
    {
        cardPlayed = null;
    }
    
    public void setDecision(int d)
    {
        hasMadeDecision = d;
    }
    
    public void setStrategy(Strategy s)
    {
        //System.out.println("The strategy of player: " + this + " is being set to " + s); //debug info
        controller.setStrategy(s);
    }
            
    public void setWinPile(int i, Card[] winnings)
    {
        winPile[i] = winnings;
    }
    
    public double[][][] getGameInfo()
    {
        return cardInfo;
    }
    
    public Player clonePlayer(GameController contr)
    {
        return new Player(hand, nOfPlayers, playNum, controller.cloneController(), comm, cardPlayed, cardInfo, contr, winPile, hasCommunicated, personalMissions);
    }
    
    public void CleanUp()
    {
        
    }
    
}
