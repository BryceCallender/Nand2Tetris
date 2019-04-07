import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

enum CommandType
{
    C_ARITHMETIC,
    C_PUSH,
    C_POP,
    C_LABEL,
    C_GOTO,
    C_IF,
    C_FUNCTION,
    C_RETURN,
    C_CALL
}

public class VirtualMachine
{
    private ArrayList<Parser> parsers;
    private ArrayList<String> parserPaths;
    static CodeWriter codeWriter;
    private static String path = "/Users/brycecallender/desktop/Nand2Tetris/nand2tetris/projects/08";
    private boolean wroteInit = false;

    VirtualMachine(String fileName)
    {
        parsers = new ArrayList<>();
        parserPaths = new ArrayList<>();
        File file = new File(path);
        codeWriter = new CodeWriter();

        //gets rid of this random file that is attached???
        File[] nand2tetrisFiles = file.listFiles((dir, name) -> !name.equals(".DS_Store"));

        StringBuilder fullPathToFile = new StringBuilder(path);

        //We have to find the files and eventually determine if they gave us a directory to search for or
        //a specific file to decode
        for(File files: nand2tetrisFiles)
        {
            //If the directory is named the same thing as the command line argument
            if(files.isDirectory())
            {
                if(files.getName().equals(fileName))
                {
                    fullPathToFile.append('/').append(files.getName());
                    for(File subDirFiles: files.listFiles())
                    {
                        if(subDirFiles.isDirectory())
                        {
                            for(String subSubDir: subDirFiles.list())
                            {
                                if(subSubDir.contains(".vm"))
                                {
                                    String path = fullPathToFile + "/" + subDirFiles.getName() + "/" + subSubDir;
                                    parsers.add(new Parser(path));
                                    parserPaths.add(path);
                                }
                            }
                        }
                        fullPathToFile = new StringBuilder(path + '/' + files.getName());
                    }
                }
                //Look only for 1 file
                else
                {
                    for(File subDirFiles: files.listFiles())
                    {
                        if(subDirFiles.isDirectory())
                        {
                            if(subDirFiles.getName().equals(fileName))
                            {
                                String path = fullPathToFile + "/" + files.getName() + "/" + subDirFiles.getName();
                                for(String subSubDir: subDirFiles.list())
                                {
                                    if(subSubDir.contains(".vm"))
                                    {
                                        String vmPath = path + "/" + subSubDir;
                                        parsers.add(new Parser(vmPath));
                                        parserPaths.add(path);
                                    }
                                }
                            }
                            else
                            {
                                for(String subSubDir: subDirFiles.list())
                                {
                                    if(subSubDir.equals(fileName))
                                    {
                                        String path = fullPathToFile + "/" + files.getName() + "/" + subDirFiles.getName() + "/" + subSubDir;
                                        parsers.add(new Parser(path));
                                        parserPaths.add(path);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        parseFiles();
    }

    void parseFiles()
    {
        while (!parsers.isEmpty())
        {
            try
            {
                codeWriter.setFileName(parserPaths.get(0));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            try
            {
                if(!wroteInit && parserPaths.get(0).contains("FunctionCalls") && !parserPaths.get(0).contains("SimpleFunction"))
                {
                    codeWriter.writeInit();
                    wroteInit = true;
                }
                parsers.get(0).readFile();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            parsers.remove(0);
            parserPaths.remove(0);
            codeWriter.outputFileStream.close();
        }
    }

}
