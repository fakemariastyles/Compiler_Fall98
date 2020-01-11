package main.visitor.codeGenerator;

import main.ast.node.Main;
import main.ast.node.Program;
import main.ast.node.declaration.ActorDeclaration;
import main.ast.node.declaration.ActorInstantiation;
import main.ast.node.declaration.VarDeclaration;
import main.ast.node.declaration.handler.HandlerDeclaration;
import main.ast.node.expression.*;
import main.ast.node.expression.operators.BinaryOperator;
import main.ast.node.expression.operators.UnaryOperator;
import main.ast.node.expression.values.BooleanValue;
import main.ast.node.expression.values.IntValue;
import main.ast.node.expression.values.StringValue;
import main.ast.node.statement.*;
import main.symbolTable.SymbolTable;
import main.symbolTable.SymbolTableActorItem;
import main.symbolTable.SymbolTableHandlerItem;
import main.symbolTable.SymbolTableMainItem;
import main.symbolTable.itemException.ItemNotFoundException;
import main.symbolTable.symbolTableVariableItem.SymbolTableVariableItem;
import main.visitor.Visitor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

public class CodeGenerator implements Visitor {

    private FileWriter bufferedWriter = null;

    private String currentActorName = "";
    private Stack<String> forLoops = new Stack<>();
    private Stack<String> endForLoops = new Stack<>();
    private boolean isLeftOfAssign = false;
    private int varIndex = 0;
    private Stack<HandlerDeclaration> send_foo = new Stack<>();
    private boolean isField = false;
    private String callInstance = "";
    private boolean isBrokenLoop = false;
    private int lastIndexOfVar = 0;
    private String insNode = "";
    private boolean isInAssign = false;

    private int index = 0;

    private void fileMaker(String fileName) {
        try {
            if (bufferedWriter != null) bufferedWriter.close();
            boolean file = new File("jasmin").mkdirs();
            bufferedWriter = new FileWriter("./jasmin/" + fileName + ".j", true);
        } catch (IOException e) {
            System.out.println("there is something wrong in classFileMaker");
        }
    }

    private void initDefaultActorFile() {
        try {
            fileMaker("DefaultActor");
            bufferedWriter.write(".class public DefaultActor\n");
            bufferedWriter.write(".super java/lang/Thread\n");
            bufferedWriter.write(".method public <init>()V\n");
            bufferedWriter.write(".limit stack 1\n");
            bufferedWriter.write(".limit locals 1\n");
            bufferedWriter.write("aload_0\n");
            bufferedWriter.write("invokespecial java/lang/Thread/<init>()V\n");
            bufferedWriter.write("return\n");
            bufferedWriter.write(".end method\n\n");

            for (HandlerDeclaration call : send_foo) {
                bufferedWriter.write(".method public send_" + call.getName().getName());
                StringBuilder args = new StringBuilder();
                for (int i = 0; i < call.getArgs().size(); i++) {
                    if (i == call.getArgs().size() - 1) {
                        args.append(call.getArgs().get(i).getType().getCode());
                    } else {
                        args.append(call.getArgs().get(i).getType().getCode()).append(";");
                    }
                }
                bufferedWriter.write("(LActor;" + args + ")V\n");
                bufferedWriter.write(".limit stack 2\n");
                bufferedWriter.write(".limit locals 3\n");
                bufferedWriter.write("getstatic java/lang/System/out Ljava/io/PrintStream;\n");
                bufferedWriter.write("ldc \"there is no msghandler named foo in sender\"\n");
                bufferedWriter.write("invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V\n");
                bufferedWriter.write("return\n");
                bufferedWriter.write(".end method\n\n");
            }
            bufferedWriter.flush();
        } catch (IOException e) {
            System.out.println("initDefaultActorFile");
        }
    }

    @Override
    public void visit(Program program) {
        SymbolTable.push(SymbolTable.root);
        for (ActorDeclaration actorDeclaration : program.getActors()) {
            actorDeclaration.accept(this);
        }
        if (program.getMain() != null) {
            program.getMain().accept(this);
        }
        SymbolTable.pop();
        initDefaultActorFile();
    }

