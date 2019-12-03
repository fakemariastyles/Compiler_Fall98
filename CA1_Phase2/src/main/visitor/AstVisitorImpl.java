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

public class AstVisitorImpl implements Visitor {

    @Override
    public void visit(Program program) {
        print(program.toString());
        for (ActorDeclaration i : program.getActors()) {
            i.accept(this);
        }
        program.getMain().accept(this);
    }

    @Override
    public void visit(ActorDeclaration actorDeclaration) {
        print(actorDeclaration.toString());
        actorDeclaration.getName().accept(this);
        if (actorDeclaration.getParentName() != null) {
            actorDeclaration.getParentName().accept(this);
        }
        for (VarDeclaration knownActor : actorDeclaration.getKnownActors()) {
            knownActor.accept(this);
        }
        for (VarDeclaration actorVars : actorDeclaration.getActorVars()) {
            actorVars.accept(this);
        }
        actorDeclaration.getInitHandler().accept(this);
        for (MsgHandlerDeclaration msgHandlerDeclaration : actorDeclaration.getMsgHandlers()) {
            msgHandlerDeclaration.accept(this);
        }
    }

    @Override
    public void visit(HandlerDeclaration handlerDeclaration) {
        print(handlerDeclaration.toString());
        handlerDeclaration.getName().accept(this);
        if (handlerDeclaration.getArgs() != null) {
            for (VarDeclaration arg : handlerDeclaration.getArgs()) {
                arg.accept(this);
            }
        }
        for (VarDeclaration localVar : handlerDeclaration.getLocalVars()) {
            localVar.accept(this);
        }
        for (Statement body : handlerDeclaration.getBody()) {
            body.accept(this);
        }
    }

    @Override
    public void visit(VarDeclaration varDeclaration) {
        print(varDeclaration.toString());
        varDeclaration.getIdentifier().accept(this);
        varDeclaration.getType().accept(this);
    }

    @Override
    public void visit(Main mainActors) {
        print(mainActors.toString());
        for (ActorInstantiation mainActor : mainActors.getMainActors()) {
            mainActor.accept(this);
        }
    }

    @Override
    public void visit(ActorInstantiation actorInstantiation) {
        print(actorInstantiation.toString());
        for (Identifier knownActor : actorInstantiation.getKnownActors()) {
            knownActor.accept(this);
        }
        for (Expression initArg : actorInstantiation.getInitArgs()) {
            initArg.accept(this);
        }
    }

    @Override
    public void visit(UnaryExpression unaryExpression) {
        print(unaryExpression.toString());
        print(unaryExpression.getUnaryOperator().toString());
        unaryExpression.getOperand().accept(this);
    }

    @Override
    public void visit(BinaryExpression binaryExpression) {
        print(binaryExpression.toString());
        binaryExpression.getLeft().accept(this);
        binaryExpression.getRight().accept(this);
    }

    @Override
    public void visit(ArrayCall arrayCall) {
        print(arrayCall.toString());
        arrayCall.getArrayInstance().accept(this);
        arrayCall.getIndex().accept(this);
    }

    @Override
    public void visit(ActorVarAccess actorVarAccess) {
        print(actorVarAccess.toString());
        actorVarAccess.getVariable().accept(this);
        actorVarAccess.getSelf().accept(this);
    }

    @Override
    public void visit(Identifier identifier) {
        print(identifier.toString());
    }

    @Override
    public void visit(Self self) {
        print(self.toString());
    }

    @Override
    public void visit(Sender sender) {
        print(sender.toString());
    }

    @Override
    public void visit(BooleanValue value) {
        print(value.toString());
    }

    @Override
    public void visit(IntValue value) {
        print(value.toString());
    }

    @Override
    public void visit(StringValue value) {
        print(value.toString());
    }

    @Override
    public void visit(Block block) {
        print(block.toString());
        for (Statement stmt : block.getStatements()) {
            stmt.accept(this);
        }
    }

    @Override
    public void visit(Conditional conditional) {
        print(conditional.toString());
        conditional.getExpression().accept(this);
        conditional.getThenBody().accept(this);
        if (conditional.getElseBody() != null) {
            conditional.getElseBody().accept(this);
        }
    }

    @Override
    public void visit(For loop) {
        print(loop.toString());
        loop.getInitialize().accept(this);
        loop.getCondition().accept(this);
        loop.getUpdate().accept(this);
        loop.getBody().accept(this);
    }

    @Override
    public void visit(Break breakLoop) {
        print(breakLoop.toString());
    }

    @Override
    public void visit(Continue continueLoop) {
        print(continueLoop.toString());
    }

    @Override
    public void visit(MsgHandlerCall msgHandlerCall) {
        print(msgHandlerCall.toString());
        msgHandlerCall.getInstance().accept(this);
        msgHandlerCall.getMsgHandlerName().accept(this);
        for (Expression arg : msgHandlerCall.getArgs()) {
            arg.accept(this);
        }
    }

    @Override
    public void visit(Print print) {
        print(print.toString());
        print.getArg().accept(this);
    }

    @Override
    public void visit(Assign assign) {
        print(assign.toString());
        assign.getlValue().accept(this);
        assign.getrValue().accept(this);
    }

    public void print(String str) {
        System.out.println(str);
    }
}
