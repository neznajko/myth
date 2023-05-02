////////////////////////////////////////////////////////////////
package myth;
////////////////////////////////////////////////////////////////
import java.util.ArrayList;
import java.util.HashMap;
////////////////////////////////////////////////////////////////
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
////////////////////////////////////////////////////////////////
import static java.lang.System.out;
////////////////////////////////////////////////////////////////
class Token {
    enum Type {
        NAMBA, // number
        SAMBO, // symbol
        OPERA, // operator
        ASTER, // asterisk
    }
    static final Token ASTERISK = new Token( Type.ASTER, "*"  ); 
    static final Token MINUS    = new Token( Type.OPERA, "-"  );
    static final Token PLUS     = new Token( Type.OPERA, "+"  );
    static final Token FIELD    = new Token( Type.OPERA, ":"  );
    static final Token DIV      = new Token( Type.OPERA, "/"  );
    static final Token DIV2     = new Token( Type.OPERA, "//" );
    //
    Type   key;
    String value;
    //
    Token( Type key, String value ){
        this.key   = key;
        this.value = value;
    }
    @Override
    public String toString() {
        return key.name() + ":=" + value;
    }
    public static void main( String[] args ){
        Token m = new Token( Token.Type.NAMBA, "5134" );
        out.println( m );
    }
}
////////////////////////////////////////////////////////////////
class Espresso {
    static final int MAXSIZ = 10; // f0r symbols and numbers
    //
    static HashMap<Character, Token> map = new HashMap<>();
    static {
        map.put( '*', Token.ASTERISK );
        map.put( '-', Token.MINUS );
        map.put( '+', Token.PLUS );
        map.put( ':', Token.FIELD );
    }
    //
    String coffee;
    //
    static boolean isDigit( char c ){
        if( c >= '0' && c <= '9' ) return true;
        return false;
    }
    static boolean isLetter( char c ){
        if( c >= 'A' && c <= 'Z' ) return true;
        return false;
    }
    static boolean isAlphaNum( char c ){
        return isLetter( c ) || isDigit( c );
    }
    //
    Espresso( String coffee ){
        this.coffee = coffee + "*"; // add Guard
    }
    ////////////////////////////////////////////////////////////
    // getOpera: -> Token <- int( j )
    // j must be over non ( digit or letter )
    Token getOpera( int j ){ //////////////////////////////////_
        char c = coffee.charAt( j );
        if( c == '/' ){
            return( coffee.charAt( j + 1 ) == '/' ?
                    Token.DIV2 :
                    Token.DIV );
        }
        if(! map.containsKey( c )) throw new Error( "ö_o" );
        return map.get( c );
    }
    int atOpera( int i ){ // find next begining of an OPERA token
        for(; isAlphaNum( coffee.charAt( i )); i++ )
            ;
        return i;
    }
    Token getSamba( String value ){ // Nambà ö Sambo
        if( value.length() > MAXSIZ ) throw new Error( "wtf?" );
        int dc = 0; // digit counter
        for( int j = 0; j < value.length(); ++j ){
            if( isDigit( value.charAt( j ))) ++dc;
        }
        Token.Type key = Token.Type.SAMBO;
        if( dc == value.length() ){
            key = Token.Type.NAMBA;
        }
        return new Token( key, value );
    }
    ArrayList<Token> Analyze(){               
        var ingredients = new ArrayList<Token>();
        int j;
        for( int i = 0;; i = j ){
            j = atOpera( i );
            if( j > i ){
                String a = coffee.substring( i, j );
                ingredients.add( getSamba( a ));
            }
            if( j == coffee.length() - 1 ) break; // Guard
            Token operator = getOpera( j );
            ingredients.add( operator );
            j += operator.value.length();
        }
        return ingredients;
    }
    static public void main( String[] args ){
        try {
            String coffee = ( args.length > 0 ?
                              coffee = args[ 0 ] :
                              "-15:WTF2/4+HAHA+10" );
            out.println( new Espresso( coffee ).Analyze());
        } catch( Error e ){
            out.println( e );
        }
    }
}
////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////
class Skanner {
//LOC OP ADR REMRK
    static final String GUARD = "   remark"; // f0r op and adr
    String loc;
    String op;
    String adr;
    BufferedReader sors;
    //
    static boolean isRemark( String line ){
        return( line.charAt( 0 ) == '*' );
    }
    //
    Skanner( String fpath ){
        try {
            sors = new BufferedReader( new FileReader( fpath ));
        } catch( FileNotFoundException e ){
            throw new Error( e.getMessage());
        }
    }
    void Decompose( String line ){
        line += GUARD;
        String[] f = line.split( "\\s" );
        loc  = f[ 0 ];
        op   = f[ 1 ];
        adr  = f[ 2 ];
    }
    boolean getnext() throws IOException {
        while( true ){
            String line = sors.readLine();
            if( line == null ){
                sors.close();
                break;
            }
            if( isRemark( line ) || line.trim().isEmpty()){
                continue;
            }
            Decompose( line );
            return true;
        }
        return false;
    }
    @Override
    public String toString(){
        return loc + "|" + op + "|" + adr;
    }
    public static void main( String args[] ){
        try {
            Skanner ska = new Skanner( args[0] );
            while( ska.getnext()){
                out.println( ska );
            }
        } catch( Throwable t ){
            out.println( t.getMessage());
        }
    }
}
////////////////////////////////////////////////////////////////
class Pair<X,Y> {
    X x;
    Y y;
    Pair( X x, Y y ){
        this.x = x;
        this.y = y;
    }
    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}
