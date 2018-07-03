package components.rotor;

import javafx.util.Pair;

import java.io.Serializable;
import java.util.ArrayList;

public class Rotor implements Serializable
{
    private final int ROTORS_ID;
    private final int ABC_LEN;
    private final int NOTCH_INIT_POSITION;
    private final char NOTCH_CHAR_POSITION;
    private int shiftCounter; //shiftCounter manages the number of times the notch moved since last settings
    private ArrayList<Pair<Character, Integer>> rotorsInputSide;
    private ArrayList<Pair<Character, Integer>> rotorsOutputSide;

    public Rotor(int _id, int _notch, String from, String to)
    {
        ROTORS_ID = _id;
        NOTCH_INIT_POSITION = _notch - 1;//adjusting to 0 base
        NOTCH_CHAR_POSITION = from.charAt(NOTCH_INIT_POSITION);
        ABC_LEN = from.length();

        rotorsInputSide = new ArrayList<>(ABC_LEN);
        rotorsOutputSide = new ArrayList<>(ABC_LEN);
        for (int i = 0; i < ABC_LEN; i++) {
            rotorsInputSide.add(i, new Pair<>(from.charAt(i), to.indexOf(from.charAt(i))));
            rotorsOutputSide.add(i, new Pair<>(to.charAt(i), from.indexOf(to.charAt(i))));
        }
    }
    public  Rotor (Rotor other){
        ROTORS_ID = other.ROTORS_ID;
        NOTCH_INIT_POSITION = other.NOTCH_INIT_POSITION;
        NOTCH_CHAR_POSITION = other.NOTCH_CHAR_POSITION;
        ABC_LEN = other.ABC_LEN;
        shiftCounter = other.shiftCounter;

        rotorsInputSide = new ArrayList<>(other.rotorsInputSide);
        rotorsOutputSide = new ArrayList<>(other.rotorsOutputSide);
    }

    public void setRotor(char ch) { shiftCounter = rotorsInputSide.indexOf(ch); }
    public char getCharPositionByInteger(int initPosition) {return rotorsInputSide.get(initPosition).getKey(); }
    public int getId() {return ROTORS_ID; }
    public void setShiftCounter(int newSC){shiftCounter=newSC; }
    public char getNotchLocation(){ return NOTCH_CHAR_POSITION; }
    public boolean isNotchOnPane(){return NOTCH_INIT_POSITION == shiftCounter;}
    public int getInitPositionByChar(char initPosition)
    {
        int i=0;
        for(Pair<Character, Integer> pair: rotorsInputSide){
            if(pair.getKey()==initPosition)
                return i;
            else
                i++;
        }
        return Integer.parseInt(null);
    }
    public int process(int position, boolean in, boolean shift)
    {
            if(in)
            {
                if (shift)
                    shiftCounter = ++shiftCounter% ABC_LEN;
                return (rotorsInputSide.get((position+shiftCounter)% ABC_LEN).getValue()-shiftCounter+ABC_LEN)%ABC_LEN;
            }
            else
                return (rotorsOutputSide.get((position+shiftCounter)% ABC_LEN).getValue()-shiftCounter+ABC_LEN)%ABC_LEN;



    }
}

