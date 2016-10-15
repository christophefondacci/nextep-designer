lexer grammar sql;

options {
  language = Java;
}

multiLineStmt : CREATE (WS | WORD)+ STMT_END;

//CREATE (WORD | WS)+ IS (WS)+ (WORD WS (PL_STMT_END)?)+ END (WORD)? STMT_END;

//pl_syntax: pl_word+;
//
//pl_word : ((WORD)+ PL_STMT_END) | BEGIN ;

fragment LETTER : ('a'..'z' | 'A'..'Z') ;
fragment DIGIT : '0'..'9';

WORD: LETTER (LETTER | DIGIT)*;
WS: (' ' | '\t' | '\n' | '\r' | '\f')+ { $channel = HIDDEN; };
END_LINE: ('\r')? ('\n')
CREATE: ('c'|'C')('r'|'R')('e'|'E')('a'|'A')('t'|'T')('e'|'E')
IS: ('i'|'I')('s'|'S')
DECLARE: ('d'|'D')('e'|'E')('c'|'C')('l'|'L')('a'|'A')('r'|'R')('e'|'E')
BEGIN: ('b'|'B')('e'|'E')('g'|'G')('i'|'I')('n'|'N')
END: ('e'|'E')('n'|'N')('d'|'D')
STMT_END: ';' | ( WS* END_LINE '/' WS* END_LINE )
PL_STMT_END : ';'