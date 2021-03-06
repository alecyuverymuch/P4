import java.io.*;
import java.util.*;

// **********************************************************************
// The ASTnode class defines the nodes of the abstract-syntax tree that
// represents a Moo program.
//
// Internal nodes of the tree contain pointers to children, organized
// either in a list (for nodes that may have a variable number of 
// children) or as a fixed set of fields.
//
// The nodes for literals and ids contain line and character number
// information; for string literals and identifiers, they also contain a
// string; for integer literals, they also contain an integer value.
//
// Here are all the different kinds of AST nodes and what kinds of children
// they have.  All of these kinds of AST nodes are subclasses of "ASTnode".
// Indentation indicates further subclassing:
//
//     Subclass            Kids
//     --------            ----
//     ProgramNode         DeclListNode
//     DeclListNode        linked list of DeclNode
//     DeclNode:
//       VarDeclNode       TypeNode, IdNode, int
//       FnDeclNode        TypeNode, IdNode, FormalsListNode, FnBodyNode
//       FormalDeclNode    TypeNode, IdNode
//       StructDeclNode    IdNode, DeclListNode
//
//     FormalsListNode     linked list of FormalDeclNode
//     FnBodyNode          DeclListNode, StmtListNode
//     StmtListNode        linked list of StmtNode
//     ExpListNode         linked list of ExpNode
//
//     TypeNode:
//       IntNode           -- none --
//       BoolNode          -- none --
//       VoidNode          -- none --
//       StructNode        IdNode
//
//     StmtNode:
//       AssignStmtNode      AssignNode
//       PostIncStmtNode     ExpNode
//       PostDecStmtNode     ExpNode
//       ReadStmtNode        ExpNode
//       WriteStmtNode       ExpNode
//       IfStmtNode          ExpNode, DeclListNode, StmtListNode
//       IfElseStmtNode      ExpNode, DeclListNode, StmtListNode,
//                                    DeclListNode, StmtListNode
//       WhileStmtNode       ExpNode, DeclListNode, StmtListNode
//       CallStmtNode        CallExpNode
//       ReturnStmtNode      ExpNode
//
//     ExpNode:
//       IntLitNode          -- none --
//       StrLitNode          -- none --
//       TrueNode            -- none --
//       FalseNode           -- none --
//       IdNode              -- none --
//       DotAccessNode       ExpNode, IdNode
//       AssignNode          ExpNode, ExpNode
//       CallExpNode         IdNode, ExpListNode
//       UnaryExpNode        ExpNode
//         UnaryMinusNode
//         NotNode
//       BinaryExpNode       ExpNode ExpNode
//         PlusNode     
//         MinusNode
//         TimesNode
//         DivideNode
//         AndNode
//         OrNode
//         EqualsNode
//         NotEqualsNode
//         LessNode
//         GreaterNode
//         LessEqNode
//         GreaterEqNode
//
// Here are the different kinds of AST nodes again, organized according to
// whether they are leaves, internal nodes with linked lists of kids, or
// internal nodes with a fixed number of kids:
//
// (1) Leaf nodes:
//        IntNode,   BoolNode,  VoidNode,  IntLitNode,  StrLitNode,
//        TrueNode,  FalseNode, IdNode
//
// (2) Internal nodes with (possibly empty) linked lists of children:
//        DeclListNode, FormalsListNode, StmtListNode, ExpListNode
//
// (3) Internal nodes with fixed numbers of kids:
//        ProgramNode,     VarDeclNode,     FnDeclNode,     FormalDeclNode,
//        StructDeclNode,  FnBodyNode,      StructNode,     AssignStmtNode,
//        PostIncStmtNode, PostDecStmtNode, ReadStmtNode,   WriteStmtNode   
//        IfStmtNode,      IfElseStmtNode,  WhileStmtNode,  CallStmtNode
//        ReturnStmtNode,  DotAccessNode,   AssignExpNode,  CallExpNode,
//        UnaryExpNode,    BinaryExpNode,   UnaryMinusNode, NotNode,
//        PlusNode,        MinusNode,       TimesNode,      DivideNode,
//        AndNode,         OrNode,          EqualsNode,     NotEqualsNode,
//        LessNode,        GreaterNode,     LessEqNode,     GreaterEqNode
//
// **********************************************************************

// **********************************************************************
// ASTnode class (base class for all other kinds of nodes)
// **********************************************************************

