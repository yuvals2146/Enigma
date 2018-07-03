package builder;

import components.machine.api.EnigmaMachine;
import components.reflector.Reflector;
import components.rotor.Rotor;
import factory.EnigmaComponentFactory;
import jaxb.schema.generated.Decipher;
import jaxb.schema.generated.Enigma;
import manager.DecryptionManager;

import javax.xml.bind.JAXBException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public final class DecryptionManagerBuilder {
    public static EnigmaMachine getMyMachine() {
        return myMachine;
    }

    private static EnigmaMachine myMachine = null;
    private static int numAgents;
    private static Decipher xmlDecipher = null;
    private static String excludeChars;
    private static Set<String> dictionary;
    private static int missionSize;
    private static DecryptionManager.Difficulty myDiff = null;
    private static String EncryptedMessage;
    private static double  allOptionalSecrets;
    private static List<List<Integer>> allRotorsPermutations;

    public static List<Reflector.Id> getAllPossibleReflectors() {
        return allPossibleReflectors;
    }

    private static List<Reflector.Id> allPossibleReflectors;

    //Getters
    public static double getAllOptionalSecrets() {
        return allOptionalSecrets;
    }

    public static List<List<Integer>> getAllRotorsPermutations() {
        return allRotorsPermutations;
    }
    public static int getNumAgents() {
        return numAgents;
    }
    public static String getExcludeChars() {
        return excludeChars;
    }
    public static Set<String> getDictionary() {
        return dictionary;
    }
    public static int getMissionSize() {
        return missionSize;
    }
    public static DecryptionManager.Difficulty getMyDiff() {
        return myDiff;
    }
    public static String getEncryptedMessage() {
        return EncryptedMessage;
    }

    public static DecryptionManagerBuilder INSTANCE = new DecryptionManagerBuilder();

    private DecryptionManagerBuilder(){}

    public static void setValueFromXml(Decipher xmlDecipher){
        INSTANCE.xmlDecipher = xmlDecipher;
    }

    public static void startDecryptionProcess(EnigmaMachine machine) throws IOException, JAXBException {
        System.out.println("Follow the instruction:");
        DecryptionManager DM = INSTANCE.buildDM(machine);
        if(DM == null){
            System.out.println("Decryption process canceled");
            return;
        }
        DM.runDecryptionManager();
//        Thread DMThread =  new Thread(DM);
//        DMThread.start();

//        try {
           // DMThread.join();
//        while(DMThread.isAlive()){
//            System.out.println("Press 1 for interrupt:");
//            Scanner scanner = new Scanner(System.in);
//            Integer res = Integer.parseInt(scanner.nextLine());
//            if(res == 1)
//                DMThread.interrupt();
//        }

//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        Scanner scanner = new Scanner(System.in);
    //    while (DMThread.isAlive()){}
    }

    public static DecryptionManager buildDM(EnigmaMachine theMachine) throws IOException, JAXBException {
        myMachine = new EnigmaMachine(theMachine);
        getDecipherJaxbElements();
        getMessageToEncrypt();
        getInfoOfDiffAndMissionSize();

        Scanner scanner = new Scanner(System.in);
        boolean start = false;
        do {
            System.out.println("Please select:\n1. Start encryption\n2. Exit");
            try {
                Integer temp = Integer.parseInt(scanner.nextLine());
                if (temp == 1) start = true;
                else if (temp == 2) return null;
            } catch (NumberFormatException e) {
                System.out.println(e.getMessage() + "\nTry again\n");

            }
        }while (!start);
        return create();
    }

    private static void getMessageToEncrypt(){
        boolean validMessage = false;
        Scanner scanner = new Scanner(System.in);
        do{
            System.out.println("Please enter message to encrypt:");
            String msgToEncrypt = scanner.nextLine();
            msgToEncrypt = msgToEncrypt.toUpperCase();
            msgToEncrypt = msgToEncrypt.replaceAll(excludeChars,"");
            String[] words = msgToEncrypt.split(" ");
            validMessage = true;
            for (String word: words
                 ) {
                if(!dictionary.contains(word))
                    validMessage = false;

            }
            if(validMessage){
                EncryptedMessage = myMachine.process(msgToEncrypt);
                System.out.println("Encryption result: " + EncryptedMessage);
            }
            else
                System.out.println("Error: Entered message didn't found in dictionary\nPlease try again\n");

        }while(!validMessage);
    }
    private static DecryptionManager create(){
        return new DecryptionManager(INSTANCE);
    }
    public static void getInfoOfDiffAndMissionSize(){
        Scanner scanner = new Scanner(System.in);
        boolean validDiff = false;
        do {
            System.out.println("Please select difficulty:\n" +
                    "1. Easy - Find the first position of the rotors\n" +
                    "2. Medium - Find the first position of the rotors and the reflector id\n" +
                    "3. Hard - Find the first position of the rotors, their position and the reflector\n" +
                    "4. Impossible - Find the secret");
            try {
                Integer temp = Integer.parseInt(scanner.nextLine());
                if (temp < 5 && temp > 0) {
                    validDiff = true;
                    myDiff = DecryptionManager.Difficulty.values()[Integer.parseInt(temp.toString()) - 1];
                } else
                    System.out.println("Error: selection need to be between 1 to 4\nTry again\n");
            } catch (NumberFormatException e) {
                System.out.println(e.getMessage() + "\nTry again\n");
            }
        }while(!validDiff);
        setAllReflectorAndRotorsByDifficulty();
        allOptionalSecrets = calcTotalMissionSize();
        boolean validMissionSize = false;
        do{
            System.out.println("Please enter mission size smaller then: " + (long)allOptionalSecrets/numAgents);
            try {
                missionSize = Integer.parseInt(scanner.nextLine());
                if(missionSize < allOptionalSecrets/numAgents)
                validMissionSize = true;
                else
                    System.out.println("Mission size must be smaller then: " + allOptionalSecrets/numAgents +"\nTry again\n");
            }
            catch (NumberFormatException e){
                System.out.println(e.getMessage()+"\nTry again\n");
            }
        }
        while (!validMissionSize);


        
            boolean validAgents = false;
            do {
                System.out.println("Please select number of agents between 2 and " + numAgents);
                try {
                    Integer temp = Integer.parseInt(scanner.nextLine());
                    if (temp > 1 && temp <= numAgents) {
                        validAgents = true;
                        numAgents = temp;
                    } else
                       System.out.println("Error: selection need to be between 2 to " + numAgents + "\nTry again\n");
                } catch (NumberFormatException e) {
                    System.out.println(e.getMessage() + "\nTry again\n");
                }
            }while (!validAgents) ;


        }
    public static void getDecipherJaxbElements() throws IOException, JAXBException {
        if(xmlDecipher == null){
            Scanner scanner = new Scanner(System.in);
            String xml = scanner.nextLine();
            System.out.println("Enigma machine was loaded from file\nPlease enter xml file path:\n");
            Path filePath = Paths.get(xml);
            if(!Files.exists(filePath))
                throw new IOException("Error: XML File didn't exist");
            if(!filePath.toString().endsWith(".xml"))
                throw new IOException("Error: File should by in xml type");
            InputStream inputStream = new FileInputStream(xml);
             Enigma xmlEnigma = EnigmaComponentFactory.INSTANCE.deserializeFrom(inputStream);
            xmlDecipher = xmlEnigma.getDecipher();
        }
        if(xmlDecipher.getAgents() < 2 ||xmlDecipher.getAgents() > 50 )
            throw new IOException("Error: XML Decipher agents number need to be between 2 to 50");
        numAgents = xmlDecipher.getAgents();
        excludeChars = "[" +xmlDecipher.getDictionary().getExcludeChars() + "]";
        String tempDictionary = xmlDecipher.getDictionary().getWords().trim();
        tempDictionary = tempDictionary.replaceAll(excludeChars,"");
        tempDictionary = tempDictionary.toUpperCase();
        String[] splited = tempDictionary.split(" ");
        dictionary = new HashSet(Arrays.asList(splited));

    }

    private static double calcTotalMissionSize() {
        if (myDiff == DecryptionManager.Difficulty.EASY) {
            return  Math.pow(myMachine.getABC().length(),myMachine.getRotorsInUse());
        } else if (myDiff == DecryptionManager.Difficulty.MEDIUM) {
            return Math.pow(myMachine.getABC().length(),myMachine.getRotorsInUse()) * myMachine.getTheReflectors().size();
        } else { // diff == NOT COOL or HARD, the difference is by the size of all permutation list size
            return allRotorsPermutations.size() *
                    Math.pow(myMachine.getABC().length(),myMachine.getRotorsInUse())*
                    myMachine.getTheReflectors().size();
        }
    }

    private static void setAllReflectorAndRotorsByDifficulty() {
        allPossibleReflectors = new ArrayList<>();
        allRotorsPermutations = new ArrayList<>();
        switch(myDiff){
            case EASY:
                allRotorsPermutations.add(myMachine.getCurrentSecret().getSelectedRotorsInOrder());
                allPossibleReflectors.add(myMachine.getCurrentSecret().getSelectedReflector());
                break;
            case MEDIUM:
                allRotorsPermutations.add(myMachine.getCurrentSecret().getSelectedRotorsInOrder());
                for(int i = 0; i<myMachine.getTheReflectors().size(); i++)
                    allPossibleReflectors.add(myMachine.getTheReflectors().get(i).getId());
                break;
            case HARD:
                findAllRotorsPermutations(myMachine.getCurrentSecret().getSelectedRotorsInOrder(),
                        allRotorsPermutations,
                        new ArrayList<>(),
                        myMachine.getRotorsInUse());
                for(int i = 0; i<myMachine.getTheReflectors().size(); i++)
                    allPossibleReflectors.add(myMachine.getTheReflectors().get(i).getId());
                break;
            case NOT_COOL_MAN:
                List<Integer> rotorsId = new ArrayList<>();

                for(Rotor rotor: myMachine.getTheRotors())
                    rotorsId.add(rotor.getId());

                findAllRotorsPermutations(rotorsId,
                        allRotorsPermutations,
                        new ArrayList<>(),
                        myMachine.getRotorsInUse());
                for(int i = 0; i<myMachine.getTheReflectors().size(); i++)
                    allPossibleReflectors.add(myMachine.getTheReflectors().get(i).getId());
                break;
        }
    }
    private static void findAllRotorsPermutations(List<Integer> allRotorsAvailable, List<List<Integer>> res, List<Integer> currentRotorsList, int k) {
        if (currentRotorsList.size() == k) {
            res.add(currentRotorsList);
        } else {
            for (Integer i : allRotorsAvailable) {
                List<Integer> lst = new ArrayList<>(k);
                lst.addAll(currentRotorsList);
                lst.add(i);

                List<Integer> allRotorsAvailableNextCall = new ArrayList<>(allRotorsAvailable);
                allRotorsAvailableNextCall.remove(i);

                findAllRotorsPermutations(allRotorsAvailableNextCall, res, lst, k);
            }
        }
    }

}
