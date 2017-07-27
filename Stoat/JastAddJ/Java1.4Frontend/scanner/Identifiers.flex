// 3.8 Identifiers located at end of current state due to rule priority disambiguation
<YYINITIAL> {
  {Identifier}                   { return sym(Terminals.IDENTIFIER); }
}
