//
// Created by Bryce Callender on 2019-03-14.
//

#include "Parser.h"
#include "Assembler.h"

Parser::Parser(std::string &fileName)
{
    symbolTable.insert(std::make_pair("SP",0));
    symbolTable.insert(std::make_pair("LCL",1));
    symbolTable.insert(std::make_pair("ARG",2));
    symbolTable.insert(std::make_pair("THIS",3));
    symbolTable.insert(std::make_pair("THAT",4));
    for(int i = 0; i < 16; i++)
    {
        symbolTable.insert(std::make_pair("R" + std::to_string(i),i));
    }
    symbolTable.insert(std::make_pair("SCREEN",16384));
    symbolTable.insert(std::make_pair("KBD",24576));

    initDest();
    initComp();
    initJump();

    if(fileName.find('L') != std::string::npos)
    {
        fileName.erase(fileName.find('L'));
        symbolLess = true;
    }

    //lowercase for finding the directory
    fileName[0] = (char)std::tolower(fileName[0]);
    for (auto & p : std::filesystem::directory_iterator(path))
    {
        if(p.path().generic_string().find(fileName) != std::string::npos)
        {
            currentDirectory = p.path().generic_string();
        }
    }

    //the asm files start with a capital so we need to uppercase in order to find it to read
    fileName[0] = (char)std::toupper(fileName[0]);

    std::string inputFileName = currentDirectory + "/" + fileName;
    std::string outputFileName = currentDirectory + "/" + fileName;

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
    inputFileStream.open(inputFileName,std::ios::in); //open up the file to read
    outputFileStream.open(outputFileName,std::ios::out);//make the hack file to write to
    firstPass();
}

Parser::~Parser()
{
    inputFileStream.close();
    outputFileStream.close();
}

//First pass is calculating the symbols that need to be defined for this program to work
void Parser::firstPass()
{
    std::string line;
    while(!inputFileStream.eof())
    {
        getline(inputFileStream,line);
        line.erase(remove_if(line.begin(), line.end(), isspace), line.end()); //get rid of whitespace
        if(line.length() >= 2) //check to have at least 2 characters to do these operations
        {
            if(line[0] == '(' && line[line.length()-1] == ')') //pseudo-command
            {
                //put the pseudo-command into the symbol table and dont push the line into the vector
                //it'll have the rom address of size (vector size will be 1 when something added and
                //we want to start at 0 so essentially when you load the label into rom just take the
                //current size of the codeLines vector)
                symbolTable.insert(std::make_pair(line.substr(1,line.length() - 2),codeLines.size()));
            }
            else if(line[0] != '/' && line[1] != '/') //only come in if its not a comment
            {
                if(line.find("//") != std::string::npos)
                {
                    line.erase(line.find("//"),line.length()); //get rid of the comment at end of line
                    codeLines.push_back(line); //put the code line into the vector
                }
                else
                {
                    codeLines.push_back(line);
                }
            }
        }
    }

//    for(const std::string& codeLine: codeLines)
//    {
//        std::cout << codeLine << std::endl;
//    }
//
//    std::cout << "\nSymbol Table:\n";
//
//    for(const auto &pair: symbolTable)
//    {
//        std::cout << pair.first << " " << pair.second << std::endl;
//    }

    secondPass();
}

void Parser::secondPass()
{
    std::string line;
    while(!codeLines.empty())
    {
        line = codeLines.front();

        if(line[0] == '@')
        {
            if(!checkForNumbersOnly(line) && symbolTable.find(line.substr(1,line.length())) == symbolTable.end())
            {
                symbolTable.insert(std::make_pair(line.substr(1,line.length()),Assembler::variablePlacement));
                Assembler::variablePlacement++;
            }
            outputFileStream << decodeAInstruction(line) << "\n";
        }
        else
        {
            outputFileStream << decodeCInstruction(line) << "\n";
        }
        codeLines.erase(codeLines.begin());
    }
}

std::string Parser::decodeAInstruction(std::string &line)
{
    std::string binary = "0";

    int number = 0;

    if(symbolTable.find(line.substr(1,line.length())) != symbolTable.end())
    {
        number = symbolTable[line.substr(1,line.length())];
    }
    else
    {
        number = std::stoi(line.substr(1,line.length())); // after @
    }

    std::bitset<15> bitRepresentation((unsigned int)number);

    binary += bitRepresentation.to_string();

    //std::cout << "Binary for " << line << ": " << binary << std::endl;

    return binary;
}

