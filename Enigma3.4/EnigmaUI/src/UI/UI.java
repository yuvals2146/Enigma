package UI;

import builder.DecryptionManagerBuilder;
import components.machine.api.EnigmaMachine;
import factory.EnigmaComponentFactory;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Scanner;

public class UI {
    static EnigmaMachine theMachine = null;
    static boolean secretCreated = false;
    static int userInput;
    static Scanner scanner = new Scanner(System.in);

        public static void main(String[] argv)
        {
            while(true){
                try{
                    if(theMachine == null)
                        theMachine = initEnigmaMachine();

                    printFullMenu();
                    generalMain();

                }
                catch (ClassNotFoundException e) {
                    System.out.println(e.getMessage()+"\n");
                }
                catch (NumberFormatException e) {
                    if(theMachine == null)
                        System.out.println("Please insert a number between 1 to 3.\n");
                    else
                    System.out.println("Please insert a number between 1 to 11.\n");
                }
                catch (IOException e) {
                    System.out.println(e.getMessage()+"\n");
                }
                catch (JAXBException e) {
                    System.out.println(e.getMessage()+"\n");
                }
                catch (IllegalArgumentException e){
                    System.out.println(e.getMessage()+"\n");
                }
            }
        }

        private static EnigmaMachine initEnigmaMachine() throws IOException, NumberFormatException, JAXBException, ClassNotFoundException {
            while(true) {

                System.out.println("1. Load machine from XML file\n" +"2. Load machine from file\n"+ "3. Exit");
                    userInput = Integer.parseInt(scanner.nextLine());
                switch (userInput) {
                    case 1:
                        System.out.println("Please enter xml file path:");
                        EnigmaMachine res = EnigmaComponentFactory.INSTANCE.buildMachine(scanner.nextLine());
                        System.out.println("Machine has been uploaded successfully.\n");
                        return res;
                    case 2:
                        System.out.println("Please enter path to file to read:");
                        res = EnigmaComponentFactory.INSTANCE.fromFileToEnigma(scanner.nextLine());
                        System.out.println("Machine uploaded successfully\n");
                        return res;
                    case 3:
                        System.exit(0);
                        break;

                }
            }
        }
        private static void generalMain() throws IOException, NumberFormatException, JAXBException, IllegalArgumentException, ClassNotFoundException {
            userInput = Integer.parseInt(scanner.nextLine());
                switch (userInput) {
                    case 1:
                        System.out.println("Please enter xml file path:");
                        theMachine = EnigmaComponentFactory.INSTANCE.buildMachine(scanner.nextLine());
                        System.out.println("Machine has been uploaded successfully.\n");
                        break;
                    case 2:
                        theMachine.displayMachineCurrentState();
                        break;
                    case 3:
                        theMachine.setCurrentSecret(true);
                       // theMachine.resetMachine();
                        System.out.println("Machine initialized successfully.\n");
                        secretCreated = true;
                        break;
                    case 4:
                        theMachine.setCurrentSecret(false);
                       // theMachine.resetMachine();
                        System.out.println("Machine initialized successfully.\n");
                        secretCreated = true;
                        break;
                    case 5:
                        if(secretCreated)
                            System.out.println(theMachine.processFromUser()); //Get from user
                        else throw new IOException("Error: Initialize machine state first\n");
                        break;
                    case 6:
                        if(secretCreated)
                            theMachine.resetMachine();
                        else throw new IOException("Error: Initialize machine state first\n");
                        break;
                    case 7:
                        theMachine.displayStatistics();
                        break;
                    case 8:
                        if(secretCreated)
                            DecryptionManagerBuilder.INSTANCE.startDecryptionProcess(theMachine);
                        else throw new IOException("Error: Initialize machine state first\n");
                        break;
                    case 9:
                        if(secretCreated)
                        EnigmaComponentFactory.INSTANCE.fromEnigmaToFile(theMachine);
                        else throw new IOException("Error: Initialize machine state first\n");
                        break;
                    case 10:
                        System.out.println("Please enter path to file to read:");
                        theMachine = EnigmaComponentFactory.INSTANCE.fromFileToEnigma(scanner.nextLine());
                        System.out.println("Machine uploaded successfully\n");
                        break;
                    case 11:
                        System.out.println("YALlA BYEEEEEEEEEEEEEEE");
                        System.exit(0);
                        break;
                }


        }

        private static void printFullMenu()
        {
            System.out.println("1. Load new machine from XML file\n"+
                    "2. Display machine state\n"+
                    "3. Initialize machine state manually\n"+
                    "4. Initialize machine state randomly\n"+
                    "5. Process string\n"+
                    "6. Reset machine to base mode\n"+
                    "7. Display statistics\n"+
                    "8. Start decryption process\n"+
                    "9. Save current machine state to file\n"+
                    "10. Load machine from file\n" +
                    "11. Exit");
        }

}
