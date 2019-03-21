#include <iostream>
#include "Assembler.h"

int main(int argc, char** argv)
{
    std::string name(argv[1]);
    Assembler assembler(name); // the name of the file
    return 0;
}