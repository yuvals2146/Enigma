package components.reflector;


import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Reflector implements Serializable
{
    public enum Id{I, II, III, IV, V};

    private Id id;
    private Map<Integer,Integer> reflector;

    public Reflector(Id _id, List<Integer> in, List<Integer> out)
    {
        id=_id;
        reflector = new HashMap<>();
        for(int i=0; i<in.size(); i++)
        {
            reflector.put(in.get(i), out.get(i));
            reflector.put(out.get(i),in.get(i));
        }
    }
    public Reflector(Reflector other){
        id = other.id;
        reflector = new HashMap<>(other.reflector);
    }
    public Id getId() { return id; }

    public int reflect(int inPosition){return (int)reflector.get(inPosition);}
}
