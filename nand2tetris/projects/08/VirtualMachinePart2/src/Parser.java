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
                //avoid comment lines
                if(line.charAt(0) != '/' && line.charAt(1) != '/')
                {
                    instructionLine = line;
                    //Get rid of the comments on the end
                    if(instructionLine.contains("//"))
                    {
                        instructionLine = instructionLine.substring(0,instructionLine.indexOf("//"));
                    }

                    //Write to file based on the command type
                    switch (getCommandType())
                    {
                        case C_ARITHMETIC: VirtualMachine.codeWriter.writeArithmetic(instructionLine);
                            break;
                        case C_PUSH: VirtualMachine.codeWriter.writePushPop(getCommandType(),getArg1(),getArg2());
                            break;
                        case C_POP: VirtualMachine.codeWriter.writePushPop(getCommandType(),getArg1(),getArg2());
                            break;
                        case C_LABEL: VirtualMachine.codeWriter.writeLabel(getArg1());
                            break;
                        case C_GOTO: VirtualMachine.codeWriter.writeGoto(getArg1());
                            break;
                        case C_IF: VirtualMachine.codeWriter.writeIf(getArg1());
                            break;
                        case C_FUNCTION: VirtualMachine.codeWriter.writeFunction(getArg1(),getArg2());
                            break;
                        case C_RETURN: VirtualMachine.codeWriter.writeReturn();
                            break;
                        case C_CALL: VirtualMachine.codeWriter.writeCall(getArg1(),getArg2());
                            break;
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
        else if(splitString.length == 2)
        {
            switch (splitString[0])
            {
                case "if-goto":
                    return CommandType.C_IF;
                case "goto":
                    return CommandType.C_GOTO;
                case "label":
                    return CommandType.C_LABEL;
            }
        }
        else if(splitString.length == 3)
        {
            switch (splitString[0])
            {
                case "push":
                    return CommandType.C_PUSH;
                case "pop":
                    return CommandType.C_POP;
                case "function":
                    return CommandType.C_FUNCTION;
                case "call":
                    return CommandType.C_CALL;
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
            case C_POP:
            case C_LABEL:
            case C_GOTO:
            case C_IF:
            case C_CALL:
            case C_FUNCTION:return splitString[1];
            case C_RETURN:
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