    @Override
    public void visit(ActorDeclaration actorDeclaration) {
        try {
            currentActorName = actorDeclaration.getName().getName();
            SymbolTableActorItem actorItem = (SymbolTableActorItem) SymbolTable.root.get("Actor_" + currentActorName);
            SymbolTable.push(actorItem.getActorSymbolTable());

            fileMaker(currentActorName);
            bufferedWriter.write(".class public " + currentActorName + "\n");
            bufferedWriter.write(".super Actor\n\n");

            bufferedWriter.flush();
            for (VarDeclaration knownActor : actorDeclaration.getKnownActors()) {
                isField = true;
                knownActor.accept(this);
                isField = false;
            }
            for (VarDeclaration localVar : actorDeclaration.getActorVars()) {
                isField = true;
                localVar.accept(this);
                isField = false;
            }
            bufferedWriter.write("\n");

            //init
            bufferedWriter.write(".method public <init>(I)V\n");
            bufferedWriter.write(".limit stack 2\n" + ".limit locals 2\n");
            bufferedWriter.write("aload_0\n");
            bufferedWriter.write("iload_1\n");
            bufferedWriter.write("invokespecial Actor/<init>(I)V\n");
            bufferedWriter.write("return\n" + ".end method\n\n");

            if (actorDeclaration.getInitHandler() != null) {
                actorDeclaration.getInitHandler().accept(this);
            }
            bufferedWriter.write("\n");
            //setKnownActors
            String knownArgs = "";
            for (VarDeclaration knownActor : actorDeclaration.getKnownActors()) {
                knownArgs += knownActor.getType().getCode() + ";";
            }
            bufferedWriter.write(".method public setKnownActors(" + knownArgs + ")V\n");
            bufferedWriter.write(".limit stack 16\n" + ".limit locals " + (actorDeclaration.getKnownActors().size() + 1) + "\n");
            int index = 1;
            for (VarDeclaration knownActor : actorDeclaration.getKnownActors()) {
                bufferedWriter.write("aload_0\n");
                bufferedWriter.write("aload_" + index + "\n");
                index++;
                bufferedWriter.write("putfield " + currentActorName + "/" + knownActor.getIdentifier().getName() + " " + knownActor.getType().getCode() + ";\n");
            }
            bufferedWriter.write("return\n" + ".end method\n\n");

            bufferedWriter.flush();
            for (HandlerDeclaration handlerDeclaration : actorDeclaration.getMsgHandlers()) {
                send_foo.push(handlerDeclaration);
                handlerDeclaration.accept(this);
            }
            SymbolTable.pop();
        } catch (ItemNotFoundException | IOException e) {
            System.out.println("actorDeclaration " + actorDeclaration.getName().getName());
        }
    }

    private void send_handler(HandlerDeclaration handlerDeclaration, String args) {
        if (handlerDeclaration.getName().getName().equals("initial")) return;
        try {
            String name = handlerDeclaration.getName().getName();
            bufferedWriter.write(".method public send_" + name + "(LActor;" + args + ")V\n");
            bufferedWriter.write(".limit stack 16\n" + ".limit locals " + (handlerDeclaration.getArgs().size() + 2) + "\n");
            bufferedWriter.write("aload_0\n");
            bufferedWriter.write("new " + currentActorName + "_" + name + "\n");
            bufferedWriter.write("dup\n");
            //this
            bufferedWriter.write("aload_0\n");
            //sender
            bufferedWriter.write("aload_1\n");
            int index = 2;
            for (VarDeclaration v : handlerDeclaration.getArgs()) {
                String type = v.getType().toString();
                if (type.equals("int") || type.equals("boolean")) {
                    bufferedWriter.write("iload_" + index + "\n");
                }
                if (type.equals("int[]")) {
                    bufferedWriter.write("aaload_" + index + "\n");
                }
                if (type.equals("string")) {
                    bufferedWriter.write("aload_" + index + "\n");
                }
                index++;
            }
            String handlerName = currentActorName + "_" + handlerDeclaration.getName().getName();
            bufferedWriter.write("invokespecial " + handlerName + "/<init>(L" + currentActorName + ";LActor;" + args + ")V\n");
            bufferedWriter.write("invokevirtual " + currentActorName + "/send(LMessage;)V\n");
            bufferedWriter.write("return\n" + ".end method\n\n");
        } catch (IOException e) {
            System.out.println("send_foo");
        }
    }

