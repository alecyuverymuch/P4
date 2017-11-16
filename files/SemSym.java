import java.util.*;

public class SemSym {
    private String name;
    private String type;
    private boolean isFunc;
    private int params;
    private List<String> paramTypes;
    private String returnType;
    
    public SemSym(String name, String type) {
        this.name = name;
        this.type = type;
        isFunc = false;
    }

    public SemSym(String name, String returnType, int params, List<String> paramTypes){
        isFunc = true;
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

    public String getReturnType(){
        return returnType;
    }

    public List<String> getParamTypes(){
        return paramTypes;
    }

    public boolean isFunc(){
        return isFunc;
    }
    
    public String toString() {
        return type;
    }
}