abstract class ASTnode { 
    public static SymTable symTab;
    // every subclass must provide an unparse operation
    abstract public void unparse(PrintWriter p, int indent);

    abstract public void nameAnalysis();

    protected void setSymTable(SymTable symTable) {
        symTab = symTable;
    }
    // this method can be used by the unparse methods to do indenting
    protected void doIndent(PrintWriter p, int indent) {
        for (int k=0; k<indent; k++) p.print(" ");
    }
}

// **********************************************************************
// ProgramNode,  DeclListNode, FormalsListNode, FnBodyNode,
// StmtListNode, ExpListNode
// **********************************************************************

class ProgramNode extends ASTnode {
    public ProgramNode(DeclListNode L) {
        myDeclList = L;
    }

    /**
     * Sample name analysis method. 
     * Creates an empty symbol table for the outermost scope, then processes
     * all of the globals, struct defintions, and functions in the program.
     */
    public void nameAnalysis() {
        myDeclList.nameAnalysis();
	// TODO: Add code here 
    }

    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
    }

    // 1 kid
    private DeclListNode myDeclList;
}

class DeclListNode extends ASTnode {
    public DeclListNode(List<DeclNode> S) {
        myDecls = S;
    }

    public void nameAnalysis() {
        Iterator it = myDecls.iterator();
        try {
            while (it.hasNext()) {
                ((DeclNode)it.next()).nameAnalysis();
            }
        } catch (NoSuchElementException ex) {
            System.err.println("unexpected NoSuchElementException in DeclListNode.nameAnalysis");
            System.exit(-1);
        }
    }

    public HashMap<String,SemSym> getSyms(){
	Iterator it = myDecls.iterator();
	HashMap<String,SemSym> syms = new HashMap<String,SemSym>();
        try {
	    symTab.addScope();
	    
            while (it.hasNext()) {
                SemSym s = ((VarDeclNode)it.next()).getSym();
		if(s != null){
		    syms.put(s.getName(),s);
		}
            }
	    symTab.removeScope();
	    
        } catch (NoSuchElementException ex) {
            System.err.println("unexpected NoSuchElementException in DeclListNode.nameAnalysis");
            System.exit(-1);
        }catch(EmptySymTableException e){

	}
	return syms;
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator it = myDecls.iterator();
        try {
            while (it.hasNext()) {
                ((DeclNode)it.next()).unparse(p, indent);
            }
        } catch (NoSuchElementException ex) {
            System.err.println("unexpected NoSuchElementException in DeclListNode.print");
            System.exit(-1);
        }
    }

    // list of kids (DeclNodes)
    private List<DeclNode> myDecls;
}

class FormalsListNode extends ASTnode {
    public FormalsListNode(List<FormalDeclNode> S) {
        myFormals = S;
    }

    public LinkedList<String> getParamTypes() {
        LinkedList<String> paramTypes = new LinkedList<String>();
        Iterator<FormalDeclNode> it = myFormals.iterator();
        while(it.hasNext()) {
            paramTypes.addLast(it.next().getType());
        }
        return paramTypes;
    }

    public void nameAnalysis(){
        Iterator<FormalDeclNode> it = myFormals.iterator();
        while (it.hasNext()) { // if there is at least one element
            it.next().nameAnalysis();
        } 
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<FormalDeclNode> it = myFormals.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) {  // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        } 
    }

    // list of kids (FormalDeclNodes)
    private List<FormalDeclNode> myFormals;
}

class FnBodyNode extends ASTnode {
    public FnBodyNode(DeclListNode declList, StmtListNode stmtList) {
        myDeclList = declList;
        myStmtList = stmtList;
    }

    public void nameAnalysis(){
        myDeclList.nameAnalysis();
        myStmtList.nameAnalysis();
    }

    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
        myStmtList.unparse(p, indent);
    }

    // 2 kids
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class StmtListNode extends ASTnode {
    public StmtListNode(List<StmtNode> S) {
        myStmts = S;
    }

    public void nameAnalysis(){
        Iterator<StmtNode> it = myStmts.iterator();
        while (it.hasNext()) {
            it.next().nameAnalysis();
        }
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<StmtNode> it = myStmts.iterator();
        while (it.hasNext()) {
            it.next().unparse(p, indent);
        }
    }

    // list of kids (StmtNodes)
    private List<StmtNode> myStmts;
}

class ExpListNode extends ASTnode {
    public ExpListNode(List<ExpNode> S) {
        myExps = S;
    }