    private void Actor_handler(HandlerDeclaration handlerDeclaration, String args) {
        //checked
        try {
            if (handlerDeclaration.getName().getName().equals("initial")) return;
            String name = currentActorName + "_" + handlerDeclaration.getName().getName();
            fileMaker(name);
            bufferedWriter.write(".class public " + name + "\n");
            bufferedWriter.write(".super Message\n\n");

            for (VarDeclaration var : handlerDeclaration.getArgs()) {
                bufferedWriter.write(".field private " + var.getIdentifier().getName() + " " + var.getType().getCode() + "\n");
            }
            bufferedWriter.write(".field private receiver L" + currentActorName + ";\n");
            bufferedWriter.write(".field private sender LActor;\n\n");

            bufferedWriter.write(".method public <init>(L" + currentActorName + ";LActor;" + args + ")V\n");
            //because this sender receiver :)
            bufferedWriter.write(".limit stack 16\n" + ".limit locals " + (handlerDeclaration.getArgs().size() + 3) + "\n");
            //this and init parent
            bufferedWriter.write("aload_0\n");
            bufferedWriter.write("invokespecial Message/<init>()V\n");

            //receiver
            bufferedWriter.write("aload_0\n");
            bufferedWriter.write("aload_1\n");
            bufferedWriter.write("putfield " + name + "/receiver L" + currentActorName + ";\n");

            //sender
            bufferedWriter.write("aload_0\n");
            bufferedWriter.write("aload_2\n");
            bufferedWriter.write("putfield " + name + "/sender LActor;\n");

            int index = 3;
            //other args
            for (VarDeclaration var : handlerDeclaration.getArgs()) {
                bufferedWriter.write("aload_0\n");
                String type = var.getType().toString();
                if (type.equals("int") || type.equals("boolean")) {
                    bufferedWriter.write("iload_" + index + "\n");
                }
                if (type.equals("int[]")) {
                    bufferedWriter.write("aaload_" + index + "\n");
                }
                if (type.equals("string")) {
                    bufferedWriter.write("aload_" + index + "\n");
                }
                bufferedWriter.write("putfield " + name + "/" + var.getIdentifier().getName() + " " + var.getType().getCode() + "\n");
                index++;
            }
            bufferedWriter.write("return\n.end method\n\n");

            //execute
            bufferedWriter.write(".method public execute()V\n" + ".limit stack 16\n" + ".limit locals " + (handlerDeclaration.getArgs().size() + 3) + "\n");
            bufferedWriter.write("aload_0\n");
            bufferedWriter.write("getfield " + name + "/receiver L" + currentActorName + ";\n");
            bufferedWriter.write("aload_0\n");
            bufferedWriter.write("getfield " + name + "/sender LActor;\n");

            for (VarDeclaration var : handlerDeclaration.getArgs()) {
                bufferedWriter.write("aload_0\n");
                bufferedWriter.write("getfield " + name + "/" + var.getIdentifier().getName() + " " + var.getType().getCode() + "\n");
            }

            bufferedWriter.write("invokevirtual " + currentActorName + "/" + handlerDeclaration.getName().getName() + "(LActor;" + args + ")V\n");
            bufferedWriter.write("return\n" + ".end method\n");
        } catch (IOException e) {
            System.out.println("A_foo");
        }
    }

    private void handler(HandlerDeclaration handlerDeclaration, String args) {
        try {
            String name = handlerDeclaration.getName().getName();
            SymbolTableHandlerItem handlerItem = (SymbolTableHandlerItem) SymbolTable.top.get("Handler_" + name);
            if (name.equals("initial")) {
                bufferedWriter.write(".method public " + name + "(" + args + ")V\n");
            } else {
                bufferedWriter.write(".method public " + name + "(LActor;" + args + ")V\n");
            }
            bufferedWriter.write(".limit stack 16\n" + ".limit locals " + (handlerDeclaration.getArgs().size() +
                    handlerDeclaration.getLocalVars().size() + 2) + "\n");
            //one for sender
            lastIndexOfVar += handlerDeclaration.getArgs().size() + 1;
            SymbolTable.push(handlerItem.getHandlerSymbolTable());
            for (Statement statement : handlerDeclaration.getBody()) {
                statement.accept(this);
            }
            SymbolTable.pop();
            lastIndexOfVar = 0;
            bufferedWriter.write("return\n" + ".end method\n\n");
        } catch (IOException | ItemNotFoundException e) {
            System.out.println("foo");
        }
    }

    @Override
    public void visit(HandlerDeclaration handlerDeclaration) {
        String args = "";
        for (int i = 0; i < handlerDeclaration.getArgs().size(); i++) {
            if (i == handlerDeclaration.getArgs().size() - 1) {
                args += handlerDeclaration.getArgs().get(i).getType().getCode();
            } else {
                args += handlerDeclaration.getArgs().get(i).getType().getCode() + ";";
            }
        }
        Actor_handler(handlerDeclaration, args);
        fileMaker(currentActorName);
        send_handler(handlerDeclaration, args);
        handler(handlerDeclaration, args);
    }

    @Override
    public void visit(VarDeclaration varDeclaration) {
        String type = varDeclaration.getType().getCode();
        try {
            if (isField) {
                bufferedWriter.write(".field " + varDeclaration.getIdentifier().getName() + " ");
                if (!type.equals("int") && !type.equals("boolean") && !type.equals("int[]") && !type.equals("string")) {
                    //is knownActor
                    bufferedWriter.write(type + ";\n");
                } else {
                    bufferedWriter.write(varDeclaration.getType().getCode() + ";\n");
                }
                isField = false;
            }
        } catch (IOException e) {
            System.out.println("varDeclaration " + varDeclaration.getIdentifier().getName());
        }
    }

