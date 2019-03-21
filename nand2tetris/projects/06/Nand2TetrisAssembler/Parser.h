//
// Created by Bryce Callender on 2019-03-14.
//

#ifndef NAND2TETRISASSEMBLER_PARSER_H
#define NAND2TETRISASSEMBLER_PARSER_H

#include <string>
#include <unordered_map>
#include <iostream>
#include <fstream>
#include <filesystem>
#include <algorithm>
#include <bitset>

class Parser
{
public:
    Parser(std::string &fileName);
    ~Parser();
    void firstPass();
    void secondPass();
    std::string decodeAInstruction(std::string &line);
    std::string decodeCInstruction(std::string &line);
    std::string getDest(std::string &dest);
    std::string getComp(std::string &comp);
    std::string getJump(std::string &jump);
    bool checkForNumbersOnly(std::string &line);

    void initDest();
    void initComp();
    void initJump();

private:
    std::unordered_map<std::string,int> symbolTable; //(xxx,n) symbol and address in memory
    std::unordered_map<std::string,std::string> compBinary;
    std::unordered_map<std::string,std::string> destBinary;
    std::unordered_map<std::string,std::string> jumpBinary;

    bool symbolLess = false;
    std::vector<std::string> codeLines;
    std::ifstream inputFileStream;
    std::ofstream outputFileStream;
    const std::string path = "/Users/brycecallender/Desktop/nand2tetris/projects/06";
    std::string currentDirectory;
};


#endif //NAND2TETRISASSEMBLER_PARSER_H
