package components.machine.statistics;
import components.machine.secret.Secret;
import javafx.util.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Statistics implements Serializable
{
    private int processedMessagesNumber = 0;
    private int currentSecretProcessedMessagesNumber;
    private Long averageProcessingTime = null;
    private List<Pair<String,List<String>>> processedMassagesHistory;

    public Statistics() { processedMassagesHistory = new ArrayList<>(); }
    public int getProcessedMessagesNumber(){return processedMessagesNumber;}
    public void addSecretToStatistics(String currentSecret) {
        currentSecretProcessedMessagesNumber=0;
        processedMassagesHistory.add(new Pair(currentSecret,new ArrayList<>()));
    }
    public void addProcessedString(String from, String to, long nanoSecProcessDuration)
    {
        String newProcessedString = ++currentSecretProcessedMessagesNumber +". <"+from+"> --> <" + to +"> ("+ nanoSecProcessDuration +" nano-seconds)";

        processedMassagesHistory.get(processedMassagesHistory.size()-1).getValue().add(newProcessedString);

        if(processedMessagesNumber==0) {
            processedMessagesNumber++;
            averageProcessingTime = nanoSecProcessDuration;
        }
        else
            averageProcessingTime = ((averageProcessingTime*processedMessagesNumber++)+nanoSecProcessDuration)/processedMessagesNumber;
    }

    public void printStatistics()
    {
        if(processedMassagesHistory.isEmpty())
            System.out.println("No secret was ever loaded to this machine.\n");
        else {
            for(Pair<String,List<String>> pair: processedMassagesHistory) {
                System.out.println(pair.getKey());

                if(pair.getValue().isEmpty())
                    System.out.println("No massage was ever processed in this configuration.\n");
                else {
                    for(String processedString: pair.getValue())
                        System.out.println(processedString);
                }
            }
            if(averageProcessingTime!=null)
                System.out.println("Average processing time is: "+averageProcessingTime+"\n");
            else
                System.out.println("No massages were ever processed in this configuration.");
        }
    }

}
