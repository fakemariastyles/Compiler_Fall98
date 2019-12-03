package main.visitor;

import main.ast.node.*;
import main.ast.node.Program;
import main.ast.node.declaration.*;
import main.ast.node.declaration.handler.*;
import main.ast.node.declaration.VarDeclaration;
import main.ast.node.expression.*;
import main.ast.node.expression.values.BooleanValue;
import main.ast.node.expression.values.IntValue;
import main.ast.node.expression.values.StringValue;
import main.ast.node.statement.*;
import main.symbolTable.SymbolTable;
import main.symbolTable.SymbolTableActorItem;
import main.symbolTable.SymbolTableHandlerItem;
import main.symbolTable.SymbolTableItem;
import main.symbolTable.itemException.ItemExistsException;
import main.symbolTable.itemException.ItemNotFoundException;
import main.symbolTable.symbolTableVariableItem.SymbolTableLocalVariableItem;
import main.symbolTable.symbolTableVariableItem.SymbolTableVariableItem;

import java.util.*;


public class SecondNameAnalyzerVisitor implements Visitor {

    private int count = 0;
    private int errors = 0;

    private Map<String, Integer> lineErrors = new HashMap<>();

    public void setErrors(int errors) {
        this.errors = errors;
    }

    public void setLineErrors(Map<String, Integer> e) {
        this.lineErrors.putAll(e);
    }

    public Map<String, Integer> getLineErrors() {
        return lineErrors;
    }

    public int getErrors() {
        return errors;
    }

    @Override
    public void visit(Program program) {
        if (program.getActors() != null) {
            Set<String> visited = new HashSet<>();

            for (ActorDeclaration actor : program.getActors()) {
                try {
                    SymbolTableActorItem actorItem = ((SymbolTableActorItem) SymbolTable.root.get("Actor_" + actor.getName().getName()));

                    while (actorItem != null && actorItem.getParentName() != null) {
                        if (visited.contains(actorItem.getParentName())) break;

                        if (!Objects.equals(actor.getName().getName(), actorItem.getParentName())) {
                            try {
                                actorItem = (SymbolTableActorItem) SymbolTable.root.getInCurrentScope("Actor_" + actorItem.getParentName());
                            } catch (ItemNotFoundException e) {
                                visited.clear();
                                break;
                            }
                        } else {
                            if (visited.contains(actor.getName().getName())) break;
                            this.errors += 1;
                            this.count += 1;
                            lineErrors.put("Line:" +
                                    Integer.toString(actor.getLine()) +
                                    ":Cyclic inheritance involving actor " +
                                    actor.getName().getName(), actor.getLine());
                            break;
                        }
                        visited.add(actorItem.getActorDeclaration().getName().getName());
                    }

                } catch (ItemNotFoundException e) {
                    e.printStackTrace();
                }
                actor.accept(this);
            }
        }

        if (program.getMain() != null) {
            program.getMain().accept(this);
        }
    }


    @Override
    public void visit(ActorDeclaration actorDeclaration) {
        SymbolTable st;

        //find symbolTable of this actor
        String actorKey = "Actor_" + actorDeclaration.getName().getName();


        try {
            st = ((SymbolTableActorItem) (SymbolTable.root.get(actorKey))).getActorSymbolTable();
        } catch (ItemNotFoundException e) {
            st = new SymbolTable();
            st.setPreSymbolTable(SymbolTable.top);
        }

        if (st.getPreSymbolTable() != SymbolTable.root) {
            SymbolTable parentSt = st.getPreSymbolTable();


            for (String key : parentSt.getSymbolTableItems().keySet()) {
                SymbolTableItem item = parentSt.getSymbolTableItems().get(key);
                try {
                    st.put(item);
                } catch (ItemExistsException e) {
                }
            }
        }

        if (actorDeclaration.getParentName() != null) {
            String parentKey = "Actor_" + actorDeclaration.getParentName().getName();
            try {
                SymbolTable parent = ((SymbolTableActorItem) SymbolTable.root.get(parentKey)).getActorSymbolTable();
                st.setPreSymbolTable(parent);
            } catch (ItemNotFoundException e) {
            }
        }


        SymbolTable.push(st);

        //visit children nodes
        if (actorDeclaration.getActorVars() != null) {
            for (VarDeclaration actorVar : actorDeclaration.getActorVars()) {
                actorVar.accept(this);
            }
        }

        if (actorDeclaration.getKnownActors() != null) {
            SymbolTable s = new SymbolTable();
            s.setPreSymbolTable(SymbolTable.top);
            SymbolTable.push(s);
            for (VarDeclaration knownActor : actorDeclaration.getKnownActors()) {
                knownActor.accept(this);
            }
            SymbolTable.pop();
        }

        if (actorDeclaration.getMsgHandlers() != null) {
            for (MsgHandlerDeclaration msgHandler : actorDeclaration.getMsgHandlers()) {
                msgHandler.accept(this);
            }
        }

        if (actorDeclaration.getInitHandler() != null) {
            SymbolTable s = new SymbolTable();
            s.setPreSymbolTable(SymbolTable.top);
            actorDeclaration.getInitHandler().accept(this);
            SymbolTable.pop();
        }
        SymbolTable.pop();
    }