    public void nameAnalysis(){
        Iterator<ExpNode> it = myExps.iterator();
        while (it.hasNext()) {
            it.next().nameAnalysis();
        }
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<ExpNode> it = myExps.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) {  // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        } 
    }

    // list of kids (ExpNodes)
    private List<ExpNode> myExps;
}

// **********************************************************************
// DeclNode and its subclasses
// **********************************************************************

abstract class DeclNode extends ASTnode {
}

class VarDeclNode extends DeclNode {
    public VarDeclNode(TypeNode type, IdNode id, int size) {
        myType = type;
        myId = id;
        mySize = size;
    }

    public void nameAnalysis() {
        String type = myType.getType();
        myId.setVar(type);
        myId.setDecl(true);
	if(myType instanceof StructNode){
	   if(((StructNode)myType).checkType()){
		SemSym s = new SemSym(myId.getName(),(symTab.lookupGlobal(myType.getType	())).getDecls(),myType.getType());
	      try{
              myId.setDecl(true);
                myId.setSym(s);
		((StructNode)myType).setType(s.getType());		
            if (myId.checkId(s.getType())){
                symTab.addDecl(s.getName(),s);
            }
	      }catch(DuplicateSymException e){

	      }catch(EmptySymTableException e){
		
	      }
	   }
	}else{
           myId.nameAnalysis();
	}
    }

    public SemSym getSym(){
	if(myType instanceof StructNode){
	   if(((StructNode)myType).checkType()){
           SemSym sym = new SemSym(myId.getName(),(symTab.lookupGlobal(myType.getType())).getDecls(),myType.getType());
           ((StructNode)myType).setType(sym.getType());
           myId.setDecl(true);
           myId.setSym(sym);
           if (myId.checkId(sym.getType())){
               try {
                    symTab.addDecl(myId.getName(), sym);
               } catch (EmptySymTableException e) {
                   
               } catch (DuplicateSymException e1) {
                   
               }
            //return null;
            }
		return sym;
	   }
	}
	else if(myId.checkId(myType.getType())){
            SemSym s = new SemSym(myId.getName(),myType.getType());
	    try{
            myId.setSym(s);
            myId.setDecl(true);
            //myId.nameAnalysis();
	        symTab.addDecl(s.getName(),s);
	        return s;
	     }catch(DuplicateSymException e){

	     }catch(EmptySymTableException e){
		
	     }
	}
	return null;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        myId.unparse(p, 0);
        p.println(";");
    }

    // 3 kids
    private TypeNode myType;
    private IdNode myId;
    private int mySize;  // use value NOT_STRUCT if this is not a struct type

    public static int NOT_STRUCT = -1;
}

class FnDeclNode extends DeclNode {
    public FnDeclNode(TypeNode type,
                      IdNode id,
                      FormalsListNode formalList,
                      FnBodyNode body) {
        myType = type;
        myId = id;
        myFormalsList = formalList;
        myBody = body;
    }

    public void nameAnalysis() {
        String returnType = myType.getType();
        LinkedList<String> paramTypes = myFormalsList.getParamTypes();
        int params = paramTypes.size();
        myId.setFunc(returnType, params, paramTypes);
        myId.setDecl(true);
        myId.nameAnalysis();
        /////
        symTab.addScope();
        myFormalsList.nameAnalysis();
        myBody.nameAnalysis();
        try {
            symTab.removeScope();
        } catch (EmptySymTableException e) {

        }
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        myId.unparse(p, 0);
        p.print("(");
        myFormalsList.unparse(p, 0);
        p.println(") {");
        myBody.unparse(p, indent+4);
        p.print("}\n");
    }

    // 4 kids
    private TypeNode myType;
    private IdNode myId;
    private FormalsListNode myFormalsList;
    private FnBodyNode myBody;
}

class FormalDeclNode extends DeclNode {
    public FormalDeclNode(TypeNode type, IdNode id) {
        myType = type;
        myId = id;
    }

    public String getType(){
        return myType.getType();
    }

    public void nameAnalysis(){
        myId.setVar(myType.getType());
        myId.setDecl(true);
        myId.nameAnalysis();
    }

    public void unparse(PrintWriter p, int indent) {
        myType.unparse(p, 0);
        p.print(" ");
        myId.unparse(p, 0);
    }

    // 2 kids
    private TypeNode myType;
    private IdNode myId;
}

class StructDeclNode extends DeclNode {
    public StructDeclNode(IdNode id, DeclListNode declList) {
        myId = id;
        myDeclList = declList;
    }

