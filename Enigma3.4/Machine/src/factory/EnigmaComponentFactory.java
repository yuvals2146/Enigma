package factory;

import builder.DecryptionManagerBuilder;
import components.machine.api.EnigmaMachine;
import components.machine.builder.EnigmaMachineBuilder;
import jaxb.schema.generated.Enigma;
import jaxb.schema.generated.Reflector;
import jaxb.schema.generated.Rotor;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public final class EnigmaComponentFactory {

    private final static String JAXB_XML_GENERATED = "jaxb.schema.generated";

    public static EnigmaComponentFactory INSTANCE = new EnigmaComponentFactory();

    public static EnigmaMachine buildMachine(String xml) throws IOException, JAXBException,IllegalArgumentException
    {
        Path filePath = Paths.get(xml);
        if(!Files.exists(filePath))
            throw new IOException("Error: XML File didn't exist");
        if(!filePath.toString().endsWith(".xml"))
            throw new IOException("Error: File should by in xml type");
        InputStream inputStream = new FileInputStream(xml);
        Enigma xmlEnigma = deserializeFrom(inputStream);
        EnigmaMachineBuilder builder = buildMachine(xmlEnigma.getMachine().getRotorsCount(),xmlEnigma.getMachine().getABC());

        for(Rotor r : xmlEnigma.getMachine().getRotors().getRotor()) {
            String  from = "",to = "";
            for(int i = 0 ; i < r.getMapping().size() ; ++i){
                from += r.getMapping().get(i).getRight();//Check!!!
                to += r.getMapping().get(i).getLeft();
            }
            builder.defineRotor(r.getId(), from, to, r.getNotch());
        }

        for(Reflector r : xmlEnigma.getMachine().getReflectors().getReflector()) {
            List<Integer> input = new ArrayList<Integer>(), output = new ArrayList<Integer>();
            for(int i = 0 ; i < r.getReflect().size() ; ++i)
            {
                input.add(r.getReflect().get(i).getInput() - 1);
                output.add(r.getReflect().get(i).getOutput() - 1);
            }
            builder.defineReflector(r.getId(), input, output);
        }
        DecryptionManagerBuilder.INSTANCE.setValueFromXml(xmlEnigma.getDecipher());
        return builder.create();
    }

    public static EnigmaMachineBuilder buildMachine(int rotors_count, String abc) throws IOException {
        return new EnigmaMachineBuilder(rotors_count,abc);
    }

    public static Enigma deserializeFrom(InputStream in) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(JAXB_XML_GENERATED);
        Unmarshaller u = jc.createUnmarshaller();
        return (Enigma) u.unmarshal(in);
    }
    public static void fromEnigmaToFile(EnigmaMachine machine) throws IOException{
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter directory path:");
        ObjectOutputStream out =
                new ObjectOutputStream(
                    new FileOutputStream(scanner.nextLine()));
            out.writeObject(machine);
        out.flush();

    }

    public static EnigmaMachine fromFileToEnigma(String file) throws IOException, ClassNotFoundException {
        DecryptionManagerBuilder.INSTANCE.setValueFromXml(null);
        ObjectInputStream in =
                new ObjectInputStream(
                        new FileInputStream(file));
        return (EnigmaMachine)in.readObject();

    }

}
