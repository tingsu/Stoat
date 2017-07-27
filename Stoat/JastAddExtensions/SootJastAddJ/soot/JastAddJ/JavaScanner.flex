package soot.JastAddJ;

import beaver.Symbol;
import beaver.Scanner;
import soot.JastAddJ.JastAddJavaParser.Terminals;
import java.io.*;

%%

%public 
%final 
%class JavaScanner
%extends Scanner

%type Symbol 
%function nextToken 
%yylexthrow Scanner.Exception

%unicode
%line %column

%{
  StringBuffer strbuf = new StringBuffer(128);
  int sub_line;
  int sub_column;
  int strlit_start_line, strlit_start_column;

  private Symbol sym(short id) {
    return new Symbol(id, yyline + 1, yycolumn + 1, len(), str());
  }

  private Symbol sym(short id, String value) {
    return new Symbol(id, yyline + 1, yycolumn + 1, len(), value);
  }
  
  private Symbol sym(short id, String value, int start_line, int start_column, int len) {
    return new Symbol(id, start_line, start_column, len, value);
  }

  private String str() { return yytext(); }
  private int len() { return yylength(); }

  private void error(String msg) throws Scanner.Exception {
    throw new Scanner.Exception(yyline + 1, yycolumn + 1, msg);
  }
%}


// 3.4 Line Terminators
LineTerminator = \n|\r|\r\n
InputCharacter = [^\r\n]

// 3.6 White Space
WhiteSpace = [ ] | \t | \f | {LineTerminator}

// 3.7 Comments
Comment = {TraditionalComment}
        | {EndOfLineComment}

TraditionalComment = "/*" [^*] ~"*/" | "/*" "*"+ "/" | "/*" "*"+ [^/*] ~"*/"
EndOfLineComment = "//" {InputCharacter}* {LineTerminator}?

// the following macro is unused since the JastAddJ 1.5 extension
// 3.8 Identifiers
//Identifier = [:jletter:][:jletterdigit:]*

// Modified numeric literal parsing;
// accept anything which starts with a digit 0-9
// and contains any combination of the legal numeric
// literal component characters
// 
// component characters (uppercase also accepted):
// 0-9  decimal and hex digits
// a-f  hex digits
// x    hex specifier
// e    decimal exponent
// p    hex exponent
// .    floating point
NumericLiteral = 0 [_0-9a-fA-FxXlL.]* {ExponentPart}?
               | [1-9] [_0-9a-fA-FxXlL.]* {ExponentPart}?
               | \. [0-9] [_0-9a-fA-FxXlL.]* {ExponentPart}?
ExponentPart = [eE] [+-]? [_0-9a-fA-FxX.]*
             | [pP] [+-]? [_0-9a-fA-F]+

// 3.10.4 Character Literals
SingleCharacter = [^\r\n\'\\]

// 3.10.5 String Literals
StringCharacter = [^\r\n\"\\]

// 3.10.6 Escape Sequences for Character and String Literals
OctalEscape = \\ {OctalDigit} 
            | \\ {OctalDigit} {OctalDigit}
            | \\  {ZeroToThree} {OctalDigit} {OctalDigit}
OctalDigit = [0-7]
ZeroToThree = [0-3]

HexadecimalFloatingPointLiteral = {HexSignificand} {BinaryExponent}

HexSignificand = {HexNumeral} [\.]?
 | 0 [xX] [0-9a-fA-F]* \. [0-9a-fA-F]+

BinaryExponent = [pP] [+-]? {Digits}
%state STRING

%%

// 3.6 White Space
<YYINITIAL> { {WhiteSpace} { } }

// 3.7 Comments
<YYINITIAL> { {Comment} { } }

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

<YYINITIAL> { "enum" { return sym(Terminals.ENUM); } }
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

<YYINITIAL> {
  "@"                            { return sym(Terminals.AT); }
  "..."                          { return sym(Terminals.ELLIPSIS); }
}
// 3.8 Identifiers located at end of current state due to rule priority disambiguation
<YYINITIAL> {
  ([:jletter:]|[\ud800-\udfff])([:jletterdigit:]|[\ud800-\udfff])* { return sym(Terminals.IDENTIFIER); }
}
// hack to detect the SUB character as the last input character
\u001a                           { if(sub_line == 0 && sub_column == 0) {
                                     sub_line = yyline; sub_column = yycolumn;
                                   } 
                                 }
// fall through errors
.|\n                             { error("illegal character \""+str()+ "\""); }
<<EOF>>                          { // detect position of first SUB character
                                   if(!(sub_line == 0 && sub_column == 0) && (sub_line != yyline || sub_column != yycolumn-1)) {
                                     // reset to only return error once
                                     sub_line = 0;
                                     sub_column = 0;
                                     // return error
                                     error("error");
                                   }
                                   return sym(Terminals.EOF);
                                 }
