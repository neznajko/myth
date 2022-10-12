# myth
The mythical *mix* machine.

Ok, this is a simple project writing my first Compiler.
It consist of two parts, first is to build machine
instructions from source code, and the second is to
actually execute those instructions.

For now I'm kind of a ready with the first part, but it's
very buggy, but I have to start from somewhere:)

At this point I'm almost ready with the second part
as well. What remains is to write the GO button and
run the machine, actually what I'm suspecting is that,
I'll have to fix a lot of bugs. Then I'm going to start
writing the GUI and the Interpreter.

Ok it's kind of working here is an example program( src.mixal):

```mixal
HAHA    EQU     2
        ORIG    10
        CON     6
STAAT   JMP     1F
BOOM    NOP
1H      SUB     BOOM-HAHA
2H      ADD     =10= * o_o
        END     STAAT
* log:
```

This is java code to compile and run the program:

```java
////////////////////////////////////////////////////////////////    
    public static void main( String[] args ) throws Exception {
        Parser parser = new Parser();
        try {
            parser.compile( "src.mixal" );
            parser.vm.dumpMemory( 8, 18 );
            parser.vm.go();
            out.println( "rA: " + parser.vm.rA );
        } catch( Exception e ){
            out.println( e );
        }
    }
```

This is how it looks in memory, and the result: *4* in the ***rA*** register.

```bash
$ java -cp bin/ myth.Parser
0008 +  0  0  0  0  0 [0]
0009 +  0  0  0  0  0 [0]
0010 +  0  0  0  0  6 [6]
0011 +  0 13  0  0 39 [3407911]
0012 +  0  0  0  0  0 [0]
0013 +  0 10  0  5  2 [2621762]
0014 +  0 15  0  5  1 [3932481]
0015 +  0  0  0  0 10 [10]
0016 +  0  0  0  0  0 [0]
0017 +  0  0  0  0  0 [0]

rA: +  0  0  0  0  4 [4]
```
