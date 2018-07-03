package manager;

import agent.Agent;
import agent.DecodedMsg;
import builder.DecryptionManagerBuilder;
import components.machine.api.EnigmaMachine;
import components.machine.secret.Secret;
import components.reflector.Reflector;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class DecryptionManager //implements Runnable
{
    private EnigmaMachine myMachine;
    private Difficulty myDiff;

    private final boolean END_OF_MISSIONS = true;
    private final int queueSize;
    private final int numAgents;
    private final int missionSize;
    private final double allOptionalSecrets;

    private double decryptionProgress = 0;

    private List<DecodedMsg> allPossibleDecodings;
    private List<List<Integer>> allRotorsPermutations;
    private List<Thread> theAgents;
    private List<Reflector.Id> allPossibleReflectors;
    private LocalDateTime start;
    private int numOfActiveAgents;

    protected BlockingQueue<Secret> missionsQueue;
    protected BlockingQueue<List<DecodedMsg>> correctlyDecodedCandidate;

    Thread agentsOperator , decodedMassagesManager;
    Instruction instruction = new Instruction(Instruction.options.PLAY);
    boolean pause = false;
    List<DecodedMsg> endList;

    public enum Difficulty {
        EASY(1), MEDIUM(2), HARD(3), NOT_COOL_MAN(4);
        private final int val;

        Difficulty(int val) {
            this.val = val;
        }

        public int getValue() {
            return val;
        }
    }

    public DecryptionManager(DecryptionManagerBuilder builder){
        this.myDiff = builder.getMyDiff();
        this.myMachine = builder.getMyMachine();
        this.numAgents = builder.getNumAgents();
        this.missionSize = builder.getMissionSize();
        this.allOptionalSecrets = builder.getAllOptionalSecrets();
        //this.queueSize = 2430000;//for test
        this.queueSize = this.numAgents * 3; //should make cool function that finds the ultimate queue size depending on the mission
        this.allPossibleReflectors = builder.getAllPossibleReflectors();
        this.allRotorsPermutations = builder.getAllRotorsPermutations();
        this.start = LocalDateTime.now();



        this.missionsQueue = new ArrayBlockingQueue<>(queueSize);
        this.correctlyDecodedCandidate = new ArrayBlockingQueue<>(queueSize);

        this.allPossibleDecodings = new ArrayList<>();
        //setAllReflectorAndRotorsByDifficulty(Difficulty.values()[this.myDiff.getValue()-1]);

        theAgents = new ArrayList<>(this.numAgents);
        int idCounter = 0;
        for (int i = 0; i < this.numAgents; i++) { //maybe outside? less readable..?
            theAgents.add(new Thread(new Agent(missionsQueue, correctlyDecodedCandidate, missionSize, idCounter++,
                    myMachine, builder.getEncryptedMessage(), builder.getDictionary(),instruction)));
        }
    }


    public void runDecryptionManager() {

        //Thread.currentThread().setName("DecFuckingManager");
        //System.out.println("In Thread " + Thread.currentThread().getName() + "and im about to fuck shit up");

        //this thread is a "agents operator", his only task is to hand out missions
        this.agentsOperator = new  Thread(() -> {
            Thread.currentThread().setName("agentsOperator");
            final String threadName = Thread.currentThread().getName();


            for (List<Integer> rotorsPermutation : allRotorsPermutations) {
                for (Reflector.Id refId : allPossibleReflectors) {
                    Secret initMissionSecret = initCurrentSecretSession(rotorsPermutation, refId);
                    myMachine.setMachineSecret(initMissionSecret);
                    do {
                        try {
                            myMachine.convertSecretToString();
                            missionsQueue.put(new Secret(myMachine.getCurrentSecret()));
                            updateMissionsProgress();
                        } catch (InterruptedException e) {

                            //System.out.println("agentsOperator Was interrupted! ");
                            if (instruction.getInstraction() == Instruction.options.EXIT) {
                                //  System.out.println("agentsOperator Was exit! ");
                                return;
                            }

//                                    else
//                                        System.out.println("agentsOperator Was continue! ");
                        }

                    }
                    while (myMachine.getCurrentSecret().nextMissionForAgentsAndDM(myMachine.getABC().length(), missionSize));
                }
            }
            //System.out.println(threadName + " terminating. Producing EOM");
            try {
                for (int i = 0; i < numAgents; i++) {

                    missionsQueue.put(new Secret(END_OF_MISSIONS));
                }
            } catch (InterruptedException e) {
                //System.out.println("agentsOperator Was interrupted! ");
                if (instruction.getInstraction() == Instruction.options.EXIT) {
                    //  System.out.println("agentsOperator Was exit! ");
                    return;
                }
//                else
//                    System.out.println("agentsOperator Was continue!");

            }

        });
        agentsOperator.start();

       // activating all the agents!
        for (Thread agentThread : theAgents)
            agentThread.start();

        //activate the deciphered massages manager thread
        this.decodedMassagesManager = new Thread(() -> {

            numOfActiveAgents = numAgents;
            Thread.currentThread().setName("decodedMassagesManager");
            final String threadName = Thread.currentThread().getName();
            List<DecodedMsg> checkInfo = new ArrayList<>();
            endList = new ArrayList<>();
                do {
                    try {
                        checkInfo.clear();
                        //System.out.println("Thread " + threadName + " is about to consume item");

                        checkInfo.addAll(correctlyDecodedCandidate.take());

                        if (checkInfo.get(0).agentRetired() == true) {
                            numOfActiveAgents--;
                            endList.add(checkInfo.get(0));
                        } else {
                            allPossibleDecodings.addAll(checkInfo); //and need to update the current state function!
                        }

                        //System.out.println("Thread " + threadName + " succeeded consuming items ");
                    }catch (InterruptedException e) {
                      //  System.out.println("decodedMassagesManager Was interrupted!");
                        if (instruction.getInstraction() == Instruction.options.EXIT) {
                           // System.out.println("decodedMassagesManager Was exit!");
                            return;
                        }
                    }
                } while (numOfActiveAgents != 0);//someone is alive..

            //System.out.println("Thread " + threadName + " has finished his job");
            //allPossibleDecodings.forEach(DecodedMsg::print);
            displayCurrentDecryptionStatus(true);
        });
        decodedMassagesManager.start();
    
        //Menu
        Scanner scanner = new Scanner(System.in);
        while (agentsOperator.isAlive() || decodedMassagesManager.isAlive()){
            menu();
            try{

                Integer result = Integer.parseInt(scanner.nextLine());
                switch (result) {
                    case 1:
                        displayCurrentDecryptionStatus(false);
                        break;
                    case 2:
                        playOrPauseDecryption();
                        break;
                    case 3:
                        stopProcess();
                        return;
                    default:
                        System.out.println("Please enter number between 1 to 3");
                        break;

                }
            }
            catch (NumberFormatException e) {
                System.out.println("Please enter number between 1 to 3");
            }

        }


            


        //System.out.println("in Thread " + Thread.currentThread().getName() + " and ive finished my fucking job\n");
    }

    private void menu(){
        System.out.println("1. Get decryption status\n" +
        "2. Play/Pause decryption process\n" +
        "3. Stop decryption process");
    }

    private void stopProcess(){
        instruction.setInstruction(Instruction.options.EXIT);
        if(agentsOperator.isAlive())
            agentsOperator.interrupt();
        if(decodedMassagesManager.isAlive())
            decodedMassagesManager.interrupt();
        for (Thread agentThread : theAgents) {
            if (agentThread.isAlive())
                agentThread.interrupt();
        }
        System.out.println("Decryption process stopped");
        displayCurrentDecryptionStatus(false);
    }

    private void playOrPauseDecryption() {
        if(pause) {
            instruction.setInstruction(Instruction.options.PLAY);
            System.out.println("Decryption process continue");
        }
        else{
            instruction.setInstruction(Instruction.options.PAUSE);
                System.out.println("Decryption process paused");
            if(agentsOperator.isAlive())
                agentsOperator.interrupt();
            if(decodedMassagesManager.isAlive())
                decodedMassagesManager.interrupt();
            for (Thread agentThread : theAgents) {
                if (agentThread.isAlive())
                    agentThread.interrupt();
            }

            }
            pause = !pause;

    }

    private Secret initCurrentSecretSession(List<Integer> rotorsPermut, Reflector.Id refId)
    {
        return new Secret(rotorsPermut,new ArrayList<Integer>(Collections.nCopies(rotorsPermut.size(), 0)),refId);
        //would rather do it through the builder with the unused function buildForDM but couldnt make it work..;/
    }




    public void displayCurrentDecryptionStatus(boolean isOperationOver) {
        if (isOperationOver == true) {
            System.out.println("**Final decryption status**");
            System.out.println("Number of mission that completed: " + (long)this.allOptionalSecrets/missionSize);
            System.out.println("Decryption duration: " + calcTime());
            printAgents();
        } else {
            System.out.println("*Current decryption status*");
            System.out.println("Decryption progress is at: " + (int)(decryptionProgress*100) + "%");
            Double approximateMissionsPerAgent = ((allOptionalSecrets * (1 - decryptionProgress) )/missionSize  / numAgents);
            System.out.println("Approximate missions per agent: " + approximateMissionsPerAgent.intValue());
            System.out.println("Decryption duration: " + calcTime());
            instruction.setInstruction(Instruction.options.INFO);
            instruction.setNumOfActive(numOfActiveAgents);
            for (Thread agent :theAgents
                 ) {
                if(agent.isAlive()){
                    agent.interrupt();

                }
            }
            instruction.waitForAgents();

        }
        if(allPossibleDecodings.size() > 0) {
            System.out.println("The candidates for the correct decoding are:");
            int j = 1;
            List<DecodedMsg> toPrint;
            if (!isOperationOver)
            {
                if (allPossibleDecodings.size() > 10)
                    toPrint = new ArrayList<>(allPossibleDecodings.subList(allPossibleDecodings.size() - 11, allPossibleDecodings.size() - 1));
                else
                    toPrint = new ArrayList<>(allPossibleDecodings);
            for (DecodedMsg msg : toPrint
                    ) {
                System.out.print(j++ + ".");
                msg.print();
            }
        }
        else
            {
             for (DecodedMsg msg : allPossibleDecodings
                        ) {
                    System.out.print(j++ + ".");
                    msg.print();
                }
            }

        }
        else
            System.out.println("There is no candidates for decoding");
    }

    private void printAgents(){
        System.out.println("The participants agents:");
        Collections.sort(endList);
        for (DecodedMsg msg:endList
             ) {
            msg.print();
        }
    }

    private String calcTime(){
        int second = LocalDateTime.now().getSecond() - start.getSecond();
        int min = LocalDateTime.now().getMinute() - start.getMinute();
        int hour = LocalDateTime.now().getHour()- start.getHour();
        String time = "";
        if (second < 0){
            second += 60;
            min -=1;
        }
        if(min < 0){
            min += 60;
            hour -= 1;
        }
        if(hour > 0) {
            if(hour < 10)
                time += "0";
            time += String.valueOf(hour) + ":";

        }
        if(min < 10)
            time += "0";
        time += String.valueOf(min) + ":";
        if(second < 10)
            time += "0";
        time += String.valueOf(second);
        return time;
    }
    private void updateMissionsProgress(){ decryptionProgress += (double)(missionSize/allOptionalSecrets);}


}
