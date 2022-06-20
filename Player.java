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
    private LinkedList<Task> personalTasks = new LinkedList();
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
    
    public Player(LinkedList<Card> h, int players, int pn, Controller c, Communication comm, Card played, double[][][] info, GameController contr, Card[][] winnings, boolean hasComm, LinkedList<Task> pTasks)
    {
        for(Card card: h)
        {
            hand.add(card);
        }
        personalTasks = new LinkedList();
        for(Task m: pTasks)
        {
            personalTasks.add(m);
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
        controller = new Controller(s, io, contr);
        hand = h;
        feedback = feed;
        hand.remove(null);
        //System.out.println("The hand size of player " + pn + " is " + hand.size());
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
            Communication wantComm = controller.chooseCommunication(communication, players, playNum);
            
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
        for(int j = 0; j < general.getTasksChosen().size(); j++)
        {
            Task task1 = general.getTasksChosen().get(j);
            Task task2 = simulated.getTasksChosen().get(j);

            if(!task1.equals(task2) || task1.getPlayerNum() != task2.getPlayerNum())
            {
                return false;
            }
        }
        
        if(moveNumber >= 0)
        {
            //System.out.println("We are now primarily concerned with cards palyed");
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
        else
        {
            return true;
        }
        
        
        
    }
    
    public Task simulateGameSetUp(GameController contr, LinkedList<Task> tasks, int simulations, Strategy[] strats, boolean honest)
    {
        assert(tasks.size() != 0);
        GameController game;
        int moveNumber = contr.getHistory().getPlayerActing().size();
        double[] successes = new double[tasks.size()];
        int[] attempts = new int[tasks.size()];
        int[] timesWon = new int[tasks.size()];
        for(int i = 0; i < simulations; i++)
        {
            game = contr.cloneGameController();
            game.setGame(game);
            if(honest)
            {
                LinkedList<LinkedList<Card>> hands = createLegalHands(game.getHistory());
                game.setHands(hands);


            }
            //System.out.println("This should be equal, to this");
            //System.out.println(tasks.size());
            Task chosen = tasks.remove(i % tasks.size());
            game.chooseTask(playNum, chosen);
            game.simulateDevideNormally(tasks, strats);
            tasks.add(i % (tasks.size()+1), chosen);
            //System.out.println(tasks.size());
            //for(Task m : game.getTaskOverview())
            //{
            //    System.out.println((m.getPlayerNum()+1));
            //}
            History results = game.simulateGame(40/nOfPlayers, strats);
            simulationStorage.add(results);
        }
        
        
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
        
        
        
        int tasksChosen = contr.getTaskOverview().size() - tasks.size();
        //System.out.println(tasks.size());
        for(History h: simulationStorage)
        {
            int i = tasks.indexOf(h.getTasksChosen().get(tasksChosen));
            if(i == -1)
            {
                System.out.println("Something went wrong this task:");
                System.out.println(h.getTasksChosen().get(tasksChosen));
                System.out.println("is not present here:");
                for(Task m: tasks)
                {
                    System.out.println(m);
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
        
        if(feedback)
        {
            for(int i = 0; i < tasks.size(); i++)
            {
                System.out.println("Choosing task " + tasks.get(i) + " won an estimated " + successes[i]*100 + "% of the times");
            } 
        }
        
        double highest = successes[0];
        for(int i = 0; i < tasks.size(); i++)
        {
            if(successes[i] > highest)
            {
                highest = successes[i];
                highestPosition = i;
            }
        }
        
        return tasks.get(highestPosition);
    }
    
    public Card simulateGame(GameController contr, LinkedList<Card> moves, int simulations, Strategy[] strats, boolean honest)
    {
        GameController game;
        //System.out.println("currect players turn");
        //System.out.println(contr.getPlayer());
        int moveNumber = contr.getHistory().getPlayerActing().size();
        if(feedback)
        {
            System.out.println("There have been: " + moveNumber + " moves made");
        }
        double[] successes = new double[moves.size()];
        int[] attempts = new int[moves.size()];
        int[] timesWon = new int[moves.size()];
        // prune storage
        //LinkedList<Card> handSave = (LinkedList<Card>) hand.clone();
        
        
        if(honest)
        {
            double[][][] succesfullCards = new double[nOfPlayers][5][9];
            double[][][] attemptCards = new double[nOfPlayers][5][9];
            for(int j = 0; j < nOfPlayers; j++)
            {
                for(int k = 0; k < 5; k++)
                {
                    for(int n = 0; n < 9; n++)
                    {
                        succesfullCards[j][k][n] = 0;
                        attemptCards[j][k][n] = 0;
                    }
                }
            }
            for(int i = 0; i < (simulations/moves.size()); i++)
            {
                game = contr.cloneGameController();
                game.setGame(game);
                LinkedList<LinkedList<Card>> hands = createLegalHands(new History());
                game.setHands(hands);
                //game.setLeadPlayer();
                Strategy[] s = new Strategy[]{Strategy.SETLIST, Strategy.SETLIST, Strategy.SETLIST, Strategy.SETLIST};
                if(strats[0] == Strategy.RANDOM)
                {
                    s = new Strategy[]{Strategy.SETLIST, Strategy.SETLIST, Strategy.SETLIST, Strategy.SETLIST};
                }
                else if(strats[0] == Strategy.NOTCOMPLETELYINCOMPETENT)
                {
                    s = new Strategy[]{Strategy.SETLISTNOTDUMB, Strategy.SETLISTNOTDUMB, Strategy.SETLISTNOTDUMB, Strategy.SETLISTNOTDUMB};
                }
                else if(strats[0] == Strategy.SMART)
                {
                    s = new Strategy[]{Strategy.SETLISTSMART, Strategy.SETLISTSMART, Strategy.SETLISTSMART, Strategy.SETLISTSMART};
                }
                
                game.setPreSelectedMoves(game.getHistory().getCardsPlayed());
                game.setPreSelectedTasks(game.getHistory().getTasksChosen());
                
                //System.out.println("Start a history check");
                //System.out.println("Check the history, player " + game.getPlayer() + " will make the first move");
                boolean succesfull = game.historyCheck(s);
                //System.out.println("History check is finished");
                
                Color[] colors = {Color.BLUE, Color.GREEN, Color.YELLOW, Color.PINK, Color.BLACK};
                
                for(int j = 0; j < nOfPlayers; j++)
                {
                    for(int k = 0; k < 5; k++)
                    {
                        for(int n = 0; n < 9; n++)
                        {
                            if(playNum != j && cardInfo[j][k][n] != 1.00 && cardInfo[j][k][n] == 0.00 && hands.get(j).contains(new Card(colors[k], n)))
                            {
                                if(succesfull)
                                {
                                    succesfullCards[j][k][n] += 1;
                                }
                                attemptCards[j][k][n] += 1;
                            }
                        }
                    }
                }
                
            }
            
            for(int j = 0; j < nOfPlayers; j++)
            {
                for(int k = 0; k < 5; k++)
                {
                    for(int n = 0; n < 9; n++)
                    {
                        if(cardInfo[j][k][n] != 1.00 && cardInfo[j][k][n] != 0.00)
                        {
                            cardInfo[j][k][n] = (succesfullCards[j][k][n] + 1.00)/(succesfullCards[j][k][n] + attemptCards[j][k][n] + 2.00);
                        }
                    }
                }
            }
        }
        
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
            //System.out.println("Before");
            //System.out.println(contr.getPlayer());
            game = contr.cloneGameController();
            
            if(honest)
            {
                LinkedList<LinkedList<Card>> hands = createLegalHands(game.getHistory());
                
                /* debug information
                if(i == 0)
                {
                    System.out.println("{");
                    for(LinkedList<Card> h: hands)
                    {
                        System.out.println("{");
                        for(Card c: h)
                        {
                            System.out.println(c.toString());
                        }
                        System.out.println("}");
                    }
                    System.out.println("}");
                }
                */
                game.setHands(hands);
                /* debug info
                System.out.println("THese are the hands that we pass on");
                System.out.println("(");
                for(LinkedList<Card> ha: hands)
                {
                    System.out.println("(");
                    for(Card h: ha)
                    {
                        System.out.println(h);
                    }
                    System.out.println(")");
                }
                System.out.println(")");
                */
            }
            /* //debuginfo
            System.out.println("EverAfter");
            System.out.println(playNum);
            System.out.println(moves.get(i % moves.size()));
            
            System.out.println("rigdiugnaijgnadig");
            for(Card h: hand)
            {
                System.out.println(h);
            }
            */
            //System.out.println("After");
            //System.out.println(game.getPlayer());
            game.makeMove(playNum, moves.get(i % moves.size()));
            /* //debuginfo
            System.out.println("EverEverAfter");
            for(Card h: hand)
            {
                System.out.println(h);
            }
            System.out.println("Once?");
            
            System.out.println(game.getPlayer());
            */
            History results = game.simulateGame(40/nOfPlayers, strats);
            simulationStorage.add(results);
            
        }
        
        for(History h: simulationStorage)
        {
            if(moveNumber >= h.getCardsPlayed().size())
            {
                System.out.println("FATAL ERROR");
                System.out.println("Something went wrong this number:");
                System.out.println(moveNumber);
                System.out.println("is not present here:");
                for(Card c: moves)
                {
                    System.out.println(c);
                }
                System.out.println("The complete history:");
                System.out.println(h.toString());
                System.out.println("Now we give the context one layer up: ");
                System.out.println(contr.getHistory().toString());
                
            }
            else
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
                System.out.println("Playing a " + moves.get(i) + " won an estimated " + successes[i]*100 + "% of the times");
            } 
        }
        //*/
        
        return moves.get(highestPosition);
    }
    
    public LinkedList<LinkedList<Card>> createLegalHands(History h)
    {
        LinkedList<LinkedList<Card>> hands = new LinkedList<LinkedList<Card>>();
        LinkedList<Card> deck = new LinkedList<Card>();
        
        for(int i = 0; i < nOfPlayers; i++)
        {
            hands.add(new LinkedList<Card>());
        }
        
        Color[] colors = {Color.BLUE, Color.GREEN, Color.YELLOW, Color.PINK, Color.BLACK};
        int value = 1, j = 0;
        for(int i = 0; i < 40; i++)
        {
            deck.add(new Card(colors[j], value));
            if(value==9)
            {
                j++;
            }
            value = value % 9 + 1;
        }
        LinkedList certainCards = new LinkedList<Card>();
        
        //Assign the cards we know the owner of
        for(Card c: deck)
        {
            for(int i = 0; i < nOfPlayers; i++)
            {
                if(cardInfo[i][c.getSuit().ordinal()][c.getValue()-1] == 1)
                {
                    hands.get(i).add(c);
                    certainCards.add(c);
                }
            }
        }
        deck.removeAll(certainCards);
        //System.out.println("We devided the certain cards");
        int thisPlayer = playNum-1;
        //boolean[] handFull = new boolean[nOfPlayers];
        //for(int i = 0; i < nOfPlayers; i++)
        //{
        //    handFull[i] = i == thisPlayer;
        //}
        //int maxHandSize = 40/nOfPlayers;
        
        //Assign the cards that cannot be owned by all the other players
        certainCards = new LinkedList<Card>();
        for(Card c: deck)
        {
            if(notEveryone(c))
            {
                int candidate;
                do
                {
                    candidate = r.nextInt(nOfPlayers-1);
                    //System.out.println("Selected a candidate");
                }
                while(maxHandSizeExeeded(candidate, hands.get(candidate)));
                hands.get(candidate).add(c);
                certainCards.add(c);

            }
        }
        deck.removeAll(certainCards);
        //System.out.println("We have assigned all cards that cannot be owned by everyone");
        
        //assign the rest of the cards
        for(int i = 0; i < nOfPlayers; i++)
        {
            while(!deck.isEmpty())
            {
                Card next = deck.remove(r.nextInt(deck.size()));
                int playerTarget = 0;
                Color c = next.getSuit();
                int valueNext = next.getValue();
                double[] playerOdds = new double[nOfPlayers];
                double combinedOdds = 0.00;
                for(int n = 0; n < nOfPlayers; n++)
                {
                    playerOdds[n] = cardInfo[n][c.ordinal()][valueNext-1];  
                    combinedOdds += playerOdds[n];
                }
                
                for(int n = 0; n < nOfPlayers; n++)
                {
                    playerOdds[n] = playerOdds[n]/combinedOdds;
                }
                double ranNum  = r.nextDouble();
                combinedOdds = 0.00;
                do
                {
                    
                    combinedOdds += playerOdds[playerTarget];
                    //System.out.println("The new combined Odds");
                    //System.out.println(combinedOdds);
                    playerTarget++;
                }
                while(combinedOdds < ranNum);
                
                boolean assigned = false;
                while(!assigned)
                {
                    playerTarget = playerTarget % nOfPlayers;
                    if(!maxHandSizeExeeded(playerTarget, hands.get(playerTarget)))
                    {
                        hands.get(playerTarget).add(next);
                        assigned = true;
                    }
                    else
                    {
                        playerTarget++;
                    }
                
                
                }
            }
        }
        
        
        //remove the cards that are already played
        for(Card c: h.getCardsPlayed())
        {
            for(int i = 0; i < nOfPlayers; i++)
            {
                hands.get(i).remove(c);
            }
        }
        
        /*//debug info
        System.out.println("(");
        for(double[][] i: cardInfo)
        {
            System.out.println("(");
            for(double[] ar: i)
            {
                System.out.println("(");
                for(double a: ar)
                {
                    System.out.print(a + ", ");
                }
                System.out.println(")");
            }
            System.out.println(")");
        }
        System.out.println(")");
        
        System.out.println("(");
        for(LinkedList<Card> list: hands)
        {
            System.out.println("(");
            for(Card c: list)
            {
                System.out.print(c.toString() + ", ");
            }
            System.out.println(")");
        }
        System.out.println(")");
        */
        
        
        return hands;
    }
    
    public boolean maxHandSizeExeeded(int player, LinkedList<Card> hand)
    {
        int bigger = 0; int extra = 40%nOfPlayers;
        if(extra > player)
        {
            bigger = 1;
        }
        // debug info
        //System.out.println("Player " + (player+1) + " has a hand size of: " + (40/nOfPlayers+bigger) + " Extra is equal to " + extra);
        return (hand.size()+1) > (40/nOfPlayers + bigger);
    }
    
    public boolean notEveryone(Card c)
    {
        for(int i = 0; i < nOfPlayers; i++)
        {
            if(i != playNum)
            {
                if(0 == cardInfo[i][c.getSuit().ordinal()][c.getValue()-1])
                {
                    return true;
                }
            }
        }
        
        return false;
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
        if(communication.size() == 0)
        {
            System.out.println("qwe");
            System.out.println(hand.size());
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
    
    public Task bestTask(LinkedList<Task> tasks)
    {
        int i = 0;
        double[] values = new double[tasks.size()];
        
        for(Task m: tasks)
        {
            values[i] = calculateSuccesValue(m);
            i++;
        }
        
        i = 0;
        int j = 0;
        for(double n: values)
        {
            if(n > values[i])
            {
                i = j;
            }
            j++;
        }
        
        return tasks.get(i);
    }
    
    public double calculateSuccesValue(Task m)
    {
        int value = 0;
        int higher = 0;
        int lower = -1;
        if(hand.contains(m))
        {
            value += m.getTaskCard().getValue();
            
            for(Card c: hand)
            {
                if(m.getTaskCard().getSuit() == c.getSuit())
                {
                    if(m.getTaskCard().getValue() < c.getValue())
                    {
                        higher++;
                    }
                    else
                    {
                        lower++;
                    }
                }
            }
            value += higher;
            value += lower*0.5;
        }
        else
        {
            value += (9-m.getTaskCard().getValue());
            
            for(Card c: hand)
            {
                if(m.getTaskCard().getSuit() == c.getSuit())
                {
                    if(m.getTaskCard().getValue() < c.getValue())
                    {
                        higher++;
                    }
                }
            }
            
            value *= (0.5 + (1+higher/(10-m.getTaskCard().getValue())));
            //the amount of those you have/amount of cards above it
            
        }
        
        return value;
    }
    
    public Communication bestCommunication(LinkedList<Communication> communications)
    {
        int i = 0;
        double[] values = new double[communications.size()];
        
        for(Communication c: communications)
        {
            values[i] = calculateGain(c);
            //System.out.println("We have communication " + c.toString() + " and we assign it the value" + values[i]);
            i++;
        }
        
        i = 0;
        int j = 0;
        for(double n: values)
        {
            if(n > values[i])
            {
                i = j;
            }
            j++;
        }
        
        return communications.get(i);
    }
        
    public double calculateGain(Communication c)
    {
        int value = 0;
        
        if(c.getPos() == Position.MIDDLE)
        {
            value = 8;
        }
        else if(c.getPos() == Position.BOTTOM)
        {
            value = c.getCard().getValue()-1;
        }
        else
        {
            value = 9 - c.getCard().getValue();
        }
        
        LinkedList<Task> tasks = game.getTaskOverview();
        
        for(Task m: tasks)
        {
            if(m.getTaskCard().getSuit() == c.getCard().getSuit())
            {
                value *= (1 + 1/tasks.size());
            }
        }
        
        return value;
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
        if(cardPlayed != null)
        {
            return cardPlayed;
        }
        LinkedList<Card> moves = legalMoves(leadPlayer, players);
        
        //System.out.println(game); //debug info
        Card playing = controller.chooseCard(moves, players, playNum, game);
        if(hand.indexOf(playing) == -1)
        {
            System.out.println("The card " + playing + " was not in our (player " + (playNum+1) + "\'s) hand");
            return playing;
        }
        cardPlayed = hand.remove(hand.indexOf(playing));
        return playing;
    }
    
    public void forcePlayCard(Card c)
    {
        if(hand.indexOf(c) == -1)
        {
            System.out.println(c);
            
            System.out.println("player number");
            System.out.println(playNum);
            
            System.out.println("Oi, we (player " + playNum + " dont have card " + c.toString());
            for(Card h: hand)
            {
                System.out.println(h);
            }
            System.out.println("We do have these cards");
            return;
        }
        
        cardPlayed = hand.remove(hand.indexOf(c));
    }
    
    public void forceChooseTask(Task m)
    {
        personalTasks.add(m);
        m.setPlayerNum(playNum);
    }
    
    public LinkedList<Card> legalMoves(int leadPlayer, Player[] players)
    {
        assert(!hand.isEmpty());
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
                //System.out.println("Player " + leadPlayer + " is the lead player, but now player " + playNum + " wants to play a card");
                //System.out.println("but the lead player played " + players[leadPlayer].getCardPlayed());//debug info
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
            assert(!legalMoves.isEmpty());
            return legalMoves;
        }
        
    }
    
    public LinkedList<Task> chooseTask(LinkedList<Task> tasks, IOInterface f, Player[] players)
    {
        Task m = controller.chooseTask(tasks, players, playNum, game);
        personalTasks.add(m);
        game.getHistory().taskUpdate(m, playNum);
        //System.out.println("Now we assign the task " + m + " to player " + (playNum+1));
        m.setPlayerNum(playNum);
        if(feedback)
        {
            f.taskSelected(m, playNum);
        }
        
        return tasks;
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
    
    public void setCardPlayed(Card c)
    {
        cardPlayed = c;
    }
    
    public void setGame(GameController game)
    {
        this.game = game;
        this.controller.setGame(game);
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
    
    public void setHand(LinkedList<Card> hand)
    {
        this.hand = new LinkedList<Card>();
        for(Card c: hand)
        {
            this.hand.add(new Card(c.getSuit(), c.getValue()));
        }
        
    }
    
    public LinkedList<Task> getPersonalTasks()
    {
        return personalTasks;
    }
    
    public double[][][] getGameInfo()
    {
        return cardInfo;
    }
    
    public Player clonePlayer(GameController contr)
    {
        return new Player(hand, nOfPlayers, playNum, controller.cloneController(), comm, cardPlayed, cardInfo, contr, winPile, hasCommunicated, personalTasks);
    }
   
    
}
