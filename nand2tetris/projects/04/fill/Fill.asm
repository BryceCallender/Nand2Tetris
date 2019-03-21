// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input.
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel;
// the screen should remain fully black as long as the key is pressed. 
// When no key is pressed, the program clears the screen, i.e. writes
// "white" in every pixel;
// the screen should remain fully clear as long as no key is pressed.

// Put your code here.
@SCREEN
D = A
@COUNTER
M = 0

(KEYBOARDINPUT) //Check for keyboard input in case we let go or started touching a key
    @8192 //side effect of this program to get constants is you need to take address number
    D = A
    @pixelCounter
    M = D
    @COUNTER
    M = 0

    @KBD
    D = M
    @WHITE
    D;JEQ //If keyboard input is zero
    @BLACK
    0;JMP //If keyboard is not zero(touching a key)

(WHITE) //set all the pixels white
    @SCREEN //Assume we were in black loop and switched to this so we want to start from the beginning
    D = A

//Set all pixels to white in a range of 16 pixels since its 16 bits bit wide registers
//then we have to move 16 bits in the screen space to write another 16 bits and on. 
(WHITELOOP)
    @COUNTER
    D = M
    @SCREEN //set pixel to white
    A = D + A
    M = 0
    @COUNTER
    M = M + 1
    @pixelCounter //how many pixels we need to write to
    M = M - 1
    D = M
    @KEYBOARDINPUT
    D;JEQ //If we are out of pixels to write to
    @KBD
    D = M
    @WHITELOOP
    D;JEQ //If keyboard input is zero
    @KEYBOARDINPUT
    0;JMP //If keyboard is not zero(touching a key)

(BLACK) //set all the pixels black
    @SCREEN //Assume we were in white loop and switched to this so we want to start from the beginning
    D = A
    @ScreenPos
    M = D

(BLACKLOOP)
    @COUNTER
    D = M
    @SCREEN //set pixel to black
    A = D + A
    M = -1
    @COUNTER
    M = M + 1
    @pixelCounter //how many pixels we need to write to
    M = M - 1
    D = M
    @KEYBOARDINPUT
    D;JEQ //If we are out of pixels to write to
    @KBD
    D = M
    @BLACKLOOP
    D;JNE //If keyboard input is zero
    @KEYBOARDINPUT
    0;JMP //If keyboard is not zero(touching a key)