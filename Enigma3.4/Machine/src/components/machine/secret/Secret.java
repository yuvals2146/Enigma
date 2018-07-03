package components.machine.secret;

import components.machine.api.EnigmaMachine;
import components.reflector.Reflector;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Secret implements Serializable
{
    private boolean END_OF_MISSIONS;
    private Reflector.Id SELECTED_REFLECTOR;
    private List<Integer> SELECTED_ROTORS_IN_ORDER;
    private String secretToString;
    private List<Integer> selectedRotorsInitPos;

    public void setSecretToString(String secretToString) {
        this.secretToString = secretToString;
    }

    //????????
    public void setSecret(Secret other){
        END_OF_MISSIONS = other.END_OF_MISSIONS;
        SELECTED_ROTORS_IN_ORDER.clear();
        SELECTED_ROTORS_IN_ORDER.addAll(other.SELECTED_ROTORS_IN_ORDER);
        SELECTED_REFLECTOR = other.SELECTED_REFLECTOR;
        selectedRotorsInitPos.clear();
        selectedRotorsInitPos.addAll(other.selectedRotorsInitPos);
        secretToString = other.secretToString;
    }
    public Secret(Secret other){
        END_OF_MISSIONS = other.END_OF_MISSIONS;
        SELECTED_ROTORS_IN_ORDER = new ArrayList<>();
        SELECTED_ROTORS_IN_ORDER.addAll(other.SELECTED_ROTORS_IN_ORDER);
        SELECTED_REFLECTOR = other.SELECTED_REFLECTOR;
        selectedRotorsInitPos = new ArrayList<>();
        selectedRotorsInitPos.addAll(other.selectedRotorsInitPos);
        secretToString = other.secretToString;
    }

    public Secret(boolean isEndOfMissions){
        secretToString = null;
        SELECTED_REFLECTOR = null;
        SELECTED_ROTORS_IN_ORDER = null;
        selectedRotorsInitPos = null;
        END_OF_MISSIONS = isEndOfMissions;
    }
    public Secret(EnigmaMachine.SecretBuilder builder)
    {
        SELECTED_ROTORS_IN_ORDER = builder.getSelectedRotorsInOrder();
        SELECTED_REFLECTOR = builder.getSelectedReflector();
        END_OF_MISSIONS = false;
        selectedRotorsInitPos = builder.getSelectedRotorsPositions();
    }
    public Secret(List<Integer>rotorsInOrder,List<Integer>rotorsPos,Reflector.Id id)
    {
        SELECTED_ROTORS_IN_ORDER = new ArrayList<>();
        SELECTED_ROTORS_IN_ORDER.addAll(rotorsInOrder);
        SELECTED_REFLECTOR = id;
        END_OF_MISSIONS = false;
        selectedRotorsInitPos = new ArrayList<>();
        selectedRotorsInitPos.addAll(rotorsPos);
    }

    public final boolean isEndOfMissions(){return END_OF_MISSIONS;}

    public final List<Integer> getSelectedRotorsInOrder() {return SELECTED_ROTORS_IN_ORDER;}

    public final List<Integer> getSelectedRotorsPositions(){return selectedRotorsInitPos;}

    public final Reflector.Id getSelectedReflector(){return SELECTED_REFLECTOR;}

//    public boolean nextMissionForAgentsAndDM(int countingBase, int missionSize)
//    {
//        int carry = 0;
//
//        for (int i = selectedRotorsInitPos.size() - 1; i >= 0; i--)
//        {
//            if((selectedRotorsInitPos.get(i) + (missionSize%countingBase) + carry) < countingBase) {
//                selectedRotorsInitPos.set(i,selectedRotorsInitPos.get(i) + (missionSize%countingBase) + carry);
//                return true;
//            }
//            else {
//                selectedRotorsInitPos.set(i,(missionSize%countingBase) + selectedRotorsInitPos.get(i) + carry - countingBase);
//                missionSize/=countingBase;
//                carry = 1;
//            }
//        }
//        return false;
//    }
        public boolean nextMissionForAgentsAndDM(int countingBase, int missionSize){
        int i = selectedRotorsInitPos.size() - 1;
        int carry = 0;
        do{

            if(selectedRotorsInitPos.get(i) + (missionSize%countingBase) + carry < countingBase) {
                selectedRotorsInitPos.set(i, selectedRotorsInitPos.get(i) + (missionSize % countingBase) + carry);
                carry = 0;
            }
            else {
                if(i == 0){
                    return false;
                }
                selectedRotorsInitPos.set(i, (missionSize % countingBase) + selectedRotorsInitPos.get(i) + carry - countingBase);
                carry = 1;
            }
            missionSize/=countingBase;
            --i;
        }while (missionSize != 0 || carry == 1 );

        return true;
        }



    @Override
    public String toString() { return secretToString; }
}
