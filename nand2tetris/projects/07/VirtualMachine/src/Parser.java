import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class Parser
{
    BufferedReader inputFileStream;
    String instructionLine;

    HashMap<String,Integer> arithmeticOperators;

    Parser(String fileName)
    {
        arithmeticOperators = new HashMap<>();

        arithmeticOperators.put("add",0);
        arithmeticOperators.put("sub",0);
        arithmeticOperators.put("neg",0);
        arithmeticOperators.put("eq",0);
        arithmeticOperators.put("gt",0);
        arithmeticOperators.put("lt",0);
        arithmeticOperators.put("and",0);
        arithmeticOperators.put("or",0);
        arithmeticOperators.put("not",0);

        try
        {
            inputFileStream = new BufferedReader(new FileReader(fileName));
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    public void readFile() throws IOException
    {
        String line;
        while((line = inputFileStream.readLine()) != null)
        {
            if(line.length() > 0)
            {
                if(line.charAt(0) != '/' && line.charAt(1) != '/')
                {
                    instructionLine = line;
                    if(getCommandType() != CommandType.C_ARITHMETIC)
                    {
                        VirtualMachine.codeWriter.writePushPop(getCommandType(),getArg1(),getArg2());
                    }
                    else
                    {
                        VirtualMachine.codeWriter.writeArithmetic(instructionLine);
                    }

                }
            }
        }
    }

    CommandType getCommandType()
    {
        String[] splitString = instructionLine.split("\\s+");

        if(splitString.length == 1)
        {
            if(arithmeticOperators.containsKey(splitString[0]))
            {
                return CommandType.C_ARITHMETIC;
            }
            else if(splitString[0].equals("return"))
            {
                return CommandType.C_RETURN;
            }
        }
        else if(splitString.length == 3)
        {
            if(splitString[0].equals("push"))
            {
                return CommandType.C_PUSH;
            }
            else if(splitString[0].equals("pop"))
            {
                return CommandType.C_POP;
            }
        }

        return null;
    }

    String getArg1()
    {
        String[] splitString = instructionLine.split("\\s+");

        switch (getCommandType())
        {
            case C_ARITHMETIC: return instructionLine;
            case C_PUSH:
            case C_POP: return splitString[1];
            case C_LABEL:
                break;
            case C_GOTO:
                break;
            case C_IF:
                break;
            case C_FUNCTION:
                break;
            case C_RETURN:
                break;
            case C_CALL:
                break;
        }
        return "";
    }

    int getArg2()
    {
        String[] splitString = instructionLine.split("\\s+");

        return splitString.length == 3 ? Integer.parseInt(splitString[2]) : -1;
    }
}