    @Override
    public void visit(Main mainActors) {
        try {
            fileMaker("Main");
            bufferedWriter.write(".class public Main\n" + ".super java/lang/Object\n\n");
            bufferedWriter.write(".method public <init>()V\n" + ".limit stack 1\n");
            bufferedWriter.write(".limit locals 1\n" + "0: aload_0\n" + "1: invokespecial java/lang/Object/<init>()V\n");
            bufferedWriter.write("4: return\n" + ".end method\n\n");
            bufferedWriter.write(".method public static main([Ljava/lang/String;)V\n");
            bufferedWriter.write(".limit stack 16\n" + ".limit locals " + (mainActors.getMainActors().size() + 1) + "\n");
            SymbolTableMainItem st = (SymbolTableMainItem) SymbolTable.root.get("Main_main");
            SymbolTable.push(st.getMainSymbolTable());
            varIndex = 1;
            for (ActorInstantiation actorInstantiation : mainActors.getMainActors()) {
                actorInstantiation.accept(this);
                varIndex++;
            }
            varIndex = 0;

            //set knownActors
            int index = 1;
            for (ActorInstantiation actorInstantiation : mainActors.getMainActors()) {
                SymbolTableActorItem actorItem = (SymbolTableActorItem) SymbolTable.root.get("Actor_" + actorInstantiation.getType().toString());
                String actorName = actorItem.getActorDeclaration().getName().getName();
                ArrayList<VarDeclaration> knownActors = actorItem.getActorDeclaration().getKnownActors();
                String args = "";
                for (VarDeclaration v : knownActors) {
                    args += "L" + v.getType().toString() + ";";
                }
                bufferedWriter.write("aload_" + index + "\n");
                for (int i = 0; i < actorInstantiation.getKnownActors().size(); i++) {
                    Identifier act = actorInstantiation.getKnownActors().get(i);

                    int knownIndex = 0;
                    for (int j = 0; j < mainActors.getMainActors().size(); j++) {
                        if (mainActors.getMainActors().get(j).getIdentifier().toString().equals(act.toString())) {
                            knownIndex = j + 1;
                        }
                    }
                    bufferedWriter.write("aload_" + knownIndex + "\n");
                }
                bufferedWriter.write("invokevirtual " + actorName + "/setKnownActors(" + args + ")V\n");
                index++;
            }

            //initials
            for (ActorInstantiation actorInstantiation : mainActors.getMainActors()) {
                SymbolTableActorItem actorItem = (SymbolTableActorItem) SymbolTable.root.get("Actor_" + actorInstantiation.getType().toString());
                if (actorItem.getActorDeclaration().getInitHandler() != null) {
                    String args = "";
                    int instanceIndex = 0;
                    for (int j = 0; j < mainActors.getMainActors().size(); j++) {
                        if (mainActors.getMainActors().get(j).getIdentifier().toString().equals(actorInstantiation.getIdentifier().toString())) {
                            instanceIndex = j + 1;
                        }
                    }
                    bufferedWriter.write("aload_" + instanceIndex + "\n");
                    for (int i = 0; i < actorItem.getActorDeclaration().getInitHandler().getArgs().size(); i++) {
                        VarDeclaration v = actorItem.getActorDeclaration().getInitHandler().getArgs().get(i);
                        if (i != actorItem.getActorDeclaration().getInitHandler().getArgs().size() - 1) {
                            args += v.getType().getCode() + ";";
                        } else {
                            args += v.getType().getCode();
                        }
                    }
                    bufferedWriter.write("invokevirtual " + actorItem.getActorDeclaration().getName().getName() +
                            "/initial(" + args + ")V\n");
                }
            }

            //start
            index = 1;
            for (ActorInstantiation actorInstantiation : mainActors.getMainActors()) {
                SymbolTableActorItem actorItem = (SymbolTableActorItem) SymbolTable.root.get("Actor_" + actorInstantiation.getType().toString());
                bufferedWriter.write("aload_" + index + "\n");
                bufferedWriter.write("invokevirtual " + actorItem.getActorDeclaration().getName().getName() +
                        "/start()V\n");
                index++;
            }

            bufferedWriter.write("return\n" + ".end method\n");
            SymbolTable.pop();
        } catch (ItemNotFoundException | IOException e) {
            System.out.println("main");
        }
    }

