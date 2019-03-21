//
// Created by Bryce Callender on 2019-03-13.
//

#ifndef NAND2TETRISASSEMBLER_ASSEMBLER_H
#define NAND2TETRISASSEMBLER_ASSEMBLER_H

#include <string>
#include <vector>
#include "Parser.h"

class Assembler
{
public:
    Assembler(std::string &fileName); // filename will have it in the name NAME.asm
    ~Assembler();
    static int variablePlacement;
private:
    Parser *fileParser;
};


#endif //NAND2TETRISASSEMBLER_ASSEMBLER_H