    public void nameAnalysis(){
        SemSym sym = new SemSym(myId.getName(),myDeclList.getSyms());
	try{
		if(myId.checkId("struct")){
            myId.setSym(sym);
            symTab.addDecl(myId.getName(),sym);
		}
	    }catch(DuplicateSymException e){

	    }catch(EmptySymTableException e){
		
	    }
    myId.nameAnalysis();
    myId.setDecl(true);
	//myDeclList.nameAnalysis();
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("struct ");
		myId.unparse(p, 0);
		p.println("{");
        myDeclList.unparse(p, indent+4);
        doIndent(p, indent);
        p.print("};\n");

    }

    // 2 kids
    private IdNode myId;
    private DeclListNode myDeclList;
}

// **********************************************************************
// TypeNode and its Subclasses
// **********************************************************************

abstract class TypeNode extends ASTnode {
    abstract String getType();
}

class IntNode extends TypeNode {
    public IntNode() {
    }

    public String getType() {
        return "int";
    }

    public void nameAnalysis(){}

    public void unparse(PrintWriter p, int indent) {
        p.print("int");
    }
}

class BoolNode extends TypeNode {
    public BoolNode() {
    }

    public String getType() {
        return "bool";
    }

    public void nameAnalysis(){}

    public void unparse(PrintWriter p, int indent) {
        p.print("bool");
    }
}

class VoidNode extends TypeNode {
    public VoidNode() {
    }

    public void nameAnalysis(){}

    public String getType() {
        return "void";
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("void");
    }
}

class StructNode extends TypeNode {
    public StructNode(IdNode id) {
        myId = id;
    }

    public void nameAnalysis(){}

    public String getType() {
        return myId.getName();
    }
    
    public boolean checkType(){
	SemSym s = symTab.lookupGlobal(myId.getName());
	if(s == null){
        if (check){
            myId.undeclaredId();
        }
	    check = false;
	}else if(!s.getType().equals("struct")){
	   myId.structBadDecl();
	   check = false;
	}
	return check;
    }

    public IdNode getId() {
        return myId;
    }

    public void setType(String type){
	this.type = type;
    }

    public void unparse(PrintWriter p, int indent) {
	if(type.equals("")){
           p.print("struct ");
           myId.unparse(p, 0);
	}else{
	  p.print("struct ");
	  p.print(type);
	}
    }
    private IdNode myId;
    private String type = "";
    public boolean check = true;
}

// **********************************************************************
// StmtNode and its subclasses
// **********************************************************************

abstract class StmtNode extends ASTnode {
}

class AssignStmtNode extends StmtNode {
    public AssignStmtNode(AssignNode assign) {
        myAssign = assign;
    }

    public void nameAnalysis(){
        myAssign.nameAnalysis();
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myAssign.unparse(p, -1); // no parentheses
        p.println(";");
    }

    // 1 kid
    private AssignNode myAssign;
}

class PostIncStmtNode extends StmtNode {
    public PostIncStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public void nameAnalysis(){
        myExp.nameAnalysis();
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myExp.unparse(p, 0);
        p.println("++;");
    }

    // 1 kid
    private ExpNode myExp;
}

class PostDecStmtNode extends StmtNode {
    public PostDecStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public void nameAnalysis(){
        myExp.nameAnalysis();
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myExp.unparse(p, 0);
        p.println("--;");
    }

    // 1 kid
    private ExpNode myExp;
}

class ReadStmtNode extends StmtNode {
    public ReadStmtNode(ExpNode e) {
        myExp = e;
    }

    public void nameAnalysis(){
        myExp.nameAnalysis();
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("cin >> ");
        myExp.unparse(p, 0);
        p.println(";");
    }

    // 1 kid (actually can only be an IdNode or an ArrayExpNode)
    private ExpNode myExp;
}

class WriteStmtNode extends StmtNode {
    public WriteStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public void nameAnalysis(){
        myExp.nameAnalysis();
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("cout << ");
        myExp.unparse(p, 0);
        p.println(";");
    }

    // 1 kid
    private ExpNode myExp;
}

