grammar acton;

@header{
    import main.ast.node.declaration.*;
    import main.ast.node.expression.*;
    import main.ast.node.declaration.handler.*;
    import main.ast.node.expression.operators.*;
    import main.ast.node.expression.values.*;
    import main.ast.node.expression.*;
    import main.ast.node.statement.*;
    import main.ast.node.*;
    import main.ast.type.*;
    import main.ast.type.actorType.*;
    import main.ast.type.arrayType.*;
    import main.ast.type.primitiveType.*;
    import java.util.List;
    import java.util.ArrayList;
}

@members{
    void setLine(Node n,int line){
        n.setLine(line);
    }

    void print(String str){
        System.out.println(str);
    }

    void beginScope(){
        SymbolTable.push(new SymbolTable());
    }

    void endScope(){
        SymbolTable.pop();
    }
}

program
    : (actorDeclaration)+ mainDeclaration
    ;

actorDeclaration returns [ActorDeclaration actor]
    :   ACTOR (i=identifier) {$actor = new ActorDeclaration($i.id);}
        (EXTENDS parent=identifier {$actor.setParentName($parent.id);} )?
        LPAREN (queueSize=INTVAL) {
            $actor.setQueueSize(size);
        } RPAREN
        LBRACE

        (KNOWNACTORS
        LBRACE
            ((a=identifier)
            (name=identifier) {$actor.addKnownActor(new VarDeclaration($name.id, new ActorType($a.id)));}
            SEMICOLON)*
        RBRACE)

        (ACTORVARS
        LBRACE
            varDecs=varDeclarations {$actor.setActorVars($varDecs.vars);}
        RBRACE)

        (initMsgHandler=initHandlerDeclaration {$actor.setInitHandler($initMsgHandler.initMsgHandler);} )?
        {ArrayList<MsgHandlerDeclaration> msgHanlders;}
        (msgHandler=msgHandlerDeclaration {msgHandlers.add($msgHandler.msgHandlerDec);} )*

        RBRACE
    ;

mainDeclaration
    :   MAIN
    	LBRACE
        actorInstantiation*
    	RBRACE
    ;

actorInstantiation
    :	identifier identifier
     	LPAREN (identifier(COMMA identifier)* | ) RPAREN
     	COLON LPAREN expressionList RPAREN SEMICOLON
    ;

initHandlerDeclaration returns [InitHandlerDeclaration initMsgHandler]
    :	MSGHANDLER (name=INITIAL)
        {$initMsgHandler = new InitHandlerDeclaration(new Identifier($name.text) );}
        LPAREN argDeclarations RPAREN
     	LBRACE
     	varDeclarations
     	(statement)*
     	RBRACE
    ;

msgHandlerDeclaration returns [MsgHandlerDeclaration msgHandlerDec]
    :	MSGHANDLER i=identifier
        {$msgHandlerDec = new MsgHandlerDeclaration($i.id);}
        LPAREN argDeclarations RPAREN
       	LBRACE
       	varDeclarations
       	(statement)*
       	RBRACE
    ;

argDeclarations
    :	varDeclaration(COMMA varDeclaration)* |
    ;

varDeclarations returns [ArrayList<VarDeclaration> vars]
    :	(varDec=varDeclaration {vars.add($varDec.var);} SEMICOLON)*
    ;

varDeclaration returns [VarDeclaration var]
    :	INT i=identifier {$var = new VarDeclaration($i.id, new IntType());}
    |   STRING i=identifier {$var = new VarDeclaration($i.id, new StringType());}
    |   BOOLEAN i=identifier {$var = new VarDeclaration($i.id, new BooleanType());}
    |   INT i=identifier LBRACKET size=INTVAL RBRACKET {
            int size = Integer.parseInt($size.text);
            $var = new VarDeclaration($i.id, new ArrayType(size) );
        }
    ;

statement returns [Statement stmt]
    :	b=blockStmt {$stmt = $b.block; }
    | 	p=printStmt {$stmt = $p.print; }
    |  	a=assignStmt {$stmt = $a.ass; }
    |  	f=forStmt {$stmt = $f.for; }
    |  	i=ifStmt {$stmt = $i.conditional; }
    |  	c=continueStmt {$stmt = $c.continue; }
    |  	br=breakStmt {$stmt = $br.break; }
    |  	ms=msgHandlerCall {$stmt = $ms.msghandlerCall; }
    ;

blockStmt returns [Block block]
    : 	{$block = new Block();} LBRACE (s=statement {$block.addStatement($s.stmt);})* RBRACE
    ;

printStmt returns [Print print]
    :   PRINT LPAREN ex=expression {$print = new Print($ex.expr);} RPAREN SEMICOLON
    ;