std::string Parser::decodeCInstruction(std::string &line)
{
    std::string binary = "111";

    //dest = comp;jump if dest is empty the "=" is emitted
    //if jump is empty then the ";" is emitted
    std::string dest = "null";
    std::string comp = "000000";
    std::string jump = "null";

    if(line.find('=') != std::string::npos)
    {
        dest = line.substr(0, line.find('='));

        if(line.find(';') != std::string::npos)
        {
            comp = line.substr(line.find('=')+1,line.find(';'));
            jump = line.substr(line.find(';'),line.length());
        }
        else
        {
            comp = line.substr(line.find('=')+1,line.length());
        }
    }
    else //dest is empty
    {
        if(line.find(';') != std::string::npos)
        {
            comp = line.substr(0,line.find(';'));
            jump = line.substr(line.find(';')+1,line.length());
        }
    }

    return binary + getComp(comp) + getDest(dest) + getJump(jump);
}

std::string Parser::getDest(std::string &dest)
{
    return destBinary[dest];
}

std::string Parser::getComp(std::string &comp)
{
    return compBinary[comp];
}

std::string Parser::getJump(std::string &jump)
{
    return jumpBinary[jump];
}

void Parser::initDest()
{
    std::string variables[] = {"null","M","D","MD","A","AM","AD","AMD"};
    std::string bitString;
    for(unsigned int i = 0; i < 8; i++)
    {
        std::bitset<3> bits(i);
        bitString = bits.to_string();
        destBinary.insert(std::make_pair(variables[i],bitString));
    }
}

void Parser::initComp()
{
    compBinary.insert(std::make_pair("0","0101010"));
    compBinary.insert(std::make_pair("1","0111111"));
    compBinary.insert(std::make_pair("-1","0111010"));
    compBinary.insert(std::make_pair("D","0001100"));
    compBinary.insert(std::make_pair("A","0110000"));
    compBinary.insert(std::make_pair("M","1110000"));
    compBinary.insert(std::make_pair("!D","0001101"));
    compBinary.insert(std::make_pair("!A","0110001"));
    compBinary.insert(std::make_pair("!M","1110001"));
    compBinary.insert(std::make_pair("-D","0001111"));
    compBinary.insert(std::make_pair("-A","0110011"));
    compBinary.insert(std::make_pair("-M","1110011"));
    compBinary.insert(std::make_pair("D+1","0011111"));
    compBinary.insert(std::make_pair("A+1","0110111"));
    compBinary.insert(std::make_pair("M+1","1110111"));
    compBinary.insert(std::make_pair("D-1","0001110"));
    compBinary.insert(std::make_pair("A-1","0110010"));
    compBinary.insert(std::make_pair("M-1","1110010"));
    compBinary.insert(std::make_pair("D+A","0000010"));
    compBinary.insert(std::make_pair("D+M","1000010"));
    compBinary.insert(std::make_pair("D-A","0010011"));
    compBinary.insert(std::make_pair("D-M","1010011"));
    compBinary.insert(std::make_pair("A-D","0000111"));
    compBinary.insert(std::make_pair("M-D","1000111"));
    compBinary.insert(std::make_pair("D&A","0000000"));
    compBinary.insert(std::make_pair("D&M","1000000"));
    compBinary.insert(std::make_pair("D|A","0010101"));
    compBinary.insert(std::make_pair("D|M","1010101"));

}

void Parser::initJump()
{
    std::string variables[] = {"null","JGT","JEQ","JGE","JLT","JNE","JLE","JMP"};
    std::string bitString;
    for(unsigned int i = 0; i < 8; i++)
    {
        std::bitset<3> bits(i);
        bitString = bits.to_string();
        jumpBinary.insert(std::make_pair(variables[i],bitString));
    }
}

bool Parser::checkForNumbersOnly(std::string &line)
{
    if(line[0] != '@')
    {
        return false;
    }
    std::string updatedLine = line.substr(1,line.length()); //gets rid of the @

    for (char i : updatedLine)
    {
        if(i < '0' || i > '9')
        {
            return false;
        }
    }
    return true;
}