class IfStmtNode extends StmtNode {
    public IfStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myDeclList = dlist;
        myExp = exp;
        myStmtList = slist;
    }

    public void nameAnalysis(){
        ////
        symTab.addScope();
        myDeclList.nameAnalysis();
        myExp.nameAnalysis();
        myStmtList.nameAnalysis();
        try {
            symTab.removeScope();
        } catch (EmptySymTableException e) {
            
        }
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("if (");
        myExp.unparse(p, 0);
        p.println(") {");
        myDeclList.unparse(p, indent+4);
        myStmtList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("}");
    }

    // e kids
    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class IfElseStmtNode extends StmtNode {
    public IfElseStmtNode(ExpNode exp, DeclListNode dlist1,
                          StmtListNode slist1, DeclListNode dlist2,
                          StmtListNode slist2) {
        myExp = exp;
        myThenDeclList = dlist1;
        myThenStmtList = slist1;
        myElseDeclList = dlist2;
        myElseStmtList = slist2;
    }

    public void nameAnalysis(){
        ////
        symTab.addScope();
        myExp.nameAnalysis();
        myThenDeclList.nameAnalysis();
        myThenStmtList.nameAnalysis();
        myElseDeclList.nameAnalysis();
        myElseStmtList.nameAnalysis();
        try {
            symTab.removeScope();
        } catch (EmptySymTableException e) {
            
        }
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("if (");
        myExp.unparse(p, 0);
        p.println(") {");
        myThenDeclList.unparse(p, indent+4);
        myThenStmtList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("}");
        doIndent(p, indent);
        p.println("else {");
        myElseDeclList.unparse(p, indent+4);
        myElseStmtList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("}");        
    }

    // 5 kids
    private ExpNode myExp;
    private DeclListNode myThenDeclList;
    private StmtListNode myThenStmtList;
    private StmtListNode myElseStmtList;
    private DeclListNode myElseDeclList;
}

class WhileStmtNode extends StmtNode {
    public WhileStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myExp = exp;
        myDeclList = dlist;
        myStmtList = slist;
    }

    public void nameAnalysis(){
        ////
        symTab.addScope();
        myExp.nameAnalysis();
        myDeclList.nameAnalysis();
        myStmtList.nameAnalysis();
        try {
            symTab.removeScope();
        } catch (EmptySymTableException e) {
            
        }
    }
	
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("while (");
        myExp.unparse(p, 0);
        p.println(") {");
        myDeclList.unparse(p, indent+4);
        myStmtList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("}");
    }

    // 3 kids
    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class CallStmtNode extends StmtNode {
    public CallStmtNode(CallExpNode call) {
        myCall = call;
    }

    public void nameAnalysis(){
        myCall.nameAnalysis();
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myCall.unparse(p, indent);
        p.println(";");
    }

    // 1 kid
    private CallExpNode myCall;
}

class ReturnStmtNode extends StmtNode {
    public ReturnStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public void nameAnalysis(){
        if (myExp != null) {
            myExp.nameAnalysis();
        }
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("return");
        if (myExp != null) {
            p.print(" ");
            myExp.unparse(p, 0);
        }
        p.println(";");
    }

    // 1 kid
    private ExpNode myExp; // possibly null
}

// **********************************************************************
// ExpNode and its subclasses
// **********************************************************************

abstract class ExpNode extends ASTnode {
    public boolean isDecl = false;

    public void setDecl(boolean isDecl){
        this.isDecl = isDecl;
    }
}

class IntLitNode extends ExpNode {
    public IntLitNode(int lineNum, int charNum, int intVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myIntVal = intVal;
    }

    public void nameAnalysis(){
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myIntVal);
    }

    private int myLineNum;
    private int myCharNum;
    private int myIntVal;
}

class StringLitNode extends ExpNode {
    public StringLitNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
    }

    public void nameAnalysis(){}

    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);
    }

    private int myLineNum;
    private int myCharNum;
    private String myStrVal;
}

class TrueNode extends ExpNode {
    public TrueNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }

    public void nameAnalysis(){}

    public void unparse(PrintWriter p, int indent) {
        p.print("true");
    }

    private int myLineNum;
    private int myCharNum;
}

class FalseNode extends ExpNode {
    public FalseNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }

    public void nameAnalysis(){}

    public void unparse(PrintWriter p, int indent) {
        p.print("false");
    }

    private int myLineNum;
    private int myCharNum;
}

