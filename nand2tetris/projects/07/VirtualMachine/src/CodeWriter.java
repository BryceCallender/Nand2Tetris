import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class CodeWriter
{
    String fileName;
    public PrintWriter outputFileStream;
    int equalCount = 0;
    int greaterCount = 0;
    int lessCount = 0;

    CodeWriter() {}

    public void setFileName(String name) throws FileNotFoundException
    {
        fileName = name.substring(name.lastIndexOf('/'),name.indexOf(".vm"));
        name = name.substring(0,name.lastIndexOf('/'));
        name += fileName + ".asm";
        System.out.println(name);
        outputFileStream = new PrintWriter(name);
    }

    public void writeArithmetic(String line)
    {
        switch (line)
        {
            case "add":
                popFromStack();
                outputFileStream.println("D=M");
                popFromStack();
                outputFileStream.println("D=D+M");
                outputFileStream.println("M=D");
                incrementSP();
                break;
            case "sub":
                popFromStack();
                outputFileStream.println("D=M");
                popFromStack();
                outputFileStream.println("D=M-D");
                outputFileStream.println("M=D");
                incrementSP();
                break;
            case "neg":
                popFromStack();
                outputFileStream.println("M=-M");
                incrementSP();
                break;
            case "eq":
                //Nothing special just checking for the 0 for equals or not
                checkCondition("JEQ",equalCount,"Equal");
                equalCount++;
                break;
            case "gt":
                //Check for less than since that makes it true because of the way
                //it does comparison (it grabs the other number first then the first one)
                checkCondition("JLT",greaterCount,"Greater");
                greaterCount++;
                break;
            case "lt":
                //Check for less than since that makes it true because of the way
                //it does comparison (it grabs the other number first then the first one)
                checkCondition("JGT",lessCount,"Less");
                lessCount++;
                break;
            case "and":
                popFromStack();
                outputFileStream.println("D=M");
                popFromStack();
                outputFileStream.println("D=D&M");
                outputFileStream.println("M=D");
                incrementSP();
                break;
            case "or":
                popFromStack();
                outputFileStream.println("D=M");
                popFromStack();
                outputFileStream.println("D=D|M");
                outputFileStream.println("M=D");
                incrementSP();
                break;
            case "not":
                popFromStack();
                outputFileStream.println("M=!M");
                incrementSP();
                break;
        }
    }

    public void writePushPop(CommandType commandType, String segment, int index)
    {
        //If its a push command
        if(commandType == CommandType.C_PUSH)
        {
            switch(segment)
            {
                case "argument":
                    calculateAndStoreOffset("ARG",index,false);
                    getOffset();
                    break;
                case "local":
                    calculateAndStoreOffset("LCL",index,false);
                    getOffset();
                    break;
                case "constant":
                    outputFileStream.println("@" + index);
                    outputFileStream.println("D=A");
                    break;
                case "this":
                    calculateAndStoreOffset("THIS",index,false);
                    getOffset();
                    break;
                case "that":
                    calculateAndStoreOffset("THAT",index,false);
                    getOffset();
                    break;
                case "pointer":
                    pointerArithmetic(true,index);
                    break;
                case "temp":
                    calculateAndStoreOffset("5",index,true);
                    getOffset();
                    break;
                case "static":
                    calculateAndStoreOffset("16",index,true);
                    getOffset();
                    break;
            }
            //Write what happens to the stack
            pushToStack();
        }
        else //its a pop command
        {
            switch(segment)
            {
                case "argument":
                    calculateAndStoreOffset("ARG",index,false);
                    getOffsetAndStore();
                    break;
                case "local":
                    calculateAndStoreOffset("LCL",index,false);
                    getOffsetAndStore();
                    break;
                case "this":
                    calculateAndStoreOffset("THIS",index,false);
                    getOffsetAndStore();
                    break;
                case "that":
                    calculateAndStoreOffset("THAT",index,false);
                    getOffsetAndStore();
                    break;
                case "pointer":
                    pointerArithmetic(false,index);
                    break;
                case "temp":
                    //Starts at R5
                    calculateAndStoreOffset("5",index,true);
                    getOffsetAndStore();
                    break;
                case "static":
                    calculateAndStoreOffset("16",index,true);
                    getOffsetAndStore();
                    break;
            }
            popFromStack();
        }
    }

    void pushToStack()
    {
        outputFileStream.println("@SP");
        outputFileStream.println("AM=M+1");
        outputFileStream.println("A=A-1");
        outputFileStream.println("M=D");
    }

    void popFromStack()
    {
        outputFileStream.println("@SP");
        outputFileStream.println("AM=M-1");
    }

    void incrementSP()
    {
        outputFileStream.println("@SP");
        outputFileStream.println("AM=M+1");
        outputFileStream.println("A=A-1");
    }

    void pushResultToSP(int result)
    {
        outputFileStream.println("@SP");
        outputFileStream.println("A=M");
        outputFileStream.println("M=" + result);
    }

    void checkCondition(String jumpCommand, int count, String labelCommand)
    {
        popFromStack();
        outputFileStream.println("D=M");
        popFromStack();
        outputFileStream.println("D=D-M");
        outputFileStream.println("@" + labelCommand + count);
        outputFileStream.println("D;" + jumpCommand);
        pushResultToSP(0);
        outputFileStream.println("@End" + labelCommand + count);
        outputFileStream.println("0;JMP");
        outputFileStream.println("(" + labelCommand + count + ")");
        pushResultToSP(-1);
        outputFileStream.println("(End" + labelCommand + count + ")");
        incrementSP();
    }

    void calculateAndStoreOffset(String label, int offset, boolean temp)
    {
        outputFileStream.println("@" + label);

        //If were going to an actual valid address or we want a constant
        //value to grab like we do for temp since it starts at address 5 and goes
        //to 13
        if(temp)
        {
            outputFileStream.println("D=A");
        }
        else
        {
            outputFileStream.println("D=M");
        }

        outputFileStream.println("@"+offset);
        outputFileStream.println("D=D+A");
        outputFileStream.println("@R13");
        outputFileStream.println("M=D");
    }

    void getOffsetAndStore()
    {
        popFromStack();
        outputFileStream.println("@SP");
        outputFileStream.println("A=M");
        outputFileStream.println("D=M");
        outputFileStream.println("@R13");
        outputFileStream.println("A=M");
        outputFileStream.println("M=D");
        incrementSP();
    }

    void getOffset()
    {
        outputFileStream.println("D=M");
        outputFileStream.println("@R13");
        outputFileStream.println("A=M");
        outputFileStream.println("D=M");
    }

    void pointerArithmetic(boolean push, int offset)
    {
        if(push)
        {
            outputFileStream.println("@" + (3 + offset));
            outputFileStream.println("D=M");
            outputFileStream.println("@SP");
            outputFileStream.println("A=M");
            outputFileStream.println("M=D");
        }
        else
        {
            outputFileStream.println("@" + (3 + offset));
            outputFileStream.println("M=D");
            outputFileStream.println("@SP");
            outputFileStream.println("A=M");
        }
    }
}
