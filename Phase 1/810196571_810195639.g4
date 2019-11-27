grammar Acton;

program:
    actor+ | main
    ;
actor:
    (ACTOR (id=ID) EXTENDS ACTOR (parent=ID)
    {System.out.println("ActorDec:" + $id.getText() + "," + $parent.getText());}
    | ACTOR (id=ID) {System.out.println("ActorDec:" + $id.getText());})
    L_PARENTHESES INT R_PARENTHESES actorimpl
    ;

actorimpl:
    L_BRACE NEW_LINE knownActors actorVars msgHandler* R_BRACE (NEW_LINE)?
    ;

knownActors:
    NEW_LINE KNOWNACTORS L_BRACE NEW_LINE ((ch=CHILD) (id=ID) SEMICOLON NEW_LINE)+ R_BRACE (NEW_LINE)?
    {System.out.println("KnownActor:" + $ch.getText() + "," + $id.getText());}
    ;

actorVars:
    NEW_LINE ACTORVARS L_BRACE NEW_LINE
    (varDef SEMICOLON NEW_LINE) *
    R_BRACE NEW_LINE
    ;

//to do add stamet {}
msgHandler:
    NEW_LINE MSGHANDLER (id = ID) L_PARENTHESES
    {System.out.println("MsgHandlerDece:" + $id.getText() + ",");}
    funArg R_PARENTHESES
    L_BRACE stmt R_BRACE
    ;

argument:
    (TYPE (id =ID) | (id=ID) L_BRACKET INT R_BRACKET)
    {System.out.println( "," + $id.getText());}
    ;

stmt:
    (varDef | expr | ifrule | forrule | scoperule | print) SEMICOLON  NEW_LINE
    ;

scoperule:
    L_BRACE NEW_LINE (stmt | NEW_LINE)* R_BRACE NEW_LINE
    ;

ifrule:
    IF L_PARENTHESES expr R_PARENTHESES L_BRACE NEW_LINE {System.out.println("Conditional:if");}(stmt | NEW_LINE)* R_BRACE NEW_LINE
    (ELSE L_BRACE NEW_LINE {System.out.println("Conditional:else");}(stmt | NEW_LINE)* R_BRACE NEW_LINE)?
    ;

forrule:
    FOR {System.out.println("Loop:for");} L_PARENTHESES
    initVarDef? SEMICOLON termComma? SEMICOLON termAss?  L_BRACE NEW_LINE
    (stmt | NEW_LINE | BREAK | CONTINUE)*
    R_BRACE NEW_LINE
    ;

expr:
    funCall | termComma SEMICOLON
    ;

varDef:
    (INT (id=ID) L_BRACKET INT R_BRACKET SEMICOLON NEW_LINE)
    {System.out.println("VarDec:" + $id.getText());}
    | defVarDef | initVarDef
    ;

initVarDef:
    ((type=Type) (id =ID))
    {System.out.println("VarDec:" + $type.getText() + "," + $id.getText());}
    (ASSIGN expr)? SEMICOLON NEW_LINE
    ;

defVarDef:
    (type=TYPE) (id =ID)
    {System.out.println("VarDec:" + $type.getText() + "," + $id.getText());}
    SEMICOLON NEW_LINE
    ;

funCall:
    (id =ID) DOT (fun=STRING) L_PARENTHESES R_PARENTHESES
    {System.out.println("MsgHandlerCall:" + $id.getText() + "," + $fun.getText());}
    ;

arrayCall:
    ID (L_BRACKET expr R_BRACKET)+
    ;

termComma:
    (ID (op = COMMA) ID) {System.out.println("Operator:" + $op.getText());} | termAss
    ;

termAss:
    (termOr (op = ASSIGN) (termAss)) {System.out.println("Operator:" + $op.getText());} | termOr
    ;

termOr:
    (termAnd (op = LOGICAL_OR) termOr) {System.out.println("Operator:" + $op.getText());} | termAnd
    ;

termAnd:
    (termEq (op = LOGICAL_AND) termAnd) {System.out.println("Operator:" + $op.getText());} | termEq
    ;

termEq:
    (termRel (op = EQUAL | NOT_EQUAL) termEq) {System.out.println("Operator:" + $op.getText());} | termRel
    ;

termRel:
    (termPM (op = LESS_THAN | GREATER_THAN) termRel) {System.out.println("Operator:" + $op.getText());} | termPM
    ;