class IdNode extends ExpNode {
    public IdNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
        myType = "";
        isFunc = false;
        isDupl = false;
        returnType = "";
        params = 0;
        paramTypes = null;
        mySym = null;
    }

    public String getName(){
        return myStrVal;
    }

    public void setSym(SemSym sym){
        mySym = sym;
        myStrVal = sym.getName();
    }

    public void setVar(String type){
        myType = type;
    }

    public void setFunc(String returnType, int params, LinkedList<String> paramTypes){
        isFunc = true;
        myType = returnType;
        this.returnType = returnType;
        this.params = params;
        this.paramTypes = paramTypes;
    }

    public void nameAnalysis(){
        if (!isDecl){
            SemSym sym = symTab.lookupGlobal(myStrVal);
            if (sym == null) {
                ErrMsg.fatal(myLineNum, myCharNum, "Undeclaired identifier");
                return;
            }
            mySym = sym;
            if (sym.isFunc()){
                returnType = sym.getReturnType();
                paramTypes = new LinkedList<String>(sym.getParamTypes());
            }
            else {
                myType = sym.getType();
            }
            return;
        }
        if (isFunc) {
            // For function declarations
            try {
                Iterator<String> it = paramTypes.iterator();
                while(it.hasNext()) {
                    if (it.next().equals("void")) {
                        ErrMsg.fatal(myLineNum, myCharNum, "Non-fuction declared void");
                    }
                }
                mySym = new SemSym(myStrVal, returnType, params, paramTypes);
                symTab.addDecl(myStrVal, mySym);
            } catch (DuplicateSymException e) {

            } catch (EmptySymTableException e) {
                //TODO maybe to do with scopes?
            }
        }
        else {
            // For variable declarations
            try {
                if (myType.equals("void")) {
                    ErrMsg.fatal(myLineNum, myCharNum, "Non-fuction declared void");
                }
                mySym = new SemSym(myStrVal, myType);
                symTab.addDecl(myStrVal, mySym);
            } catch (DuplicateSymException e) {
                ErrMsg.fatal(myLineNum, myCharNum, "Multiply declared identifier");
            } catch (EmptySymTableException e) {
                //TODO maybe to do with scopes?
            }
        }
    }

    public boolean checkId(String type){
	boolean check = true;
	if (type.equals("void")) {
            ErrMsg.fatal(myLineNum, myCharNum, "Non-fuction declared void");
	    check = false;
        }
	if(symTab.lookupLocal(myStrVal) != null){
	    ErrMsg.fatal(myLineNum, myCharNum, "Multiply declared identifier");
	    check = false;
	}
	return check;
    }

    public void undeclaredId(){
	ErrMsg.fatal(myLineNum, myCharNum, "Undeclared identifier");
    }

    public void structBadDecl(){
	ErrMsg.fatal(myLineNum, myCharNum, "Invalid name of struct type");
    }

    public void structLhsAccess(){
	ErrMsg.fatal(myLineNum, myCharNum, "Dot-access of non-struct type");
    }

    public void structRhsAccess(){
	ErrMsg.fatal(myLineNum, myCharNum, "Invalid struct field name");
    }

    public void unparse(PrintWriter p, int indent) {
        if (mySym == null) return;
        if (isDecl){
            p.print(myStrVal);
            return;
        }
	
        if (mySym.isFunc()) {
            p.print(myStrVal);
            List<String> types = mySym.getParamTypes();
            returnType = mySym.getReturnType();
            p.print("(");
            Iterator<String> it = types.iterator();
            if (it.hasNext()) { // if there is at least one element
                p.print(it.next());
                while (it.hasNext()) {  // print the rest of the list
                    p.print(", ");
                    p.print(it.next());
                }
            }
            p.print("->" + returnType + ")");
        }
        else {
            myType = mySym.getType();
            p.print(myStrVal);
            p.print("(" + myType + ")");
        }
    }

    private int myLineNum;
    private int myCharNum;
    private String myStrVal;
    public String myType;
    public boolean isFunc;
    public String returnType;
    public int params;
    public boolean isDupl;
    public LinkedList<String> paramTypes;
    public SemSym mySym;
}

class DotAccessExpNode extends ExpNode {
    public DotAccessExpNode(ExpNode loc, IdNode id) {
        myLoc = loc;	
        myId = id;
    }

    public void nameAnalysis(){
	if(myLoc instanceof IdNode){ //only one . access
	    SemSym s = symTab.lookupGlobal(((IdNode)myLoc).getName());
	    if(s == null){
		((IdNode)myLoc).undeclaredId();
	    }else if(!s.getActualType().equals("struct")){
		((IdNode)myLoc).structLhsAccess();
	    }else{
		SemSym sym = s.getDecls().get(myId.getName());
		if(sym == null){
		    myId.structRhsAccess();
		    
		}else{
		((IdNode)myLoc).setSym(s);
		myId.setSym(sym);
		}
	    }
	    //maybe need to store something
	}else {//more then one . access
	    multiNameAnalysis();
	}
    }

