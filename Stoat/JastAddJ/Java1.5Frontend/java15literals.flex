  {HexadecimalFloatingPointLiteral} [fF]    { return sym(Terminals.FLOATING_POINT_LITERAL, str().substring(0,len()-1)); }
  {HexadecimalFloatingPointLiteral} [dD]    { return sym(Terminals.DOUBLE_LITERAL, str().substring(0,len()-1)); }
  {HexadecimalFloatingPointLiteral}         { return sym(Terminals.DOUBLE_LITERAL); }
