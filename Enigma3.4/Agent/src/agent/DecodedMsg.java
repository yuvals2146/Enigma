package agent;

public class DecodedMsg implements Comparable {
    private String decryptedMsg = "";
    private long HowManyMission = 0;
    private int agentId;
    private String secretStr = "";
    private final boolean AGENT_RETIRED;

    public DecodedMsg(boolean agentIsRetiring,long missionCount , int id){
        AGENT_RETIRED = true;
        HowManyMission = missionCount;
        agentId = id;
    }

    public DecodedMsg(String decMsg, int id, String sec) {
        decryptedMsg = decMsg;
        agentId = id;
        secretStr = sec;
        AGENT_RETIRED = false;
    }

    public void print() {
        if(decryptedMsg != "")
        System.out.println(decryptedMsg);
        System.out.println("  Agents id: " + agentId);
        if(HowManyMission != 0)
        System.out.println("  Mission accomplished: " + HowManyMission);
        if(secretStr != "")
        System.out.println("  Machine status: " + secretStr);
    }
    public void printToFile(){

    }

    public boolean agentRetired(){return AGENT_RETIRED;}


    @Override
    public int compareTo(Object o) {
        DecodedMsg msg = (DecodedMsg)o;
        return this.agentId - msg.agentId;
    }
}