    public SemSym multiNameAnalysis(){
	if(myLoc instanceof IdNode){
	    SemSym s = symTab.lookupGlobal(((IdNode)myLoc).getName());
	    if(s == null){
		((IdNode)myLoc).undeclaredId();
	    }else if(!s.getActualType().equals("struct")){
		((IdNode)myLoc).structLhsAccess();
	    }else{
		SemSym sym = s.getDecls().get(myId.getName());
		if(sym == null){
		    myId.structRhsAccess();
		}else{
		   ((IdNode)myLoc).setSym(s);
		   sym.setId(myId);
		   myId.setSym(sym);
		   return sym;
		}
	    }
	    return null;
	    
	}
	//give the sym?
	SemSym lastSym = ((DotAccessExpNode)myLoc).multiNameAnalysis();
	if(lastSym == null){
	    return null;
	}else{
	    if(!lastSym.getActualType().equals("struct")){
		lastSym.getId().structLhsAccess();
	    }else{
		SemSym sym = lastSym.getDecls().get(myId.getName());
		if(sym == null){
		    myId.structRhsAccess();
		}else{
		   myId.setSym(sym);
		   sym.setId(myId);
		   return sym;
		}
	    } 
	}
	return null;
    }

    public void unparse(PrintWriter p, int indent) {
	    p.print("");
        myLoc.unparse(p, 0);
		p.print(".");
        myId.unparse(p, 0);
    }

    // 2 kids
    private ExpNode myLoc;	
    private IdNode myId;
}

class AssignNode extends ExpNode {
    public AssignNode(ExpNode lhs, ExpNode exp) {
        myLhs = lhs;
        myExp = exp;
    }

    public void nameAnalysis(){
        myLhs.nameAnalysis();
        myExp.nameAnalysis();
    }

    public void unparse(PrintWriter p, int indent) {
		if (indent != -1)  p.print("(");
	    myLhs.unparse(p, 0);
		p.print(" = ");
        myExp.unparse(p, 0);
		if (indent != -1)  p.print(")");
    }

    // 2 kids
    private ExpNode myLhs;
    private ExpNode myExp;
}

class CallExpNode extends ExpNode {
    public CallExpNode(IdNode name, ExpListNode elist) {
        myId = name;
        myExpList = elist;
    }

    public CallExpNode(IdNode name) {
        myId = name;
        myExpList = new ExpListNode(new LinkedList<ExpNode>());
    }

    public void nameAnalysis(){
        myId.nameAnalysis();
        myExpList.nameAnalysis();
    }

    // ** unparse **
    public void unparse(PrintWriter p, int indent) {
        myId.unparse(p, 0);
		p.print("(");
		if (myExpList != null) {
			myExpList.unparse(p, 0);
		}
        p.print(")");
    }

    // 2 kids
    private IdNode myId;
    private ExpListNode myExpList;  // possibly null
}

abstract class UnaryExpNode extends ExpNode {
    public UnaryExpNode(ExpNode exp) {
        myExp = exp;
    }

    // one child
    protected ExpNode myExp;
}

abstract class BinaryExpNode extends ExpNode {
    public BinaryExpNode(ExpNode exp1, ExpNode exp2) {
        myExp1 = exp1;
        myExp2 = exp2;
    }

    // two kids
    protected ExpNode myExp1;
    protected ExpNode myExp2;
}

// **********************************************************************
// Subclasses of UnaryExpNode
// **********************************************************************

class UnaryMinusNode extends UnaryExpNode {
    public UnaryMinusNode(ExpNode exp) {
        super(exp);
    }

    public void nameAnalysis(){
        myExp.nameAnalysis();
    }

    public void unparse(PrintWriter p, int indent) {
	    p.print("(-");
        myExp.unparse(p, 0);
		p.print(")");
    }
}

class NotNode extends UnaryExpNode {
    public NotNode(ExpNode exp) {
        super(exp);
    }

    public void nameAnalysis(){
        myExp.nameAnalysis();
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(!");
        myExp.unparse(p, 0);
		p.print(")");
    }
}

// **********************************************************************
// Subclasses of BinaryExpNode
// **********************************************************************

class PlusNode extends BinaryExpNode {
    public PlusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void nameAnalysis(){
        myExp1.nameAnalysis();
        myExp2.nameAnalysis();
    }

