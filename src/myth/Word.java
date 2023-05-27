////////////////////////////////////////////////////////////////
package myth;
////////////////////////////////////////////////////////////////
import static java.lang.System.out;
////////////////////////////////////////////////////////////////
// Word is implemented as 64-bit number, plus boolean sign, to
// handle overflow and stuff, and the -0 scenario, respectively.
////////////////////////////////////////////////////////////////
class Word {
    static final int BYTES = 5;
    static final int BYTESIZ = 6; // nof bits
    static final int INTSIZ = 32;
    static final long WORD_MASK = 0x000000003fffffffL;// 30 bits
    static final long OVERFLOW_MASK = ~WORD_MASK;
    static final int FLD_WIDTH = 8; // L:R = F = 8L + R
    static int L( int fld ){
        return fld/ FLD_WIDTH;
    }
    static int R( int fld ){
        return fld% FLD_WIDTH;
    }
    static int F( int left, int ryte ){
        return FLD_WIDTH * left + ryte;
    }
    // Instruction fields
    static final int INSTR_ADR = F( 0, 2 );
    static final int INSTR_IDX = F( 3, 3 );
    static final int INSTR_FLD = F( 4, 4 );
    static final int INSTR_OPC = F( 5, 5 ); // opcode
    ////////////////////////////////////////////////////////////
    // fedcba9876543210fedcba9876543210
    // --______``````______``````______
    //   1     2     3     4     5      BYTEnumber
    //   24    18    12    6     0      BYTEoffset
    static int GetByteOffset( int byteNumber ){
        return BYTESIZ *( BYTES - byteNumber );
    }
    static long GetMask( int left, int ryte ){
        int loff = GetByteOffset( left - 1 );
        int roff = GetByteOffset( ryte );
        return ~(-1L << loff) & (-1L << roff);
    }
    ////////////////////////////////////////////////////////////
    boolean sign; // allowing for -0
    long    bufr; // 64-bit, handles overflows, AX extended
                  // register
    ////////////////////////////////////////////////////////////
    Word( boolean sign, long bufr ){
        this.sign = sign; // false is +
        this.bufr = bufr; // this should be positive
    }
    void setvalue( int left, int ryte, int value ){
        if( left == 0 ){
            sign = value < 0;
            if( sign ) value = -value;
        }
        long mask = GetMask( left, ryte );
        // clear
        this.bufr &= ~mask;
        // set
        int off = GetByteOffset( ryte );
        // casting to long allows to use the negative bytes,
        // for the SLC command etc.
        long shifted = ( long )value << GetByteOffset( ryte );
        this.bufr |= shifted;
    }
    void setvalue( int fld, int value ){
        setvalue( L( fld ), R( fld ), value );
    }
    void setvalue( long value ){
        sign = value < 0;
        bufr = Math.abs( value );
    }
    Word( long value ){
        setvalue( value );
    }
    Word(){
        this( 0L );
    }
    ////////////////////////////////////////////////////////////
    void copy( Word w ){
        sign = w.sign;
        bufr = w.bufr;
    }
    int getfld( int left, int ryte ){
        long mask = GetMask( left, ryte );
        long val = ( bufr & mask ) >> GetByteOffset( ryte );
        if( left == 0 && sign ) val = -val;
        return( int )val;
    }
    int getfld( int fld ){
        return getfld( L( fld ), R( fld ));
    }
    @Override
    public String toString(){
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
    long getval(){
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
        setvalue( res );
    }
    ////////////////////////////////////////////////////////////
    void mul( final Word w ) throws Exception {
        long res = getval()* w.getval();
        // Here we have to check if there is an overflow over
        // the 60th bit.
        if(( res & 0xc000000000000000L ) > 0 ){
            throw new Exception( "Overflow" );
        }
        setvalue( res );
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
        setvalue( q );
        return r;
    }
    ////////////////////////////////////////////////////////////
    boolean isNull(){
        return ( bufr & WORD_MASK ) == 0;
    }
////////////////////////////////////////////////////////////////
//                    2                   1
//  9 8 7 6 5 4 3 2 1 0 9 8 7 6 5 4 3 2 1 0 9 8 7 6 5 4 3 2 1 0
// , , , , , , , , , , , , , , , , , , , , , , , , , , , , , , ,
// :           :           :           :           :           :
//       1           2           3           4           5
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
        n %= 2 * BYTES;
        if( n == 0 ) return;
        n -= 5;
        reverse( -4, 5 );
        reverse( -4, n );
        reverse( n + 1, 5 );
    }
    String toBinaryString(){
        String s = Long.toBinaryString( bufr );
        s = String.format( "%60s", s ).replace( ' ', '0' );
        return s.replaceAll( "(.{6})", "$1 " );
    }
    ///////_////////////////////////////////////////////////////
    public static void main( String[] args ){
        out.println( "Word" );
        Word w = new Word( true, 0 );
        out.println( w );
        out.println( w.isNull());
    }                     
}
///////////////////////////////////////////////////////////////_
///////////////////////////////////////////////////////////////=
//////////////////////////////// sed -i 's/old/new/g' src/myth/*
///////////////////////////////////////////////////////////////-
// + make some tests here
