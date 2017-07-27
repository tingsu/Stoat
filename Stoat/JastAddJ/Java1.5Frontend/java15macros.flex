HexadecimalFloatingPointLiteral = {HexSignificand} {BinaryExponent}

HexSignificand = {HexNumeral} [\.]?
 | 0 [xX] [0-9a-fA-F]* \. [0-9a-fA-F]+

BinaryExponent = [pP] [+-]? {Digits}
