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

program returns [Program pro]
    : {$pro = new Program();}
      (ad=actorDeclaration {$pro.addActor($ad.actor);})+
      md=mainDeclaration {$pro.setMain($md.main);}
    ;

actorDeclaration returns [ActorDeclaration actor]
    :   ac=ACTOR (i=identifier) {$actor = new ActorDeclaration($i.id); $actor.setLine($ac.line);}
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

mainDeclaration returns [Main main]
    :   m=MAIN {$main = new Main(); $main.setLine($m.line);}
    	LBRACE
        (ac=actorInstantiation {$main.addActorInstantiation($ac.actor);} )*
    	RBRACE
    ;

actorInstantiation returns [ActorInstantiation actor]
    :	iid1=identifier iid2=identifier
        {$actor = new ActorInstantiation(new ActorType($iid1.id), $iid2.id); $actor.setLine();}
     	paren=LPAREN {$actor.setLine($paren.line);} (k1=identifier {$actor.addKnownActor($k1.id);} (COMMA k2=identifier{$actor.addKnownActor($k2.id);})* | ) RPAREN
     	COLON LPAREN exList=expressionList {$actor.setInitArgs($exList.expList);} RPAREN SEMICOLON
    ;

initHandlerDeclaration returns [InitHandlerDeclaration initMsgHandler]
    :	MSGHANDLER (name=INITIAL)
        {$initMsgHandler = new InitHandlerDeclaration(new Identifier($name.text) );}
        {$initMsgHandler.setLine($name.line);}
        LPAREN ar=argDeclarations {$initMsgHandler.setArgs($ar.args);} RPAREN
     	LBRACE
     	v=varDeclarations {$initMsgHandler.setLocalVars($v.vars);}
     	(s=statement {$initMsgHandler.addStatement($s.stmt);})*
     	RBRACE
    ;

msgHandlerDeclaration returns [HandlerDeclaration msgHandlerDec]
    :	token=MSGHANDLER i=identifier
        {$msgHandlerDec = new MsgHandlerDeclaration($i.id);}
        {$msgHandlerDec.setLine($token.line);}
        LPAREN ar=argDeclarations {$msgHandlerDec.setArgs($ar.args);} RPAREN
       	LBRACE
       	v=varDeclarations {$msgHandlerDec.setLocalVars($v.vars);}
       	(s=statement {$msgHandlerDec.addStatement($s.stmt);} )*
       	RBRACE
    ;

argDeclarations returns [ArrayList<VarDeclaration> args]
    :	{$args = new ArrayList<VarDeclaration>();}
        v1=varDeclaration {$args.add(v1.var);}
        (COMMA v2=varDeclaration {$args.add(v2.var);})* |
    ;

varDeclarations returns [ArrayList<VarDeclaration> vars]
    :	(varDec=varDeclaration {vars.add($varDec.var);} SEMICOLON)*
    ;

