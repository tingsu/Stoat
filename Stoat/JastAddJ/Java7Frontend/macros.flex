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

