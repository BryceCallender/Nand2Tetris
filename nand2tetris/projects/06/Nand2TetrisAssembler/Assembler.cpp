//
// Created by Bryce Callender on 2019-03-13.
//

#include "Assembler.h"

int Assembler::variablePlacement = 16;

Assembler::Assembler(std::string &fileName)
{
    unsigned long findDotASM = fileName.find(".asm");
    std::string asmFileName = fileName.substr(0,findDotASM);
    fileParser = new Parser(asmFileName);
}

Assembler::~Assembler()
{
    delete fileParser;
}

