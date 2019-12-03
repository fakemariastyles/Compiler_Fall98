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
import main.symbolTable.SymbolTableMainItem;
import main.symbolTable.itemException.ItemExistsException;

import java.util.*;


public class FirstNameAnalyzerVisitor implements Visitor {

    private int count = 0;
    private int errors = 0;

    private Map<String, Integer> lineErrors = new HashMap<>();


    public void setLineErrors(Map<String, Integer> lineErrors) {
        this.lineErrors = lineErrors;
    }

    public Map<String, Integer> getLineErrors() {
        return lineErrors;
    }

    public int getErrors() {
        return errors;
    }

    @Override
    public void visit(Program program) {
        SymbolTable.root = new SymbolTable();
        if (program.getActors() != null) {
            for (ActorDeclaration actor : program.getActors()) {
                actor.accept(this);
            }
        }
        if (program.getMain() != null) {
            program.getMain().accept(this);
        }
    }

    @Override
    public void visit(ActorDeclaration actorDeclaration) {
        SymbolTableActorItem actorItem = new SymbolTableActorItem(actorDeclaration);
        SymbolTable st = new SymbolTable();
        st.setName(actorDeclaration.getName().getName());

        st.setPreSymbolTable(SymbolTable.root);
        SymbolTable.push(st);
        actorItem.setActorSymbolTable(SymbolTable.top);

        //catch error of queue size is 0
        int size = actorDeclaration.getQueueSize();
        if (size <= 0) {
            lineErrors.put("Line:" +
                    Integer.toString(actorDeclaration.getLine()) +
                    ":Queue size must be positive", actorDeclaration.getLine());
            actorDeclaration.setQueueSize(0);
        }

        //put actor in root
        try {
            SymbolTable.root.put(actorItem);
        } catch (ItemExistsException e1) {
            this.errors += 1;
            this.count += 1;
            lineErrors.put("Line:" +
                    Integer.toString(actorDeclaration.getLine()) +
                    ":Redefinition of actor " +
                    actorDeclaration.getName().getName(), actorDeclaration.getLine());

            String number = Integer.toString(count);
            String newName = "$Temp_" + actorDeclaration.getName().getName() + "_" + number;
            actorDeclaration.getName().setName(newName);
            actorItem.setName(newName);
            try {
                this.errors += 1;
                SymbolTable.root.put(actorItem);
            } catch (ItemExistsException e2) {
            }
        }

        //visit children
        if (actorDeclaration.getActorVars() != null) {
            for (VarDeclaration actorVar : actorDeclaration.getActorVars()) {
                actorVar.accept(this);
            }
        }

        if (actorDeclaration.getKnownActors() != null) {
            for (VarDeclaration knownActor : actorDeclaration.getKnownActors()) {
                knownActor.accept(this);
            }
        }

        if (actorDeclaration.getMsgHandlers() != null) {
            for (MsgHandlerDeclaration msgHandler : actorDeclaration.getMsgHandlers()) {
                msgHandler.accept(this);
            }
        }

        SymbolTable.pop();
    }

    @Override
    public void visit(HandlerDeclaration handlerDeclaration) {
    }

    @Override
    public void visit(VarDeclaration varDeclaration) {
    }

    @Override
    public void visit(Main mainActors) {
        SymbolTable st = new SymbolTable();
        st.setPreSymbolTable(SymbolTable.top);

        SymbolTableMainItem mainItem = new SymbolTableMainItem(mainActors);
        mainItem.setMainSymbolTable(st);
        try {
            SymbolTable.root.put(mainItem);
        } catch (ItemExistsException e) {
        }

        SymbolTable.push(st);
        SymbolTable.pop();
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
        SymbolTable.pop();
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