////////////////////////////////////////////////////////////////
class Instruction {
    static HashMap<String,Pair<Integer,Integer>> map;
    static {
        map = new HashMap<>(); //     C  F
        map.put(  "LDA", new Pair<>(  8, 5 )); // Loading
        map.put(  "LDX", new Pair<>( 15, 5 ));
        map.put(  "LD1", new Pair<>(  9, 5 ));
        map.put(  "LD2", new Pair<>( 10, 5 ));
        map.put(  "LD3", new Pair<>( 11, 5 ));
        map.put(  "LD4", new Pair<>( 12, 5 ));
        map.put(  "LD5", new Pair<>( 13, 5 ));
        map.put(  "LD6", new Pair<>( 14, 5 ));
        map.put( "LDAN", new Pair<>( 16, 5 ));
        map.put( "LDXN", new Pair<>( 23, 5 ));
        map.put( "LD1N", new Pair<>( 17, 5 ));
        map.put( "LD2N", new Pair<>( 18, 5 ));
        map.put( "LD3N", new Pair<>( 19, 5 ));
        map.put( "LD4N", new Pair<>( 20, 5 ));
        map.put( "LD5N", new Pair<>( 21, 5 ));
        map.put( "LD6N", new Pair<>( 22, 5 ));
        map.put(  "STA", new Pair<>( 24, 5 )); // Storing
        map.put(  "STX", new Pair<>( 31, 5 ));
        map.put(  "ST1", new Pair<>( 25, 5 ));
        map.put(  "ST2", new Pair<>( 26, 5 ));
        map.put(  "ST3", new Pair<>( 27, 5 ));
        map.put(  "ST4", new Pair<>( 28, 5 ));
        map.put(  "ST5", new Pair<>( 29, 5 ));
        map.put(  "ST6", new Pair<>( 30, 5 ));
        map.put(  "STJ", new Pair<>( 32, 2 ));
        map.put(  "STZ", new Pair<>( 33, 5 ));
        map.put(  "ADD", new Pair<>(  1, 5 )); // Arithmetic
        map.put(  "SUB", new Pair<>(  2, 5 ));
        map.put(  "MUL", new Pair<>(  3, 5 ));
        map.put(  "DIV", new Pair<>(  4, 5 ));
        map.put( "ENTA", new Pair<>( 48, 2 )); // Address Transfer
        map.put( "ENTX", new Pair<>( 55, 2 ));
        map.put( "ENT1", new Pair<>( 49, 2 ));
        map.put( "ENT2", new Pair<>( 50, 2 ));
        map.put( "ENT3", new Pair<>( 51, 2 ));
        map.put( "ENT4", new Pair<>( 52, 2 ));
        map.put( "ENT5", new Pair<>( 53, 2 ));
        map.put( "ENT6", new Pair<>( 54, 2 ));
        map.put( "ENNA", new Pair<>( 48, 3 ));
        map.put( "ENNX", new Pair<>( 55, 3 ));
        map.put( "ENN1", new Pair<>( 49, 3 ));
        map.put( "ENN2", new Pair<>( 50, 3 ));
        map.put( "ENN3", new Pair<>( 51, 3 ));
        map.put( "ENN4", new Pair<>( 52, 3 ));
        map.put( "ENN5", new Pair<>( 53, 3 ));
        map.put( "ENN6", new Pair<>( 54, 3 ));
        map.put( "INCA", new Pair<>( 48, 0 ));
        map.put( "INCX", new Pair<>( 55, 0 ));
        map.put( "INC1", new Pair<>( 49, 0 ));
        map.put( "INC2", new Pair<>( 50, 0 ));
        map.put( "INC3", new Pair<>( 51, 0 ));
        map.put( "INC4", new Pair<>( 52, 0 ));
        map.put( "INC5", new Pair<>( 53, 0 ));
        map.put( "INC6", new Pair<>( 54, 0 ));
        map.put( "DECA", new Pair<>( 48, 1 ));
        map.put( "DECX", new Pair<>( 55, 1 ));
        map.put( "DEC1", new Pair<>( 49, 1 ));
        map.put( "DEC2", new Pair<>( 50, 1 ));
        map.put( "DEC3", new Pair<>( 51, 1 ));
        map.put( "DEC4", new Pair<>( 52, 1 ));
        map.put( "DEC5", new Pair<>( 53, 1 ));
        map.put( "DEC6", new Pair<>( 54, 1 ));
        map.put( "CMPA", new Pair<>( 56, 5 )); // Comparison
        map.put( "CMPX", new Pair<>( 63, 5 ));
        map.put( "CMP1", new Pair<>( 57, 1 ));
        map.put( "CMP2", new Pair<>( 58, 1 ));
        map.put( "CMP3", new Pair<>( 59, 1 ));
        map.put( "CMP4", new Pair<>( 60, 1 ));
        map.put( "CMP5", new Pair<>( 61, 1 ));
        map.put( "CMP6", new Pair<>( 62, 1 ));
        map.put(  "JMP", new Pair<>( 39, 0 )); // Jump
        map.put(  "JSJ", new Pair<>( 39, 1 ));
        map.put(  "JOV", new Pair<>( 39, 2 ));
        map.put( "JNOV", new Pair<>( 39, 3 ));
        map.put(   "JL", new Pair<>( 39, 4 ));
        map.put(   "JE", new Pair<>( 39, 5 ));
        map.put(   "JG", new Pair<>( 39, 6 ));
        map.put(  "JGE", new Pair<>( 39, 7 ));
        map.put(  "JNE", new Pair<>( 39, 8 ));
        map.put(  "JLE", new Pair<>( 39, 9 ));
        map.put(  "JAN", new Pair<>( 40, 0 ));
        map.put(  "JAZ", new Pair<>( 40, 1 ));
        map.put(  "JAP", new Pair<>( 40, 2 ));
        map.put( "JANN", new Pair<>( 40, 3 ));
        map.put( "JANZ", new Pair<>( 40, 4 ));
        map.put( "JANP", new Pair<>( 40, 5 ));
        map.put(  "JXN", new Pair<>( 47, 0 ));
        map.put(  "JXZ", new Pair<>( 47, 1 ));
        map.put(  "JXP", new Pair<>( 47, 2 ));
        map.put( "JXNN", new Pair<>( 47, 3 ));
        map.put( "JXNZ", new Pair<>( 47, 4 ));
        map.put( "JXNP", new Pair<>( 47, 5 ));
        map.put(  "J1N", new Pair<>( 41, 0 ));
        map.put(  "J1Z", new Pair<>( 41, 1 ));
        map.put(  "J1P", new Pair<>( 41, 2 ));
        map.put( "J1NN", new Pair<>( 41, 3 ));
        map.put( "J1NZ", new Pair<>( 41, 4 ));
        map.put( "J1NP", new Pair<>( 41, 5 ));
        map.put(  "J2N", new Pair<>( 42, 0 ));
        map.put(  "J2Z", new Pair<>( 42, 1 ));
        map.put(  "J2P", new Pair<>( 42, 2 ));
        map.put( "J2NN", new Pair<>( 42, 3 ));
        map.put( "J2NZ", new Pair<>( 42, 4 ));
        map.put( "J2NP", new Pair<>( 42, 5 ));
        map.put(  "J3N", new Pair<>( 43, 0 ));
        map.put(  "J3Z", new Pair<>( 43, 1 ));
        map.put(  "J3P", new Pair<>( 43, 2 ));
        map.put( "J3NN", new Pair<>( 43, 3 ));
        map.put( "J3NZ", new Pair<>( 43, 4 ));
        map.put( "J3NP", new Pair<>( 43, 5 ));
        map.put(  "J4N", new Pair<>( 44, 0 ));
        map.put(  "J4Z", new Pair<>( 44, 1 ));
        map.put(  "J4P", new Pair<>( 44, 2 ));
        map.put( "J4NN", new Pair<>( 44, 3 ));
        map.put( "J4NZ", new Pair<>( 44, 4 ));
        map.put( "J4NP", new Pair<>( 44, 5 ));
        map.put(  "J5N", new Pair<>( 45, 0 ));
        map.put(  "J5Z", new Pair<>( 45, 1 ));
        map.put(  "J5P", new Pair<>( 45, 2 ));
        map.put( "J5NN", new Pair<>( 45, 3 ));
        map.put( "J5NZ", new Pair<>( 45, 4 ));
        map.put( "J5NP", new Pair<>( 45, 5 ));
        map.put(  "J6N", new Pair<>( 46, 0 ));
        map.put(  "J6Z", new Pair<>( 46, 1 ));
        map.put(  "J6P", new Pair<>( 46, 2 ));
        map.put( "J6NN", new Pair<>( 46, 3 ));
        map.put( "J6NZ", new Pair<>( 46, 4 ));
        map.put( "J6NP", new Pair<>( 46, 5 ));
        map.put(  "SLA", new Pair<>(  6, 0 )); // Miscellaneous
        map.put(  "SRA", new Pair<>(  6, 1 ));
        map.put( "SLAX", new Pair<>(  6, 2 ));
        map.put( "SRAX", new Pair<>(  6, 3 ));
        map.put(  "SLC", new Pair<>(  6, 4 ));
        map.put(  "SRC", new Pair<>(  6, 5 ));
        map.put( "MOVE", new Pair<>(  7, 0 ));
        map.put(  "NOP", new Pair<>(  0, 0 ));
        map.put(  "HLT", new Pair<>(  0, 2 ));
        map.put(   "IN", new Pair<>( 36, 0 )); // I/O
        map.put(  "OUT", new Pair<>( 37, 0 ));
        map.put(  "IOC", new Pair<>( 35, 0 ));
        map.put( "JRED", new Pair<>( 38, 0 ));
        map.put( "JBUS", new Pair<>( 34, 0 ));
        map.put(  "NUM", new Pair<>(  5, 0 )); // Conversion
        map.put( "CHAR", new Pair<>(  5, 1 ));
    }
    public static void main( String[] args ){
        out.println( map );
    }
}
////////////////////////////////////////////////////////////////
public class Myth {
    public static void main( String[] args ){
        out.println( "Myth" );
    }
}
////////////////////////////////////////////////////////////////
// log: - Create Future Reference RuntimeException           []
//      - allow spaces to appear in the ska.adr              []
//      - dump litab as well                                 []

