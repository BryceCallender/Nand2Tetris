import java.io.File;

public class JackAnalyzer
{
    private String path = "/Users/brycecallender/desktop/Nand2Tetris/nand2tetris/projects/10";

    JackAnalyzer(String fileName)
    {
        File file = new File(path);
        File[] nand2tetrisFiles = file.listFiles((dir, name) -> !name.equals(".DS_Store"));
        StringBuilder fullPathToFile = new StringBuilder(path);

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
                        if(subDirFiles.getName().contains(".jack"))
                        {
                            String path = fullPathToFile + "/" + subDirFiles.getName();
                            System.out.println(path);
                            File input = new File(path);

                            String outputName = subDirFiles.getName().substring(0,subDirFiles.getName().lastIndexOf(".")) + "mine.xml";
                            path = path.substring(0,path.lastIndexOf("/")+1) + outputName;

                            System.out.println(path);
                            File output = new File(path);
                            CompilationEngine compilationEngine = new CompilationEngine(input,output);
                        }

                    }
                }
                //Look only for 1 file
                else
                {
                    for(File subDirFiles: files.listFiles())
                    {
                        if(subDirFiles.getName().contains(".jack"))
                        {
                            path = fullPathToFile + "/" + subDirFiles.getName();

                            System.out.println(path);
                            File input = new File(path);

                            String outputName = subDirFiles.getName().substring(0,subDirFiles.getName().lastIndexOf(".")) + "mine.xml";
                            path = path.substring(0,path.lastIndexOf("/")+1) + outputName;

                            System.out.println(path);
                            File output = new File(path);
                            CompilationEngine compilationEngine = new CompilationEngine(input,output);
                        }
                    }
                }
            }
        }
    }
}
