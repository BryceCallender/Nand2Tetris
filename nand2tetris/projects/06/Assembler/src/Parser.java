import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Parser
{
    private HashMap<String,Integer> symbolTable; //(xxx,n) symbol and address in memory
    private HashMap<String,String> compBinary;
    private HashMap<String,String> destBinary;
    private HashMap<String,String> jumpBinary;

    private boolean symbolLess = false;
    private ArrayList<String> codeLines;
    private BufferedReader inputFileStream;
    private PrintWriter outputFileStream;
    private final String path = "/Users/brycecallender/Desktop/Nand2tetris/nand2tetris/projects/06/";
    private String currentDirectory;

    Parser(String fileName)
    {
        symbolTable = new HashMap<>();
        compBinary = new HashMap<>();
        destBinary = new HashMap<>();
        jumpBinary = new HashMap<>();
        codeLines = new ArrayList<>();
        
        symbolTable.put("SP",0);
        symbolTable.put("LCL",1);
        symbolTable.put("ARG",2);
        symbolTable.put("THIS",3);
        symbolTable.put("THAT",4);
        for(int i = 0; i < 16; i++)
        {
            symbolTable.put("R" + i,i);
        }
        symbolTable.put("SCREEN",16384);
        symbolTable.put("KBD",24576);

        initDest();
        initComp();
        initJump();

        if(fileName.indexOf('L') != -1)
        {
            fileName = fileName.replace("L","");
            symbolLess = true;
        }

        //lowercase for finding the directory
        char c[] = fileName.toCharArray();
        c[0] += 32;
        fileName = new String(c);

        File file = new File(path);

        for (String name : file.list())
        {
            if(name.contains(fileName))
            {
                currentDirectory = name;
            }
        }

        //the asm files start with a capital so we need to uppercase in order to find it to read
        c = fileName.toCharArray();
        c[0] -= 32;
        fileName = new String(c);

        String inputFileName = currentDirectory + "/" + fileName;
        String outputFileName = currentDirectory + "/" + fileName;

        if(symbolLess)
        {
            inputFileName += "L.asm";
            outputFileName += "L.hack";
        }
        else
        {
            inputFileName += ".asm";
            outputFileName += ".hack";
        }

        try
        {
            inputFileStream = new BufferedReader(new FileReader(path + inputFileName));
            outputFileStream = new PrintWriter(path + outputFileName);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        try
        {
            firstPass();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    void firstPass() throws IOException
    {
        String line;
        while((line = inputFileStream.readLine()) != null)
        {
            line = line.replaceAll("\\s+", ""); //get rid of whitespace
            if(line.length() >= 2) //check to have at least 2 characters to do these operations
            {
                if(line.charAt(0) == '(' && line.charAt(line.length()-1) == ')') //pseudo-command
                {
                    //put the pseudo-command into the symbol table and dont push the line into the vector
                    //it'll have the rom address of size (vector size will be 1 when something added and
                    //we want to start at 0 so essentially when you load the label into rom just take the
                    //current size of the codeLines vector)
                    symbolTable.put(line.substring(1,line.length() - 1),codeLines.size());
                }
                else if(line.charAt(0) != '/' && line.charAt(1) != '/') //only come in if its not a comment
                {
                    if(line.contains("//"))
                    {
                        line = line.substring(0,line.indexOf("//")); //get rid of the comment at end of line
                        codeLines.add(line); //put the code line into the vector
                    }
                    else
                    {
                        codeLines.add(line);
                    }
                }
            }
        }
        secondPass();
    }

    void secondPass()
    {
        String line;
        while(codeLines.size() > 0)
        {
            line = codeLines.get(0);

            if(line.charAt(0) == '@')
            {
                if(!checkForNumbersOnly(line) && !symbolTable.containsKey(line.substring(1)))
                {
                    symbolTable.put(line.substring(1),Assembler.variablePlacement);
                    Assembler.variablePlacement++;
                }
                outputFileStream.write(decodeAInstruction(line) + "\n");
            }
            else
            {
                outputFileStream.write(decodeCInstruction(line) + "\n");
            }
            codeLines.remove(0);
        }
        outputFileStream.close();
    }

    String decodeAInstruction(String line)
    {
        String binary = "0";

        int number;

        if(symbolTable.containsKey(line.substring(1)))
        {
            number = symbolTable.get(line.substring(1));
        }
        else
        {
            number = Integer.valueOf(line.substring(1)); // after @
        }
        
        binary += padStringWithZeros(Integer.toBinaryString(number),15);

        return binary;
    }

    String decodeCInstruction(String line)
    {
        String binary = "111";

        //dest = comp;jump if dest is empty the "=" is emitted
        //if jump is empty then the ";" is emitted
        String dest = "null";
        String comp = "000000";
        String jump = "null";

        if(line.indexOf('=') != -1)
        {
            dest = line.substring(0, line.indexOf('='));

            if(line.indexOf(';') != -1)
            {
                comp = line.substring(line.indexOf('=')+1,line.indexOf(';'));
                jump = line.substring(line.indexOf(';'));
            }
            else
            {
                comp = line.substring(line.indexOf('=')+1);
            }
        }
        else //dest is empty
        {
            if(line.indexOf(';') != -1)
            {
                comp = line.substring(0,line.indexOf(';'));
                jump = line.substring(line.indexOf(';')+1);
            }
        }

        return binary + getComp(comp) + getDest(dest) + getJump(jump);
    }

    String getDest(String dest)
    {
        return destBinary.get(dest);
    }

    String getComp(String comp)
    {
        return compBinary.get(comp);
    }

    String getJump(String jump)
    {
        return jumpBinary.get(jump);
    }

    boolean checkForNumbersOnly(String line)
    {
        if(line.charAt(0) != '@')
        {
            return false;
        }

        String updatedLine = line.substring(1); //gets rid of the @

        for(int i = 0; i < updatedLine.length(); i++)
        {
            if(updatedLine.charAt(i) < '0' || updatedLine.charAt(i) > '9')
            {
                return false;
            }
        }
        return true;
    }

    void initDest()
    {
        String variables[] = {"null","M","D","MD","A","AM","AD","AMD"};
        for(int i = 0; i < 8; i++)
        {
            destBinary.put(variables[i],padStringWithZeros(Integer.toBinaryString(i),3));
        }
    }

    void initComp()
    {
        compBinary.put("0","0101010");
        compBinary.put("1","0111111");
        compBinary.put("-1","0111010");
        compBinary.put("D","0001100");
        compBinary.put("A","0110000");
        compBinary.put("M","1110000");
        compBinary.put("!D","0001101");
        compBinary.put("!A","0110001");
        compBinary.put("!M","1110001");
        compBinary.put("-D","0001111");
        compBinary.put("-A","0110011");
        compBinary.put("-M","1110011");
        compBinary.put("D+1","0011111");
        compBinary.put("A+1","0110111");
        compBinary.put("M+1","1110111");
        compBinary.put("D-1","0001110");
        compBinary.put("A-1","0110010");
        compBinary.put("M-1","1110010");
        compBinary.put("D+A","0000010");
        compBinary.put("D+M","1000010");
        compBinary.put("D-A","0010011");
        compBinary.put("D-M","1010011");
        compBinary.put("A-D","0000111");
        compBinary.put("M-D","1000111");
        compBinary.put("D&A","0000000");
        compBinary.put("D&M","1000000");
        compBinary.put("D|A","0010101");
        compBinary.put("D|M","1010101");
    }

    void initJump()
    {
        String variables[] = {"null","JGT","JEQ","JGE","JLT","JNE","JLE","JMP"};
        for(int i = 0; i < 8; i++)
        {
            jumpBinary.put(variables[i],padStringWithZeros(Integer.toBinaryString(i),3));
        }
    }

    String padStringWithZeros(String binaryString, int numToPad)
    {
        int numRemaining = numToPad - binaryString.length();
        StringBuilder paddedString = new StringBuilder(binaryString);
        for(int i = 0; i < numRemaining; i++)
        {
            paddedString.insert(0, "0");
        }
        return paddedString.toString();
    }
};
