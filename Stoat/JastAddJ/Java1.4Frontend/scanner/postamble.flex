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
