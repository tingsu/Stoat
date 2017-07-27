<YYINITIAL> {
  "@"                            { return sym(Terminals.AT); }
  "..."                          { return sym(Terminals.ELLIPSIS); }
}