    @Override
    public void visit(HandlerDeclaration handlerDeclaration) {

        //create new symbolTable
        SymbolTable st = new SymbolTable();
        st.setPreSymbolTable(SymbolTable.top);


        SymbolTableHandlerItem handlerItem = new SymbolTableHandlerItem(handlerDeclaration);
        String handlerKey = "Handler_" + handlerDeclaration.getName().getName();

        //get actor of this msgHandler
        String actorKey = "Actor_" + SymbolTable.top.getName();
        SymbolTableActorItem actorItem = null;
        ActorDeclaration actor = null;

        try {
            actor = ((SymbolTableActorItem) SymbolTable.root.get(actorKey)).getActorDeclaration();
            try {
                actorItem = ((SymbolTableActorItem) SymbolTable.root.get("Actor_" + actor.getName().getName()));
            } catch (ItemNotFoundException e) {
            }
        } catch (ItemNotFoundException e) {
        }

        while (actorItem != null && actorItem.getParentName() != null && !handlerKey.equals("Handler_initial")) {
            boolean found = true;
            try {
                if (!Objects.equals(actor.getName().getName(), actorItem.getParentName())) {
                    break;
                }
                actorItem = (SymbolTableActorItem) SymbolTable.root.get("Actor_" + actorItem.getParentName());
                try {
                    actorItem.getActorSymbolTable().getInCurrentScope(handlerKey);
                } catch (ItemNotFoundException e) {
                    found = false;
                }

                if (found) {
                    this.errors += 1;
                    this.count += 1;
                    lineErrors.put("Line:" +
                            Integer.toString(handlerDeclaration.getLine()) +
                            ":Redefinition of msghandler " +
                            handlerDeclaration.getName().getName(), handlerDeclaration.getLine());
                    String number = Integer.toString(count);
                    String newName = "$Temp_" + handlerDeclaration.getName().getName() + "_" + number;
                    handlerItem.setName(newName);
                    break;
                } else {
                    try {
                        actorItem = (SymbolTableActorItem) SymbolTable.root.get("Actor_" + actorItem.getParentName());
                    } catch (ItemNotFoundException e) {
                        break;
                    }
                }
            } catch (ItemNotFoundException e) {
                break;
            }
        }

        try {
            SymbolTable.top.put(handlerItem);
        } catch (ItemExistsException e) {
            this.errors += 1;
            this.count += 1;
            lineErrors.put("Line:" +
                    Integer.toString(handlerDeclaration.getLine()) +
                    ":Redefinition of msghandler " +
                    handlerDeclaration.getName().getName(), handlerDeclaration.getLine());
            String number = Integer.toString(count);
            String newName = "$Temp_" + handlerDeclaration.getName().getName() + "_" + number;
            handlerItem.setName(newName);
        }


        st.setName(handlerDeclaration.getName().getName());
        //push new symbolTable
        SymbolTable.push(st);

        //visit children nodes
        if (handlerDeclaration.getArgs() != null) {
            for (VarDeclaration arg : handlerDeclaration.getArgs()) {
                arg.accept(this);
            }
        }

        if (handlerDeclaration.getLocalVars() != null) {
            for (VarDeclaration localVar : handlerDeclaration.getLocalVars()) {
                localVar.accept(this);
            }
        }

        SymbolTable.pop();
    }

