public class Assembler
{
    static int variablePlacement;
    private Parser parser;

    Assembler(String fileName) // filename will have it in the name NAME.asm
    {
        int findDotASM = fileName.indexOf(".asm");
        String asmFileName = fileName.substring(0,findDotASM);
        parser = new Parser(asmFileName);
    }
};