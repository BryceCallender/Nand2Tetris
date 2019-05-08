import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

enum Segment
{
    CONSTANT,
    ARGUMENT,
    LOCAL,
    STATIC,
    THIS,
    THAT,
    POINTER,
    TEMP
}

enum Command
{
    ADD,
    SUB,
    NEG,
    EQ,
    GT,
    LT,
    AND,
    OR,
    NOT
}

public class VMWriter
{
    PrintWriter outputFileStream;

    VMWriter(File file)
    {
        try
        {
            outputFileStream = new PrintWriter(file);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    void writePush(Segment segment, int index)
    {
        outputFileStream.println("push " + segment.name().toLowerCase() + " " + index);
    }

    void writePop(Segment segment, int index)
    {
        outputFileStream.println("pop " + segment.name().toLowerCase() + " " + index);
    }

    void writeArithmetic(Command command)
    {
        outputFileStream.println(command.name().toLowerCase());
    }

    void writeLabel(String label)
    {
        outputFileStream.println("label " + label);
    }

    void writeGoto(String label)
    {
        outputFileStream.println("goto " + label);
    }

    void writeIf(String label)
    {
        outputFileStream.println("if-goto " + label);
    }

    void writeCall(String name, int nArgs)
    {
        outputFileStream.println("call " + name + " " + nArgs);
    }

    void writeFunction(String name, int nLocals)
    {
        outputFileStream.println("function " + name + " " + nLocals);
    }

    void writeReturn()
    {
        outputFileStream.println("return");
    }

    void close()
    {
        outputFileStream.close();
        System.out.println("Closing file");
    }

    Segment getSegment(Kind kind)
    {
        switch(kind)
        {
            case STATIC: return Segment.LOCAL;
            case FIELD: return  Segment.THIS;
            case ARG: return Segment.ARGUMENT;
            case VAR: return Segment.LOCAL;
            case NONE:
        }
        return null;
    }
}
