package manager;

public class Instruction {
    public enum options{INFO,PAUSE,PLAY,EXIT};
    private options instraction;
    private int howManyLeft = 0;

    public Instruction(options op){
        instraction = op;
    }

    public synchronized  void setInstruction(options op){
        instraction = op;
        notifyAll();
    }
    public synchronized void  setNumOfActive(int num){
        howManyLeft = num;

    }
    public synchronized void waitForAgents(){
        try {
            if(howManyLeft > 0)
            this.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public synchronized void updateNumOfAgents(){
        --howManyLeft;
        if(howManyLeft == 0)
            notifyAll();
    }

    public synchronized options getInstraction(){
        switch (instraction){
            case PAUSE:
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();

                }
                break;
            case EXIT:
                break;
            case PLAY:
                break;
        }
        return instraction;
    }
}