    @Override
    public void visit(ActorInstantiation actorInstantiation) {
        try {
            bufferedWriter.write("new " + actorInstantiation.getType().toString() + "\n");
            bufferedWriter.write("dup\n");

            SymbolTableActorItem actorItem = (SymbolTableActorItem) SymbolTable.root.get("Actor_" + actorInstantiation.getType().toString());
            bufferedWriter.write("iconst_" + actorItem.getActorDeclaration().getQueueSize() + "\n");
            bufferedWriter.write("invokespecial " + actorInstantiation.getType().toString() + "/<init>(I)V\n");
            bufferedWriter.write("astore_" + varIndex + "\n");
        } catch (IOException | ItemNotFoundException e) {
            System.out.println("actorInstantiation " + actorInstantiation.getIdentifier().getName());
        }
    }

    @Override
    public void visit(UnaryExpression unaryExpression) {
        unaryExpression.getOperand().accept(this);
        UnaryOperator op = unaryExpression.getUnaryOperator();
        try {
            switch (op) {
                case not:
                    bufferedWriter.write("ifne NOT" + index + "\n");
                    bufferedWriter.write("iconst_1\n");
                    bufferedWriter.write("goto NOT_COMPLETE" + index + 1 + "\n");
                    bufferedWriter.write("NOT" + index + ":\n");
                    index++;
                    bufferedWriter.write("iconst_0\n");
                    bufferedWriter.write("NOT_COMPLETE" + index + ":\n");
                    index++;
                    break;
                case minus:
                    bufferedWriter.write("ineg\n");
                    break;
                case preinc:

                case postinc:
                    bufferedWriter.write("iload_1\n");
                    bufferedWriter.write("iadd\n");
                    break;

                case predec:
                case postdec:
                    bufferedWriter.write("iload_1\n");
                    bufferedWriter.write("isub\n");
                    break;
            }
        } catch (IOException e) {
            System.out.println("unaryExpression " + unaryExpression.getUnaryOperator());
        }
    }

    @Override
    public void visit(BinaryExpression binaryExpression) {
        BinaryOperator op = binaryExpression.getBinaryOperator();
        String type = binaryExpression.getType().toString();

        try {
            switch (op) {
                case assign:
                    isLeftOfAssign = true;
                    binaryExpression.getLeft().accept(this);
                    isLeftOfAssign = false;
                    binaryExpression.getRight().accept(this);
                    try {
                        bufferedWriter.write(insNode);
                        insNode = "";
                    } catch (IOException e) {
                        System.out.println("assign");
                    }
                    binaryExpression.getRight().accept(this);
                case eq:
                    binaryExpression.getLeft().accept(this);
                    binaryExpression.getRight().accept(this);
                    if (type.equals("int") || type.equals("boolean")) {
                        bufferedWriter.write("if_icmpne NOT_EQUAL" + index + "\n");
                        bufferedWriter.write("iconst_1\n");
                        bufferedWriter.write("goto EQUAL_COMPLETE" + (index + 1) + "\n");
                        bufferedWriter.write("NOT_EQUAL" + index + ":");
                        bufferedWriter.write("iconst_0\n");
                        index++;
                        bufferedWriter.write("EQUAL_COMPLETE" + index + ":\n");
                        index++;
                    } else {
                        bufferedWriter.write("invokevirtual " + "java/lang/Object.equals" + "(Ljava/lang/Object;)Z\n");
                    }
                    break;
                case neq:
                    binaryExpression.getLeft().accept(this);
                    binaryExpression.getRight().accept(this);
                    if (type.equals("int") || type.equals("boolean")) {
                        bufferedWriter.write("if_icmpne NOT_EQUAL" + index + "\n");
                        bufferedWriter.write("iconst_0\n");
                        bufferedWriter.write("goto EQUAL_COMPLETE" + (index + 1) + "\n");
                        bufferedWriter.write("NOT_EQUAL" + index + ":");
                        bufferedWriter.write("iconst_1\n");
                        index++;
                        bufferedWriter.write("EQUAL_COMPLETE" + index + ":\n");
                        index++;
                    } else {
                        bufferedWriter.write("invokevirtual " + "java/lang/Object.equals" + "(Ljava/lang/Object;)Z\n");
                        bufferedWriter.write("ifeq IS_ZERO" + index + "\n");
                        bufferedWriter.write("iconst_0\n");
                        bufferedWriter.write("goto COMPLETE" + index + 1 + "\n");
                        bufferedWriter.write("IS_ZERO" + index + ":");
                        index++;
                        bufferedWriter.write("iconst_1\n");
                        bufferedWriter.write("COMPLETE" + index + ":\n");
                        index++;
                    }
                    break;
                case gt:
                    binaryExpression.getLeft().accept(this);
                    binaryExpression.getRight().accept(this);
                    bufferedWriter.write("if_icmpgt GT" + index + "\n");
                    bufferedWriter.write("iconst_0\n");
                    bufferedWriter.write("goto GT_COMPLETE" + (index + 1) + "\n");
                    bufferedWriter.write("GT" + index + ":");
                    bufferedWriter.write("iconst_1\n");
                    index++;
                    bufferedWriter.write("GT_COMPLETE" + index + ":\n");
                    index++;
                    break;
                case lt:
                    binaryExpression.getLeft().accept(this);
                    binaryExpression.getRight().accept(this);
                    bufferedWriter.write("if_icmplt LT" + index + "\n");
                    bufferedWriter.write("iconst_0\n");
                    bufferedWriter.write("goto LT_COMPLETE" + (index + 1) + "\n");
                    bufferedWriter.write("LT" + index + ":");
                    bufferedWriter.write("iconst_1\n");
                    index++;
                    bufferedWriter.write("LT_COMPLETE" + index + ":\n");
                    index++;
                    break;
                case add:
                    binaryExpression.getLeft().accept(this);
                    binaryExpression.getRight().accept(this);
                    bufferedWriter.write("iadd\n");
                    break;
                case sub:
                    binaryExpression.getLeft().accept(this);
                    binaryExpression.getRight().accept(this);
                    bufferedWriter.write("isub\n");
                    break;
                case mult:
                    binaryExpression.getLeft().accept(this);
                    binaryExpression.getRight().accept(this);
                    bufferedWriter.write("imul\n");
                    break;
                case div:
                    binaryExpression.getLeft().accept(this);
                    binaryExpression.getRight().accept(this);
                    bufferedWriter.write("idiv\n");
                    break;
                case mod:
                    binaryExpression.getLeft().accept(this);
                    binaryExpression.getRight().accept(this);
                    bufferedWriter.write("irem\n");
                    break;
                case and:
                    binaryExpression.getLeft().accept(this);
                    binaryExpression.getRight().accept(this);
                    bufferedWriter.write("iand\n");
                    break;
                case or:
                    binaryExpression.getLeft().accept(this);
                    binaryExpression.getRight().accept(this);
                    bufferedWriter.write("ior\n");
                    break;
            }
        } catch (IOException e) {
            System.out.println("binaryExpression " + binaryExpression.getBinaryOperator());
        }
    }