assignStmt returns [Assign ass]
    :    assign=assignment {$ass = new Assign($assign.lVal, $assign.rVal);} SEMICOLON
    ;

assignment returns [Expression lVal, Expression rVal]
    :   orExp=orExpression {$lVal = $orExp.orExpr;} ASSIGN ex=expression {$rVal = $ex.expr;}
    ;

forStmt returns [For for]
    : 	{$for = new For();}
        FOR LPAREN (init=assignStmt {$for.setInitialize($init.ass);} )?
        SEMICOLON (exp=expression {$for.setCondition($exp.expr);} )?
        SEMICOLON (as=assignStmt {$for.setUpdate($as.ass);} )?
        RPAREN st=statement {$for.setBody($st.stmt);}
    ;

ifStmt returns [Conditional conditional]
    :   IF LPAREN if=expression RPAREN then=statement
        {$conditional = new Conditional($if.expr , $then.stmt);}
        (else=elseStmt {if ($else.else != null){$$conditional.setElseBody($else.else);}} )
    ;

elseStmt returns [Statement else]
    : ELSE st=statement {$else = $st.stmt;} | {$else = null;}
    ;

continueStmt returns [Continue continue]
    : 	CONTINUE {$continue = new Continue();} SEMICOLON
    ;

breakStmt returns [Break break]
    : 	BREAK SEMICOLON {$break = new Break();}
    ;

msgHandlerCall returns [MsgHandlerCall msghandlerCall]
    :   {$msghandlerCall = new MsgHandlerCall();}
        (instance=identifier | SELF | SENDER) DOT
        iid=identifier LPAREN expressionList RPAREN SEMICOLON
    ;

expression returns [Expression expr]
    :	orexp=orExpression {$expr = $orexp.expr;}
        (ass=ASSIGN ex=expression {$expr = new BinaryExpression($orexp.expr, $ex.expr, new BinaryOperator(assign));})?

    ;

orExpression returns [Expression expr]
    :	exp1=andExpression {$expr=$exp1.expr;}
        (or=OR exp2=andExpression {$expr = new BinaryExpression($exp1.expr, $exp2.expr, new BinaryOperator(or));})*
    ;

andExpression returns [Expression expr]
    :	exp1=equalityExpression {$expr=$exp1.exp;}
        (and=AND exp2=equalityExpression {$expr = new BinaryExpression($exp1.expr, $exp2.expr, new BinaryOperator(and));} )*
    ;

equalityExpression returns [Expression expr] locals [BinaryOperator binaryOperator]
    :	exp1=relationalExpression {$expr=$exp1.exp;}
        ( (eq=EQ {$binaryOperator = new BinaryOperator(eq); } | neq=NEQ {$binaryOperator = new BinaryOperator(neq); })
         exp2=relationalExpression {$expr = new BinaryExpression($exp1.expr, $exp2.expr, $binaryOperator);} )*
    ;

relationalExpression returns [Expression expr] locals [BinaryOperator binaryOperator]
    : exp1=additiveExpression {$expr=$exp1.exp;}
     ((LT {$binaryOperator = new BinaryOperator(lt);} | GT {$binaryOperator = new BinaryOperator(gt);})
      exp2=additiveExpression {$expr = new BinaryExpression($exp1.expr, $exp2.expr, $binaryOperator );} )*
    ;

additiveExpression returns [Expression expr] locals [BinaryOperator binaryOperator]
    : exp1=multiplicativeExpression {$expr=$exp1.exp;}
    ((PLUS {$binaryOperator = new BinaryOperator(add);} | MINUS {$binaryOperator = new BinaryOperator(sub);})
    expr2=multiplicativeExpression {$expr = new BinaryExpression($exp1.expr, $exp2.expr, $binaryOperator);} )*
    ;

multiplicativeExpression returns [Expression expr] locals [BinaryOperator binaryOperator]
    : exp1=preUnaryExpression
      ((MULT {$binaryOperator = new BinaryOperator(mult);} |
       DIV {$binaryOperator = new BinaryOperator(div);} |
       PERCENT {$binaryOperator = new BinaryOperator(mod);})
      exp2=preUnaryExpression {$expr = new BinaryExpression($exp1.expr, $exp2.expr, $binaryOperator);} )*
    ;

preUnaryExpression returns [Expression expr]
    :   NOT exp1=preUnaryExpression {$expr = new UnaryExpression(new UnaryOperator(not), $exp1.expr); }
    |   MINUS exp2=preUnaryExpression {$expr = new UnaryExpression(new UnaryOperator(minus), $exp2.expr); }
    |   PLUSPLUS exp3=preUnaryExpression {$expr = new UnaryExpression(new UnaryOperator(preinc), $exp3.expr); }
    |   MINUSMINUS exp4=preUnaryExpression {$expr = new UnaryExpression(new UnaryOperator(predec), $exp4.expr); }
    |   exp5=postUnaryExpression {$expr = $exp5.expr; }
    ;

