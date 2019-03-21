// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Mult.asm

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)

// Put your code here.
@R2
M = 0

//If left side is zero end
@R0
D = M
@END
D; JEQ

//If right side is zero end
@R1
D = M
@END
D; JEQ

(ADDLOOP)
    @R1 //the successive adder
    D = M
    @R2 //product
    M = D + M

    @R0 //the counter
    M = M - 1
    D = M
    @END //end loop
    D;JEQ
    @ADDLOOP //keep adding
    0;JMP

(END)
    @END
    0;JMP