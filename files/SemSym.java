import java.util.*;

public class SemSym {
    private String name;
    private String type;
    private boolean function;
    private int params;
    private LinkedList<String> paramTypes;
    private String returnType;
    
    public SemSym(String name, String type) {
        this.name = name;
        this.type = type;
        function = false;
    }

    public SemSym(String name, String returnType, int params, LinkedList<String> paramTypes){
        function = true;
        this.name = name;
        this.returnType = returnType;
        this.params = params;
        this.paramTypes = paramTypes;
    }
    
    public String getName(){
        return name;
    }

    public String getType() {
        return type;
    }
    
    public String toString() {
        return type;
    }
}
