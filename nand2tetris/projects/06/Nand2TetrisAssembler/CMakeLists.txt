cmake_minimum_required(VERSION 3.13)
project(Nand2TetrisAssembler)

set(CMAKE_CXX_STANDARD 17)

link_libraries(stdc++fs)

add_executable(Nand2TetrisAssembler main.cpp Assembler.cpp Assembler.h Parser.cpp Parser.h)