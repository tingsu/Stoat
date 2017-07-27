// 3.12 Operators
<YYINITIAL> {
  "="                            { return sym(Terminals.EQ); }
  ">"                            { return sym(Terminals.GT); }
  "<"                            { return sym(Terminals.LT); }
  "!"                            { return sym(Terminals.NOT); }
  "~"                            { return sym(Terminals.COMP); }
  "?"                            { return sym(Terminals.QUESTION); }
  ":"                            { return sym(Terminals.COLON); }
  "=="                           { return sym(Terminals.EQEQ); }
  "<="                           { return sym(Terminals.LTEQ); }
  ">="                           { return sym(Terminals.GTEQ); }
  "!="                           { return sym(Terminals.NOTEQ); }
  "&&"                           { return sym(Terminals.ANDAND); }
  "||"                           { return sym(Terminals.OROR); }
  "++"                           { return sym(Terminals.PLUSPLUS); }
  "--"                           { return sym(Terminals.MINUSMINUS); }
  "+"                            { return sym(Terminals.PLUS); }
  "-"                            { return sym(Terminals.MINUS); }
  "*"                            { return sym(Terminals.MULT); }
  "/"                            { return sym(Terminals.DIV); }
  "&"                            { return sym(Terminals.AND); }
  "|"                            { return sym(Terminals.OR); }
  "^"                            { return sym(Terminals.XOR); }
  "%"                            { return sym(Terminals.MOD); }
  "<<"                           { return sym(Terminals.LSHIFT); }
  ">>"                           { return sym(Terminals.RSHIFT); }
  ">>>"                          { return sym(Terminals.URSHIFT); }
  "+="                           { return sym(Terminals.PLUSEQ); }
  "-="                           { return sym(Terminals.MINUSEQ); }
  "*="                           { return sym(Terminals.MULTEQ); }
  "/="                           { return sym(Terminals.DIVEQ); }
  "&="                           { return sym(Terminals.ANDEQ); }
  "|="                           { return sym(Terminals.OREQ); }
  "^="                           { return sym(Terminals.XOREQ); }
  "%="                           { return sym(Terminals.MODEQ); }
  "<<="                          { return sym(Terminals.LSHIFTEQ); }
  ">>="                          { return sym(Terminals.RSHIFTEQ); }
  ">>>="                         { return sym(Terminals.URSHIFTEQ); }
}

