// 3.11 Separators
<YYINITIAL> {
  "("                            { return sym(Terminals.LPAREN); }
  ")"                            { return sym(Terminals.RPAREN); }
  "{"                            { return sym(Terminals.LBRACE); }
  "}"                            { return sym(Terminals.RBRACE); }
  "["                            { return sym(Terminals.LBRACK); }
  "]"                            { return sym(Terminals.RBRACK); }
  ";"                            { return sym(Terminals.SEMICOLON); }
  ","                            { return sym(Terminals.COMMA); }
  "."                            { return sym(Terminals.DOT); }
}