    @Override
    public void visit(ArrayCall arrayCall) {
        boolean isLeft = isLeftOfAssign;
        isLeftOfAssign = false;

        arrayCall.getArrayInstance().accept(this);

        try {
            //because we only have int[]
            if (!isLeft) {
                //iaload is used for loading an int from an array
                bufferedWriter.write("iaload\n");
            } else {
                bufferedWriter.write("iastore\n");
            }
        } catch (IOException e) {
            System.out.println("arrayCall");
        }
    }

    @Override
    public void visit(ActorVarAccess actorVarAccess) {
        String type = actorVarAccess.getType().toString();
        try {

            if (!isLeftOfAssign) {
                bufferedWriter.write("aload_0\n");
                bufferedWriter.write("getfield " + currentActorName + "/" + actorVarAccess.getVariable().getName()
                        + " " + type + "\n");
            } else {
                bufferedWriter.write("putfield " + currentActorName + "/" + actorVarAccess.getVariable().getName()
                        + " " + type + "\n");
            }
        } catch (IOException e) {
            System.out.println("actorVarAccess " + actorVarAccess.getVariable().getName());
        }

    }

    @Override
    public void visit(Identifier identifier) {
        try {
            boolean isL = isLeftOfAssign;
            try {
                //is variable
                SymbolTableVariableItem variableItem = (SymbolTableVariableItem) SymbolTable.top.getInCurrentScope("Variable_" + identifier.getName());
                String type = variableItem.getVarDeclaration().getType().toString();

                SymbolTableHandlerItem handlerItem = (SymbolTableHandlerItem) SymbolTable.top.get("Handler_" + SymbolTable.top.getName());

                int index = 0;

                if (handlerItem.getHandlerDeclaration().getName().getName().equals("initial")) {
                    index = 1;
                } else {
                    index = 2;
                }

                for (VarDeclaration var : handlerItem.getHandlerDeclaration().getArgs()) {
                    if (identifier.getName().equals(var.getIdentifier().getName())) {
                        index += handlerItem.getHandlerDeclaration().getArgs().indexOf(var);
                    }
                }
                for (VarDeclaration var : handlerItem.getHandlerDeclaration().getLocalVars()) {
                    if (identifier.getName().equals(var.getIdentifier().getName())) {
                        index += handlerItem.getHandlerDeclaration().getLocalVars().indexOf(var);
                    }
                }

                if (!isL) {
                    if (type.equals("int") || type.equals("boolean")) {
                        bufferedWriter.write("iload_" + lastIndexOfVar + "\n");
                    } else {
                        bufferedWriter.write("aload_" + lastIndexOfVar + "\n");
                    }
                } else {
                    if (type.equals("int") || type.equals("boolean")) {
                        bufferedWriter.write("istore_" + index + "\n");
                    } else {
                        bufferedWriter.write("astore_" + index + "\n");
                    }
                }
                bufferedWriter.flush();
            } catch (ItemNotFoundException e1) {
                //is not in this scope :)
                //is field
                SymbolTableActorItem actorItem = (SymbolTableActorItem) SymbolTable.top.get("Actor_" + currentActorName);
                SymbolTableVariableItem variableItem = (SymbolTableVariableItem) actorItem.getActorSymbolTable().get("Variable_" + identifier.getName());
                bufferedWriter.write("aload_0\n");
                if (!isLeftOfAssign) {
                    bufferedWriter.write("getfield " + currentActorName + "/" + identifier.getName() + " " +
                            identifier.getType().getCode() + ";\n");
                } else {
                    isField = true;
                    insNode = "putfield " + currentActorName + "/" + identifier.getName() + " "
                            + variableItem.getVarDeclaration().getType().getCode() + "\n";
                }
            }
        } catch (IOException | ItemNotFoundException e) {
            System.out.println("identifier " + identifier.getName());
        }

    }