termPM:
    (termMDR (op = MINUS | ADD ) termMDR) {System.out.println("Operator:" + $op.getText());} | termMDR
    ;

termMDR:
    (termUnaryPrefix (op = MULTIPLY | DIVIDE | REMAINDER) termMDR) {System.out.println("Operator:" + $op.getText());} | termUnaryPrefix
    ;

termUnaryPrefix:
    ((op = ADD_UNARY_EXP | SUB_UNARY_EXP | LOGICAL_COMPLEMENT | MINUS) termUnaryPrefix) {System.out.println("Operator:" + $op.getText());}
    | (termBracket | termUnaryPostFix)
    ;

termUnaryPostFix:
    (termPar (op = ADD_UNARY_EXP | SUB_UNARY_EXP) termUnaryPostFix) {System.out.println("Operator:" + $op.getText());} | termPar
    ;

termBracket:
    (L_BRACKET termBracket R_BRACKET) {System.out.println("Operator: []");} | termPar
    ;

termPar:
    ( L_PARENTHESES expr R_PARENTHESES)
    {System.out.println("Operator:()");}
    | funCall | arrayCall | CHARACTER | STRING | ID | INT
    ;

main:
    MAIN L_BRACE actorDef R_BRACE NEW_LINE
    ;

actorDef:
    ((id = ID) (name = ID) L_PARENTHESES
    funArg* R_PARENTHESES COLON L_PARENTHESES INT R_PARENTHESES SEMICOLON NEW_LINE)
    {System.out.println("ActorInstantiation:" + $id.getText() + "," + $name.getText() + ",");}
    ;

funArg:
    (argument COMMA argument)* argument
    ;

print:
    PRINT L_PARENTHESES (ID | varDef | '"' STRING '"') R_PARENTHESES SEMICOLON NEW_LINE
    {System.out.println("Built-in:Print");}
    ;


PRINT:
    'Print'
    ;
NEW_LINE:
    [\r\n]+
    ;

TYPE:
    INT | STRING| BOOLEAN
    ;

CHILD:
    'child'
    ;

COMMENT:
    '//' ~[\n]*  -> skip
    ;

TAB:
    ('\t' | ' ')+ -> skip
    ;

BREAK:
    'break'
    ;

CONTINUE:
    'continue'
    ;

FALSE:
    'false'
    ;

TRUE:
    'true'
    ;

INT:
    [1-9][0-9]*
    ;

BOOLEAN:
    'boolean'
    ;

STRING:
    '"'~["]*'"'
    ;

MAIN:
    'main'
    ;

SELF:
    'self'
    ;

SENDER:
    'sender'
    ;

IF:
    'if'
    ;

ELSE:
    'else'
    ;

FOR:
    'for'
    ;

ACTOR:
    'actor'
    ;

KNOWNACTORS:
    'knownactors'
    ;

ACTORVARS:
    'actorvars'
    ;

EXTENDS:
    'extends'
    ;

INITIAL:
    'initial'
    ;

MSGHANDLER:
    'msghandler'
    ;

ID:
    [A-Za-z][A-Za-z0-9]*
    ;

R_PARENTHESES:
    ')'
    ;

L_PARENTHESES:
    '('
    ;

L_BRACE:
    '{'
    ;

R_BRACE:
    '}'
    ;

L_BRACKET:
    '['
    ;

R_BRACKET:
    ']'
    ;

SEMICOLON:
    ';'
    ;

ASSIGN:
    '='
    ;

ADD_UNARY_EXP:
    '++'
    ;

SUB_UNARY_EXP:
    '--'
    ;

LOGICAL_COMPLEMENT:
    '!'
    ;

MINUS:
    '-'
    ;

MULTIPLY:
    '*'
    ;

DIVIDE:
    '/'
    ;

REMAINDER:
    '%'
    ;

ADD:
    '+'
    ;

GREATER_THAN:
    '>'
    ;

LESS_THAN:
    '<'
    ;

EQUAL:
    '=='
    ;

NOT_EQUAL:
    '!='
    ;

LOGICAL_AND:
    '&&'
    ;

LOGICAL_OR:
    '||'
    ;

COMMA:
    ','
    ;

COLON:
    ':'
    ;

DOT:
    '.'
    ;
