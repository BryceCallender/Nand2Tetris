@256
D=A
@SP
M=D
//call Sys.init 0
@Sys.init$ret.0
D=A
@SP
AM=M+1
A=A-1
M=D
@LCL
D=M
@SP
AM=M+1
A=A-1
M=D
@ARG
D=M
@SP
AM=M+1
A=A-1
M=D
@THIS
D=M
@SP
AM=M+1
A=A-1
M=D
@THAT
D=M
@SP
AM=M+1
A=A-1
M=D
@SP
D=M
@R13
M=D
@0
D=A
@R13
M=M-D
@5
D=A
@R13
MD=M-D
@ARG
M=D
@SP
D=M
@LCL
M=D
//goto Sys.init
@Sys.init
0;JMP
//label Sys.init$ret.0
(Sys.init$ret.0)
//function Sys.init 0
(Sys.init)
//push constant 4000
@4000
D=A
@SP
AM=M+1
A=A-1
M=D
//pop pointer 0
@3
M=D
@SP
A=M
@SP
AM=M-1
//push constant 5000
@5000
D=A
@SP
AM=M+1
A=A-1
M=D
//pop pointer 1
@4
M=D
@SP
A=M
@SP
AM=M-1
//call Sys.main 0
@Sys.main$ret.1
D=A
@SP
AM=M+1
A=A-1
M=D
@LCL
D=M
@SP
AM=M+1
A=A-1
M=D
@ARG
D=M
@SP
AM=M+1
A=A-1
M=D
@THIS
D=M
@SP
AM=M+1
A=A-1
M=D
@THAT
D=M
@SP
AM=M+1
A=A-1
M=D
@SP
D=M
@R13
M=D
@0
D=A
@R13
M=M-D
@5
D=A
@R13
MD=M-D
@ARG
M=D
@SP
D=M
@LCL
M=D
//goto Sys.main
@Sys.main
0;JMP
//label Sys.main$ret.1
(Sys.main$ret.1)
//pop temp 1
@5
D=A
@1
D=D+A
@R13
M=D
@SP
AM=M-1
@SP
A=M
D=M
@R13
A=M
M=D
@SP
AM=M+1
A=A-1
@SP
AM=M-1
//label LOOP
(LOOP)
//goto LOOP
@LOOP
0;JMP
//function Sys.main 5
(Sys.main)
@SP
A=M
M=0
@SP
AM=M+1
A=A-1
@SP
A=M
M=0
@SP
AM=M+1
A=A-1
@SP
A=M
M=0
@SP
AM=M+1
A=A-1
@SP
A=M
M=0
@SP
AM=M+1
A=A-1
@SP
A=M
M=0
@SP
AM=M+1
A=A-1
//push constant 4001
@4001
D=A
@SP
AM=M+1
A=A-1
M=D
//pop pointer 0
@3
M=D
@SP
A=M
@SP
AM=M-1
//push constant 5001
@5001
D=A
@SP
AM=M+1
A=A-1
M=D
//pop pointer 1
@4
M=D
@SP
A=M
@SP
AM=M-1
//push constant 200
@200
D=A
@SP
AM=M+1
A=A-1
M=D
//pop local 1
@LCL
D=M
@1
D=D+A
@R13
M=D
@SP
AM=M-1
@SP
A=M
D=M
@R13
A=M
M=D
@SP
AM=M+1
A=A-1
@SP
AM=M-1
//push constant 40
@40
D=A
@SP
AM=M+1
A=A-1
M=D
//pop local 2
@LCL
D=M
@2
D=D+A
@R13
M=D
@SP
AM=M-1
@SP
A=M
D=M
@R13
A=M
M=D
@SP
AM=M+1
A=A-1
@SP
AM=M-1
//push constant 6
@6
D=A
@SP
AM=M+1
A=A-1
M=D
//pop local 3
@LCL
D=M
@3
D=D+A
@R13
M=D
@SP
AM=M-1
@SP
A=M
D=M
@R13
A=M
M=D
@SP
AM=M+1
A=A-1
@SP
AM=M-1
//push constant 123
@123
D=A
@SP
AM=M+1
A=A-1
M=D
//call Sys.add12 1
@Sys.add12$ret.2
D=A
@SP
AM=M+1
A=A-1
M=D
@LCL
D=M
@SP
AM=M+1
A=A-1
M=D
@ARG
D=M
@SP
AM=M+1
A=A-1
M=D
@THIS
D=M
@SP
AM=M+1
A=A-1
M=D
@THAT
D=M
@SP
AM=M+1
A=A-1
M=D
@SP
D=M
@R13
M=D
@1
D=A
@R13
M=M-D
@5
D=A
@R13
MD=M-D
@ARG
M=D
@SP
D=M
@LCL
M=D
//goto Sys.add12
@Sys.add12
0;JMP
//label Sys.add12$ret.2
(Sys.add12$ret.2)
//pop temp 0
@5
D=A
@0
D=D+A
@R13
M=D
@SP
AM=M-1
@SP
A=M
D=M
@R13
A=M
M=D
@SP
AM=M+1
A=A-1
@SP
AM=M-1
//push local 0
@LCL
D=M
@0
D=D+A
@R13
M=D
D=M
@R13
A=M
D=M
@SP
AM=M+1
A=A-1
M=D
//push local 1
@LCL
D=M
@1
D=D+A
@R13
M=D
D=M
@R13
A=M
D=M
@SP
AM=M+1
A=A-1
M=D
//push local 2
@LCL
D=M
@2
D=D+A
@R13
M=D
D=M
@R13
A=M
D=M
@SP
AM=M+1
A=A-1
M=D
//push local 3
@LCL
D=M
@3
D=D+A
@R13
M=D
D=M
@R13
A=M
D=M
@SP
AM=M+1
A=A-1
M=D
//push local 4
@LCL
D=M
@4
D=D+A
@R13
M=D
D=M
@R13
A=M
D=M
@SP
AM=M+1
A=A-1
M=D
//add
@SP
AM=M-1
D=M
@SP
AM=M-1
D=D+M
M=D
@SP
AM=M+1
A=A-1
//add
@SP
AM=M-1
D=M
@SP
AM=M-1
D=D+M
M=D
@SP
AM=M+1
A=A-1
//add
@SP
AM=M-1
D=M
@SP
AM=M-1
D=D+M
M=D
@SP
AM=M+1
A=A-1
//add
@SP
AM=M-1
D=M
@SP
AM=M-1
D=D+M
M=D
@SP
AM=M+1
A=A-1
//return
@SP
AM=M-1
@SP
A=M
D=M
@R13
M=D
@SP
AM=M+1
A=A-1
@LCL
D=M
@R14
M=D
@5
D=A
@R14
A=M-D
D=M
@R15
M=D
@SP
AM=M-1
@SP
D=M
@ARG
A=M
M=D
@R13
D=M
@ARG
A=M
M=D
@ARG
D=M+1
@SP
M=D
@R14
A=M-1
D=M
@THAT
M=D
@2
D=A
@R14
A=M-D
D=M
@THIS
M=D
@3
D=A
@R14
A=M-D
D=M
@ARG
M=D
@4
D=A
@R14
A=M-D
D=M
@LCL
M=D
@R15
A=M
0;JMP
//function Sys.add12 0
(Sys.add12)
//push constant 4002
@4002
D=A
@SP
AM=M+1
A=A-1
M=D
//pop pointer 0
@3
M=D
@SP
A=M
@SP
AM=M-1
//push constant 5002
@5002
D=A
@SP
AM=M+1
A=A-1
M=D
//pop pointer 1
@4
M=D
@SP
A=M
@SP
AM=M-1
//push argument 0
@ARG
D=M
@0
D=D+A
@R13
M=D
D=M
@R13
A=M
D=M
@SP
AM=M+1
A=A-1
M=D
//push constant 12
@12
D=A
@SP
AM=M+1
A=A-1
M=D
//add
@SP
AM=M-1
D=M
@SP
AM=M-1
D=D+M
M=D
@SP
AM=M+1
A=A-1
//return
@SP
AM=M-1
@SP
A=M
D=M
@R13
M=D
@SP
AM=M+1
A=A-1
@LCL
D=M
@R14
M=D
@5
D=A
@R14
A=M-D
D=M
@R15
M=D
@SP
AM=M-1
@SP
D=M
@ARG
A=M
M=D
@R13
D=M
@ARG
A=M
M=D
@ARG
D=M+1
@SP
M=D
@R14
A=M-1
D=M
@THAT
M=D
@2
D=A
@R14
A=M-D
D=M
@THIS
M=D
@3
D=A
@R14
A=M-D
D=M
@ARG
M=D
@4
D=A
@R14
A=M-D
D=M
@LCL
M=D
@R15
A=M
0;JMP
