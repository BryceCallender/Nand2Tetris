import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class CompilationEngine
{
    private JackTokenizer jackTokenizer;
    private PrintWriter xmlOutput;

    private String className;
    private String subroutineName;

    CompilationEngine(File input, File output)
    {
        jackTokenizer = new JackTokenizer(input);

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
    }

    // ('static' | 'field') type varName (',' varName)* ';'
    // type = 'int' | 'char' | 'boolean' | className
    void compileClassVarDec()
    {
        if(jackTokenizer.getTokenType() == TokenType.KEYWORD && (jackTokenizer.getKeyWord() == KeyWord.STATIC || jackTokenizer.getKeyWord() == KeyWord.FIELD))
        {
            xmlOutput.println("<classVarDec>");
            //Prints our the field or static variable
            xmlOutput.println(jackTokenizer.printXML());

            jackTokenizer.advance();
            //prints out the type
            xmlOutput.println(jackTokenizer.printXML());

            jackTokenizer.advance();
            //prints out the varName
            xmlOutput.println(jackTokenizer.printXML());

            jackTokenizer.advance();

            //Check if we have a comma and if not then we have a semicolon
            while(jackTokenizer.getCurentToken() == ',' && jackTokenizer.getTokenType() == TokenType.SYMBOL)
            {
                xmlOutput.println(jackTokenizer.printXML());

                jackTokenizer.advance();
                //name of variable
                xmlOutput.println(jackTokenizer.printXML());

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

                //prints out the ')'
                xmlOutput.println(jackTokenizer.printXML());

                xmlOutput.println("<subroutineBody>");

                jackTokenizer.advance();
                //prints out the '{'
                xmlOutput.println(jackTokenizer.printXML());


                jackTokenizer.advance();

                //varDec*
                compileVarDec();

                //statments = statement*
                xmlOutput.println("<statements>");
                compileStatements();
                xmlOutput.println("</statements>");

                if(jackTokenizer.getCurentToken() != '}')
                {
                    System.err.println("Ended a function not on a right bracket");
                }

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
        xmlOutput.println("<parameterList>");

        //if its a type and if not just go straight to ending the list (no params)
        if(jackTokenizer.getTokenType() == TokenType.KEYWORD && jackTokenizer.isTypeOrClassName(className))
        {
            //type of parameter
            xmlOutput.println(jackTokenizer.printXML());

            jackTokenizer.advance();
            //name of variable
            xmlOutput.println(jackTokenizer.printXML());

            jackTokenizer.advance();

            while(jackTokenizer.getCurentToken() == ',')
            {
                xmlOutput.println(jackTokenizer.printXML());

                jackTokenizer.advance();
                //type
                xmlOutput.println(jackTokenizer.printXML());

                jackTokenizer.advance();
                //name of variable
                xmlOutput.println(jackTokenizer.printXML());

                jackTokenizer.advance();
            }
        }

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

            jackTokenizer.advance();
            //varName
            xmlOutput.println(jackTokenizer.printXML());

            jackTokenizer.advance();

            while(jackTokenizer.getTokenType() == TokenType.SYMBOL && jackTokenizer.getCurentToken() == ',')
            {
                xmlOutput.println(jackTokenizer.printXML());

                jackTokenizer.advance();
                //varName
                xmlOutput.println(jackTokenizer.printXML());

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

        //while
        xmlOutput.println(jackTokenizer.printXML());

        jackTokenizer.advance();
        //'('
        xmlOutput.println(jackTokenizer.printXML());

        jackTokenizer.advance();
        compileExpression();

        //')'
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

            xmlOutput.println("</returnStatement>");

            return;
        }
        else
        {
            //Could have no expression
            compileExpression();
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

        xmlOutput.println(jackTokenizer.printXML());

        jackTokenizer.advance();
        //'('
        xmlOutput.println(jackTokenizer.printXML());

        jackTokenizer.advance();
        compileExpression();

        //')'
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

            jackTokenizer.advance();
            //term
            compileTerm();
        }

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
            jackTokenizer.advance();
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

            compileExpression();
        }

        xmlOutput.println("</expressionList>");
    }

    void compileSubroutineCall()
    {
        if(jackTokenizer.getTokenType() == TokenType.IDENTIFIER)
        {
            //subroutineName | (className | varName)
            xmlOutput.println(jackTokenizer.printXML());

            jackTokenizer.advance();

            //'(' expressionList ')'
            if(jackTokenizer.getTokenType() == TokenType.SYMBOL && jackTokenizer.getCurentToken() == '(')
            {
                //jackTokenizer.advance();
                //'('
                xmlOutput.println(jackTokenizer.printXML());

                jackTokenizer.advance();
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

                jackTokenizer.advance();
                //'('
                xmlOutput.println(jackTokenizer.printXML());

                jackTokenizer.advance();
                compileExpressionList();

                //')'
                xmlOutput.println(jackTokenizer.printXML());
            }
        }
    }
}
