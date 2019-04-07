import java.io.*;

public class CodeWriter
{
    String fileName;
    PrintWriter outputFileStream;
    int equalCount = 0;
    int greaterCount = 0;
    int lessCount = 0;
    int returnCount = 0;
    String functionName = "";

    CodeWriter() {}

    public void setFileName(String name) throws IOException
    {
        if(name.contains(".vm"))
        {
            fileName = name.substring(name.lastIndexOf('/'),name.indexOf(".vm"));
            name = name.substring(0,name.lastIndexOf('/'));
            name += fileName + ".asm";
            outputFileStream = new PrintWriter(name);
        }
        else
        {
            fileName = name.substring(name.lastIndexOf('/'));
            name += fileName + ".asm";
            outputFileStream = new PrintWriter(new FileWriter(name,true));
            System.out.println(name);
        }
    }

    public void writeArithmetic(String line)
    {
        line = line.trim();
        outputFileStream.println("//" + line);
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
            outputFileStream.println("//push " + segment + " " + index);
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
                    staticArithmetic(true,index);
                    break;
            }
            //Write what happens to the stack
            pushToStack();
        }
        else //its a pop command
        {
            outputFileStream.println("//pop " + segment + " " + index);
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
                    staticArithmetic(false,index);
                    break;
            }
            popFromStack();
        }
    }

    public void writeInit()
    {
        pushResultToRegister("SP", 256);
        writeCall("Sys.init",0);
    }

    public void writeLabel(String label)
    {
        outputFileStream.println("//label " + label);
        outputFileStream.println("(" + label + ")");
    }

    public void writeGoto(String gotoCommand)
    {
        outputFileStream.println("//goto " + gotoCommand);
        outputFileStream.println("@" + gotoCommand);
        outputFileStream.println("0;JMP");
    }

    public void writeIf(String ifCommand)
    {
        //Will jump if the value is not zero
        //so if its 1
        popFromStack();
        outputFileStream.println("@SP");
        outputFileStream.println("A=M");
        outputFileStream.println("D=M");
        outputFileStream.println("@" + ifCommand);
        outputFileStream.println("D;JNE");
    }

    public void writeCall(String functionName, int numArgs)
    {
        outputFileStream.println("//call " + functionName + " " + numArgs);
        //Makes the label and things of Name$ret.Digit where digit is unique identifier
        String returnString = functionName +  "$ret." + returnCount;
        //Push the return address onto the stack
        outputFileStream.println("@" + returnString);
        outputFileStream.println("D=A");
        pushToStack();

        //Push local
        outputFileStream.println("@LCL");
        outputFileStream.println("D=M");
        pushToStack();

        //Push ARG
        outputFileStream.println("@ARG");
        outputFileStream.println("D=M");
        pushToStack();

        //Push this
        outputFileStream.println("@THIS");
        outputFileStream.println("D=M");
        pushToStack();

        //Push that
        outputFileStream.println("@THAT");
        outputFileStream.println("D=M");
        pushToStack();

        //ARG = SP - n - 5
        //This puts SP into a temp register R13
        outputFileStream.println("@SP");
        outputFileStream.println("D=M");
        outputFileStream.println("@R13");
        outputFileStream.println("M=D");

        //SP - n
        outputFileStream.println("@" + numArgs);
        outputFileStream.println("D=A");
        outputFileStream.println("@R13");
        outputFileStream.println("M=M-D");

        //SP - n - 5
        outputFileStream.println("@5");
        outputFileStream.println("D=A");
        outputFileStream.println("@R13");
        outputFileStream.println("MD=M-D");
        outputFileStream.println("@ARG");
        outputFileStream.println("M=D");

        //LCL = SP
        outputFileStream.println("@SP");
        outputFileStream.println("D=M");
        outputFileStream.println("@LCL");
        outputFileStream.println("M=D");

        //Goto f
        writeGoto(functionName);

        //Declare label for return address
        writeLabel(returnString);
        returnCount++;
    }

    //FRAME is R14
    //RET is R15
    public void writeReturn()
    {
        outputFileStream.println("//return");
        popFromStack();
        //Store temp variable
        outputFileStream.println("@SP");
        outputFileStream.println("A=M");
        outputFileStream.println("D=M");
        outputFileStream.println("@R13");
        outputFileStream.println("M=D");
        incrementSP();

        //FRAME = LCL
        outputFileStream.println("@LCL");
        outputFileStream.println("D=M");
        outputFileStream.println("@R14");
        outputFileStream.println("M=D");

        //RET = *(FRAME - 5)
        outputFileStream.println("@5");
        outputFileStream.println("D=A");
        outputFileStream.println("@R14");
        outputFileStream.println("A=M-D");
        outputFileStream.println("D=M");
        outputFileStream.println("@R15");
        outputFileStream.println("M=D");

        //*ARG = pop()
        popFromStack();
        outputFileStream.println("@SP");
        outputFileStream.println("D=M");
        outputFileStream.println("@ARG");
        outputFileStream.println("A=M");
        outputFileStream.println("M=D");

        //Push Result to the stack
        outputFileStream.println("@R13");
        outputFileStream.println("D=M");
        outputFileStream.println("@ARG");
        outputFileStream.println("A=M");
        outputFileStream.println("M=D");

        //SP = ARG + 1
        outputFileStream.println("@ARG");
        outputFileStream.println("D=M+1");
        outputFileStream.println("@SP");
        outputFileStream.println("M=D");

        //THAT = *(FRAME - 1)
        outputFileStream.println("@R14");
        outputFileStream.println("A=M-1");
        outputFileStream.println("D=M");
        outputFileStream.println("@THAT");
        outputFileStream.println("M=D");

        //THIS = *(FRAME - 2)
        outputFileStream.println("@2");
        outputFileStream.println("D=A");
        outputFileStream.println("@R14");
        outputFileStream.println("A=M-D");
        outputFileStream.println("D=M");
        outputFileStream.println("@THIS");
        outputFileStream.println("M=D");

        //ARG = *(FRAME - 3)
        outputFileStream.println("@3");
        outputFileStream.println("D=A");
        outputFileStream.println("@R14");
        outputFileStream.println("A=M-D");
        outputFileStream.println("D=M");
        outputFileStream.println("@ARG");
        outputFileStream.println("M=D");

        //LCL = *(FRAME - 4)
        outputFileStream.println("@4");
        outputFileStream.println("D=A");
        outputFileStream.println("@R14");
        outputFileStream.println("A=M-D");
        outputFileStream.println("D=M");
        outputFileStream.println("@LCL");
        outputFileStream.println("M=D");

        outputFileStream.println("@R15");
        outputFileStream.println("A=M");
        outputFileStream.println("0;JMP");
    }

    public void writeFunction(String functionName, int numLocals)
    {
        outputFileStream.println("//function " + functionName + " " + numLocals);
        this.functionName = functionName;
        outputFileStream.println("(" + functionName + ")");
        for(int i = 0; i < numLocals; i++)
        {
            //Initialize all of the local variables to 0
            outputFileStream.println("@SP");
            outputFileStream.println("A=M");
            outputFileStream.println("M=0");
            incrementSP();
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

    void pushResultToRegister(String register, int result)
    {
        outputFileStream.println("@" + result);
        outputFileStream.println("D=A");
        outputFileStream.println("@" + register);
        outputFileStream.println("M=D");
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

    void staticArithmetic(boolean push, int offset)
    {
        String splitString = functionName.substring(0,functionName.indexOf("."));
        if(push)
        {
            outputFileStream.println("@" + splitString + ".static" + offset);
            outputFileStream.println("D=M");
            outputFileStream.println("@SP");
            outputFileStream.println("A=M");
            outputFileStream.println("M=D");
        }
        else
        {
            popFromStack();
            outputFileStream.println("@SP");
            outputFileStream.println("A=M");
            outputFileStream.println("D=M");
            outputFileStream.println("@" + splitString + ".static" + offset);
            outputFileStream.println("M=D");
            incrementSP();
        }
    }
}