    @Override
    public void visit(Self self) {
        try {
            bufferedWriter.flush();
        } catch (IOException e) {
            System.out.println("self");
        }
    }

    @Override
    public void visit(Sender sender) {
        try {
            bufferedWriter.write("aload_1\n");
        } catch (IOException e) {
            System.out.println("sender");
        }
    }

    @Override
    public void visit(BooleanValue value) {
        try {
            if (value.getConstant()) {
                bufferedWriter.write("iconst_1\n");
            } else {
                bufferedWriter.write("iconst_0\n");
            }
        } catch (IOException e) {
            System.out.println("booleanValue");
        }
    }

    @Override
    public void visit(IntValue value) {
        try {
            bufferedWriter.write("ldc " + value.getConstant() + "\n");
        } catch (IOException e) {
            System.out.println("intValue");
        }
    }

    @Override
    public void visit(StringValue value) {
        try {
            bufferedWriter.write("ldc " + value.getConstant() + "\n");
        } catch (IOException e) {
            System.out.println("stringValue");
        }
    }

    @Override
    public void visit(Block block) {
        //we didn't make new symbol table for blocks
        for (Statement s : block.getStatements()) {
            s.accept(this);
        }
    }

    @Override
    public void visit(Conditional conditional) {
        conditional.getExpression().accept(this);
        try {
            bufferedWriter.write("ifeq ELSE" + index + "\n");
            conditional.getThenBody().accept(this);
            bufferedWriter.write("goto IF_COMPLETE" + (index + 1) + "\n");

            bufferedWriter.write("ELSE" + index + ":");
            if (conditional.getElseBody() != null)
                conditional.getElseBody().accept(this);
            index++;
            bufferedWriter.write("IF_COMPLETE" + index + ":\n");
            index++;
        } catch (IOException e) {
            System.out.println("conditional");
        }
    }

    @Override
    public void visit(For loop) {
        if (loop.getInitialize() != null) {
            loop.getInitialize().accept(this);
        }
        String forLabel = "FOR" + index;
        forLoops.push(forLabel);
        index++;
        String endForLabel = "END_FOR" + index;
        endForLoops.push(endForLabel);
        index++;
        try {
            bufferedWriter.write(forLabel + ":" + "\n");
            if (loop.getCondition() != null) {
                loop.getCondition().accept(this);
                bufferedWriter.write("ifeq " + endForLabel + "\n");
            }
            loop.getBody().accept(this);
            if (loop.getUpdate() != null) {
                loop.getUpdate().accept(this);
            }
            bufferedWriter.write("goto " + forLabel + "\n");
            bufferedWriter.write(endForLabel + ":\n");
        } catch (IOException e) {
            System.out.println("loop");
        }
        if (!isBrokenLoop) {
            forLoops.pop();
            endForLoops.pop();
        } else {
            isBrokenLoop = false;
        }
    }

    @Override
    public void visit(Break breakLoop) {
        try {
            String label = endForLoops.pop();
            forLoops.pop();
            isBrokenLoop = true;
            bufferedWriter.write("goto " + label + "\n");
        } catch (IOException e) {
            System.out.println("breakLoop");
        }
    }

    @Override
    public void visit(Continue continueLoop) {
        try {
            String label = forLoops.pop();
            forLoops.push(label);
            bufferedWriter.write("goto " + label + "\n");
        } catch (IOException e) {
            System.out.println("continueLoop");
        }
    }