postUnaryExpression returns [Expression expr]
    :   exp1=otherExpression{$expr = $exp1.expr;}
        (op=postUnaryOp {$expr = new UnaryExpression($op.unOp, $exp1.expr);} )?
    ;

postUnaryOp returns [UnaryOperator unOp]
    :	PLUSPLUS {$unOp = new UnaryOperator(postinc);} | MINUSMINUS {$unOp = new UnaryOperator(postdec);}
    ;

otherExpression returns [Expression expr]
    :    LPAREN exp1=expression RPAREN {$expr = $exp1.expr;}
    |    iid=identifier {$expr = $iid.id;}
    |    arrcall=arrayCall {$expr = $arrcall.ac;}
    |    ava=actorVarAccess {$expr = $ava.actorVarCall;}
    |    v=value {$expr = $v.val;}
    |    SENDER {$expr = new Sender();}
    ;

arrayCall returns [ArrayCall ac] locals [Expression expr]
    :   (iid=identifier {$expr=$iid.id;} | ava=actorVarAccess {$expr=$ava.actorVarCall;})
        LBRACKET exp1=expression {$ac = new ArrayCall($expr, $exp1.expr);} RBRACKET
    ;

actorVarAccess returns [ActorVarAccess actorVarCall]
    :   SELF DOT iid=identifier {$actorVarCall = new ActorVarAccess($iid.id);}
    ;

expressionList returns [ArrayList<Expression> expList]
    :	{$expList = new ArrayList<Expression>();}
        (exp1=expression {$expList.add($exp1.expr);}
        (COMMA exp2=expression{$expList.add($exp2.expr);})* | )
    ;

identifier returns [Identifier id]
    :   (i=IDENTIFIER) {$id = new Identifier($i.text);}
    ;

value returns [Value val]
    :   int=INTVAL {$val = new IntValue(Integer.parseInt($int.text), new IntType());} |
        str=STRINGVAL {$val = new StringValue($str.text, new StringType());} |
        tr=TRUE {$val = new BooleanValue(true, new BooleanType());} |
        fal=FALSE {$val = new BooleanValue(false, new BooleanType());}
    ;

// values
INTVAL
    : [1-9][0-9]* | [0]
    ;

STRINGVAL
    : '"'~["]*'"'
    ;

TRUE
    :   'true'
    ;

FALSE
    :   'false'
    ;

//types
INT
    : 'int'
    ;

BOOLEAN
    : 'boolean'
    ;

STRING
    : 'string'
    ;

//keywords
ACTOR
	:	'actor'
	;

EXTENDS
	:	'extends'
	;

ACTORVARS
	:	'actorvars'
	;

KNOWNACTORS
	:	'knownactors'
	;

INITIAL
    :   'initial'
    ;

MSGHANDLER
	: 	'msghandler'
	;

SENDER
    :   'sender'
    ;

SELF
    :   'self'
    ;

MAIN
	:	'main'
	;

FOR
    :   'for'
    ;

CONTINUE
    :   'continue'
    ;

BREAK
    :   'break'
    ;

IF
    :   'if'
    ;

ELSE
    :   'else'
    ;

PRINT
    :   'print'
    ;

//symbols
LPAREN
    :   '('
    ;

RPAREN
    :   ')'
    ;

LBRACE
    :   '{'
    ;

RBRACE
    :   '}'
    ;

LBRACKET
    :   '['
    ;

RBRACKET
    :   ']'
    ;

COLON
    :   ':'
    ;

SEMICOLON
    :   ';'
    ;

COMMA
    :   ','
    ;

DOT
    :   '.'
    ;

//operators
ASSIGN
    :   '='
    ;

EQ
    :   '=='
    ;

NEQ
    :   '!='
    ;

GT
    :   '>'
    ;

LT
    :   '<'
    ;

PLUSPLUS
    :   '++'
    ;

MINUSMINUS
    :   '--'
    ;

PLUS
    :   '+'
    ;

MINUS
    :   '-'
    ;

MULT
    :   '*'
    ;

DIV
    :   '/'
    ;

PERCENT
    :   '%'
    ;

NOT
    :   '!'
    ;

AND
    :   '&&'
    ;

OR
    :   '||'
    ;

QUES
    :   '?'
    ;

IDENTIFIER
    :   [a-zA-Z_][a-zA-Z0-9_]*
    ;

COMMENT
    :   '//' ~[\n\r]* -> skip
    ;

WHITESPACE
    :   [ \t\r\n] -> skip
    ;