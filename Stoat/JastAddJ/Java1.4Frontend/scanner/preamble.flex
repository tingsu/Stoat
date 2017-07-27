package scanner;

import beaver.Symbol;
import beaver.Scanner;
import parser.JavaParser.Terminals;
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