    @Override
    public void visit(MsgHandlerCall msgHandlerCall) {
        try {
            String instance = "";
            if (msgHandlerCall.getInstance().toString().equals("Sender")) {
                //first is this , second is sender
                bufferedWriter.write("aload_1\n");
                instance = "Actor";
            } else {
                try {
                    SymbolTableActorItem actorItem = (SymbolTableActorItem) SymbolTable.root.get("Actor_" + currentActorName);
                    for (VarDeclaration knownActor : actorItem.getActorDeclaration().getKnownActors()) {
                        SymbolTableActorItem knownActorItem = (SymbolTableActorItem) SymbolTable.root.get("Actor_" + knownActor.getType().toString());
                        for (HandlerDeclaration handler : knownActorItem.getActorDeclaration().getMsgHandlers()) {
                            if (handler.getName().getName().equals(msgHandlerCall.getMsgHandlerName().getName())) {
                                instance = knownActor.getIdentifier().getName();
                                bufferedWriter.write("aload_0\n");
                                bufferedWriter.write("getfield " + currentActorName + "/" + instance +
                                        " " + knownActor.getType().getCode() + ";\n");
                                break;
                            }
                        }
                    }
                } catch (ItemNotFoundException e) {
                    System.out.println("Couldn't find msgHandlerCall " + msgHandlerCall.getMsgHandlerName().getName());
                }
            }

            bufferedWriter.write("aload_0\n");
            for (Expression e : msgHandlerCall.getArgs()) {
                e.accept(this);
            }

            try {
                if (msgHandlerCall.getInstance().toString().equals("Self")) {
                    SymbolTableHandlerItem handlerItem = (SymbolTableHandlerItem) SymbolTable.top.get("Handler_" + msgHandlerCall.getMsgHandlerName().getName());

                    String args = "";
                    for (int i = 0; i < handlerItem.getHandlerDeclaration().getArgs().size(); i++) {
                        args += handlerItem.getHandlerDeclaration().getArgs().get(i).getType().getCode();
                        if (i != handlerItem.getHandlerDeclaration().getArgs().size() - 1) {
                            args += ";";
                        }
                    }
                    bufferedWriter.write("invokevirtual " + currentActorName + "/" + msgHandlerCall.getMsgHandlerName().getName() + "(" + args + ")" + "V" + "\n");

                } else {
                    StringBuilder args = new StringBuilder("LActor;");
                    if (!instance.equals("Actor")) {
                        SymbolTableActorItem actorItem = (SymbolTableActorItem) SymbolTable.root.get("Actor_" + msgHandlerCall.getInstance().getType().toString());
                        SymbolTableHandlerItem handlerItem = (SymbolTableHandlerItem) actorItem.getActorSymbolTable().get("Handler_" + msgHandlerCall.getMsgHandlerName().getName());

                        for (VarDeclaration e : handlerItem.getHandlerDeclaration().getArgs()) {
                            args.append(e.getType().getCode()).append(";");
                        }
                    }

                    bufferedWriter.write("invokevirtual " + instance + "/" + "send_" + msgHandlerCall.getMsgHandlerName().getName() + "(" + args + ")" + "V" + "\n");
                    bufferedWriter.flush();
                }
            } catch (ItemNotFoundException e) {
                System.out.println("msgHandlerCall " + msgHandlerCall.getMsgHandlerName().getName());
            }
        } catch (IOException e1) {
            System.out.println("msgHandlerCallFile");
        }
    }

    @Override
    public void visit(Print print) {
        String type = print.getArg().getType().toString();
        try {
            bufferedWriter.write("getstatic java/lang/System/out Ljava/io/PrintStream;\n");
            if (type.equals("int[]")) {
                print.getArg().accept(this);
                bufferedWriter.write("invokestatic java/util/Arrays/toString([I)Ljava/lang/String;\n");
                bufferedWriter.write("invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V\n");
            } else {
                print.getArg().accept(this);
                //means we only have int[] array
                bufferedWriter.write("invokevirtual java/io/PrintStream/println(" + print.getArg().getType().getCode() + ")V\n");
            }
        } catch (IOException e) {
            System.out.println("print");
        }
    }

    @Override
    public void visit(Assign assign) {
        try {
            isLeftOfAssign = true;
            assign.getlValue().accept(this);
            if (isField) {
                bufferedWriter.write("aload_0\n");
            }
            isLeftOfAssign = false;
            assign.getrValue().accept(this);
            bufferedWriter.write(insNode);
            insNode = "";
            isLeftOfAssign = false;
        } catch (IOException e) {
            System.out.println("assign");
        }
    }
}
