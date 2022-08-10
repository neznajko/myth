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
    boolean sign; // allowing for -0
    long    bufr; // 64-bit, handles overflows, AX extended register
    Word( boolean sign, long bufr ){ // no default constructor
        this.sign = sign; // false is +
        this.bufr = bufr; // this should be positive
    }
    Word( int value ){
        sign = value < 0;
        if( sign ) value = -value;
        bufr = value;
    }
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
    int getfld( int left, int ryte ){
        long mask = getmask( left, ryte );
        int val = (int)(( bufr & mask ) >> getoff( ryte ));
        if( left == 0 && sign ) val = -val;
        return val;
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
    void setvalue( int fld, int value ){
        int left = L( fld );
        int ryte = R( fld );
        if( left == 0 ){
            sign = value < 0;
            if( sign ) value = -value;
        }
        long mask = getmask( left, ryte );
        // clear
        this.bufr &= ~mask;
        // set
        this.bufr |= value << getoff( ryte );
    }
    Word getWord( int fld ){
        int left = L( fld );
        int ryte = R( fld );
        boolean sign = false; // +
        if( left == 0 ){
            sign = this.sign;
            left = 1;
        }
        return new Word( sign, getfld( left, ryte ));
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
    long getval() {
        long val = bufr;
        if( sign ) val = -val;
        return val;
    }
    static boolean overflow( long value ){
        // f - nibble
        // ff - byte
        // ffff - short
        // ffff ffff - int
        // ffff ffff ffff ffff - long
        // 0000 0000 3fff ffff - maximum value ( 30 bits )
        // ffff ffff c000 0000 - overflow mask
        return ( value & 0xffffffffc0000000L ) > 0;
    }
    void inc( final Word w ) throws Exception {
        long res = getval() + w.getval();
        if( overflow( res )){
            throw new Exception( "Overflow" );
        }
        sign = res < 0;
        if( sign ) res = -res;
        bufr = res;
    }
    void mul( final Word w ) throws Exception {
        long res = getval() * w.getval();
        // Here we have to check if there is an overflow over the 60th
        // bit.
        if(( res & 0xc000000000000000L ) > 0 ){
            throw new Exception( "Overflow" );
        }
        sign = res < 0;
        if( sign ) res = -res;
        bufr = res;
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
        sign = q < 0;
        if( sign ) q = -q;
        bufr = q;
        return r;
    }
    ////////////////////////////////////////////////////////////////////
    // Check W-Value Component E1(F1),[E2(F2)],...,EN(FN)
    static Pair<String,String> checkWalueComp( String walueComp ){
        String E, // expression
               F; // field
        int i = walueComp.indexOf( '(' );
        if( i == -1 ){ // not found, use default field
            E = walueComp;
            F = "0:5";
        } else {
            E = walueComp.substring( 0, i );
            int j = walueComp.length() - 1;
            if( walueComp.charAt( j ) != ')' ){
                throw new Error( "Unbalanced parens:)" );
            }
            F = walueComp.substring( i + 1, j );
        }
        return new Pair<>( E, F );
    }
    // E1(F1),E2(F2),...,EN(FN)
    static ArrayList<Pair<String,String>> walueSplit( String walue ){
        var ls = new ArrayList<Pair<String,String>>();
        String walueComp; // W-Value Component
        walue += ",";     // make it simple
        int j = 0;        // position of next ','
        for( int i = 0; i < walue.length(); i = j + 1 ){
            j = walue.indexOf( ',', i );
            walueComp = walue.substring( i, j );
            ls.add( checkWalueComp( walueComp ));
        }
        return ls;
    }
    ///////_////////////////////////////////////////////////////////////
    public static void main( String[] args ){
        out.println( "Word" );
        Word w = new Word( false , 0 );
        w.setvalue( 2, -15 );
        out.println( w );
        w.shiftryte( 1 );
        out.println( w );
        Word x = new Word( false, 0 );
        x.setvalue( 45, 63 );
        out.println( x );
        try {
            long r = w.div( x );
            out.println( w );
            out.println( r );
        } catch( Exception e ){
            out.println( e );
        }
    }                     
}
////////////////////////////////////////////////////////////////////////
// Ok, now we have a memory with instructions,( and data ) first we want
// to decode the instruction
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
////////////////////////////////////////////////////////////////////////
class VM { // Virtual Machine
    static final int MEMSIZ = 100; // number of words
    static final int NIDX   = 6;   // nof index registers
    static final int LESS    = -1;
    static final int EQUAL   = 0;
    static final int GREATER = 1;    
    Word memory[] = new Word[ MEMSIZ ]; // per sé( wtf? )
    int end = 5; // vhere to insert literals
    Word rA;
    Word rX;
    Word rI[] = new Word[ NIDX ];
    Word rJ;
    boolean overflowToggle;
    int comparizonIndykate;
    Operator op;
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
        op = new Operator( this );
    }
    // Dump memory in the address interval [ lo, hi ), for dumping all
    // memory use [ 0, MEMSIZ ) range.
    void dumpMemory( int lo, int hi ){
        var b = new StringBuilder();
        for( int j = lo; j < hi; j++) {
            b.append( String.format( "%04d ", j ));
            b.append( memory[j].toString());
            b.append( "\n" );
        }
        out.println( b.toString());
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
////////////////////////////////////////////////////////////////////////
