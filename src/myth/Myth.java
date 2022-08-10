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
class Cappuccino {
    static final int MAXSIZ = 10; // symbols and numbers
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
    static boolean estDigit( char c ){
        if( c >= '0' && c <= '9' ) return true;
        return false;
    }
    static boolean estLetter( char c ){
        if( c >= 'A' && c <= 'Z' ) return true;
        return false;
    }
    static boolean estAlphaNumeric( char c ){
        return estLetter( c ) || estDigit( c );
    }
    //
    Cappuccino( String coffee ){
        this.coffee = coffee + "*"; // Guard
    }
    ////////////////////////////////////////////////////////////
    // getOperator: -> Token <- int( j )
    // j must be over non digit or letter character
    Token getOperator( int j ){ ///////////////////////////////_
        char c = coffee.charAt( j );
        if( c == '/' ){
            try {
                return( coffee.charAt( j + 1 ) == '/' ?
                        Token.DIV2 :
                        Token.DIV );
            } catch( Exception e ){
                throw new Error( "// <<" );
            }
        }
        if(! map.containsKey( c )) throw new Error( "o_o" );

        return map.get( c );
    }
    int eatAlphaNumerics( int i ){
        for(; estAlphaNumeric( coffee.charAt( i )); i++ )
            ;
        return i;
    }
    Token getOperand( String value ){
        if( value.length() > MAXSIZ ) throw new Error( "wtf?" );
        int dc = 0; // digit counter
        for( int j = 0; j < value.length(); ++j ){
            if( estDigit( value.charAt( j ))) ++dc;
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
        for( int i = 0; i < coffee.length(); i = j ){
            j = eatAlphaNumerics( i );
            if( j > i ){
                String a = coffee.substring( i, j );
                Token operand = getOperand( a );
                ingredients.add( operand );                
            }
            Token operator = getOperator( j );
            if( operator != null ){
                ingredients.add( operator );
                j += operator.value.length();
            }
        }
        // Discard Guard Token
        ingredients.remove( ingredients.size() - 1 );
        return ingredients;
    }
    static public void main( String[] args ){
        try {
            out.println( new Cappuccino( "-5/2+HAHA" )
                         .Analyze());
        } catch( Error e ){
            out.println( e );
        }
    }
}
////////////////////////////////////////////////////////////////
class Espresso {
    static final int MAXLEN = 10;
    static boolean estDigit( char c ){
        if( c >= '0' && c <= '9' ) return true;
        return false;
    }
    static boolean estLetter( char c ){
        if( c >= 'A' && c <= 'Z' ) return true;
        return false;
    }
    // log: try to split this function
    static ArrayList<Token> Analyze( String coffee ){
        var ingredients = new ArrayList<Token>();
        int n = coffee.length();
        for( int i = 0; i < n; ){
            char c = coffee.charAt( i++ );
            switch( c ){
            case '*':
                ingredients.add( Token.ASTERISK );
                break;
            case ':':
                ingredients.add( Token.FIELD );
                break;
            case '-':
                ingredients.add( Token.MINUS );
                break;
            case '+':
                ingredients.add( Token.PLUS );
                break;
            case '/':
                if( i < n && coffee.charAt( i ) == '/' ){
                    ingredients.add( Token.DIV2 );
                    i++;
                } else {
                    ingredients.add( Token.DIV );
                }
                break;
            default:
                int j = --i;
                boolean flag = true; // number flag
                for(; j < n; j++ ){
                    c = coffee.charAt( j );
                    if( estLetter( c )){
                        flag = false;
                    } else if( estDigit( c )){
                        continue;
                    } else {
                        break;
                    }
                }
                if( i == j || j - i > MAXLEN ){
                    throw new Error( "espresso: " + coffee );
                }
                String value = coffee.substring( i, j );
                if( flag ){
                    var key = Token.Type.NAMBA;
                    ingredients.add( new Token( key, value ));
                } else {
                    var key = Token.Type.SAMBO;
                    ingredients.add( new Token( key, value ));
                }
                i = j;
            }
        }
        return ingredients;
    }
    static public void main( String[] args ){
        try {
            for( Token m: Espresso.Analyze( args[ 0 ])) {
                out.println( m );
            }
        } catch( Error e ){
            out.println( e );
        }
    }
}
////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////
class Fields {
    String loc;
    String op;
    String adr;
    Fields( String line ){
        line += "   remark"; // add op and adr guards
        String[] f = line.split( "\\s" );
        loc  = f[ 0 ];
        op   = f[ 1 ];
        adr = f[ 2 ];
    }
    @Override
    public String toString() {
        return "(" + loc + "|" + op + "|" + adr + ")\n";
    }
    public static void main( String[] args ){
        Fields f = new Fields( args[ 0 ]);
        out.println( f );
    }
}
////////////////////////////////////////////////////////////////
class Scanner {
    BufferedReader input = null;
    static boolean estRemark( String line ){
        return( line.charAt( 0 ) == '*' );
    }
    Scanner( String fileName ){
        try {
            FileReader fr = new FileReader( fileName );
            input = new BufferedReader( fr );
        } catch ( FileNotFoundException e ){
            out.println( e );
        }
    }
    Fields getnext() throws IOException {
        while( true ){
            String line = input.readLine();
            if( line == null ){
                input.close();
                return null;
            }
            if( line.trim().isEmpty() || estRemark( line )){
                continue;
            }
            return new Fields( line );
        }
    }
    public static void main( String args[] ){
        try {
            Scanner scanner = new Scanner( args[0] );
            while( true ) {
                Fields f = scanner.getnext();
                if( f == null ) break;
                out.println( f );
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
        map = new HashMap<>();
        map.put(  "LDA", new Pair<>(  8, 5 )); // Loading
        map.put(  "LDX", new Pair<>( 15, 5 ));
        map.put(  "LD1", new Pair<>(  9, 0 ));
        map.put(  "LD2", new Pair<>( 10, 0 ));
        map.put(  "LD3", new Pair<>( 11, 0 ));
        map.put(  "LD4", new Pair<>( 12, 0 ));
        map.put(  "LD5", new Pair<>( 13, 0 ));
        map.put(  "LD6", new Pair<>( 14, 0 ));
        map.put( "LDAN", new Pair<>( 16, 5 ));
        map.put( "LDXN", new Pair<>( 23, 5 ));
        map.put( "LD1N", new Pair<>( 17, 0 ));
        map.put( "LD2N", new Pair<>( 18, 0 ));
        map.put( "LD3N", new Pair<>( 19, 0 ));
        map.put( "LD4N", new Pair<>( 20, 0 ));
        map.put( "LD5N", new Pair<>( 21, 0 ));
        map.put( "LD6N", new Pair<>( 22, 0 ));
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
        map.put(   "IN", new Pair<>( 36, 0 )); // Input-Output
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
////////////////////////////////////////////////////////////////
public class Myth {
    public static void main( String[] args ){
        out.println( "Myth" );
    }
}
////////////////////////////////////////////////////////////////
// log: -Debug a little bit and replace Espresso with Cappuccino
