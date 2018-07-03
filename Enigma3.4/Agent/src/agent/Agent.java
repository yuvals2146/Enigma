package agent;

import components.machine.api.EnigmaMachine;
import components.machine.secret.Secret;
import manager.Instruction;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

public class Agent implements Runnable
{
    private final int id;
    private final int missionSize;
    private EnigmaMachine myMachine;
    private final Set<String> dictionary;
    private String encryptedMsg;
    protected BlockingQueue<Secret> missionsQueue;
    protected BlockingQueue<List<DecodedMsg>> correctlyDecodedCandidate;
    private Instruction instruction;

    private long HowManyMission = 0;

    public Agent(BlockingQueue<Secret> in, BlockingQueue<List<DecodedMsg>> out, int missionSize, int id,
                 EnigmaMachine enigmaMachine, String encryptedMsg, Set<String> dictionary, Instruction instruction){
        this.missionsQueue = in;
        this.correctlyDecodedCandidate = out;
        this.id = id;
        this.missionSize = missionSize;
        this.encryptedMsg = encryptedMsg;
        this.dictionary = dictionary;
        myMachine = new EnigmaMachine(enigmaMachine); //copy const'
        this.instruction = instruction;
    }

    @Override
    public void run() {
            Secret currentSecret = null;
            Thread.currentThread().setName("agent"+id);

        do {
                int missionsCount = 0;
                boolean continueWithMission;
                String currentDecryptedMsg;
                //System.out.println"agent #" + id + " is about to consume item");
                try {
                    currentSecret = missionsQueue.take();
                    ++HowManyMission;
                } catch (InterruptedException e) {
                    if(interruptAction())
                        return;
                }
            //System.out.println"agent #" + id + " consumed item: " + currentSecret.toString());

                if(currentSecret != null && currentSecret.isEndOfMissions() != true) {

                    List<DecodedMsg> res = new ArrayList<>();
                    myMachine.setMachineSecret(currentSecret);

                    do{
                        myMachine.convertSecretToString();
                        try {
                            myMachine.resetMachine();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                        currentDecryptedMsg = myMachine.process(encryptedMsg);
                       // System.out.println("agent #"+ id + "is checking secret: "+ myMachine.getCurrentSecret().toString());

                        if(decMsgMakeSense(currentDecryptedMsg)){//need to adjust to capital letter!
                           res.add(new DecodedMsg(currentDecryptedMsg,id,myMachine.getCurrentSecret().toString()));
                        }
                        continueWithMission = myMachine.getCurrentSecret().nextMissionForAgentsAndDM(myMachine.getABC().length(),1);
                    } while(++missionsCount<missionSize && continueWithMission == true);


                    if(res.size()>0){
                        try {
                            correctlyDecodedCandidate.put(new ArrayList<>(res));
                        } catch (InterruptedException e) {
                            if(interruptAction())
                                return;
                        }
                    }
                }

            } while (currentSecret.isEndOfMissions() != true);
        --HowManyMission;
        try {
            correctlyDecodedCandidate.put(new ArrayList<>(Arrays.asList(new DecodedMsg(true,HowManyMission,id))));// end of job flag for agent
        } catch (InterruptedException e) {
            System.out.println(Thread.currentThread().getName() +" Was interrupted!");

        }

        //System.out.println"agent #" + id + " is going to die byeeeee");//put -1 no need for is alive..
    }
    private boolean interruptAction(){
        //System.out.println(Thread.currentThread().getName() +" Was interrupted!");
        if (instruction.getInstraction() == Instruction.options.EXIT) {
            //System.out.println(Thread.currentThread().getName() +" Was exit!");
            return true;
        }
        else if(instruction.getInstraction() == Instruction.options.INFO) {
            System.out.println("Agent id: " + id + "\nWorking on secret: " + myMachine.getCurrentSecret().toString());
            instruction.updateNumOfAgents();
        }

//        else
//            System.out.println(Thread.currentThread().getName() +" Was continue!");
        return false;
    }

    private boolean decMsgMakeSense(String currentDecryptedMsg) {

       // try {
          //  Writer out = new BufferedWriter(
                  //  new OutputStreamWriter(
                          //  new FileOutputStream("decoded.txt" , true)));


        String[] arrayOfWords = currentDecryptedMsg.split(" ");
        //out.write("Checking Message: " + currentDecryptedMsg + "\n");
        //out.write("Dictionary size: " + dictionary.size()+ "\n");
        //out.write("arrayOfWord Length: " + arrayOfWords.length+ "\n");
        for(int i=0; i < arrayOfWords.length ; i++){
            if(!dictionary.contains(arrayOfWords[i])){
                //out.write("Result false for : " + arrayOfWords[i]+ "\n");
               // out.flush();
                return false;
            }
        }
        //out.write("Result True"+ "\n");
           // out.flush();
        return true;
    //    } catch (FileNotFoundException e) {
    //        e.printStackTrace();
    //        return false;
  //  } catch (IOException e) {
     //       e.printStackTrace();
     //       return false;
    //    }

    }

}