    @Override
    public void visit(VarDeclaration varDeclaration) {

        SymbolTableLocalVariableItem variableItem = new SymbolTableLocalVariableItem(varDeclaration);

        String handlerKey = "Variable_" + varDeclaration.getIdentifier().getName();

        //get actor of this msgHandler
        String actorKey = "Actor_" + SymbolTable.top.getName();
        Set<String> visited_ = new HashSet<>();
        SymbolTableActorItem actorItem = null;
        ActorDeclaration actor = null;

        try {
            actor = ((SymbolTableActorItem) SymbolTable.root.get(actorKey)).getActorDeclaration();
            try {
                actorItem = ((SymbolTableActorItem) SymbolTable.root.get("Actor_" + actor.getName().getName()));
            } catch (ItemNotFoundException e) {
            }
        } catch (ItemNotFoundException e) {
        }

        while (actorItem != null && actorItem.getParentName() != null) {
            if (visited_.contains(actorItem.getParentName())) break;
            boolean found = true;
            try {
                actorItem = (SymbolTableActorItem) SymbolTable.root.get("Actor_" + actorItem.getParentName());
                try {
                    actorItem.getActorSymbolTable().getInCurrentScope(handlerKey);
                } catch (ItemNotFoundException e) {
                    found = false;
                }

                if (found) {
                    this.errors += 1;
                    this.count += 1;
                    lineErrors.put("Line:" +
                            Integer.toString(varDeclaration.getLine()) +
                            ":Redefinition of msghandler " +
                            varDeclaration.getIdentifier().getName(), varDeclaration.getLine());
                    String number = Integer.toString(count);
                    String newName = "$Temp_" + varDeclaration.getIdentifier().getName() + "_" + number;
                    variableItem.setName(newName);
                    break;
                } else {
                    try {
                        actorItem = (SymbolTableActorItem) SymbolTable.root.get("Actor_" + actorItem.getParentName());
                    } catch (ItemNotFoundException e) {
                        break;
                    }
                }
                visited_.add(actorItem.getActorDeclaration().getName().getName());
            } catch (ItemNotFoundException e) {
                break;
            }
        }

        try {
            SymbolTable.top.put(variableItem);
        } catch (ItemExistsException e1) {
            this.count += 1;
            lineErrors.put(
                    "Line:" + Integer.toString(varDeclaration.getLine()) + ":Redefinition of variable "
                            + varDeclaration.getIdentifier().getName(), varDeclaration.getLine()
            );
            this.errors += 1;

            String number = Integer.toString(this.count);
            String newName = "$Temp_" + varDeclaration.getIdentifier().getName() + "_" + number;
            variableItem.setName(newName);
            try {
                SymbolTable.top.put(variableItem);
            } catch (ItemExistsException e2) {
            }
        }
    }

    @Override
    public void visit(Main mainActors) {
    }

    @Override
    public void visit(ActorInstantiation actorInstantiation) {
    }

    @Override
    public void visit(UnaryExpression unaryExpression) {
    }

    @Override
    public void visit(BinaryExpression binaryExpression) {
    }

    @Override
    public void visit(ArrayCall arrayCall) {
    }

    @Override
    public void visit(ActorVarAccess actorVarAccess) {
    }

    @Override
    public void visit(Identifier identifier) {
    }

    @Override
    public void visit(Self self) {
    }

    @Override
    public void visit(Sender sender) {
    }

    @Override
    public void visit(BooleanValue value) {
    }

    @Override
    public void visit(IntValue value) {
    }

    @Override
    public void visit(StringValue value) {
    }

    @Override
    public void visit(Block block) {
        SymbolTable st = new SymbolTable();
        st.setPreSymbolTable(SymbolTable.top);
        SymbolTable.push(st);

        if (block.getStatements() != null) {
            for (Statement stmt : block.getStatements()) {
                stmt.accept(this);
            }
        }
    }

    @Override
    public void visit(Conditional conditional) {
    }

    @Override
    public void visit(For loop) {
    }

    @Override
    public void visit(Break breakLoop) {
    }

    @Override
    public void visit(Continue continueLoop) {
    }

    @Override
    public void visit(MsgHandlerCall msgHandlerCall) {
    }

    @Override
    public void visit(Print print) {
    }

    @Override
    public void visit(Assign assign) {
    }
}

