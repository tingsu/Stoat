// 3.9 Keywords
<YYINITIAL> {
  // 3.9 Keywords
  "assert"                       { return sym(Terminals.ASSERT); }
  "abstract"                     { return sym(Terminals.ABSTRACT); }
  "boolean"                      { return sym(Terminals.BOOLEAN); }
  "break"                        { return sym(Terminals.BREAK); }
  "byte"                         { return sym(Terminals.BYTE); }
  "case"                         { return sym(Terminals.CASE); }
  "catch"                        { return sym(Terminals.CATCH); }
  "char"                         { return sym(Terminals.CHAR); }
  "class"                        { return sym(Terminals.CLASS); }
  "const"                        { return sym(Terminals.EOF); }
  "continue"                     { return sym(Terminals.CONTINUE); }
  "default"                      { return sym(Terminals.DEFAULT); }
  "do"                           { return sym(Terminals.DO); }
  "double"                       { return sym(Terminals.DOUBLE); }
  "else"                         { return sym(Terminals.ELSE); }
  "extends"                      { return sym(Terminals.EXTENDS); }
  "final"                        { return sym(Terminals.FINAL); }
  "finally"                      { return sym(Terminals.FINALLY); }
  "float"                        { return sym(Terminals.FLOAT); }
  "for"                          { return sym(Terminals.FOR); }
  "goto"                         { return sym(Terminals.EOF); }
  "if"                           { return sym(Terminals.IF); }
  "implements"                   { return sym(Terminals.IMPLEMENTS); }
  "import"                       { return sym(Terminals.IMPORT); }
  "instanceof"                   { return sym(Terminals.INSTANCEOF); }
  "int"                          { return sym(Terminals.INT); }
  "interface"                    { return sym(Terminals.INTERFACE); }
  "long"                         { return sym(Terminals.LONG); }
  "native"                       { return sym(Terminals.NATIVE); }
  "new"                          { return sym(Terminals.NEW); }
  "package"                      { return sym(Terminals.PACKAGE); }
  "private"                      { return sym(Terminals.PRIVATE); }
  "protected"                    { return sym(Terminals.PROTECTED); }
  "public"                       { return sym(Terminals.PUBLIC); }
  "return"                       { return sym(Terminals.RETURN); }
  "short"                        { return sym(Terminals.SHORT); }
  "static"                       { return sym(Terminals.STATIC); }
  "strictfp"                     { return sym(Terminals.STRICTFP); }
  "super"                        { return sym(Terminals.SUPER); }
  "switch"                       { return sym(Terminals.SWITCH); }
  "synchronized"                 { return sym(Terminals.SYNCHRONIZED); }
  "this"                         { return sym(Terminals.THIS); }
  "throw"                        { return sym(Terminals.THROW); }
  "throws"                       { return sym(Terminals.THROWS); }
  "transient"                    { return sym(Terminals.TRANSIENT); }
  "try"                          { return sym(Terminals.TRY); }
  "void"                         { return sym(Terminals.VOID); }
  "volatile"                     { return sym(Terminals.VOLATILE); }
  "while"                        { return sym(Terminals.WHILE); }
}

