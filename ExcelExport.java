/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package thecrew;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Timer;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 *
 * @author Iwan382
 */
public class ExcelExport 
{
    private String sheetName;
    private int sizeOfData;
    
    public ExcelExport(String s)
    {
        sheetName = s;
    }
    
    
    public void exportHistory(History h, int offSet)
    {
        Workbook workbook;
        Sheet page;
        try
        {
            File xlsxFile = new File("D:/downloads/TheCrewData.xlsx");
            FileInputStream inputStream = new FileInputStream(xlsxFile);

            workbook = WorkbookFactory.create(inputStream);

        }
        catch(Exception e)
        {
            System.out.println("Something went wrong with opening the file");
            return;
        }
        
        if(offSet == 0)
        {
            page = workbook.createSheet(sheetName);
        }
        else
        {
            page = workbook.getSheet(sheetName);
        }
        
        
        
        
        
        
        int nOfPlayers = h.getStartingHands().length;
        sizeOfData = 7+2*nOfPlayers;
        int rowNumber = 1 + sizeOfData*offSet;
        int cellNumber = 0;
        
        
        Card[][] startingHands = h.getStartingHands();
        
        for(Card[] hand: startingHands)
        {
            Row row = page.createRow(rowNumber);
            Cell cell = row.createCell(cellNumber);
            cell.setCellValue("Hand of player " + (rowNumber % sizeOfData)); //setCellFormula for fomules
            cellNumber++;
            for(Card c: hand)
            {
                if(c != null)
                {
                    cell = row.createCell(cellNumber);
                    cell.setCellValue(c.toString()); //setCellFormula for fomules
                    cellNumber++;
                }
            }
            cellNumber = 0;
            rowNumber++;
        }
        
        
        LinkedList<Task> missions = h.getTasksChosen();
        LinkedList<Integer> playerChoice = h.getPlayerChoosing();
        
        for(int i = 0; i < missions.size(); i++)
        {
            Row row = page.getRow(1+nOfPlayers+playerChoice.get(i)+(offSet*sizeOfData));
            if(row == null)
            {
                row = page.createRow(1+nOfPlayers+playerChoice.get(i)+(offSet*sizeOfData));
                Cell c = row.createCell(0);
                c.setCellValue("Tasks of player " + (playerChoice.get(i)+1));
            }
            
            int collumNum = 0;
            for(int j = 0; j <= i; j++)
            {
                if(playerChoice.get(i).equals(playerChoice.get(j)))
                {
                    collumNum++;
                }
            }
            
            Cell c = row.createCell(collumNum);
            c.setCellValue(missions.get(i).toString());
            
        }
        rowNumber = 1+2*nOfPlayers+(offSet*sizeOfData);
        
        Row playerNumbersRow = page.createRow(rowNumber);
        rowNumber++;
        Row cardsPlayedRow = page.createRow(rowNumber);
        rowNumber++;
        
        LinkedList<Integer> playNum = h.getPlayerActing();
        LinkedList<Card> cardPlay = h.getCardsPlayed();
        
        
        Cell cell = playerNumbersRow.createCell(cellNumber);
        cell.setCellValue("Which player played a card"); //setCellFormula for fomules
        cellNumber++;
        for(Integer i: playNum)
        {
            cell = playerNumbersRow.createCell(cellNumber);
            cell.setCellValue(i+1); //setCellFormula for fomules
            cellNumber++;
        }
        cellNumber = 0;
        
        
        cell = cardsPlayedRow.createCell(cellNumber);
        cell.setCellValue("The card that was played"); //setCellFormula for fomules
        cellNumber++;
        for(Card c: cardPlay)
        {
            cell = cardsPlayedRow.createCell(cellNumber);
            cell.setCellValue(c.toString()); //setCellFormula for fomules
            cellNumber++;
        }
        cellNumber = 0;
        
        //Communication
        LinkedList<Communication> comms = h.getCommunicationsMade();
        LinkedList<Integer> rounds = h.getRoundCommunicated();
        LinkedList<Integer> players = h.getPlayerCommunicated();
        
        Row playerRow = page.createRow(rowNumber);
        rowNumber++;
        Row roundRow = page.createRow(rowNumber);
        rowNumber++;
        Row commRow = page.createRow(rowNumber);
        rowNumber++;
        
        Cell playerCell = playerRow.createCell(0);
        Cell roundCell = roundRow.createCell(0);
        Cell commCell = commRow.createCell(0);
        playerCell.setCellValue("The player that communicated");
        roundCell.setCellValue("In which round this occured");
        commCell.setCellValue("What was communicated");
        
        for(int i = 0; i < comms.size(); i++)
        {
           playerCell = playerRow.createCell(i+1);
           roundCell = roundRow.createCell(i+1);
           commCell = commRow.createCell(i+1);
           playerCell.setCellValue((players.get(i)+1));
           roundCell.setCellValue(rounds.get(i));
           commCell.setCellValue(comms.get(i).toString());
           
        }
        
        Row timeRow = page.createRow(rowNumber);
        rowNumber++;
        
        Cell timeTextCell = timeRow.createCell(0);
        timeTextCell.setCellValue("The time this game took in ms");
        Cell timeCell = timeRow.createCell(1);
        timeCell.setCellValue(h.getTimeTaken());
        Cell wonCell = timeRow.createCell(2);
        if(h.success)
        {
            wonCell.setCellValue("and the game was won");
        }
        else
        {
            wonCell.setCellValue("and the game was lost");
        }
        
        FileOutputStream dataStorage;
        try
        {
            dataStorage = new FileOutputStream(new File("D:/downloads/TheCrewData.xlsx"));
            workbook.write(dataStorage);
            dataStorage.close();
        }
        catch(IOException e)
        {
            System.out.println("No data was written as the file was not found");
        }
        
        
    }

}