varDeclaration returns [VarDeclaration var] locals [int line]
    :	i1=INT {$line = $i1.line;} i=identifier {$var = new VarDeclaration($i.id, new IntType());}
    |   s1=STRING {$line = $s1.line;} i=identifier {$var = new VarDeclaration($i.id, new StringType());}
    |   b1=BOOLEAN {$line = $b1.line;} i=identifier {$var = new VarDeclaration($i.id, new BooleanType());}
    |   i2=INT {$line = $i2.line;} i=identifier LBRACKET size=INTVAL RBRACKET {
            int size = Integer.parseInt($size.text);
            $var = new VarDeclaration($i.id, new ArrayType(size) );
            $var.setLine($line);
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
    : 	{$block = new Block();} lbrace=LBRACE {$block.setLine($lbrace.line);} (s=statement {$block.addStatement($s.stmt);})* RBRACE
    ;

printStmt returns [Print print]
    :   p=PRINT LPAREN ex=expression {$print = new Print($ex.expr);} {$print.setLine($p.line);} RPAREN SEMICOLON
    ;

assignStmt returns [Statment ass]
    :    assign=assignment {$ass = $assing.ass;} semi=SEMICOLON {$ass.setLine($semi.line);}
    ;

assignment returns [Assign ass]
    :   exp1=orExpression assign=ASSIGN exp2=expression
        {$ass = new Assign($exp1.expr , $exp2.expr); $ass.setLine($assign.line);}
    ;

forStmt returns [For for]
    : 	{$for = new For();}
        f=FOR {$for.setLine($f.line);} LPAREN
        (init=assignment {$for.setInitialize($init.ass);} )?
        SEMICOLON (exp=expression {$for.setCondition($exp.expr);} )?
        SEMICOLON (as=assignment {$for.setUpdate($as.ass);} )?
        RPAREN st=statement {$for.setBody($st.stmt);}
    ;

ifStmt returns [Conditional conditional]
    :   ifkey=IF LPAREN i=expression RPAREN then=statement
        {$conditional = new Conditional($i.expr , $then.stmt); $conditional.setLine($ifkey.line);}
        (el=elseStmt {if ($el.else != null){$$conditional.setElseBody($el.else);}} )
    ;

elseStmt returns [Statement else]
    : el=ELSE st=statement {$else = $st.stmt; $else.setLine($el.line);} | {$else = null;}
    ;

continueStmt returns [Continue continue]
    : 	c=CONTINUE {$continue = new Continue(); $continue.setLine($c.line);} SEMICOLON
    ;

breakStmt returns [Break break]
    : 	br=BREAK SEMICOLON {$break = new Break(); $break.setLine($br.line);}
    ;

msgHandlerCall returns [MsgHandlerCall msghandlerCall]
    :   {$msghandlerCall = new MsgHandlerCall();}
        (instance=identifier | SELF | SENDER) dot=DOT {$msghandlerCall.setLine($dot.line);}
        iid=identifier LPAREN expressionList RPAREN SEMICOLON
    ;

expression returns [Expression expr]
    :	orexp=orExpression {$expr = $orexp.expr;}
        (ass=ASSIGN ex=expression
        {$expr = new BinaryExpression($orexp.expr, $ex.expr, new BinaryOperator(assign)); $expr.setLine($ass.line);})?

    ;

orExpression returns [Expression expr]
    :	exp1=andExpression {$expr=$exp1.expr;}
        (or=OR exp2=andExpression
        {$expr = new BinaryExpression($exp1.expr, $exp2.expr, new BinaryOperator(or)); $expr.setLine($or.line);} )*
    ;

andExpression returns [Expression expr]
    :	exp1=equalityExpression {$expr=$exp1.expr;}
        (and=AND exp2=equalityExpression
        {$expr = new BinaryExpression($exp1.expr, $exp2.expr, new BinaryOperator(and)); $expr.setLine($and.line);} )*
    ;

equalityExpression returns [Expression expr] locals [BinaryOperator binaryOperator, int line]
    :	exp1=relationalExpression {$expr=$exp1.expr;}
        ( (eq=EQ {$binaryOperator = new BinaryOperator(eq); $line = $eq.line; }
        | neq=NEQ {$binaryOperator = new BinaryOperator(neq); $line = $neq.line;})
         exp2=relationalExpression {$expr = new BinaryExpression($exp1.expr, $exp2.expr, $binaryOperator);} )*
         {$expr.setLine($line);}
    ;

relationalExpression returns [Expression expr] locals [BinaryOperator binaryOperator, int line]
    : exp1=additiveExpression {$expr=$exp1.expr;}
     ((lt=LT {$binaryOperator = new BinaryOperator(lt); $line=$lt.line;} |
      gt=GT {$binaryOperator = new BinaryOperator(gt); $line=$git.line;})
      exp2=additiveExpression {$expr = new BinaryExpression($exp1.expr, $exp2.expr, $binaryOperator ); $expr.setLine($line); } )*
    ;

additiveExpression returns [Expression expr] locals [BinaryOperator binaryOperator, int line]
    : exp1=multiplicativeExpression {$expr=$exp1.expr;}
    ((plus=PLUS {$binaryOperator = new BinaryOperator(add); $line = $plus.line;} |
    minus=MINUS {$binaryOperator = new BinaryOperator(sub); $line = $minus.line;})
    exp2=multiplicativeExpression {$expr = new BinaryExpression($exp1.expr, $exp2.expr, $binaryOperator); $expr.setLine($line);} )*
    ;

multiplicativeExpression returns [Expression expr] locals [BinaryOperator binaryOperator, int line]
    : exp1=preUnaryExpression {$expr=$exp1.expr;}
      ((mult=MULT {$binaryOperator = new BinaryOperator(mult); $line=$mult.line; } |
       div=DIV {$binaryOperator = new BinaryOperator(div); $line=$div.line; } |
       per=PERCENT {$binaryOperator = new BinaryOperator(mod); $line=$per.line; })
      exp2=preUnaryExpression {$expr = new BinaryExpression($exp1.expr, $exp2.expr, $binaryOperator); $expr.setLine($line);} )*
    ;

preUnaryExpression returns [Expression expr]
    :   not=NOT exp1=preUnaryExpression
        {$expr = new UnaryExpression(new UnaryOperator(not), $exp1.expr); $expr.setLine($not.line); }
    |   minus=MINUS exp2=preUnaryExpression
        {$expr = new UnaryExpression(new UnaryOperator(minus), $exp2.expr); $expr.setLine($minus.line); }
    |   pp=PLUSPLUS exp3=preUnaryExpression
        {$expr = new UnaryExpression(new UnaryOperator(preinc), $exp3.expr); $expr.setLine($pp.line); }
    |   mm=MINUSMINUS exp4=preUnaryExpression
        {$expr = new UnaryExpression(new UnaryOperator(predec), $exp4.expr); $expr.setLine($mm.line); }
    |   exp5=postUnaryExpression {$expr = $exp5.expr; }
    ;

postUnaryExpression returns [Expression expr]
    :   exp1=otherExpression{$expr = $exp1.expr;}
        (op=postUnaryOp {$expr = new UnaryExpression($op.unOp, $exp1.expr);} )?
    ;

postUnaryOp returns [UnaryOperator unOp]
    :	pp=PLUSPLUS {$unOp = new UnaryOperator(postinc); $unOp.setLine($pp.line); } |
        mm=MINUSMINUS {$unOp = new UnaryOperator(postdec); $unOp.setLine($mm.line); }
    ;

otherExpression returns [Expression expr]
    :    lparen=LPAREN exp1=expression RPAREN {$expr = $exp1.expr; $expr.setLine($lparen.line);}
    |    iid=identifier {$expr = $iid.id;}
    |    arrcall=arrayCall {$expr = $arrcall.ac;}
    |    ava=actorVarAccess {$expr = $ava.actorVarCall;}
    |    v=value {$expr = $v.val;}
    |    s=SENDER {$expr = new Sender(); $expr.setLine($s.line);}
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
    :   i=INTVAL {$val = new IntValue(Integer.parseInt($i.text), new IntType());} |
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