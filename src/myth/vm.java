////////////////////////////////////////////////////////////////////////
package myth;
////////////////////////////////////////////////////////////////////////
import static java.lang.System.out;
////////////////////////////////////////////////////////////////////////
import java.util.ArrayList;
////////////////////////////////////////////////////////////////
// Ok, now we have a memory with instructions,( and data ) first
// we want to decode the instruction
class InsWord { // Instruction Word
    int adr;
    int idx;
    int fld;
    int cde;
    InsWord( final Word w ){
        adr = w.getfld( 0, 2 );
        idx = w.getfld( 3, 3 );
        fld = w.getfld( 4, 4 );
        cde = w.getfld( 5, 5 );
    }
    @Override
    public String toString() {
        return( "(" +
                adr + "," +
                idx + "," +
                fld + "," +
                cde + ")" );
    }
}
////////////////////////////////////////////////////////////////
// +-----+-----+-----+-----+-----+-----+
// |        A        |  i  |  f  |  C  |
// |                 |     |     |     |
// +-----+-----+-----+-----+-----+-----+
//    0     1     2     3     4     5
// 
// +---+---+
// | A | X |
// +---+---+
//
////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////
class VM { // Virtual Machine
    static final int MEMSIZ  = 100; // number of words
    static final int NIDX    = 6;   // nof index registers
    static final int LESS    = -1;
    static final int EQUAL   = 0;
    static final int GREATER = 1;    
    static final int NOOFDEV = 21; // number of devices
    Word memory[] = new Word[ MEMSIZ ]; // per sé( wtf? )
    int start = 0; // these should be set by the Parser
    int end   = 5; // vhere to insert literals
    Word rA;
    Word rX;
    Word rI[] = new Word[ NIDX ];
    Word rJ;
    boolean overflowToggle;
    int comparizonIndykate;
    int pc; // the program counter
    Operator op;
    Device dev[] = new Device[ NOOFDEV ];
    VM() { // Constructor
        for( int j = 0; j < MEMSIZ; j++ ){
            memory[j] = new Word( false, 0 );
        }
        rA = new Word( false, 0 );
        rX = new Word( false, 0 );
        for( int j = 0; j < NIDX; j++ ){
            rI[j] = new Word( false, 0 );
        }
        rJ = new Word( false, 0 );
        overflowToggle = false;
        comparizonIndykate = EQUAL;
        pc = 0;
        op = new Operator( this );
        dev[18] = new Device( 18, "printer", 24, this );
        dev[18].set_controller( new Clear());
    }
    ////////////////////////////////////////////////////////////
    // Dump memory in the address interval [ lo, hi ), for dumping
    // all memory use [ 0, MEMSIZ ) range.
    void dumpMemory( int lo, int hi ){
        var b = new StringBuilder();
        for( int j = lo; j < hi; j++) {
            b.append( String.format( "%04d ", j ));
            b.append( memory[j].toString());
            b.append( "\n" );
        }
        out.println( b.toString());
    }
    void cpu() { // thats the cycle
        while( pc != end ){
            Word w = memory[ pc ];
            op.exec( w );
            ++pc;
        }
    }
    void go() {
        pc = start;
        cpu();
    }
    public static void main( String[] args ){
        VM vm = new VM();
        var word = vm.memory[5];
        word.setvalue( Word.F( 1, 4 ), 15 );
        vm.dumpMemory(0, 10);
        var insWord = new InsWord( word );
        out.println( insWord );
    }
}
////////////////////////////////////////////////////////////////
// log: - create new branch dev
//      - make Word.java file and start cleaning the code
//
