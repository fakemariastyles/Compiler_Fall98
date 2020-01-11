package main.visitor.TypeChecker;


import main.ast.node.Main;
import main.ast.node.Program;
import main.ast.node.declaration.ActorDeclaration;
import main.ast.node.declaration.ActorInstantiation;
import main.ast.node.declaration.VarDeclaration;
import main.ast.node.declaration.handler.HandlerDeclaration;
import main.ast.node.declaration.handler.MsgHandlerDeclaration;
import main.ast.node.expression.*;
import main.ast.node.expression.operators.BinaryOperator;
import main.ast.node.expression.operators.UnaryOperator;
import main.ast.node.expression.values.BooleanValue;
import main.ast.node.expression.values.IntValue;
import main.ast.node.expression.values.StringValue;
import main.ast.node.statement.*;
import main.ast.type.Type;
import main.ast.type.actorType.ActorType;
import main.ast.type.arrayType.ArrayType;
import main.ast.type.noType.NoType;
import main.ast.type.primitiveType.BooleanType;
import main.ast.type.primitiveType.IntType;
import main.ast.type.primitiveType.StringType;
import main.symbolTable.*;
import main.symbolTable.itemException.ItemExistsException;
import main.symbolTable.itemException.ItemNotFoundException;
import main.symbolTable.symbolTableVariableItem.SymbolTableVariableItem;
import main.visitor.Visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TypeAnalyser implements Visitor {
    private Map<String, Integer> errors = new HashMap<>();

    public Map<String, Integer> getErrors() {
        return errors;
    }

    private int forNum = 0;

    private boolean isLValue = false;

    private String actorName = "";

    private boolean argumentsMatched(ArrayList<VarDeclaration> inputs, ArrayList<Expression> ref) {
        if (inputs.size() != ref.size()) {
            return false;
        }
        for (int i = 0; i < inputs.size(); i++) {
            if (!isSubType(inputs.get(i).getType(), ref.get(i).getType())) {
                return false;
            }
        }
        return true;
    }

    private boolean isSubType(Type child, Type parent) {
        if (child.toString().equals("notype") || parent.toString().equals("notype")) {
            return true;
        }
        if (child.toString().equals(parent.toString())) {
            return true;
        }

        String type = child.toString();
        if (type.equals("int") || type.equals("boolean") || type.equals("string") || type.equals("int[]")) {
            return false;
        }
        type = parent.toString();
        if (type.equals("int") || type.equals("boolean") || type.equals("string") || type.equals("int[]")) {
            return false;
        }
        try {
            SymbolTableActorItem actorItem = (SymbolTableActorItem) SymbolTable.root.get("Actor_" + child.toString());
            String parentName = actorItem.getParentName();
            while (actorItem.getParentName() != null) {
                if (parentName.equals(type)) return true;
                SymbolTable pre = actorItem.getActorSymbolTable().getPreSymbolTable();
                if (pre != null) {
                    actorItem = (SymbolTableActorItem) SymbolTable.root.get("Actor_" + actorItem.getParentName());
                } else {
                    break;
                }
            }
        } catch (ItemNotFoundException e) {
        }
        return false;
    }

    @Override
    public void visit(Program program) {
        for (ActorDeclaration actor : program.getActors()) {
            actorName = actor.getName().getName();
            actor.accept(this);
        }
        if (program.getMain() != null) {
            program.getMain().accept(this);
        }
    }

    @Override
    public void visit(ActorDeclaration actorDeclaration) {
        if (actorDeclaration.getParentName() != null) {
            try {
                SymbolTable.root.get("Actor_" + actorDeclaration.getParentName().getName());
            } catch (ItemNotFoundException e) {
                errors.put("Line:" + actorDeclaration.getLine() + ":actor " + actorDeclaration.getParentName().getName() + " is not declared",
                        actorDeclaration.getLine());
            }
        }
        try {
            SymbolTableActorItem actorItem = (SymbolTableActorItem) SymbolTable.root.get("Actor_" + actorDeclaration.getName().getName());
            SymbolTable st = actorItem.getActorSymbolTable();
            SymbolTable.push(st);
            for (VarDeclaration knownActor : actorDeclaration.getKnownActors()) {
                knownActor.accept(this);
            }
            for (VarDeclaration varDec : actorDeclaration.getActorVars()) {
                varDec.accept(this);
            }
            if (actorDeclaration.getInitHandler() != null) {
                actorDeclaration.getInitHandler().accept(this);
            }
            for (MsgHandlerDeclaration msgHandler : actorDeclaration.getMsgHandlers()) {
                msgHandler.accept(this);
            }
            SymbolTable.pop();
        } catch (ItemNotFoundException e) {
        }
    }

    @Override
    public void visit(HandlerDeclaration handlerDeclaration) {
        try {
            SymbolTableHandlerItem handlerItem = (SymbolTableHandlerItem) SymbolTable.top.get("Handler_" + handlerDeclaration.getName().getName());
            SymbolTable st = handlerItem.getHandlerSymbolTable();
            SymbolTable.push(st);

            for (VarDeclaration arg : handlerDeclaration.getArgs()) {
                arg.accept(this);
            }
            for (VarDeclaration var : handlerDeclaration.getLocalVars()) {
                var.accept(this);
            }
            for (Statement body : handlerDeclaration.getBody()) {
                body.accept(this);
            }
            SymbolTable.pop();
        } catch (ItemNotFoundException e) {
        }
    }

    @Override
    public void visit(VarDeclaration varDeclaration) {
        String type = varDeclaration.getType().toString();
        if (!type.equals("int") && !type.equals("boolean") && !type.equals("int[]") && !type.equals("string") && !type.equals("notype")) {
            //is actorVar in knownActors of an actor
            try {
                SymbolTableActorItem actorItem = (SymbolTableActorItem) SymbolTable.root.get("Actor_" + varDeclaration.getType().toString());
            } catch (ItemNotFoundException e) {
                errors.put("Line:" + varDeclaration.getLine() + ":actor " + varDeclaration.getType().toString() + " is not declared",
                        varDeclaration.getLine());
                varDeclaration.setType(new NoType());
            }
        }
    }

    @Override
    public void visit(Main mainActors) {
        try {
            SymbolTableMainItem main = (SymbolTableMainItem) SymbolTable.root.get("Main_main");
            SymbolTable st = main.getMainSymbolTable();
            SymbolTable.push(st);
            for (ActorInstantiation actorInstantiation : mainActors.getMainActors()) {
                for (Identifier i : actorInstantiation.getKnownActors()) {
                    i.accept(this);
                }
                actorInstantiation.accept(this);
            }
            SymbolTable.pop();
        } catch (ItemNotFoundException e) {
            //main not defined:}
        }
    }

    @Override
    public void visit(ActorInstantiation actorInstantiation) {
        SymbolTableActorItem actorItem = null;
        try {
            actorItem = (SymbolTableActorItem) SymbolTable.root.get("Actor_" + actorInstantiation.getType().toString());
        } catch (ItemNotFoundException e) {
            errors.put("Line:" + actorInstantiation.getLine() + ":actor " + actorInstantiation.getIdentifier().getName() + " is not declared",
                    actorInstantiation.getLine());
            actorInstantiation.setType(new NoType());
        }
        try {
            if (!actorInstantiation.getType().toString().equals("notype") && actorItem != null) {
                ActorDeclaration actor = actorItem.getActorDeclaration();
                if (actorInstantiation.getKnownActors().size() != actor.getKnownActors().size()) {
                    throw new ItemNotFoundException();
                }
                //check knownActorsMatch
                ArrayList<VarDeclaration> knownActors = actor.getKnownActors();
                ArrayList<Identifier> instanceKnownActors = actorInstantiation.getKnownActors();
                for (Identifier knownActor : instanceKnownActors) {
                    knownActor.accept(this);
                }
                for (int i = 0; i < knownActors.size(); i++) {
                    if (!isSubType(instanceKnownActors.get(i).getType(), knownActors.get(i).getType())) {
                        throw new ItemNotFoundException();
                    }
                }
                //check init args
                ArrayList<Expression> instanceInitArgs = actorInstantiation.getInitArgs();
                for (Expression arg : instanceInitArgs) {
                    arg.accept(this);
                }
                if (actor.getInitHandler() != null) {
                    ArrayList<VarDeclaration> initArgs = actor.getInitHandler().getArgs();
                    if (!argumentsMatched(initArgs, instanceInitArgs)) {
                        throw new ItemExistsException();
                    }
                } else {
                    if (instanceInitArgs.size() != 0) {
                        throw new ItemExistsException();
                    }
                }
            }
        } catch (ItemNotFoundException e) {
            errors.put("Line:" + actorInstantiation.getLine() + ":knownactors does not match with definition", actorInstantiation.getLine());
            actorInstantiation.setType(new NoType());
        } catch (ItemExistsException e) {
            errors.put("Line:" + actorInstantiation.getLine() + ":arguments do not match", actorInstantiation.getLine());
            actorInstantiation.setType(new NoType());
        }
    }

    @Override
    public void visit(UnaryExpression unaryExpression) {
        isLValue = false;
        unaryExpression.getOperand().accept(this);

        UnaryOperator op = unaryExpression.getUnaryOperator();

        Expression ex = unaryExpression.getOperand();

        //minus
        if (op == UnaryOperator.minus) {
            if (ex.getType().toString().equals("int")) unaryExpression.setType(new IntType());
            else {
                unaryExpression.setType(new NoType());
                if (!ex.getType().toString().equals("notype")) {
                    errors.put("Line:" + unaryExpression.getLine() + ":unsupported operand type for " + unaryExpression.getUnaryOperator()
                            , unaryExpression.getLine());
                }
            }
        }
        //pre and post
        if (op == UnaryOperator.predec || op == UnaryOperator.preinc || op == UnaryOperator.postinc || op == UnaryOperator.postdec) {
            if (ex.getType().toString().equals("int")) {
                unaryExpression.setType(new IntType());
            } else {
                unaryExpression.setType(new NoType());
                if (op == UnaryOperator.preinc || op == UnaryOperator.postinc) {
                    errors.put("Line:" + unaryExpression.getLine() + ":unsupported operand type for increment", unaryExpression.getLine());
                } else {
                    errors.put("Line:" + unaryExpression.getLine() + ":unsupported operand type for decrement", unaryExpression.getLine());
                }
            }
            if (!isLValue) {
                if (op == UnaryOperator.preinc || op == UnaryOperator.postinc) {
                    errors.put("Line:" + unaryExpression.getLine() + ":lvalue required as increment operand", unaryExpression.getLine());
                } else {
                    errors.put("Line:" + unaryExpression.getLine() + ":lvalue required as decrement operand", unaryExpression.getLine());
                }
                unaryExpression.setType(new NoType());
            }
        }
        //not
        if (op == UnaryOperator.not) {
            if (ex.getType().toString().equals("boolean")) {
                unaryExpression.setType(new BooleanType());
            } else {
                unaryExpression.setType(new NoType());
                errors.put("Line:" + unaryExpression.getLine() + ":unsupported operand type for " + unaryExpression.getUnaryOperator(),
                        unaryExpression.getLine());
            }
        }
    }

    @Override
    public void visit(BinaryExpression binaryExpression) {
        binaryExpression.getRight().accept(this);
        isLValue = false;
        binaryExpression.getLeft().accept(this);


        String left = binaryExpression.getLeft().getType().toString();
        String right = binaryExpression.getRight().getType().toString();

        BinaryOperator op = binaryExpression.getBinaryOperator();
        if (op == BinaryOperator.add || op == BinaryOperator.sub || op == BinaryOperator.div || op == BinaryOperator.mult ||
                op == BinaryOperator.mod || op == BinaryOperator.lt || op == BinaryOperator.gt) {
            if (left.equals("int") && right.equals("int")) {
                binaryExpression.setType(new IntType());
            } else {
                binaryExpression.setType(new NoType());
                if (!left.equals("notype") && !right.equals("notype")) {
                    errors.put("Line:" + binaryExpression.getLine() + ":unsupported operand type for " + binaryExpression.getBinaryOperator(),
                            binaryExpression.getLine());
                }
            }
        }

        //neq eq
        if (op == BinaryOperator.neq || op == BinaryOperator.eq) {
            if (binaryExpression.getLeft().getType().toString().equals(binaryExpression.getRight().getType().toString())) {
                //could be all types :|
                binaryExpression.setType(new BooleanType());
            } else {
                binaryExpression.setType(new NoType());
                if (!left.equals("notype") && !right.equals("notype"))
                    errors.put("Line:" + binaryExpression.getLine() + ":unsupported operand type for " + binaryExpression.getBinaryOperator(),
                            binaryExpression.getLine());
            }
        }

        if (op == BinaryOperator.or || op == BinaryOperator.add) {
            if (binaryExpression.getLeft().getType().toString().equals(binaryExpression.getRight().getType().toString())) {
                //could be all types :|
                binaryExpression.setType(new BooleanType());
            } else {
                binaryExpression.setType(new NoType());
                if (!left.equals("notype") && !right.equals("notype"))
                    errors.put("Line:" + binaryExpression.getLine() + ":unsupported operand type for " + binaryExpression.getBinaryOperator(),
                            binaryExpression.getLine());
            }
        }
    }

    @Override
    public void visit(ArrayCall arrayCall) {
        isLValue = true;
        // index
        arrayCall.getIndex().accept(this);
        String index = arrayCall.getIndex().getType().toString();
        if (!index.equals("int") && !index.equals("notype")) {
            errors.put("Line:" + arrayCall.getLine() + ":unsupported operand type for array index", arrayCall.getLine());
            arrayCall.getIndex().setType(new NoType());
        }
        //array
        arrayCall.getArrayInstance().accept(this);
        String array = arrayCall.getArrayInstance().getType().toString();
        if (!array.equals("int[]") && !array.equals("notype")) {
            errors.put("Line:" + arrayCall.getLine() + " :array is not declared", arrayCall.getLine());
            arrayCall.setType(new NoType());
            return;
        }
        arrayCall.setType(new IntType());
    }

    @Override
    public void visit(ActorVarAccess actorVarAccess) {
        actorVarAccess.getSelf().accept(this);
        isLValue = true;
        if (actorVarAccess.getSelf().getType().toString().equals("notype")) {
            actorVarAccess.setType(new NoType());
            return;
        }
        try {
            SymbolTableActorItem actorItem = (SymbolTableActorItem) SymbolTable.root.get("Actor_" + actorName);
            while (actorItem != null) {
                try {
                    SymbolTable st = actorItem.getActorSymbolTable();
                    SymbolTableVariableItem var = (SymbolTableVariableItem) st.get("Variable_" + actorVarAccess.getVariable().getName());
                    actorVarAccess.setType(var.getVarDeclaration().getType());
                    return;
                } catch (ItemNotFoundException e) {
                }
                try {
                    actorItem = (SymbolTableActorItem) SymbolTable.root.get("Actor_" + actorItem.getParentName());
                } catch (ItemNotFoundException e) {
                    break;
                }
            }
        } catch (ItemNotFoundException e) {
        }
        errors.put("Line:" + actorVarAccess.getLine() + ":variable " + actorVarAccess.getVariable().getName() + " is not declared",
                actorVarAccess.getLine());
        actorVarAccess.setType(new NoType());

    }

    @Override
    public void visit(Identifier identifier) {
        isLValue = true;
        try {
            SymbolTableVariableItem var = (SymbolTableVariableItem) SymbolTable.top.get("Variable_" + identifier.getName());
            identifier.setType(var.getVarDeclaration().getType());
        } catch (ItemNotFoundException e) {
            try {
                SymbolTableActorItem actorItem = null;
                if (SymbolTable.top.getName().equals("main")) {
                    actorItem = (SymbolTableActorItem) SymbolTable.root.get("Actor_" + SymbolTable.top.getName());
                } else {
                    actorItem = (SymbolTableActorItem) SymbolTable.root.get("Actor_" + actorName);
                }
                while (actorItem != null) {
                    try {
                        SymbolTableVariableItem var = (SymbolTableVariableItem) actorItem.getActorSymbolTable().get("Variable_" + identifier.getName());
                        identifier.setType(var.getVarDeclaration().getType());
                        return;
                    } catch (ItemNotFoundException e2) {
                        try {
                            actorItem = (SymbolTableActorItem) SymbolTable.root.get("Actor_" + actorItem.getParentName());
                        } catch (ItemNotFoundException e1) {
                            errors.put("Line:" + identifier.getLine() + ":variable " + identifier.getName() + " is not declared", identifier.getLine());
                            identifier.setType(new NoType());
                            return;
                        }

                    }
                }
            } catch (ItemNotFoundException e1) {
            }
            errors.put("Line:" + identifier.getLine() + ":variable " + identifier.getName() + " is not declared", identifier.getLine());
            identifier.setType(new NoType());
        }
    }

    @Override
    public void visit(Self self) {
        try {
            SymbolTableActorItem actorItem = (SymbolTableActorItem) SymbolTable.root.get("Actor_" + SymbolTable.top.getPreSymbolTable().getName());
            self.setType(new ActorType(actorItem.getActorDeclaration().getName()));
        } catch (ItemNotFoundException e) {
            errors.put("Line:" + self.getLine() + ":self doesn't refer to any actor", self.getLine());
            self.setType(new NoType());
        }
    }

    @Override
    public void visit(Sender sender) {
        // will be available in run time
        if (SymbolTable.top.getName().equals("initial")) {
            errors.put("Line:" + sender.getLine() + ":no sender in initial msghandler", sender.getLine());
        }
        sender.setType(new NoType());
    }

    @Override
    public void visit(BooleanValue value) {
        isLValue = false;
        value.setType(new BooleanType());
    }

    @Override
    public void visit(IntValue value) {
        isLValue = false;
        value.setType(new IntType());
    }

    @Override
    public void visit(StringValue value) {
        isLValue = false;
        value.setType(new StringType());
    }

    @Override
    public void visit(Block block) {
        SymbolTable.push(new SymbolTable());
        for (Statement s : block.getStatements()) {
            s.accept(this);
        }
        SymbolTable.pop();
    }

    @Override
    public void visit(Conditional conditional) {
        conditional.getExpression().accept(this);
        String ex = conditional.getExpression().getType().toString();
        if (!ex.equals("boolean") && !ex.equals("notype")) {
            errors.put("Line:" + conditional.getExpression().getLine() + ":condition type must be Boolean", conditional.getLine());
        }
        SymbolTable.push(new SymbolTable());
        conditional.getThenBody().accept(this);
        SymbolTable.pop();
        if (conditional.getElseBody() != null) {
            SymbolTable.push(new SymbolTable());
            conditional.getElseBody().accept(this);
            SymbolTable.pop();
        }
    }

    @Override
    public void visit(For loop) {
        forNum++;
        if (loop.getInitialize() != null) {
            loop.getInitialize().accept(this);
        }
        if (loop.getUpdate() != null) {
            loop.getUpdate().accept(this);
        }
        if (loop.getCondition() != null) {
            loop.getCondition().accept(this);
            if (!loop.getCondition().getType().toString().equals("boolean") && !loop.getCondition().getType().toString().equals("notype")) {
                errors.put("Line:" + loop.getLine() + ":condition type must be Boolean", loop.getLine());
            }
        }
        SymbolTable.push(new SymbolTable());
        loop.getBody().accept(this);
        forNum--;
        SymbolTable.pop();
    }

    @Override
    public void visit(Break breakLoop) {
        if (forNum == 0) {
            errors.put("Line:" + breakLoop.getLine() + ":break statement not within loop", breakLoop.getLine());
        }
    }

    @Override
    public void visit(Continue continueLoop) {
        if (forNum == 0) {
            errors.put("Line:" + continueLoop.getLine() + ":continue statement not within loop", continueLoop.getLine());
        }
    }

    @Override
    public void visit(MsgHandlerCall msgHandlerCall) {
        //t1.foo()
        //t1
        msgHandlerCall.getInstance().accept(this);
        String instance = msgHandlerCall.getInstance().getType().toString();
        if (instance.equals("int") || instance.equals("boolean") || instance.equals("string") || instance.equals("int[]") || instance.equals("notype")) {
            msgHandlerCall.getInstance().setType(new NoType());
            errors.put("Line:" + msgHandlerCall.getLine() + ":variable " + msgHandlerCall.getInstance().toString() + " is not callable",
                    msgHandlerCall.getLine());
        }
        //foo
        if (msgHandlerCall.getMsgHandlerName().getName().equals("initial")) {
            try {
                SymbolTableActorItem actorItem = (SymbolTableActorItem) SymbolTable.root.get("Actor_" + instance);
                if (!argumentsMatched(actorItem.getActorDeclaration().getInitHandler().getArgs(), msgHandlerCall.getArgs())) {
                    errors.put("Line:" + msgHandlerCall.getLine() + ":arguments do not match with definition",
                            msgHandlerCall.getLine());
                }
            } catch (ItemNotFoundException e) {
                //t1 does not exist
            }
        }

        for (Expression arg : msgHandlerCall.getArgs()) {
            arg.accept(this);
        }

        boolean error = true;
        String name = msgHandlerCall.getMsgHandlerName().getName();
        if (!instance.equals("notype")) {
            try {
                SymbolTableActorItem actorItem = (SymbolTableActorItem) SymbolTable.root.get("Actor_" + instance);
                while (actorItem != null) {
                    // normal msgHandler
                    ArrayList<MsgHandlerDeclaration> msgHandlers = actorItem.getActorDeclaration().getMsgHandlers();
                    for (MsgHandlerDeclaration mh : msgHandlers) {
                        if (mh.getName().getName().equals(name)) {
                            error = false;
                            //args should match
                            if (!argumentsMatched(mh.getArgs(), msgHandlerCall.getArgs())) {
                                error = true;
                                errors.put("Line:" + msgHandlerCall.getLine() + ":arguments do not match with definition",
                                        msgHandlerCall.getLine());
                                return;
                            }
                        }
                    }
                    try {
                        actorItem = (SymbolTableActorItem) SymbolTable.root.get("Actor_" + actorItem.getParentName());
                    } catch (ItemNotFoundException e) {
                        break;
                    }
                }
                if (error) {
                    errors.put("Line:" + msgHandlerCall.getLine() + ":there is no msghandler named " +
                                    msgHandlerCall.getMsgHandlerName().getName() + " in actor " + instance,
                            msgHandlerCall.getLine());
                }
            } catch (ItemNotFoundException e) {
                //t1 does nnt not exist
            }
        }
    }

    @Override
    public void visit(Print print) {
        print.getArg().accept(this);
        String type = print.getArg().getType().toString();
        if (!type.equals("int") && !type.equals("boolean") && !type.equals("string") && !type.equals("int[]")) {
            errors.put("Line:" + print.getLine() + ":unsupported type for print", print.getLine());
        }
    }

    @Override
    public void visit(Assign assign) {
        isLValue = false;
        assign.getlValue().accept(this);
        if (!isLValue) {
            errors.put("Line:" + assign.getLine() + ":left side of assignment must be a valid lvalue", assign.getLine());
        }
        assign.getrValue().accept(this);
        if (!isSubType(assign.getrValue().getType(), assign.getlValue().getType())) {
            errors.put("Line:" + assign.getLine() + ":unsupported operand type for assign", assign.getLine());
        } else {
            if (assign.getrValue().getType().toString().equals("int[]") && assign.getlValue().getType().toString().equals("int[]")) {
                ArrayType typeL = (ArrayType) assign.getlValue().getType();
                ArrayType typeR = (ArrayType) assign.getrValue().getType();
                if (typeL.getSize() != typeR.getSize()) {
                    errors.put("Line:" + assign.getLine() + ":operation assign requires equal array sizes", assign.getLine());
                }
            }
        }
    }
}
