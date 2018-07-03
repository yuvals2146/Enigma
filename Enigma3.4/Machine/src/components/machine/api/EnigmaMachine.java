package components.machine.api;

import components.machine.builder.EnigmaMachineBuilder;
import components.machine.secret.Secret;
import components.machine.statistics.Statistics;
import components.reflector.Reflector;
import components.rotor.Rotor;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class EnigmaMachine implements Serializable {
/*
* for testing git commit
* */
    private final String ABC;
    private final int ROTORS_IN_USE;
    private List<Rotor> theRotors;
    private List<Reflector> theReflectors;
    private Secret currentSecret = null;
    private Statistics machineStatistics;//static?

    public EnigmaMachine(EnigmaMachineBuilder builder) {
        ROTORS_IN_USE = builder.rotorsInUse;
        this.ABC = builder.ABC;
        theRotors = builder.theRotors;
        theReflectors = builder.theReflectors;
        machineStatistics = new Statistics();
    }

    public EnigmaMachine(EnigmaMachine otherMachine){
        ABC = otherMachine.ABC;
        ROTORS_IN_USE = otherMachine.ROTORS_IN_USE;
        theRotors = new ArrayList<>();
        for (Rotor r:otherMachine.theRotors) {
            theRotors.add(new Rotor(r));
        }
        //theRotors.addAll(otherMachine.theRotors);
        theReflectors = new ArrayList<>();
        theReflectors.addAll(otherMachine.theReflectors);
        machineStatistics = new Statistics();
        currentSecret = new Secret(otherMachine.getCurrentSecret());
    }

    public class SecretBuilder implements Serializable {
        private List<Integer> selectedRotorsInOrder;
        private List<Integer> selectedRotorsPositions;
        private Reflector.Id selectedReflector = null;

        public SecretBuilder() {
            selectedRotorsInOrder = new ArrayList<>();
            selectedRotorsPositions = new ArrayList<>();
        }

        public Reflector.Id getSelectedReflector() { return selectedReflector; }
        public List<Integer> getSelectedRotorsPositions() { return selectedRotorsPositions;}
        public List<Integer> getSelectedRotorsInOrder() { return selectedRotorsInOrder; }
        public void buildSecretFromInput() {
            boolean validRotorsID = false, validRotorsPositions = false, validReflector = false;
            Scanner scanner = new Scanner(System.in);
            String userInput, rotorsPosition = "";
            List<Integer> secretRotorsID = new ArrayList<>();

            System.out.print("Please insert "+ ROTORS_IN_USE+" rotors ID separated by white space from Current inventory: ");
            theRotors.forEach(r->System.out.print(r.getId()+ " "));
            System.out.println();

            while (validRotorsID == false) {
                userInput = scanner.nextLine();
                String[] strArray = userInput.split(" ");

                if (strArray.length != ROTORS_IN_USE) {
                    System.out.println("Error: Rotors count should be exactly " + ROTORS_IN_USE + " rotors, try again.\n");
                    continue;
                }

                for (String str : strArray) {
                    try {
                        int rotorID = Integer.valueOf(str);

                        if (theRotors.size()<rotorID) {
                            System.out.println("Error: Rotors ID wasn't found in this machine inventory, try again.\n");
                            break;
                        }
                        if (!secretRotorsID.contains(rotorID))
                            secretRotorsID.add(rotorID);
                        else {
                            System.out.println("Error: Using a rotor more than once is impossible, try again.\n");
                            break;
                        }

                    } catch (NumberFormatException e) {
                        System.out.println("Error: Rotors ID is represented as a number, try again.\n");
                        break;
                    }
                }
                if(secretRotorsID.size()==ROTORS_IN_USE) validRotorsID = true;
                else secretRotorsID.clear();
            }

            System.out.println("Insert selected rotors positions without spaces from current machine ABC: "+ABC);
            while (validRotorsPositions == false) {
                userInput = scanner.nextLine();

                if (userInput.length() != ROTORS_IN_USE) {
                    System.out.println("Error: There should be exactly " + ROTORS_IN_USE + " Rotors positions, try again.\n");
                    continue;
                }

                for (Character ch : userInput.toCharArray()) {
                    if (!ABC.contains(ch.toString())) {
                        System.out.println("Error: The character '" + ch + "' doesn't appear in this machine alphabet, try again.\n");
                        break;
                    }
                    else
                        rotorsPosition+=ch;
                }
                if(rotorsPosition.equals(userInput))
                    validRotorsPositions = true;
                else
                    rotorsPosition="";
            }

            System.out.print("Please choose reflector out of current inventory: ");
            theReflectors.forEach(r->System.out.print(r.getId().toString()+" "));
            System.out.println();
            while (validReflector == false) {
                userInput = scanner.nextLine();

                for (Reflector reflector: theReflectors) {
                    if (reflector.getId().toString().equals(userInput)){
                        selectedReflector = reflector.getId();
                        validReflector = true;
                    }
                }

                if (validReflector==false) System.out.println("Error: This reflector ID is invalid, try again.\n");
            }

            selectedRotorsInOrder = secretRotorsID;
            int i = 0;
            for(Character ch: rotorsPosition.toCharArray())
                selectedRotorsPositions.add(theRotors.get(secretRotorsID.get(i++)-1).getInitPositionByChar(ch));

        }
        public void buildRandomSecret() {
            while(selectedRotorsInOrder.size()<ROTORS_IN_USE)
            {
                int randomRotorID = (int)(Math.random()*theRotors.size())+1;//adjusting to 1 base rotors id

                if(selectedRotorsInOrder.contains(randomRotorID))
                    continue;
                else
                    selectedRotorsInOrder.add(randomRotorID);
            }

            for(int i=0; i<ROTORS_IN_USE; i++)
                selectedRotorsPositions.add((int)(Math.random()*ABC.length()));

            selectedReflector = theReflectors.get((int)(Math.random()*theReflectors.size())).getId();
        }

        public Secret create(boolean fromUser) {
            if(fromUser==true) {
                buildSecretFromInput();
            }
            else{
                buildRandomSecret();
            }
            Secret res = new Secret(this);
            machineStatistics.addSecretToStatistics(res.toString());
            return res;
        }
        
    }

    public Secret getCurrentSecret(){return currentSecret;}
    public List<Rotor> getTheRotors(){return theRotors;}
    public List<Reflector> getTheReflectors(){return theReflectors;}
    public String getABC(){return ABC;}// y
    public int getRotorsInUse(){return ROTORS_IN_USE;}// y
    public void resetMachine()throws IOException {
        int i = 0;

        if(currentSecret == null)
            throw new IOException("No secret initialize, Please set secret first.");

        for (int rotorsID : currentSecret.getSelectedRotorsInOrder())
            theRotors.get(rotorsID-1).setShiftCounter(currentSecret.getSelectedRotorsPositions().get(i++));//adjusting to zero based arrayList

    }
    public void displayMachineCurrentState() {
        System.out.println("Current machine state:");
        System.out.println("1. "+ ROTORS_IN_USE+"/"+theRotors.size()+" Rotors in use");
        System.out.println("2. Notches locations are:");
        for(Rotor rotor: theRotors)
            System.out.println("Rotor #"+rotor.getId()+" notch location: "+ rotor.getNotchLocation());
        System.out.println("3. "+theReflectors.size()+ " reflectors");
        System.out.println("4. Number of massages processed in this machine is: "+machineStatistics.getProcessedMessagesNumber());
        if(currentSecret != null) System.out.println("5. Current secret is: "+ currentSecret.toString());
        System.out.println();

    }
    public void displayStatistics(){machineStatistics.printStatistics();}
    public void setCurrentSecret(boolean fromUser){
        currentSecret = new SecretBuilder().create(fromUser);
        convertSecretToString();
        try {
            resetMachine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String processFromUser(){
        Scanner scanner = new Scanner(System.in);
        String input,output;
        boolean validInput = false;

        do {
            System.out.println("Insert string for encryption/decryption");
            input = scanner.nextLine();
            input = input.toUpperCase();
            for (Character ch : input.toCharArray()) {
                if (!ABC.contains(ch.toString())) {
                    System.out.println("Error: The character '" + ch + "' isn't a part of this machine alphaBet.\n" +
                            "Here's a reminder: " + ABC + " , try again.\n");
                    validInput=false;
                    break;
                }
                validInput=true;
            }
        } while(!validInput);
        long start = System.nanoTime();
        output = process(input);
        long elapsedTime = System.nanoTime() - start; //   put outside function for statistics
        machineStatistics.addProcessedString(input,output,elapsedTime);
        return output;
    }

    public String process(String input){
        String output = "";
       // input = input.toUpperCase();
        for(Character ch : input.toCharArray())
        {
            boolean shiftRotorsPosition = true;
            int theWire = ABC.indexOf(ch);

            for (int j = ROTORS_IN_USE - 1; j >= 0; j--) {
                theWire = theRotors
                        .get(currentSecret.getSelectedRotorsInOrder().get(j)-1)//in case theRotors is sorted!!
                        .process(theWire, true, shiftRotorsPosition);
                shiftRotorsPosition = theRotors
                        .get(currentSecret.getSelectedRotorsInOrder().get(j)-1)//same!
                        .isNotchOnPane();
            }

            theWire = theReflectors.get(currentSecret.getSelectedReflector().ordinal()).reflect(theWire);

            for (int j = 0; j < ROTORS_IN_USE; j++)
                theWire = theRotors
                        .get(currentSecret.getSelectedRotorsInOrder().get(j)-1)//same!!!
                        .process(theWire, false, false);
            try {
                output += ABC.charAt(theWire);
            }
            catch (StringIndexOutOfBoundsException e){
                System.out.println("The wire is: " + theWire + "\n For Secret: " + currentSecret.toString());
            }
        }
        return output;
    }

    public void setMachineSecret(Secret secret){
        this.currentSecret.setSecret(secret);

    }
    public void convertSecretToString() {
        String newSecretToString = "";
        newSecretToString += "<";

        for (int i = 0; i < currentSecret.getSelectedRotorsInOrder().size(); i++) {
            newSecretToString += currentSecret.getSelectedRotorsInOrder().get(i);
            if (i != currentSecret.getSelectedRotorsInOrder().size() - 1)
                newSecretToString += ",";
        }

        newSecretToString += "><";

        for (int i = 0; i < currentSecret.getSelectedRotorsPositions().size(); i++) {
            newSecretToString += theRotors.get(currentSecret.getSelectedRotorsInOrder().get(i) - 1)
                    .getCharPositionByInteger(currentSecret.getSelectedRotorsPositions().get(i));
        }
        newSecretToString += "><" + currentSecret.getSelectedReflector().toString() + ">";
        currentSecret.setSecretToString(newSecretToString);
    }
}


