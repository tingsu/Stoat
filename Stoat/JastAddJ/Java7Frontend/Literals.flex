// 3.10 Literals
<YYINITIAL> {
  // 3.10.1 Integer Literals
  {NumericLiteral}               { return sym(Terminals.NUMERIC_LITERAL); }
  
  // 3.10.3 Boolean Literals
  "true"                         { return sym(Terminals.BOOLEAN_LITERAL); }
  "false"                        { return sym(Terminals.BOOLEAN_LITERAL); }
  
  // 3.10.4 Character Literals
  \'{SingleCharacter}\'          { return sym(Terminals.CHARACTER_LITERAL, str().substring(1, len()-1)); }
  // 3.10.6 Escape Sequences for Character Literals
  \'"\\b"\'                      { return sym(Terminals.CHARACTER_LITERAL, "\b"); }
  \'"\\t"\'                      { return sym(Terminals.CHARACTER_LITERAL, "\t"); }
  \'"\\n"\'                      { return sym(Terminals.CHARACTER_LITERAL, "\n"); }
  \'"\\f"\'                      { return sym(Terminals.CHARACTER_LITERAL, "\f"); }
  \'"\\r"\'                      { return sym(Terminals.CHARACTER_LITERAL, "\r"); }
  \'"\\\""\'                     { return sym(Terminals.CHARACTER_LITERAL, "\""); }
  \'"\\'"\'                      { return sym(Terminals.CHARACTER_LITERAL, "\'"); }
  \'"\\\\"\'                     { return sym(Terminals.CHARACTER_LITERAL, "\\"); }
  \'{OctalEscape}\'              { int val = Integer.parseInt(str().substring(2,len()-1),8);
			                             return sym(Terminals.CHARACTER_LITERAL, new Character((char)val).toString()); }
  // Character Literal errors
  \'\\.                          { error("illegal escape sequence \""+str()+"\""); }
  \'{LineTerminator}             { error("unterminated character literal at end of line"); }

  // 3.10.5 String Literals
  \"                             { yybegin(STRING); 
  				   // remember start position of string literal so we can
				   // set its position correctly in the end
  				   strlit_start_line = yyline+1;
				   strlit_start_column = yycolumn+1;
  				   strbuf.setLength(0); }

  // 3.10.7 The Null Literal
  "null"                         { return sym(Terminals.NULL_LITERAL); }
}

// 3.10.5 String Literals
<STRING> {
  \"                             { yybegin(YYINITIAL); 
                                   return sym(Terminals.STRING_LITERAL, strbuf.toString(), strlit_start_line, strlit_start_column, strbuf.length()+2); }

  {StringCharacter}+             { strbuf.append(str()); }

  // 3.10.6 Escape sequences for String Literals
  "\\b"                          { strbuf.append( '\b' ); }
  "\\t"                          { strbuf.append( '\t' ); }
  "\\n"                          { strbuf.append( '\n' ); }
  "\\f"                          { strbuf.append( '\f' ); }
  "\\r"                          { strbuf.append( '\r' ); }
  "\\\""                         { strbuf.append( '\"' ); }
  "\\'"                          { strbuf.append( '\'' ); }
  "\\\\"                         { strbuf.append( '\\' ); }
  {OctalEscape}                  { strbuf.append((char)Integer.parseInt(str().substring(1),8)); }

  // String Literal errors
  \\.                            { error("illegal escape sequence \""+str()+"\""); }
  {LineTerminator}               { error("unterminated string at end of line"); }
}


