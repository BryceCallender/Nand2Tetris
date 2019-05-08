import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

enum TokenType
{
    KEYWORD,
    SYMBOL,
    IDENTIFIER,
    INT_CONST,
    STRING_CONST
}

enum KeyWord
{
    CLASS,
    METHOD,
    FUNCTION,
    CONSTRUCTOR,
    INT,
    BOOLEAN,
    CHAR,
    VOID,
    VAR,
    STATIC,
    FIELD,
    LET,
    DO,
    IF,
    ELSE,
    WHILE,
    RETURN,
    TRUE,
    FALSE,
    NULL,
    THIS
}

public class JackTokenizer
{
    String currentToken;
    String line = "";
    StringBuilder tokenString;

    ArrayList<String> tokens;
    BufferedReader inputFileStream;

    HashMap<String, Integer> keyWords;
    HashMap<String, Integer> symbols;

    boolean validToken = false;
    int stringPosition = 0;

    JackTokenizer(File file)
    {
        tokens = new ArrayList<>();
        keyWords = new HashMap<>();
        symbols = new HashMap<>();
        tokenString = new StringBuilder();

        keyWords.put("class",0);
        keyWords.put("constructor",0);
        keyWords.put("function",0);
        keyWords.put("method",0);
        keyWords.put("field",0);
        keyWords.put("static",0);
        keyWords.put("var",0);
        keyWords.put("int",0);
        keyWords.put("char",0);
        keyWords.put("boolean",0);
        keyWords.put("void",0);
        keyWords.put("true",0);
        keyWords.put("false",0);
        keyWords.put("null",0);
        keyWords.put("this",0);
        keyWords.put("let",0);
        keyWords.put("do",0);
        keyWords.put("if",0);
        keyWords.put("else",0);
        keyWords.put("while",0);
        keyWords.put("return",0);

        symbols.put("{",0);
        symbols.put("}",0);
        symbols.put("(",0);
        symbols.put(")",0);
        symbols.put("[",0);
        symbols.put("]",0);
        symbols.put(".",0);
        symbols.put(",",0);
        symbols.put(";",0);
        symbols.put("+",0);
        symbols.put("-",0);
        symbols.put("*",0);
        symbols.put("/",0);
        symbols.put("&",0);
        symbols.put("|",0);
        symbols.put("<",0);
        symbols.put(">",0);
        symbols.put("=",0);
        symbols.put("~",0);

        try
        {
            inputFileStream = new BufferedReader(new FileReader(file));
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    public void advance()
    {
        if(hasMoreTokens())
        {
            if(line != null)
            {
                line = line.trim().replaceAll(" +", " ");

                if(line.contains("//"))
                {
                    line = line.substring(0,line.indexOf("//"));
                }
                //Parse the line for tokens
                validToken = false;
                currentToken = "";
                tokenString = new StringBuilder();

                //Comment gibberish
                if(line.startsWith("//") || line.startsWith("/*") || line.startsWith("*") || line.startsWith("*/") || line.isEmpty())
                {
                    stringPosition = line.length();
                    advance();
                }

                while(!validToken)
                {
                    if(stringPosition == line.length())
                    {
                        if(checkTokenValidity())
                        {
                            validToken = true;
                        }
                        else
                        {
                            eraseLastChar();
                        }
                    }
                    else
                    {
                        if(line.charAt(stringPosition) == ' ')
                        {
                            if(currentToken.trim().isEmpty())
                            {
                                stringPosition++;
                                return;
                            }

                            if(checkTokenValidity())
                            {
                                validToken = true;
                                stringPosition++;
                            }
                            else
                            {
                                eraseLastChar();
                            }
                            currentToken = tokenString.toString();

                        }
                        else
                        {
                            if(line.charAt(stringPosition) == '\"' && currentToken.isEmpty())
                            {
                                tokenString.append(line.charAt(stringPosition));
                                stringPosition++;
                                while(line.charAt(stringPosition) != '\"')
                                {
                                    tokenString.append(line.charAt(stringPosition));
                                    stringPosition++;
                                }
                                tokenString.append(line.charAt(stringPosition));
                                currentToken = tokenString.toString();
                                stringPosition++;
                                validToken = true;
                            }
                            else
                            {
                                if(checkTokenValidity())
                                {
                                    tokenString.append(line.charAt(stringPosition));
                                    stringPosition++;
                                }
                                else
                                {
                                    eraseLastChar();
                                }
                                currentToken = tokenString.toString();
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean checkTokenValidity()
    {
        if(!currentToken.isEmpty())
        {
            return symbols.containsKey(currentToken) || keyWords.containsKey(currentToken) || isIntConst() || isStringConst() || isIdentifier();
        }

        return true;
    }

    public boolean hasMoreTokens()
    {
        if(line != null && stringPosition < line.length())
        {
            return true;
        }

        try
        {
            stringPosition = 0;
            return ((line = inputFileStream.readLine()) != null);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return false;
    }


    public TokenType getTokenType()
    {

        if(keyWords.containsKey(currentToken))
        {
            return TokenType.KEYWORD;
        }

        if(symbols.containsKey(currentToken))
        {
            return TokenType.SYMBOL;
        }

        if(isIntConst())
        {
            return TokenType.INT_CONST;
        }

        if(isStringConst())
        {
            return TokenType.STRING_CONST;
        }

        if(isIdentifier())
        {
            return TokenType.IDENTIFIER;
        }
        return null;
    }

    KeyWord getKeyWord()
    {
        switch(currentToken)
        {
            case "class": return KeyWord.CLASS;
            case "method": return KeyWord.METHOD;
            case "function": return KeyWord.FUNCTION;
            case "field": return KeyWord.FIELD;
            case "static": return KeyWord.STATIC;
            case "var": return KeyWord.VAR;
            case "int": return KeyWord.INT;
            case "char": return KeyWord.CHAR;
            case "boolean": return KeyWord.BOOLEAN;
            case "void": return KeyWord.VOID;
            case "true": return KeyWord.TRUE;
            case "false": return KeyWord.FALSE;
            case "null": return KeyWord.NULL;
            case "this": return KeyWord.THIS;
            case "let": return KeyWord.LET;
            case "do": return KeyWord.DO;
            case "if": return KeyWord.IF;
            case "else": return KeyWord.ELSE;
            case "while": return KeyWord.WHILE;
            case "constructor": return KeyWord.CONSTRUCTOR;
            case "return": return KeyWord.RETURN;
        }
        return null;
    }

    //Can only be called when its SYMBOL
    public char getCurentToken()
    {
        return currentToken.charAt(0);
    }

    //Can only be called when its an IDENTIFIER
    public String getIdentifier()
    {
        return currentToken;
    }

    //Can only be called when its an INT_CONST
    public int getIntVal()
    {
        return Integer.parseInt(currentToken);
    }

    //Can only be called when its a STRING_CONST
    public String getStringVal()
    {
        return currentToken.substring(1,currentToken.indexOf("\""));
    }

    public String printXML()
    {
        StringBuilder output = new StringBuilder();
        switch (getTokenType())
        {
            case KEYWORD:
                output.append("<keyword>");
                output.append(" ").append(currentToken).append(" ");
                output.append("</keyword>");
                break;
            case SYMBOL:
                output.append("<symbol>");
                String temp = currentToken;
                switch (currentToken)
                {
                    case "<": temp = "&lt;";
                        break;
                    case ">": temp = "&gt;";
                        break;
                    case "\"":temp = "&quot;";
                        break;
                    case "&": temp = "&amp;";
                        break;
                    default: break;
                }
                output.append(" ").append(temp).append(" ");
                output.append("</symbol>");
                break;
            case IDENTIFIER:
                output.append("<identifier>");
                output.append(" ").append(currentToken).append(" ");
                output.append("</identifier>");
                break;
            case INT_CONST:
                output.append("<integerConstant>");
                output.append(" ").append(currentToken).append(" ");
                output.append("</integerConstant>");
                break;
            case STRING_CONST:
                output.append("<stringConstant>");
                output.append(" ").append(currentToken.replace("\"", "")).append(" ");
                output.append("</stringConstant>");
                break;
        }
        return output.toString();
    }

    boolean isIntConst()
    {
        try
        {
            // checking valid integer using parseInt() method
            int value = Integer.parseInt(currentToken);
            return value >= 0;
        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }

    boolean isStringConst()
    {
        return currentToken.charAt(0) == '"' && currentToken.charAt(currentToken.length()-1) == '"';
    }

    private boolean isIdentifier()
    {
        if(Character.isDigit(currentToken.charAt(0)))
        {
            return false;
        }
        else
        {
            for (Character character: currentToken.toCharArray())
            {
                if((!Character.isDigit(character) && !Character.isLetter(character) && !character.equals('_')) || symbols.containsKey("" + character))
                {
                    return false;
                }
            }
            return true;
        }
    }

    public boolean isTypeOrClassName(String className)
    {
        return (currentToken.equals("int") || currentToken.equals("char") || currentToken.equals("boolean") || currentToken.equals(className));
    }

    private void eraseLastChar()
    {
        currentToken = tokenString.delete(tokenString.length()-1,tokenString.length()).toString();
        validToken = true;
        stringPosition--;
    }
}
