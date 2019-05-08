import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class CompilationEngine
{
    private JackTokenizer jackTokenizer;
    private PrintWriter xmlOutput;

    private SymbolTable symbolTable;
    private VMWriter vmWriter;

    private String className;
    private String subroutineName;

    private int numParams = 0;
    private int numLocals = 0;
    private String expression = "";

    private int whileCount = 0;
    private int ifCount = 0;

    CompilationEngine(File input, File output, File vmFile)
    {
        jackTokenizer = new JackTokenizer(input);
        symbolTable = new SymbolTable();
        vmWriter = new VMWriter(vmFile);

        try
        {
            xmlOutput = new PrintWriter(output);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        compileClass();
    }

    // 'class' className '{' classVarDec* subroutineDec* '}'
    void compileClass()
    {
        jackTokenizer.advance();
        xmlOutput.println("<class>");

        if(jackTokenizer.getTokenType() == TokenType.KEYWORD && jackTokenizer.getKeyWord() == KeyWord.CLASS)
        {
            //Prints out the xml for the 'class'
            xmlOutput.println(jackTokenizer.printXML());

            jackTokenizer.advance();
            className = jackTokenizer.getIdentifier();

            //Prints out the className
            xmlOutput.println(jackTokenizer.printXML());

            //Gets the left bracket '{'
            jackTokenizer.advance();
            xmlOutput.println(jackTokenizer.printXML());

            jackTokenizer.advance();
            //classVarDec*
            compileClassVarDec();
            //subroutineDec*
            compileSubroutine();

            // '}'
            xmlOutput.println(jackTokenizer.printXML());
        }
        //Finished the file so now we end it and close it
        xmlOutput.println("</class>");
        xmlOutput.close();
        vmWriter.close();
    }

    // ('static' | 'field') type varName (',' varName)* ';'
    // type = 'int' | 'char' | 'boolean' | className
    void compileClassVarDec()
    {
        if(jackTokenizer.getTokenType() == TokenType.KEYWORD && (jackTokenizer.getKeyWord() == KeyWord.STATIC || jackTokenizer.getKeyWord() == KeyWord.FIELD))
        {
            String varType;
            String varName;

            xmlOutput.println("<classVarDec>");
            //Prints our the field or static variable
            xmlOutput.println(jackTokenizer.printXML());

            Kind kind = jackTokenizer.getIdentifier().equals("static")? Kind.STATIC : Kind.FIELD;

            jackTokenizer.advance();
            //prints out the type
            xmlOutput.println(jackTokenizer.printXML());

            varType = jackTokenizer.getIdentifier();

            jackTokenizer.advance();
            //prints out the varName
            xmlOutput.println(jackTokenizer.printXML());

            varName = jackTokenizer.getIdentifier();

            symbolTable.Define(varName,varType,kind);

            jackTokenizer.advance();

            //Check if we have a comma and if not then we have a semicolon
            while(jackTokenizer.getCurentToken() == ',' && jackTokenizer.getTokenType() == TokenType.SYMBOL)
            {
                xmlOutput.println(jackTokenizer.printXML());

                jackTokenizer.advance();
                //name of variable
                xmlOutput.println(jackTokenizer.printXML());

                varName = jackTokenizer.getIdentifier();

                symbolTable.Define(varName,varType,kind);

                jackTokenizer.advance();
            }

            xmlOutput.println(jackTokenizer.printXML());

            xmlOutput.println("</classVarDec>");

            jackTokenizer.advance();

            //Allows the *
            compileClassVarDec();

        }
    }

    // ('constructor' | 'function' | 'method') ('void' | type) subroutineName '('
    // parameterList ')' subroutineBody
    void compileSubroutine()
    {
        if(jackTokenizer.getTokenType() == TokenType.KEYWORD && (jackTokenizer.getKeyWord() == KeyWord.CONSTRUCTOR || jackTokenizer.getKeyWord() == KeyWord.FUNCTION || jackTokenizer.getKeyWord() == KeyWord.METHOD))
        {
            boolean constructor = false;
            //Start a new table for the subroutine
            symbolTable.startSubroutine();
            numLocals = 0;

            if(jackTokenizer.getKeyWord() == KeyWord.METHOD)
            {
                symbolTable.Define("this",className,Kind.ARG);
            }
            else if(jackTokenizer.getKeyWord() == KeyWord.CONSTRUCTOR)
            {
                constructor = true;
            }

            xmlOutput.println("<subroutineDec>");

            //prints out function, method, or constructor
            xmlOutput.println(jackTokenizer.printXML());

            jackTokenizer.advance();

            //If the function
            if(jackTokenizer.getTokenType() == TokenType.KEYWORD && jackTokenizer.getKeyWord() == KeyWord.VOID || jackTokenizer.isTypeOrClassName(className))
            {
                //prints out the type of return
                xmlOutput.println(jackTokenizer.printXML());

                jackTokenizer.advance();
                //prints out the name of the function
                xmlOutput.println(jackTokenizer.printXML());

                if(jackTokenizer.getTokenType() == TokenType.IDENTIFIER)
                {
                    subroutineName = jackTokenizer.getIdentifier();
                }

                jackTokenizer.advance();
                //prints out the '('
                xmlOutput.println(jackTokenizer.printXML());

                //This can be the ')' or tokens it depends and dealt
                //with in the parameter list
                jackTokenizer.advance();

                compileParamaterList();

                if(constructor)
                {
                    vmWriter.writePush(Segment.CONSTANT,numParams);
                    vmWriter.writeCall("Memory.alloc",1);
                    vmWriter.writePop(Segment.POINTER,0);
                }

                //prints out the ')'
                xmlOutput.println(jackTokenizer.printXML());

                xmlOutput.println("<subroutineBody>");

                jackTokenizer.advance();
                //prints out the '{'
                xmlOutput.println(jackTokenizer.printXML());


                jackTokenizer.advance();

                //varDec*
                compileVarDec();

                vmWriter.writeFunction(className + '.' + subroutineName,numLocals);

                //statments = statement*
                xmlOutput.println("<statements>");
                compileStatements();
                xmlOutput.println("</statements>");

                //jackTokenizer.advance();
                //prints out the '}'
                xmlOutput.println(jackTokenizer.printXML());

                xmlOutput.println("</subroutineBody>");
                xmlOutput.println("</subroutineDec>");

                jackTokenizer.advance();
                compileSubroutine();
            }
        }
    }

    // ((type varName) (',' type varName)*)? (? = appears 1 or 0 times)
    void compileParamaterList()
    {
        numParams = 0;
        xmlOutput.println("<parameterList>");

        //if its a type and if not just go straight to ending the list (no params)
        if(jackTokenizer.getTokenType() == TokenType.KEYWORD && jackTokenizer.isTypeOrClassName(className))
        {
            //type of parameter
            xmlOutput.println(jackTokenizer.printXML());

            String type = jackTokenizer.getIdentifier();

            jackTokenizer.advance();
            //name of variable
            xmlOutput.println(jackTokenizer.printXML());

            String name = jackTokenizer.getIdentifier();

            symbolTable.Define(name,type,Kind.ARG);

            numParams++;

            jackTokenizer.advance();

            while(jackTokenizer.getCurentToken() == ',')
            {
                xmlOutput.println(jackTokenizer.printXML());

                jackTokenizer.advance();
                //type
                xmlOutput.println(jackTokenizer.printXML());

                type = jackTokenizer.getIdentifier();

                jackTokenizer.advance();
                //name of variable
                xmlOutput.println(jackTokenizer.printXML());

                name = jackTokenizer.getIdentifier();

                symbolTable.Define(name,type,Kind.ARG);

                numParams++;
                jackTokenizer.advance();
            }
        }

        //vmWriter.writeFunction(className + '.' + subroutineName,numParams);

        xmlOutput.println("</parameterList>");
    }

    // 'var' type varName (',' varName)* ';'
    void compileVarDec()
    {
        //If we are going to declare variables
        if(jackTokenizer.getTokenType() == TokenType.KEYWORD && jackTokenizer.getKeyWord() == KeyWord.VAR)
        {
            xmlOutput.println("<varDec>");
            
            //var
            xmlOutput.println(jackTokenizer.printXML());

            jackTokenizer.advance();
            //type
            xmlOutput.println(jackTokenizer.printXML());

            String type = jackTokenizer.getIdentifier();

            jackTokenizer.advance();
            //varName
            xmlOutput.println(jackTokenizer.printXML());

            String name = jackTokenizer.getIdentifier();

            symbolTable.Define(name,type,Kind.VAR);

            numLocals++;

            jackTokenizer.advance();

            while(jackTokenizer.getTokenType() == TokenType.SYMBOL && jackTokenizer.getCurentToken() == ',')
            {
                xmlOutput.println(jackTokenizer.printXML());

                jackTokenizer.advance();
                //varName
                xmlOutput.println(jackTokenizer.printXML());

                name = jackTokenizer.getIdentifier();

                symbolTable.Define(name,type,Kind.VAR);

                numLocals++;

                jackTokenizer.advance();
            }

            //semicolon
            xmlOutput.println(jackTokenizer.printXML());

            xmlOutput.println("</varDec>");

            //varDec*
            jackTokenizer.advance();

            compileVarDec();
        }
    }

    // statement* where statement = letStatement | ifStatement | whileStatement | doStatement | returnStatement
    void compileStatements()
    {
        if(jackTokenizer.getTokenType() == TokenType.KEYWORD &&
           (jackTokenizer.getKeyWord() == KeyWord.LET || jackTokenizer.getKeyWord() == KeyWord.IF ||
           jackTokenizer.getKeyWord() == KeyWord.WHILE || jackTokenizer.getKeyWord() == KeyWord.DO ||
           jackTokenizer.getKeyWord() == KeyWord.RETURN))
        {
            switch(jackTokenizer.getKeyWord())
            {
                case LET: compileLet();
                    jackTokenizer.advance();
                    break;
                case DO: compileDo();
                    jackTokenizer.advance();
                    break;
                case IF: compileIf();
                    break;
                case WHILE: compileWhile();
                    jackTokenizer.advance();
                    break;
                case RETURN: compileReturn();
                    jackTokenizer.advance();
                    break;
            }

            compileStatements();
        }
    }

    // 'do' subroutineCall ';'
    void compileDo()
    {
        xmlOutput.println("<doStatement>");

        //do
        xmlOutput.println(jackTokenizer.printXML());

        //subroutineCall
        jackTokenizer.advance();
        compileSubroutineCall();

        jackTokenizer.advance();
        //';'
        xmlOutput.println(jackTokenizer.printXML());

        xmlOutput.println("</doStatement>");
    }

    // 'let' varName ( '[' expression ']')? '=' expression ';'
    void compileLet()
    {
        xmlOutput.println("<letStatement>");

        //let
        xmlOutput.println(jackTokenizer.printXML());

        jackTokenizer.advance();
        //varName
        xmlOutput.println(jackTokenizer.printXML());

        String name = jackTokenizer.getIdentifier();

        jackTokenizer.advance();

        if(jackTokenizer.getTokenType() == TokenType.SYMBOL && jackTokenizer.getCurentToken() == '[')
        {
            //'['
            xmlOutput.println(jackTokenizer.printXML());

            jackTokenizer.advance();
            compileExpression();

            //']'
            xmlOutput.println(jackTokenizer.printXML());

            jackTokenizer.advance();
        }

        //'='
        xmlOutput.println(jackTokenizer.printXML());

        jackTokenizer.advance();
        compileExpression();

        expression = jackTokenizer.line.substring(jackTokenizer.line.indexOf('=')+1,jackTokenizer.line.indexOf(";")).trim();

        System.out.println(expression);
        codeWrite(expression);

        vmWriter.writePop(vmWriter.getSegment(symbolTable.KindOf(name)),symbolTable.IndexOf(name));

        //System.out.println(expression);

        if(jackTokenizer.getCurentToken() != ';')
        {
            jackTokenizer.advance();
        }

        //';'
        xmlOutput.println(jackTokenizer.printXML());

        xmlOutput.println("</letStatement>");
    }

    // 'while' '(' expression ')' '{' statements '}'
    void compileWhile()
    {
        xmlOutput.println("<whileStatement>");

        String labelL1 = "WHILE_TRUE" + whileCount;
        String labelL2 = "WHILE_FALSE" + whileCount;
        whileCount++;

        vmWriter.writeLabel(labelL1);

        //while
        xmlOutput.println(jackTokenizer.printXML());

        jackTokenizer.advance();
        //'('
        xmlOutput.println(jackTokenizer.printXML());

        jackTokenizer.advance();
        compileExpression();

        expression = jackTokenizer.line.substring(jackTokenizer.line.indexOf("(")+1,jackTokenizer.line.lastIndexOf(")"));

        codeWrite(expression);

        vmWriter.writeArithmetic(Command.NOT);

        vmWriter.writeIf(labelL2);

        //')'
        xmlOutput.println(jackTokenizer.printXML());

        jackTokenizer.advance();
        //'{'
        xmlOutput.println(jackTokenizer.printXML());

        jackTokenizer.advance();

        xmlOutput.println("<statements>");
        compileStatements();
        xmlOutput.println("</statements>");

        vmWriter.writeGoto(labelL1);
        vmWriter.writeLabel(labelL2);

        //'}'
        xmlOutput.println(jackTokenizer.printXML());

        xmlOutput.println("</whileStatement>");
    }

    // 'return' expression? ';'
    void compileReturn()
    {
        xmlOutput.println("<returnStatement>");

        xmlOutput.println(jackTokenizer.printXML());

        jackTokenizer.advance();

        if(jackTokenizer.getTokenType() == TokenType.SYMBOL)
        {
            //';'
            xmlOutput.println(jackTokenizer.printXML());

            vmWriter.writePush(Segment.CONSTANT,0);
            vmWriter.writeReturn();

            xmlOutput.println("</returnStatement>");

            return;
        }
        else
        {
            //Could have no expression
            compileExpression();

            expression = jackTokenizer.line.substring(jackTokenizer.line.indexOf("n")+1,jackTokenizer.line.indexOf(";"));
            codeWrite(expression);

            vmWriter.writeReturn();
        }

        //';'
        xmlOutput.println(jackTokenizer.printXML());

        xmlOutput.println("</returnStatement>");
    }

    // 'if' '(' expression ')' '{' statements '}' ('else' '{' statements '}')?
    // anything after if should not immediately see an advance call due to the
    // nature of it. If it reads something and doesn't see else you will lose
    // the token!
    void compileIf()
    {
        xmlOutput.println("<ifStatement>");

        String labelL1 = "IF_TRUE" + ifCount;
        String labelL2 = "IF_FALSE" + ifCount;
        ifCount++;

        xmlOutput.println(jackTokenizer.printXML());

        jackTokenizer.advance();
        //'('
        xmlOutput.println(jackTokenizer.printXML());

        jackTokenizer.advance();
        compileExpression();

        expression = jackTokenizer.line.substring(jackTokenizer.line.indexOf("(")+1,jackTokenizer.line.lastIndexOf(")"));

        codeWrite(expression);

        //')'
        xmlOutput.println(jackTokenizer.printXML());

        jackTokenizer.advance();
        //'{'
        xmlOutput.println(jackTokenizer.printXML());

        jackTokenizer.advance();

        vmWriter.writeArithmetic(Command.NOT);
        vmWriter.writeIf(labelL1);

        xmlOutput.println("<statements>");
        compileStatements();
        xmlOutput.println("</statements>");

        vmWriter.writeGoto(labelL2);
        vmWriter.writeLabel(labelL1);

        //'}'
        xmlOutput.println(jackTokenizer.printXML());

        jackTokenizer.advance();

        if(jackTokenizer.getTokenType() == TokenType.KEYWORD && jackTokenizer.getKeyWord() == KeyWord.ELSE)
        {
            //else
            xmlOutput.println(jackTokenizer.printXML());

            jackTokenizer.advance();
            //'{'
            xmlOutput.println(jackTokenizer.printXML());

            jackTokenizer.advance();

            xmlOutput.println("<statements>");
            compileStatements();
            xmlOutput.println("</statements>");

            //'}'
            xmlOutput.println(jackTokenizer.printXML());

            jackTokenizer.advance();
        }

        vmWriter.writeLabel(labelL2);

        xmlOutput.println("</ifStatement>");
    }

    // term (op term)* (could have no expression too remember that)
    void compileExpression()
    {
        if(jackTokenizer.getTokenType() == TokenType.SYMBOL && jackTokenizer.getCurentToken() == ')')
        {
            return;
        }

        xmlOutput.println("<expression>");

//        expression += "(";

        compileTerm();

        while(jackTokenizer.getTokenType() == TokenType.SYMBOL &&
          (jackTokenizer.getCurentToken() == '+' || jackTokenizer.getCurentToken() == '-' ||
           jackTokenizer.getCurentToken() == '*' || jackTokenizer.getCurentToken() == '/' ||
           jackTokenizer.getCurentToken() == '&' || jackTokenizer.getCurentToken() == '|' ||
           jackTokenizer.getCurentToken() == '<' || jackTokenizer.getCurentToken() == '>' ||
           jackTokenizer.getCurentToken() == '='))
        {
            //prints out the OP
            xmlOutput.println(jackTokenizer.printXML());

            //expression += jackTokenizer.getCurentToken() + " ";

            jackTokenizer.advance();
            //term
            compileTerm();
        }

//        expression += ")";

        //System.out.println(expression);

        xmlOutput.println("</expression>");
    }

    //intConst | stringConst | 'true' | 'false | 'null' | 'this' | varName
    // | varName '[' expression ']' | subroutineCall | '(' expression ')'
    // | unaryOpTerm
    void compileTerm()
    {
        xmlOutput.println("<term>");

        if(jackTokenizer.isIntConst())
        {
            xmlOutput.println(jackTokenizer.printXML());
//            vmWriter.writePush(Segment.CONST, jackTokenizer.getIntVal());
//            expression += jackTokenizer.getIntVal();
            jackTokenizer.advance();

            if(jackTokenizer.getCurentToken() != ')')
            {
//                expression += " ";
            }
        }
        else if(jackTokenizer.isStringConst())
        {
            xmlOutput.println(jackTokenizer.printXML());
            jackTokenizer.advance();
        }
        else if(jackTokenizer.getKeyWord() == KeyWord.TRUE || jackTokenizer.getKeyWord() == KeyWord.FALSE ||
                jackTokenizer.getKeyWord() == KeyWord.NULL || jackTokenizer.getKeyWord() == KeyWord.THIS)
        {
            xmlOutput.println(jackTokenizer.printXML());
            jackTokenizer.advance();
        }
        else if(jackTokenizer.getTokenType() == TokenType.SYMBOL)
        {
            if(jackTokenizer.getCurentToken() == '(')
            {
                //'('
                xmlOutput.println(jackTokenizer.printXML());

                jackTokenizer.advance();
                numParams = 0;
                compileExpression();

                //')'
                xmlOutput.println(jackTokenizer.printXML());

                jackTokenizer.advance();
            }
            else if(jackTokenizer.getCurentToken() == '-' || jackTokenizer.getCurentToken() == '~')
            {
                xmlOutput.println(jackTokenizer.printXML());

                jackTokenizer.advance();
                compileTerm();
            }
        }
        else
        {
            //varName or subroutineCall
            xmlOutput.println(jackTokenizer.printXML());

            jackTokenizer.advance();

            if(jackTokenizer.getCurentToken() == '[' && jackTokenizer.getTokenType() == TokenType.SYMBOL)
            {
                //'['
                xmlOutput.println(jackTokenizer.printXML());

                jackTokenizer.advance();
                //expression
                compileExpression();

                //']'
                xmlOutput.println(jackTokenizer.printXML());

                jackTokenizer.advance();
            }
            else if((jackTokenizer.getCurentToken() == '(' || jackTokenizer.getCurentToken() == '.') && jackTokenizer.getTokenType() == TokenType.SYMBOL)
            {
                if(jackTokenizer.getCurentToken() == '(')
                {
                    xmlOutput.println(jackTokenizer.printXML());

                    jackTokenizer.advance();
                    //'('
                    xmlOutput.println(jackTokenizer.printXML());

                    jackTokenizer.advance();
                    compileExpressionList();

                    //')'
                    xmlOutput.println(jackTokenizer.printXML());

                    jackTokenizer.advance();
                }
                else if(jackTokenizer.getCurentToken() == '.')
                {
                    xmlOutput.println(jackTokenizer.printXML());

                    jackTokenizer.advance();
                    //subroutineName
                    xmlOutput.println(jackTokenizer.printXML());

                    jackTokenizer.advance();
                    //'('
                    xmlOutput.println(jackTokenizer.printXML());

                    jackTokenizer.advance();
                    compileExpressionList();

                    //')'
                    xmlOutput.println(jackTokenizer.printXML());

                    jackTokenizer.advance();
                }
            }
        }
        xmlOutput.println("</term>");
    }

    //(expression (',' expression)*)?
    void compileExpressionList()
    {
        xmlOutput.println("<expressionList>");

        compileExpression();

        while(jackTokenizer.getTokenType() == TokenType.SYMBOL && jackTokenizer.getCurentToken() == ',')
        {
            xmlOutput.println(jackTokenizer.printXML());

            jackTokenizer.advance();

            numParams++;

            compileExpression();
        }

        numParams++;
        xmlOutput.println("</expressionList>");
    }

    void compileSubroutineCall()
    {
        if(jackTokenizer.getTokenType() == TokenType.IDENTIFIER)
        {
            //subroutineName | (className | varName)
            xmlOutput.println(jackTokenizer.printXML());
            String name = jackTokenizer.getIdentifier();

            jackTokenizer.advance();

            //'(' expressionList ')'
            if(jackTokenizer.getTokenType() == TokenType.SYMBOL && jackTokenizer.getCurentToken() == '(')
            {
                //jackTokenizer.advance();
                //'('
                xmlOutput.println(jackTokenizer.printXML());

                jackTokenizer.advance();
                numParams = 0;
                compileExpressionList();

                //')'
                xmlOutput.println(jackTokenizer.printXML());
            }
            //'.' subroutineName '(' expressionList ')'
            else if(jackTokenizer.getTokenType() == TokenType.SYMBOL && jackTokenizer.getCurentToken() == '.')
            {
                //'.'
                xmlOutput.println(jackTokenizer.printXML());

                jackTokenizer.advance();
                //subroutineName
                xmlOutput.println(jackTokenizer.printXML());

                String subroutineName = jackTokenizer.getIdentifier();

                jackTokenizer.advance();
                //'('
                xmlOutput.println(jackTokenizer.printXML());

                jackTokenizer.advance();
                numParams = 0;
                compileExpressionList();

                expression = jackTokenizer.line.substring(3);

                System.out.println(expression);

                codeWrite(expression);
//                vmWriter.writeCall(name + "." + subroutineName,numParams);
                vmWriter.writePop(Segment.TEMP,0);

                //')'
                xmlOutput.println(jackTokenizer.printXML());
            }
        }
    }

    void codeWrite(String exp)
    {
        try
        {
            // checking if a valid integer using parseInt() method
            int value = Integer.parseInt(exp.trim());
            if(value < 0)
            {
                vmWriter.writePush(Segment.CONSTANT,-value);
                vmWriter.writeArithmetic(Command.NEG);
            }
            else
            {
                vmWriter.writePush(Segment.CONSTANT,value);
            }
        }
        catch (NumberFormatException e)
        {
            if(!exp.contains("(") && !exp.contains(")"))
            {
                if(exp.contains("+") || exp.contains("*") || exp.contains("&") || exp.contains("|") || exp.contains("<")
                || exp.contains(">") || exp.contains("-") || exp.contains("/") || exp.contains("~") || exp.contains("="))
                {
                    char character = exp.charAt(0);
                    int position = 0;
                    String exp1 = "";
                    String exp2;
                    char op;

                    while(character != '+' && character != '*' && character != '/' && character != '&' && character != '|' && character != '<' && character != '>' && character != '=' && character != '-')
                    {
                        position++;
                        exp1 += character;
                        character = exp.charAt(position);
                    }
                    op = exp.charAt(position);
                    position++;
                    exp2 = exp.substring(position+1);

                    codeWrite(exp1);

                    codeWrite(exp2);

                    switch(op)
                    {
                        case '*': vmWriter.writeCall("Math.multiply",2);
                            break;
                        case '/': vmWriter.writeCall("Math.divide",2);
                            break;
                        case '+': vmWriter.writeArithmetic(Command.ADD);
                            break;
                        case '-': vmWriter.writeArithmetic(Command.SUB);
                            break;
                        case '&': vmWriter.writeArithmetic(Command.AND);
                            break;
                        case '|': vmWriter.writeArithmetic(Command.OR);
                            break;
                        case '<': vmWriter.writeArithmetic(Command.LT);
                            break;
                        case '>': vmWriter.writeArithmetic(Command.GT);
                            break;
                        case '=': vmWriter.writeArithmetic(Command.EQ);
                            break;
                        case '~' : vmWriter.writeArithmetic(Command.NOT);
                            break;
                    }

                }
                else
                {
                    exp = exp.trim();
                    if(exp.equals("true"))
                    {
                        vmWriter.writePush(Segment.CONSTANT,0);
                        vmWriter.writeArithmetic(Command.NOT);
                    }
                    else if(exp.equals("false"))
                    {
                        vmWriter.writePush(Segment.CONSTANT,0);
                    }
                    else if(exp.equals("this"))
                    {
                        vmWriter.writePush(Segment.THIS,0);
                    }
                    else
                    {
                        System.out.println(exp);
                        vmWriter.writePush(vmWriter.getSegment(symbolTable.KindOf(exp.trim())),symbolTable.IndexOf(exp.trim()));
                    }
                }
            }
            else
            {
                if(exp.startsWith("("))
                {
                    //codeWrite(exp1), codeWrite(exp2), output "op"
                    String exp1 = "";
                    String exp2;
                    char op;

                    int position = 1;
                    char character = exp.charAt(position);

                    if(character == '(')
                    {
                        if(exp.charAt(0) == '(')
                        {
                            int counter = 2;
                            for (int i = 1; i < exp.length(); i++)
                            {
                                exp1 += exp.charAt(i);
                                if(exp.charAt(i) == ')')
                                {
                                    counter--;
                                }
                                if(counter == 0)
                                    break;
                            }
                            System.out.println(exp1);
                        }
                        else
                        {
                            exp1 = exp.substring(1,exp.indexOf(")")+1);
                            System.out.println(exp1);
                            position += exp1.length() + 1;
                        }
                    }
                    else
                    {
                        while(character != '+' && character != '*' && character != '/' && character != '&' && character != '|' && character != '<' && character != '>' && character != '=')
                        {
                            position++;
                            exp1 += character;
                            character = exp.charAt(position);
                        }
                        System.out.println(exp1);
                    }

                    op = exp.charAt(position);

                    System.out.println(op);

                    position++;

                    exp2 = exp.substring(position,exp.length()-1);

                    System.out.println(exp2);

                    codeWrite(exp1.trim());

                    codeWrite(exp2.trim());

                    switch(op)
                    {
                        case '*': vmWriter.writeCall("Math.multiply",2);
                            break;
                        case '/': vmWriter.writeCall("Math.divide",2);
                            break;
                        case '+': vmWriter.writeArithmetic(Command.ADD);
                            break;
                        case '-': vmWriter.writeArithmetic(Command.SUB);
                            break;
                        case '&': vmWriter.writeArithmetic(Command.AND);
                            break;
                        case '|': vmWriter.writeArithmetic(Command.OR);
                            break;
                        case '<': vmWriter.writeArithmetic(Command.LT);
                            break;
                        case '>': vmWriter.writeArithmetic(Command.GT);
                            break;
                        case '=': vmWriter.writeArithmetic(Command.EQ);
                            break;
                        case '~' : vmWriter.writeArithmetic(Command.NOT);
                            break;
                    }
                }
                //starts with op or a function call
                else
                {
                    //function call
                    if(exp.contains("."))
                    {
                        if(exp.contains(","))
                        {
                            String functionName = exp.substring(0,exp.indexOf("("));
                            exp = exp.substring(exp.indexOf("(")+1,exp.lastIndexOf(")"));
                            String[] exps = exp.split("\\s*,\\s*");

                            for (int i = 0; i < exps.length; i++)
                            {
                                System.out.println(exps[i]);
                                codeWrite(exps[i]);
                            }

                            vmWriter.writeCall(functionName,exps.length);
                        }
                        else
                        {
                            String functionName = exp.substring(0,exp.indexOf("("));
                            exp = exp.substring(exp.indexOf("(")+1,exp.lastIndexOf(")"));

                            System.out.println(exp);
                            codeWrite(exp);

                            vmWriter.writeCall(functionName,1);
                        }
                    }
                    //op(exp1)
                    else if(exp.charAt(0) == '+' || exp.charAt(0) == '-' || exp.charAt(0) == '*' || exp.charAt(0) == '/' || exp.charAt(0) == '&'
                            || exp.charAt(0) == '|' || exp.charAt(0) == '<' || exp.charAt(0) == '>' || exp.charAt(0) == '~' || exp.charAt(0) == '=')
                    {
                        //codeWrite(exp1), output "op"
                        char op = exp.charAt(0);
                        String exp1 = exp.substring(exp.indexOf('('));

                        System.out.println(exp1);
                        System.out.println(op);
                        codeWrite(exp1);

                        switch(op)
                        {
                            case '*': vmWriter.writeCall("Math.multiply",2);
                                break;
                            case '/': vmWriter.writeCall("Math.divide",2);
                                break;
                            case '+': vmWriter.writeArithmetic(Command.ADD);
                                break;
                            case '-': vmWriter.writeArithmetic(Command.SUB);
                                break;
                            case '&': vmWriter.writeArithmetic(Command.AND);
                                break;
                            case '|': vmWriter.writeArithmetic(Command.OR);
                                break;
                            case '<': vmWriter.writeArithmetic(Command.LT);
                                break;
                            case '>': vmWriter.writeArithmetic(Command.GT);
                                break;
                            case '=': vmWriter.writeArithmetic(Command.EQ);
                                break;
                            case '~' : vmWriter.writeArithmetic(Command.NOT);
                                break;
                        }
                    }
                    else
                    {
                        String exp1 = "";
                        String exp2;
                        char op;

                        int position = 0;
                        char character = exp.charAt(position);

                        if(character == '(')
                        {
                            exp1 = exp.substring(1,exp.indexOf(")")+1);
                            System.out.println(exp1);
                            position += exp1.length() + 1;
                        }
                        else
                        {
                            while(character != '+' && character != '*' && character != '/' && character != '&' && character != '|' && character != '<' && character != '>' && character != '=')
                            {
                                position++;
                                exp1 += character;
                                character = exp.charAt(position);
                            }
                            System.out.println(exp1);
                        }

                        op = exp.charAt(position);

                        System.out.println(op);

                        position++;

                        exp2 = exp.substring(position);

                        System.out.println(exp2);

                        codeWrite(exp1.trim());

                        codeWrite(exp2.trim());

                        switch(op)
                        {
                            case '*': vmWriter.writeCall("Math.multiply",2);
                                break;
                            case '/': vmWriter.writeCall("Math.divide",2);
                                break;
                            case '+': vmWriter.writeArithmetic(Command.ADD);
                                break;
                            case '-': vmWriter.writeArithmetic(Command.SUB);
                                break;
                            case '&': vmWriter.writeArithmetic(Command.AND);
                                break;
                            case '|': vmWriter.writeArithmetic(Command.OR);
                                break;
                            case '<': vmWriter.writeArithmetic(Command.LT);
                                break;
                            case '>': vmWriter.writeArithmetic(Command.GT);
                                break;
                            case '=': vmWriter.writeArithmetic(Command.EQ);
                                break;
                            case '~' : vmWriter.writeArithmetic(Command.NOT);
                                break;
                        }
                    }
                }
            }
        }
    }
}