    public void unparse(PrintWriter p, int indent) {
	    p.print("(");
		myExp1.unparse(p, 0);
		p.print(" + ");
		myExp2.unparse(p, 0);
		p.print(")");
    }
}

class MinusNode extends BinaryExpNode {
    public MinusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void nameAnalysis(){
        myExp1.nameAnalysis();
        myExp2.nameAnalysis();
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
		p.print(" - ");
		myExp2.unparse(p, 0);
		p.print(")");
    }
}

class TimesNode extends BinaryExpNode {
    public TimesNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void nameAnalysis(){
        myExp1.nameAnalysis();
        myExp2.nameAnalysis();
    }

    public void unparse(PrintWriter p, int indent) {
	    p.print("(");
		myExp1.unparse(p, 0);
		p.print(" * ");
		myExp2.unparse(p, 0);
		p.print(")");
    }
}

class DivideNode extends BinaryExpNode {
    public DivideNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void nameAnalysis(){
        myExp1.nameAnalysis();
        myExp2.nameAnalysis();
    }

    public void unparse(PrintWriter p, int indent) {
	    p.print("(");
		myExp1.unparse(p, 0);
		p.print(" / ");
		myExp2.unparse(p, 0);
		p.print(")");
    }
}

class AndNode extends BinaryExpNode {
    public AndNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void nameAnalysis(){
        myExp1.nameAnalysis();
        myExp2.nameAnalysis();
    }

    public void unparse(PrintWriter p, int indent) {
	    p.print("(");
		myExp1.unparse(p, 0);
		p.print(" && ");
		myExp2.unparse(p, 0);
		p.print(")");
    }
}

class OrNode extends BinaryExpNode {
    public OrNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void nameAnalysis(){
        myExp1.nameAnalysis();
        myExp2.nameAnalysis();
    }

    public void unparse(PrintWriter p, int indent) {
	    p.print("(");
		myExp1.unparse(p, 0);
		p.print(" || ");
		myExp2.unparse(p, 0);
		p.print(")");
    }
}

class EqualsNode extends BinaryExpNode {
    public EqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void nameAnalysis(){
        myExp1.nameAnalysis();
        myExp2.nameAnalysis();
    }

    public void unparse(PrintWriter p, int indent) {
	    p.print("(");
		myExp1.unparse(p, 0);
		p.print(" == ");
		myExp2.unparse(p, 0);
		p.print(")");
    }
}

class NotEqualsNode extends BinaryExpNode {
    public NotEqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void nameAnalysis(){
        myExp1.nameAnalysis();
        myExp2.nameAnalysis();
    }

    public void unparse(PrintWriter p, int indent) {
	    p.print("(");
		myExp1.unparse(p, 0);
		p.print(" != ");
        myExp2.unparse(p, 0);
		p.print(")");
    }
}

class LessNode extends BinaryExpNode {
    public LessNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void nameAnalysis(){
        myExp1.nameAnalysis();
        myExp2.nameAnalysis();
    }

    public void unparse(PrintWriter p, int indent) {
	    p.print("(");
		myExp1.unparse(p, 0);
		p.print(" < ");
		myExp2.unparse(p, 0);
		p.print(")");
    }
}

class GreaterNode extends BinaryExpNode {
    public GreaterNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void nameAnalysis(){
        myExp1.nameAnalysis();
        myExp2.nameAnalysis();
    }

    public void unparse(PrintWriter p, int indent) {
	    p.print("(");
		myExp1.unparse(p, 0);
		p.print(" > ");
		myExp2.unparse(p, 0);
		p.print(")");
    }
}

class LessEqNode extends BinaryExpNode {
    public LessEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void nameAnalysis(){
        myExp1.nameAnalysis();
        myExp2.nameAnalysis();
    }

    public void unparse(PrintWriter p, int indent) {
	    p.print("(");
		myExp1.unparse(p, 0);
		p.print(" <= ");
		myExp2.unparse(p, 0);
		p.print(")");
    }
}

class GreaterEqNode extends BinaryExpNode {
    public GreaterEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void nameAnalysis(){
        myExp1.nameAnalysis();
        myExp2.nameAnalysis();
    }

    public void unparse(PrintWriter p, int indent) {
	    p.print("(");
		myExp1.unparse(p, 0);
		p.print(" >= ");
		myExp2.unparse(p, 0);
		p.print(")");
    }
}
