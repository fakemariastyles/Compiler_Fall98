grammar acton;

@header{
    package main.parsers;
    import main.* ;
    import main.ast.node.* ;
    import main.ast.node.declaration.* ;
    import main.ast.node.declaration.handler.* ;
    import main.ast.node.expression.* ;
    import main.ast.node.expression.operators.* ;
    import main.ast.node.expression.values.* ;
    import main.ast.node.statement.* ;
    import main.ast.type.* ;
    import main.ast.type.actorType.* ;
    import main.ast.type.arrayType.* ;
    import main.ast.type.primitiveType.* ;
    import java.util.ArrayList ;
}

@members{}

program returns [Program p]
    : {$p = new Program();}
      (ad=actorDeclaration {$p.addActor($ad.actor);})+
      md=mainDeclaration {$p.setMain($md.main);}
    ;

actorDeclaration returns [ActorDeclaration actor]
    :   ac=ACTOR (i=identifier) {$actor = new ActorDeclaration($i.id); $actor.setLine($ac.getLine());}
        (EXTENDS parent=identifier {$actor.setParentName($parent.id);} )?
        LPAREN (queueSize=INTVAL) {
            $actor.setQueueSize(Integer.parseInt($queueSize.text));
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
        (msgHandler=msgHandlerDeclaration {$actor.addMsgHandler($msgHandler.msgHandlerDec);} )*

        RBRACE
    ;

mainDeclaration returns [Main main]
    :   m=MAIN {$main = new Main(); $main.setLine($m.getLine());}
    	LBRACE
        (ac=actorInstantiation {$main.addActorInstantiation($ac.actor);} )*
    	RBRACE
    ;

actorInstantiation returns [ActorInstantiation actor]
    :	iid1=identifier iid2=identifier
        {$actor = new ActorInstantiation(new ActorType($iid1.id), $iid2.id);}
     	paren=LPAREN {$actor.setLine($paren.getLine());} (k1=identifier {$actor.addKnownActor($k1.id);} (COMMA k2=identifier{$actor.addKnownActor($k2.id);})* | ) RPAREN
     	COLON LPAREN exList=expressionList {$actor.setInitArgs($exList.expList);} RPAREN SEMICOLON
    ;

initHandlerDeclaration returns [InitHandlerDeclaration initMsgHandler]
    :	MSGHANDLER (name=INITIAL)
        {$initMsgHandler = new InitHandlerDeclaration(new Identifier($name.text) );}
        {$initMsgHandler.setLine($name.getLine());}
        LPAREN ar=argDeclarations {$initMsgHandler.setArgs($ar.args);} RPAREN
     	LBRACE
     	v=varDeclarations {$initMsgHandler.setLocalVars($v.vars);}
     	(s=statement {$initMsgHandler.addStatement($s.stmt);})*
     	RBRACE
    ;

msgHandlerDeclaration returns [MsgHandlerDeclaration msgHandlerDec]
    :	token=MSGHANDLER i=identifier
        {$msgHandlerDec = new MsgHandlerDeclaration($i.id);}
        {$msgHandlerDec.setLine($token.getLine());}
        LPAREN ar=argDeclarations {$msgHandlerDec.setArgs($ar.args);} RPAREN
       	LBRACE
       	v=varDeclarations {$msgHandlerDec.setLocalVars($v.vars);}
       	(s=statement {$msgHandlerDec.addStatement($s.stmt);} )*
       	RBRACE
    ;

argDeclarations returns [ArrayList<VarDeclaration> args]
    :	{$args = new ArrayList<VarDeclaration>();}
        v1=varDeclaration {$args.add($v1.var);}
        (COMMA v2=varDeclaration {$args.add($v2.var);})* | {$args = new ArrayList<VarDeclaration>();}
    ;

varDeclarations returns [ArrayList<VarDeclaration> vars]
    :	{$vars = new ArrayList<VarDeclaration>(); }
        (varDec=varDeclaration {$vars.add($varDec.var);} SEMICOLON)*
    ;

varDeclaration returns [VarDeclaration var] locals [int line]
    :	i1=INT {$line = $i1.getLine();} i=identifier {$var = new VarDeclaration($i.id, new IntType());}
    |   s1=STRING {$line = $s1.getLine();} i=identifier {$var = new VarDeclaration($i.id, new StringType());}
    |   b1=BOOLEAN {$line = $b1.getLine();} i=identifier {$var = new VarDeclaration($i.id, new BooleanType());}
    |   i2=INT {$line = $i2.getLine();} i=identifier LBRACKET size=INTVAL RBRACKET {
            int size = Integer.parseInt($size.text);
            $var = new VarDeclaration($i.id, new ArrayType(size) );
            $var.setLine($line);
        }
    ;

statement returns [Statement stmt]
    :	b=blockStmt {$stmt = $b.block; }
    | 	p=printStmt {$stmt = $p.print; }
    |  	a=assignStmt {$stmt = $a.ass; }
    |  	f=forStmt {$stmt = $f.fo; }
    |  	i=ifStmt {$stmt = $i.conditional; }
    |  	c=continueStmt {$stmt = $c.cont; }
    |  	br=breakStmt {$stmt = $br.br; }
    |  	ms=msgHandlerCall {$stmt = $ms.msghandlerCall; }
    ;

blockStmt returns [Block block]
    : 	{$block = new Block();} lbrace=LBRACE {$block.setLine($lbrace.getLine());} (s=statement {$block.addStatement($s.stmt);})* RBRACE
    ;

printStmt returns [Print print]
    :   p=PRINT LPAREN ex=expression {$print = new Print($ex.expr);} {$print.setLine($p.getLine());} RPAREN SEMICOLON
    ;

assignStmt returns [Statement ass]
    :    assign=assignment {$ass = $assign.ass;} semi=SEMICOLON {$ass.setLine($semi.getLine());}
    ;

assignment returns [Assign ass]
    :   exp1=orExpression assign=ASSIGN exp2=expression
        {$ass = new Assign($exp1.expr , $exp2.expr); $ass.setLine($assign.getLine());}
    ;

forStmt returns [For fo]
    : 	{$fo = new For();}
        f=FOR {$fo.setLine($f.getLine());} LPAREN
        (init=assignment {$fo.setInitialize($init.ass);} )?
        SEMICOLON (exp=expression {$fo.setCondition($exp.expr);} )?
        SEMICOLON (as=assignment {$fo.setUpdate($as.ass);} )?
        RPAREN st=statement {$fo.setBody($st.stmt);}
    ;

ifStmt returns [Conditional conditional]
    :   ifkey=IF LPAREN i=expression RPAREN then=statement
        {$conditional = new Conditional($i.expr , $then.stmt); $conditional.setLine($ifkey.getLine());}
        (e=elseStmt {if ($e.el != null){$conditional.setElseBody($e.el);}} )
    ;

elseStmt returns [Statement el]
    : e=ELSE st=statement {$el = $st.stmt; $el.setLine($e.getLine());} | {$el = null;}
    ;

continueStmt returns [Continue cont]
    : 	c=CONTINUE SEMICOLON {$cont = new Continue();} {$cont.setLine($c.getLine());}
    ;

breakStmt returns [Break br]
    : 	b=BREAK SEMICOLON {$br = new Break();} {$br.setLine($b.getLine());}
    ;

msgHandlerCall returns [MsgHandlerCall msghandlerCall] locals [Expression exp, Identifier i]
    :   (instance=identifier {$exp = $instance.id; } |
         self=SELF {$exp = new Identifier($self.text); } |
         sender=SENDER) {$exp = new Identifier($sender.text); }
         DOT
         name=identifier {$i = $name.id; }
         {$msghandlerCall = new MsgHandlerCall($exp, $i); }
         LPAREN exprList=expressionList {$msghandlerCall.setArgs($exprList.expList); } RPAREN SEMICOLON
    ;

expression returns [Expression expr]
    :	orexp=orExpression {$expr = $orexp.expr;}
        (ass=ASSIGN ex=expression
        {$expr = new BinaryExpression($orexp.expr, $ex.expr, BinaryOperator.assign); $expr.setLine($ass.getLine());})?

    ;

orExpression returns [Expression expr]
    :	exp1=andExpression {$expr=$exp1.expr;}
        (or=OR exp2=andExpression
        {$expr = new BinaryExpression($exp1.expr, $exp2.expr, BinaryOperator.or); $expr.setLine($or.getLine());} )*
    ;

andExpression returns [Expression expr]
    :	exp1=equalityExpression {$expr=$exp1.expr;}
        (and=AND exp2=equalityExpression
        {$expr = new BinaryExpression($exp1.expr, $exp2.expr, BinaryOperator.and); $expr.setLine($and.getLine());} )*
    ;

equalityExpression returns [Expression expr] locals [BinaryOperator binaryOperator, int line]
    :	exp1=relationalExpression {$expr=$exp1.expr;}
        ( (eq=EQ {$binaryOperator = BinaryOperator.eq; $line = $eq.getLine(); }
        | neq=NEQ {$binaryOperator = BinaryOperator.neq; $line = $neq.getLine();})
         exp2=relationalExpression {$expr = new BinaryExpression($exp1.expr, $exp2.expr, $binaryOperator);} )*
         {$expr.setLine($line);}
    ;

relationalExpression returns [Expression expr] locals [BinaryOperator binaryOperator, int line]
    : exp1=additiveExpression {$expr=$exp1.expr;}
     ((lt=LT {$binaryOperator = BinaryOperator.lt; $line=$lt.getLine();} |
      gt=GT {$binaryOperator = BinaryOperator.gt; $line=$gt.getLine();})
      exp2=additiveExpression {$expr = new BinaryExpression($exp1.expr, $exp2.expr, $binaryOperator ); $expr.setLine($line); } )*
    ;

additiveExpression returns [Expression expr] locals [BinaryOperator binaryOperator, int line]
    : exp1=multiplicativeExpression {$expr=$exp1.expr;}
    ((plus=PLUS {$binaryOperator =  BinaryOperator.add; $line = $plus.getLine();} |
    minus=MINUS {$binaryOperator = BinaryOperator.sub; $line = $minus.getLine();})
    exp2=multiplicativeExpression {$expr = new BinaryExpression($exp1.expr, $exp2.expr, $binaryOperator); $expr.setLine($line);} )*
    ;

multiplicativeExpression returns [Expression expr] locals [BinaryOperator binaryOperator, int line]
    : exp1=preUnaryExpression {$expr=$exp1.expr;}
      ((mult=MULT {$binaryOperator = BinaryOperator.mult; $line=$mult.getLine(); } |
       div=DIV {$binaryOperator = BinaryOperator.div; $line=$div.getLine(); } |
       per=PERCENT {$binaryOperator = BinaryOperator.mod; $line=$per.getLine(); })
      exp2=preUnaryExpression {$expr = new BinaryExpression($exp1.expr, $exp2.expr, $binaryOperator); $expr.setLine($line);} )*
    ;

preUnaryExpression returns [Expression expr]
    :   not=NOT exp1=preUnaryExpression
        {$expr = new UnaryExpression(UnaryOperator.not, $exp1.expr); $expr.setLine($not.getLine()); }
    |   minus=MINUS exp2=preUnaryExpression
        {$expr = new UnaryExpression(UnaryOperator.minus, $exp2.expr); $expr.setLine($minus.getLine()); }
    |   pp=PLUSPLUS exp3=preUnaryExpression
        {$expr = new UnaryExpression(UnaryOperator.preinc, $exp3.expr); $expr.setLine($pp.getLine()); }
    |   mm=MINUSMINUS exp4=preUnaryExpression
        {$expr = new UnaryExpression(UnaryOperator.predec, $exp4.expr); $expr.setLine($mm.getLine()); }
    |   exp5=postUnaryExpression {$expr = $exp5.expr; }
    ;

postUnaryExpression returns [Expression expr]
    :   exp1=otherExpression{$expr = $exp1.expr;}
        (op=postUnaryOp {$expr = new UnaryExpression($op.unOp, $exp1.expr);} )?
    ;

postUnaryOp returns [UnaryOperator unOp]
    :	pp=PLUSPLUS {$unOp = UnaryOperator.postinc;} |
        mm=MINUSMINUS {$unOp = UnaryOperator.postdec;}
    ;

otherExpression returns [Expression expr]
    :    lparen=LPAREN exp1=expression RPAREN {$expr = $exp1.expr; $expr.setLine($lparen.getLine());}
    |    iid=identifier {$expr = $iid.id;}
    |    arrcall=arrayCall {$expr = $arrcall.ac;}
    |    ava=actorVarAccess {$expr = $ava.actorVarCall;}
    |    v=value {$expr = $v.val;}
    |    s=SENDER {$expr = new Sender(); $expr.setLine($s.getLine());}
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