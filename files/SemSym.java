import java.util.*;

public class SemSym {
    private String name;
    private String type;
    private boolean isFunc;
    private int params;
    private List<String> paramTypes;
    private HashMap<String,SemSym> decls;
    private String returnType;
    private String actualType = "";
    private IdNode id;
    
    public SemSym(String name, String type) {
        this.name = name;
        this.type = type;
        isFunc = false;
    }

    public SemSym(String name, String returnType, int params, List<String> paramTypes){
        isFunc = true;
        this.name = name;
        this.type = returnType;
        this.returnType = returnType;
        this.params = params;
        this.paramTypes = paramTypes;
    }

    public SemSym(String name,HashMap<String,SemSym> decls){
	actualType = "struct";
	this.type = "struct";
	this.decls = decls;
	this.name = name;
    }

    public SemSym(String name,HashMap<String,SemSym> decls,String type){
	actualType = "struct";
	this.type = type;
	this.decls = decls;
	this.name = name;
    }
    
    public HashMap<String,SemSym>getDecls(){
	return decls;
    }

    public String getName(){
        return name;
    }

    public void setId(IdNode n){
	id = n;
    }

    public IdNode getId(){
	return id;
    }

    public String getType() {
        return type;
    }

    public String getActualType(){
	return actualType;
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
