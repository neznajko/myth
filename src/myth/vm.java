////////////////////////////////////////////////////////////////////////
package myth;
////////////////////////////////////////////////////////////////////////
import static java.lang.System.out;
////////////////////////////////////////////////////////////////////////
import java.util.ArrayList;
////////////////////////////////////////////////////////////////////////
class Word {
    static final int BYTES   = 5;
    static final int BYTESIZ = 6; // nof bits
    static final int INTSIZ  = 32;
    static final long WORD_MASK = 0x000000003fffffffL; // fst 30 bits
    static final long OVERFLOW_MASK = ~WORD_MASK;
    // L:R = F = 8L + R
    static int L( int fld ){
        return fld / 8;
    }
    static int R( int fld ){
        return fld % 8;
    }
    static int F( int left, int ryte ){
        return 8*left + ryte;
    }
    // Instruction fields
    static final int INSTR_ADR = F( 0, 2 );
    static final int INSTR_IDX = F( 3, 3 );
    static final int INSTR_FLD = F( 4, 4 );
    static final int INSTR_OPC = F( 5, 5 ); // opcode
    //
    // fedcba9876543210fedcba9876543210
    // --______``````______``````______
    //   1     2     3     4     5
    static int getoff( int byteNumber ){
        return BYTESIZ*( BYTES - byteNumber );
    }
    static long getmask( int left, int ryte ){
        int loff = getoff( left - 1 );
        int roff = getoff( ryte );
        return ~(-1L << loff) & (-1L << roff);
    }
    // modified code from GfG site
    static String leftPadding( String input, char ch, int L ){  
        return String
            // First left pad the string
            // with space up to length L
            .format( "%" + L + "s", input )
            // Then replace all the spaces
            // with the given character ch
            .replace( ' ', ch );
    }
    // 00 111111 222222 333333 444444 555555
    static String binary( int y ){
        String s =  Integer.toBinaryString( y );
        s = leftPadding( s, '0', INTSIZ );
        int j = 2;
        var b = new StringBuilder( s.substring( 0, j ));
        for(; j < INTSIZ; j += BYTESIZ ){
            b.append( " " );
            b.append( s.substring( j, j + BYTESIZ ));
        }
        return b.toString();
    }
    ////////////////////////////////////////////////////////////
    boolean sign; // allowing for -0
    long    bufr; // 64-bit, handles overflows, AX extended register
    Word( boolean sign, long bufr ){
        this.sign = sign; // false is +
        this.bufr = bufr; // this should be positive
    }
    void setword( long value )
    {
        sign = value < 0;
        bufr = Math.abs( value );
    }
    Word( long value ){
        setword( value );
    }
    Word(){
        this( 0 );
    }
    void copyOf( Word w ){
        sign = w.sign;
        bufr = w.bufr;
    }
    ////////////////////////////////////////////////////////////    
    int getfld( int left, int ryte ){
        long mask = getmask( left, ryte );
        int val = (int)(( bufr & mask ) >> getoff( ryte ));
        if( left == 0 && sign ) val = -val;
        return val;
    }
    int getfld( int fld ){
        return getfld( L( fld ), R( fld ));
    }
    @Override
    public String toString() {
        var b = new StringBuilder();
        if( sign ){
            b.append( "- " );
        } else {
            b.append( "+ " );
        }
        for( int j = 1; j <= BYTES; j++ ){
            b.append( String.format( "%2d ", getfld( j, j )));
        }
        b.append( "[" + bufr + "]" );
        return b.toString();
    }
    void setvalue( int left, int ryte, int value ){
        if( left == 0 ){
            sign = value < 0;
            if( sign ) value = -value;
        }
        long mask = getmask( left, ryte );
        // clear
        this.bufr &= ~mask;
        // set
        int off = getoff( ryte );
        // casting to long allows to use the negative bytes,
        // for the SLC command etc.
        long shifted = ( long )value << getoff( ryte );
        this.bufr |= shifted;
    }
    void setvalue( int fld, int value ){
        setvalue( L( fld ), R( fld ), value );
    }
    Word getWord( int left, int ryte ){
        boolean sign = false; // +
        if( left == 0 ){
            sign = this.sign;
            left = 1;
        }
        return new Word( sign, getfld( left, ryte ));
    }
    Word getWord( int fld ){
        return getWord( L( fld ), R( fld ));
    }
    long getval() {
        return sign ? -bufr : bufr;
    }
    void shiftleft( int n ){
        n *= BYTESIZ;
        bufr <<= n;
    }
    void shiftryte( int n ){
        n *= BYTESIZ;
        bufr >>= n;
    }
    void div( long d ){
        bufr /= d;
    }
    static boolean overflow( long value ){
        // f - nibble
        // ff - byte
        // ffff - short
        // ffff ffff - int
        // ffff ffff ffff ffff - long
        // 0000 0000 3fff ffff - maximum value ( 30 bits )
        // ffff ffff c000 0000 - overflow mask
        return ( value & OVERFLOW_MASK ) > 0;
    }
    void inc( final Word w ) throws Exception {
        long res = getval() + w.getval();
        if( overflow( res )){
            throw new Exception( "Overflow" );
        }
        setword( res );
    }
    void mul( final Word w ) throws Exception {
        long res = getval() * w.getval();
        // Here we have to check if there is an overflow over the 60th
        // bit.
        if(( res & 0xc000000000000000L ) > 0 ){
            throw new Exception( "Overflow" );
        }
        setword( res );
    }
    // Return the reminder, and replace with the quotent.
    long div( final Word w ) throws Exception {
        // prologue,( provide the actors )
        long x = getval();   // divident
        long y = w.getval(); // divisor
        // action
        long q = Math.floorDiv( x, y ); // quotent
        if( overflow( q )){
            throw new Exception( "Overflow" );
        }
        long r = Math.floorMod( x, y ); // remainder
        // epilogue
        setword( q );
        return r;
    }
    ////////////////////////////////////////////////////////////
    boolean isNull() {
        return ( bufr & WORD_MASK ) == 0;
    }
    ////////////////////////////////////////////////////////////////
    //                    2                   1
    //  9 8 7 6 5 4 3 2 1 0 9 8 7 6 5 4 3 2 1 0 9 8 7 6 5 4 3 2 1 0
    // , , , , , , , , , , , , , , , , , , , , , , , , , , , , , , ,
    // :           :           :           :           :           :
    //       1           2           3           4           5
//  111111 000000 000000 000000 000000 000000 000000 000000 000000 000000
//  -4     -3     -2     -1     0      1      2      3      4      5
//     
    void reverse( int left, int ryte ){
        // bcoz of the getfld make temporary the word positive
        boolean backup = sign;
        sign = false;
        for(; left < ryte; left++, ryte-- ){
            int left_value = getfld( left, left );
            int ryte_value = getfld( ryte, ryte );
            setvalue( left, left, ryte_value );
            setvalue( ryte, ryte, left_value );
        }
        // re-establish
        sign = backup;
    }
    void cycle( int n ){
        n %= 2*BYTES;
        if( n == 0 ) return;
        n -= 5;
        reverse( -4, 5 );
        reverse( -4, n );
        reverse( n + 1, 5 );
    }
    String toBinaryString() {
        String s = Long.toBinaryString( bufr );
        s = new StringBuilder( s ).reverse().toString();
        s = s.replaceAll( "(.{6})", "$1 " );
        s = new StringBuilder( s ).reverse().toString();
        return s;
    }
    ///////_////////////////////////////////////////////////////
    public static void main( String[] args ){
        out.println( "Word" );
        Word w = new Word( -12345 );
        Word v = new Word( 7891234 );
        out.println( w );
        out.println( v );
        v.copyOf( w );
        out.println( v );
    }                     
}
////////////////////////////////////////////////////////////////
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
