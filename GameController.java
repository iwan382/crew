
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
    private LinkedList<Task> taskOverview = new LinkedList();
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
    private LinkedList<Task> preSelectedTasks = new LinkedList();
    
    private Strategy[] strats;
    private int mNum;
    
    public GameController(int nPlayers, Player[] newPlayers, int round, Player lead, int player, boolean b, LinkedList<Task> overview, History h)
    {
        players = newPlayers;
        for(Player p: players)
        {
            p.setGame(this);
        }
        history = new History(h.getCardsPlayed(), h.getPlayerActing(), h.getTasksChosen(), h.getPlayerChoosing());
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
        taskOverview = overview;
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
    public double runGame(int m)
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
        if(h.getTasksChosen().contains(new Task(new Card(Color.BLACK, 9), TaskOrder.NONE)))
        {
            return false;
        }
        //h = simulateGame(preSelectedMoves.size()/players.length-1, s);
        return !h.getCardsPlayed().contains(new Card(Color.BLACK, 9));
    }
    
    /*
    Simulate a game including the deviding the tasks, then return a History object containing the relevant information about it.
    
    */
    public History simulateGameStart(int maxRounds, Strategy[] strats)
    {
        reassignStrategy(strats);
        simulateDevideNormally(taskOverview, strats);
        gameStart(maxRounds);
        results();
        return history;
    }
    
    /*
    Ensure that the tasks associated with the wanted mission are divided properly
    
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
    Make player pNum add the Task task.
    Then update which player has to take their turn.
    
    */
    public void chooseTask(int pNum, Task task)
    {
        players[pNum].forceChooseTask(task);
        //System.out.println("Assign the task " + task.toString() + " to player " + (pNum+1));
        taskOverview.get(taskOverview.indexOf(task)).setPlayerNum(pNum);
        //System.out.println("Now the task is assigned to player " + (task.getPlayerNum()+1));
        history.taskUpdate(task, player);
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
            for(Task m: taskOverview)
            {
                won &= m.TaskSuccess(players);
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
        (if (at least) one task cannot be completed anymore, or all tasks already have been completed)
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
            for(Task m: taskOverview)
            {
                won &= m.TaskSuccess(players);
                lost |= m.PrematureFailure(players);
            }
            gameFinished |= won;
            gameFinished |= lost;
        }
    }

    /*
        Return true if and only if all of the tasks have been completed succesfully.
        
    */
    public boolean results()
    {
        boolean gameWon = true;
        boolean taskSucces;
        for(Task m: taskOverview)
        {
            taskSucces = m.TaskSuccess(players);
            gameWon &= taskSucces;
            if(taskSucces && feedback)
            {
                ioManager.taskSucces(m, m.getPlayerNum());
            }
            else if(feedback)
            {
                ioManager.taskFailed(m, m.getPlayerNum());
            }
            
        }
        if(feedback)
        {
            ioManager.result(gameWon);
        }
        
        //System.out.println(history.toString());
        
        
        if(taskOverview == null)
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
                if(isInATask(c))
                {
                    if(playerHasTask(c, players[playerNumber]))
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
                if(playerHasTask(move, p))
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
    
    public boolean isInATask(Card c)
    {
        for(Player p: players)
        {
            if(playerHasTask(c,p))
            {
                return true;
            }
        }
        return false;
    }
    
    public boolean playerHasTask(Card c, Player p)
    {
        for(Task m: p.getPersonalTasks())
        {
            if(m.getTaskCard().equals(c))
            {
                return true;
            }
        }
        return false;
    }
    
    public void assignMissions(int mission, LinkedList<Card> taskCards)
    {
        LinkedList<Task> tasks = new LinkedList();
        int conditions[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        switch(mission)
        {
            case 1: 
                tasks.add(new Task(deck.remove(r.nextInt(36)), TaskOrder.NONE));
                divideNormally(tasks);
                break;
            case 2:
                tasks = createTasks(taskCards, 2, conditions);
                divideNormally(tasks);
                break;
            case 4:
                tasks = createTasks(taskCards, 3, conditions);
                divideNormally(tasks);
                break;
            case 10:
                tasks = createTasks(taskCards, 4, conditions);
                divideNormally(tasks);
                break;
            case 42:
                tasks = createTasks(taskCards, 9, conditions);
                divideNormally(tasks);
                break;
            case 47:
                tasks = createTasks(taskCards, 10, conditions);
                divideNormally(tasks);
                break;
            default: 
                break;
        }
    }
    
    public LinkedList<Task> createTasks(LinkedList<Card> cards, int n, int[] conditions)
    {
        LinkedList<Task> tasks = new LinkedList<>();
        for(int i = 0; i < n; i++)
        {
            tasks.add(new Task(deck.remove(r.nextInt(36-i)), TaskOrder.values()[conditions[i]]));
        }
        return tasks;
    }

    public void divideNormally(LinkedList<Task> tasks) 
    {
        taskOverview = new LinkedList();
        taskOverview.addAll(tasks);
        while(!tasks.isEmpty())
        {
            tasks = players[player].chooseTask(tasks, ioManager, players);
            player++;
            player = player % players.length;
        }
    }
    
    public void simulateDevideNormally(LinkedList<Task> tasks, Strategy[] strats)
    {
        LinkedList<Task> copyTasks = new LinkedList();
        
        for(Task m: tasks)
        {
            copyTasks.add(taskOverview.get(taskOverview.indexOf(m)));
        }        
        this.reassignStrategy(strats);
        while(!tasks.isEmpty())
        {
            //System.out.println("The previous number should not be 0");
            tasks = players[player].chooseTask(copyTasks, ioManager, players);
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
        LinkedList<Task> taskCopy = new LinkedList();
        for(int i = 0; i < taskOverview.size(); i++)
        {
            taskCopy.add(taskOverview.get(i).cloneTask());
        }
        
        
        return new GameController(players.length, newPlayers, round, leadPlayer, player, trickPhase, taskCopy, history);
    }
    
    public Task getNextTask()
    {
        if(preSelectedTasks.isEmpty())
        {
            return new Task(new Card(Color.BLACK, 7), TaskOrder.NONE);
        }
        else
        {
            return preSelectedTasks.remove();
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
    
    public LinkedList<Task> getTaskOverview()
    {
        return taskOverview;
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
    
    public void setPreSelectedTasks(LinkedList<Task> tasks)
    {
        this.preSelectedTasks = tasks;
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
