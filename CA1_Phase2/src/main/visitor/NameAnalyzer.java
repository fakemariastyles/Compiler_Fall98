//package main.visitor;
//
//import main.ast.node.*;
//import main.ast.node.Program;
//import main.ast.node.declaration.*;
//import main.ast.node.declaration.handler.*;
//import main.ast.node.declaration.VarDeclaration;
//import main.ast.node.expression.*;
//import main.ast.node.expression.values.BooleanValue;
//import main.ast.node.expression.values.IntValue;
//import main.ast.node.expression.values.StringValue;
//import main.ast.node.statement.*;
//import main.symbolTable.SymbolTable;
//import main.symbolTable.SymbolTableActorItem;
//import main.symbolTable.SymbolTableHandlerItem;
//import main.symbolTable.itemException.ItemExistsException;
//import main.symbolTable.itemException.ItemNotFoundException;
//import main.symbolTable.symbolTableVariableItem.SymbolTableLocalVariableItem;
//
//import java.util.*;
//
//
//public class NameAnalyzer implements Visitor {
//
//    private int count = 0;
//    private int errors = 0;
//
//    private Map<Integer, String> lineErrors = new HashMap<>();
//
//
//    public Map<Integer, String> getLineErrors() {
//        return lineErrors;
//    }
//
//    public int getErrors() {
//        return errors;
//    }
//
//    @Override
//    public void visit(Program program) {
//        SymbolTable.push(new SymbolTable());
//        SymbolTable.root = SymbolTable.top;
//
//
//        if (program.getActors() != null) {
//            for (ActorDeclaration actor : program.getActors()) {
//                actor.accept(this);
//            }
//        }
//        if (program.getMain() != null) {
//            program.getMain().accept(this);
//        }
//        SymbolTable.pop();
//    }
//
//    @Override
//    public void visit(ActorDeclaration actorDeclaration) {
//        SymbolTable st = new SymbolTable();
//        st.setPreSymbolTable(SymbolTable.top);
//        st.setName("Actor_" + actorDeclaration.getParentName().getName());
//
//        if (actorDeclaration.getParentName() != null) {
//            String key = "Actor_" + actorDeclaration.getParentName().getName();
//            try {
//                SymbolTable parent = ((SymbolTableActorItem) (SymbolTable.root.get(key))).getActorSymbolTable();
//                st.setPreSymbolTable(parent);
//            } catch (ItemNotFoundException e) {
//            }
//        }
//        SymbolTable.push(st);
//
//        //push new actor to global symbolTable
//        SymbolTableActorItem actorItem = new SymbolTableActorItem(actorDeclaration);
//
//        int size = actorDeclaration.getQueueSize();
//        if (size <= 0) {
//            lineErrors.put(actorDeclaration.getLine(), "Line:" +
//                    Integer.toString(actorDeclaration.getLine()) +
//                    ":Queue size must be positive"
//            );
//            actorDeclaration.setQueueSize(0);
//        }
//
//        try {
//            actorItem.setActorSymbolTable(st);
//            SymbolTable.root.put(actorItem);
//        } catch (ItemExistsException e1) {
//            this.errors += 1;
//            lineErrors.put(actorDeclaration.getLine(), "Line:" +
//                    Integer.toString(actorDeclaration.getLine()) +
//                    ":Redefinition of actor " +
//                    actorDeclaration.getName().getName());
//            this.count += 1;
//
//            String number = Integer.toString(count);
//            String newName = "$Temp_" + actorDeclaration.getName().getName() + "_" + number;
//            actorItem.setName(newName);
//
//            try {
//                this.errors += 1;
//                SymbolTable.root.put(actorItem);
//            } catch (ItemExistsException e2) {
//            }
//        }
//
//        if (actorDeclaration.getActorVars() != null) {
//            for (VarDeclaration actorVar : actorDeclaration.getActorVars()) {
//                actorVar.accept(this);
//            }
//        }
//
//        if (actorDeclaration.getKnownActors() != null) {
//            for (VarDeclaration knownActor : actorDeclaration.getKnownActors()) {
//                knownActor.accept(this);
//            }
//        }
//
//        if (actorDeclaration.getMsgHandlers() != null) {
//            for (MsgHandlerDeclaration msgHandler : actorDeclaration.getMsgHandlers()) {
//                msgHandler.accept(this);
//            }
//        }
//
//        if (actorDeclaration.getInitHandler() != null) {
//            actorDeclaration.getInitHandler().accept(this);
//        }
//
//        SymbolTable.pop();
//    }
//
//    @Override
//    public void visit(HandlerDeclaration handlerDeclaration) {
//        SymbolTable st = new SymbolTable();
//        st.setPreSymbolTable(SymbolTable.top);
//        SymbolTable.push(st);
//
//        SymbolTableHandlerItem handlerItem = new SymbolTableHandlerItem(handlerDeclaration);
//
//        try {
//            handlerItem.setHandlerSymbolTable(st.getPreSymbolTable());
//            st.getPreSymbolTable().put(handlerItem);
//
//        } catch (ItemExistsException e1) {
//
//            this.errors += 1;
//            lineErrors.put(
//                    handlerDeclaration.getLine(),
//                    "Line:" + Integer.toString(handlerDeclaration.getLine()) + ":Redefinition of msghandler "
//                            + handlerDeclaration.getName().getName()
//            );
//            this.count += 1;
//
//            String number = Integer.toString(count);
//            String newName = "$Temp_" + handlerDeclaration.getName().getName() + "_" + number;
//            handlerItem.setName(newName);
//
//            try {
//                this.errors += 1;
//                st.getPreSymbolTable().put(handlerItem);
//            } catch (ItemExistsException e2) {
//            }
//        }
//
//        if (handlerDeclaration.getArgs() != null) {
//            for (VarDeclaration arg : handlerDeclaration.getArgs()) {
//                arg.accept(this);
//            }
//        }
//
//        if (handlerDeclaration.getLocalVars() != null) {
//            for (VarDeclaration localVar : handlerDeclaration.getLocalVars()) {
//                localVar.accept(this);
//            }
//        }
//
//        SymbolTable.pop();
//    }
//
//    @Override
//    public void visit(VarDeclaration varDeclaration) {
//        SymbolTableLocalVariableItem variableItem = new SymbolTableLocalVariableItem(varDeclaration);
//
//        try {
//            SymbolTable.top.put(variableItem);
//        } catch (ItemExistsException e1) {
//            this.count += 1;
//            lineErrors.put(
//                    varDeclaration.getLine(),
//                    "Line:" + Integer.toString(varDeclaration.getLine()) + ":Redefinition of variable "
//                            + varDeclaration.getIdentifier().getName()
//            );
//            this.errors += 1;
//
//            String number = Integer.toString(this.count);
//            String newName = "$Temp_" + varDeclaration.getIdentifier().getName() + "_" + number;
//            variableItem.setName(newName);
//            try {
//                SymbolTable.top.put(variableItem);
//            } catch (ItemExistsException e2) {
//            }
//        }
//    }
//
//    @Override
//    public void visit(Main mainActors) {
//    }
//
//    @Override
//    public void visit(ActorInstantiation actorInstantiation) {
//    }
//
//    @Override
//    public void visit(UnaryExpression unaryExpression) {
//    }
//
//    @Override
//    public void visit(BinaryExpression binaryExpression) {
//    }
//
//    @Override
//    public void visit(ArrayCall arrayCall) {
//    }
//
//    @Override
//    public void visit(ActorVarAccess actorVarAccess) {
//    }
//
//    @Override
//    public void visit(Identifier identifier) {
//    }
//
//    @Override
//    public void visit(Self self) {
//    }
//
//    @Override
//    public void visit(Sender sender) {
//    }
//
//    @Override
//    public void visit(BooleanValue value) {
//    }
//
//    @Override
//    public void visit(IntValue value) {
//    }
//
//    @Override
//    public void visit(StringValue value) {
//    }
//
//    @Override
//    public void visit(Block block) {
//        SymbolTable st = new SymbolTable();
//        st.setPreSymbolTable(SymbolTable.top);
//        SymbolTable.push(st);
//
//        if (block.getStatements() != null) {
//            for (Statement stmt : block.getStatements()) {
//                stmt.accept(this);
//            }
//        }
//    }
//
//    @Override
//    public void visit(Conditional conditional) {
//    }
//
//    @Override
//    public void visit(For loop) {
//    }
//
//    @Override
//    public void visit(Break breakLoop) {
//    }
//
//    @Override
//    public void visit(Continue continueLoop) {
//    }
//
//    @Override
//    public void visit(MsgHandlerCall msgHandlerCall) {
//    }
//
//    @Override
//    public void visit(Print print) {
//    }
//
//    @Override
//    public void visit(Assign assign) {
//    }
//
//
//    public HashMap<Integer, String> sortHashMapByValues() {
//        List<Integer> mapKeys = new ArrayList<>(lineErrors.keySet());
//        List<String> mapValues = new ArrayList<>(lineErrors.values());
//        Collections.sort(mapValues);
//        Collections.sort(mapKeys);
//
//        LinkedHashMap<Integer, String> sortedMap =
//                new LinkedHashMap<>();
//
//        Iterator<String> valueIt = mapValues.iterator();
//        while (valueIt.hasNext()) {
//            String val = valueIt.next();
//            Iterator<Integer> keyIt = mapKeys.iterator();
//
//            while (keyIt.hasNext()) {
//                Integer key = keyIt.next();
//                String comp1 = lineErrors.get(key);
//                String comp2 = val;
//
//                if (comp1.equals(comp2)) {
//                    keyIt.remove();
//                    sortedMap.put(key, val);
//                    break;
//                }
//            }
//        }
//
//        return sortedMap;
//    }
//}
