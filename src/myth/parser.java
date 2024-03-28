//////////////////////////////////////////////////////////////
package myth;
//////////////////////////////////////////////////////////////
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
//////////////////////////////////////////////////////////////
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//////////////////////////////////////////////////////////////
import static java.lang.System.out;
//////////////////////////////////////////////////////////////
// All evaluations are within Parser because of symbol table 
// and program counter, not to mention the lab arrays, zo all
// fields ( a, i, f ) for that reason are strings
// ( not evaluated ).
class Address { // a,i(f)
    static final String A = "(^=\\d+=|^[^,()=]+)";
    static final String I = "(,([^(),]+))?";
    static final String F = "(\\(([^,()]+)\\))?$";
    static final String REGEXP = A + I + F;
    static final Pattern PATTERN = Pattern.compile( REGEXP );
    //
    String a;
    String i;
    String f; 
    //
    Address( String adr ){
        if( adr.isEmpty()) return;
        Matcher matcher = PATTERN.matcher( adr );
        if( !matcher.find()){
            throw new Error( "@8o regexps are fun ho ho ho" );
        }
        a = matcher.group( 1 );
        i = matcher.group( 3 );
        f = matcher.group( 5 );
    }
    Address( String a, String i, String f ){
        this.a = a;
        this.i = i;
        this.f = f;
    }
    @Override
    public String toString() {
        return "(" + a + "){" + i + "}[" + f + "]";
    }
    public static void main( String args[]) {
        var adr = new Address( args[0] );
        out.println( adr );
    }
}
//////////////////////////////////////////////////////////////
// After Parser's first pas create list of Snapshots with 
// Future references or Literals for second re-evaluation.
class Snapshot {
    int     pc;
    String  op;
    Address adr;
    boolean futureRef; // if false adr's a-part is a literal
    Snapshot( int pc, String op, Address adr, 
              boolean futureRef ){
        this.pc        = pc;
        this.op        = op;
        this.adr       = adr;
        this.futureRef = futureRef;
    }
    @Override
    public String toString() {
        return( pc + ":" + op + "," + adr.toString() + "|" + 
                futureRef );
    }
    public static void main( String args[] ){
        out.println( new Snapshot( 12,
                                   "ADD",
                                   new Address( "3,5(1)" ),
                                   false ));
    }
}
//////////////////////////////////////////////////////////////
//```` | {@@|  :@@;  }@@           | _ _ | _ _ _ _ _ _ _ _ _ _
class Parser { ///////////////////////////////////////////////
    static final int DIGITS = 10; // local symbol labels.
    static final Pattern LABPAT = Pattern.compile
        ( "^\\d[HBF]$" );
    //
    static HashMap<Character,Integer> charmap;
    static HashMap<Integer,Character> mapchar;
    static {
        charmap = new HashMap<>();
        charmap.put(  ' ',  0 );
        charmap.put(  'A',  1 );
        charmap.put(  'B',  2 );
        charmap.put(  'C',  3 );
        charmap.put(  'D',  4 );
        charmap.put(  'E',  5 );
        charmap.put(  'F',  6 );
        charmap.put(  'G',  7 );
        charmap.put(  'H',  8 );
        charmap.put(  'I',  9 );
        charmap.put(  'Δ', 10 ); // 0394
        charmap.put(  'J', 11 );
        charmap.put(  'K', 12 );
        charmap.put(  'L', 13 );
        charmap.put(  'M', 14 );
        charmap.put(  'N', 15 );
        charmap.put(  'O', 16 );
        charmap.put(  'P', 17 );
        charmap.put(  'Q', 18 );
        charmap.put(  'R', 19 );
        charmap.put(  'Σ', 20 ); // 03A3
        charmap.put(  'Π', 21 ); // 03A0
        charmap.put(  'S', 22 );
        charmap.put(  'T', 23 );
        charmap.put(  'U', 24 );
        charmap.put(  'V', 25 );
        charmap.put(  'W', 26 );
        charmap.put(  'X', 27 );
        charmap.put(  'Y', 28 );
        charmap.put(  'Z', 29 );
        charmap.put(  '0', 30 );
        charmap.put(  '1', 31 );
        charmap.put(  '2', 32 );
        charmap.put(  '3', 33 );
        charmap.put(  '4', 34 );
        charmap.put(  '5', 35 );
        charmap.put(  '6', 36 );
        charmap.put(  '7', 37 );
        charmap.put(  '8', 38 );
        charmap.put(  '9', 39 );
        charmap.put(  '.', 40 );
        charmap.put(  ',', 41 );
        charmap.put(  '(', 42 );
        charmap.put(  ')', 43 );
        charmap.put(  '+', 44 );
        charmap.put(  '-', 45 );
        charmap.put(  '*', 46 );
        charmap.put(  '/', 47 );
        charmap.put(  '=', 48 );
        charmap.put(  '$', 49 );
        charmap.put(  '<', 50 );
        charmap.put(  '>', 51 );
        charmap.put(  '@', 52 );
        charmap.put(  ';', 53 );
        charmap.put(  ':', 54 );
        charmap.put( '\'', 55 );
        mapchar = new HashMap<>();
        for( var entry: charmap.entrySet()){
            mapchar.put( entry.getValue(), entry.getKey());
        }
    }
    //
    // This is the famous Symbol Table.
    HashMap<String,Integer> tab;
    // The program counter.
    int pc; 
    // Labels( local symbols ) stacks.
    @SuppressWarnings("unchecked")
    ArrayList<Integer>[] lab = new ArrayList[ DIGITS ];
    ArrayList<Snapshot> timeshift;
    VM vm; // Virtual Machine
    // Literal Constant to Address, mapping
    HashMap<String,String> litab;
    Walue walue;
    Skanner ska;
    int[] line_number_map = new int[ VM.MEMSIZ ];
    //////////////////////////////////////////////////////////
    // Cons
    Parser(){
        tab = new HashMap<>();
        pc = -1;
        for( int j = 0; j < DIGITS; j++ ){
            lab[ j ] = new ArrayList<>();
        }
        timeshift = new ArrayList<>();
        vm = new VM();
        litab = new HashMap<>();
        walue = new Walue( this );
    }
    //////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////
    // Check if token tok is in the form dB, dH, or dF,
    // where d is 0-9 digit.
    static Pair<Character,Integer> checkLab( String tok ){
        if( !LABPAT.matcher( tok ).find()){
            throw new RuntimeException( "labpat" );
        }
        return new Pair<>( tok.charAt( 1 ),
                           tok.charAt( 0 ) - '0' );
    }
    //////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////
    // Check the correctness of the LOC field. On error throw
    // an Error, if it's a local label return its number, 
    // otherwise if it's normal label return -1.
    static int checkLoc( String loc ){
        var espresso = new Espresso( loc ).Analyze();
        if( espresso.size() != 1 ){
            throw new Error( "what is this?" );
        }
        var tok = espresso.get( 0 );
        if( tok.key != Token.Type.SAMBO ){
            throw new Error( "If you want to be like Rambo,"
                             + " train Sambo." );
        }
        // Check if local symbol, dH, etc.
        try {
            var p = checkLab( tok.value );
            if( p.x == 'H' ) return p.y;
        } catch( Throwable t ){
            return -1;
        }
        throw new Error( "ö_Ò" );
    }
    //////////////////////////////////////////////////////////
    // Print for debugging and stuff.
    @Override
    public String toString() {
        var b = new StringBuilder();
        b.append( "tab: " + tab.toString());
        b.append( "\npc: " + pc );
        for( int j = 0; j < DIGITS; j++ ){
            b.append( "\n" + j + ": " );
            b.append( Arrays.toString( lab[j].toArray()));
        }
        b.append( "\ntimeshift: " +
                  Arrays.toString( timeshift.toArray()));
        b.append( "\nlitab: " + litab.toString());
        return b.toString();
    }
    //////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////
    int getTokenValue( Token tok ){
        String value = tok.value;
        switch( tok.key ){
        case NAMBA: return Integer.parseInt( value );
        case ASTER: return pc;
        case SAMBO:
            // Check vhether it's backward local label.
            try {
                var p = checkLab( value );
                switch( p.x ){
                case 'H': break;
                case 'B':
                    int n = lab[ p.y ].size();
                    return lab[ p.y ].get( n - 1 );
                case 'F': // Future Reference
                    throw new RuntimeException( value );
                }
            } catch( RuntimeException e ){
                if( tab.containsKey( value )){
                    return tab.get( value );
                } else {
                    // Ok Future Reference / Literal Constant
                    throw new RuntimeException( value );
                }
            }
        }
        throw new Error( "haha" );
    }
    //////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////
    // coffee:
    // <opera,-><namba,5><opera,+><aster,*><opera,-><sambo,2B>
    int eval( ArrayList<Token> coffee ) throws RuntimeException {
        // Get first the sign if present, and then othr operatrs
        // the evaluation is straight from left to right e.g.:
        // -8+1/2+3-2 evaluates to -2
        boolean sign = false;
        Token tok = coffee.get( 0 );
        int j = 0; 
        if( tok.key == Token.Type.OPERA ){
            switch( tok.value.charAt( 0 )){
            case '-': sign = true;
            case '+':
                ++j;
                break;
            default: throw new Error( tok.toString());
            }
        }
        // check here coffee's length
        // a) no sign
        // a+b-c/d
        // 0123456: 6 - 0 + 1 = 7 is odd
        // b) sign
        // -3/5*7-*: 7 - 1 + 1 = 7 is not even!
        // 01234567
        int n = coffee.size();
        if((( n - j ) & 1 ) == 0 ){ // Check the odd bit
            throw new Error( "Expression!" );
        }
        // initialize return value
        tok = coffee.get( j );
        int res;
        res = getTokenValue( tok );
        if( sign ) res = -res;
        // -5+3//HAHA-8**
        for( j++, n--; j < n; j += 2 ){
            tok = coffee.get( j );
            if( tok.key == Token.Type.NAMBA ||
                tok.key == Token.Type.SAMBO ){
                throw new Error( "#8|" );
            }
            String op = tok.value;
            tok = coffee.get( j + 1 );
            if( tok.key == Token.Type.OPERA ){
                throw new Error( "=8|" );
            }
            int val = getTokenValue( tok );
            if( op.equals( "-" )){
                res -= val;
            } else if( op.equals( "+" )){
                res += val;
            } else if( op.equals( "*" )){
                res *= val;
            } else if( op.equals( "/" )){
                res /= val;
            } else if( op.equals( "//" )){
                Word w = new Word( res );
                w.shiftleft( Word.BYTES );
                w.div( val );
                res = w.getfld( 0, Word.BYTES );
            } else if( op.equals( ":" )){
                res *= 8;
                res += val;
            } else {
                throw new Error( ";)" );
            }
        }
        return res;
    }
    ////////////////////////////////////////////////////////////
    // =abcdefghij=
    // 0123456789ab
    String isLiteral( String a ){
        if( a == null ) return ""; // n.o p
        int b = a.length() - 1; // back
        if( b > DIGITS + 1 ){
            throw new Error( "That's too long even for gcc, I guess." );
        }
        if( a.charAt(0) != '=' || a.charAt(b) != '=' ){
            return ""; // nope
        }
        a = a.substring( 1, b ); // discard ='s
        return String.valueOf( walue.ewal(a).getval());
        // 4 values:  1        2     3          4
    }
    ////////////////////////////////////////////////////////////
    // Pass a-field string, obtained from Address constructor,
    // try running eval on it, if there is an exception, check
    // if there is only one token. In such a case this is a 
    // future reference, return MAX_VALUE to indicate positive
    // result, otherwise return the expression's evaluation.
    ////////////////////////////////////////////////////////////
    int isFutureRef( String a ){
        if( a == null ) return 0;
        var coffee = new Espresso(a).Analyze();
        int val = Integer.MAX_VALUE;
        try {
            val = eval( coffee );
        } catch( RuntimeException e ){
            if( coffee.size() > 1 ){
                throw new Error( "Future Reference" );
            }
        }
        return val;
    }
    ////////////////////////////////////////////////////////////
    // called by firstPass
    void asm( String op, Address adr ){
        // check if a-Part is Literal
        String a = isLiteral( adr.a );
        if( !a.isEmpty()) { // literal constant
            adr = new Address( a, adr.i, adr.f );
            timeshift.add( new Snapshot( pc, op, adr, false ));
            return;
        }
        // evaluate a-Part
        int aval = isFutureRef( adr.a );
        if( aval == Integer.MAX_VALUE ){ // Future refernce
            timeshift.add( new Snapshot( pc, op, adr, true ));
            return;
        }
        ////////////////////////////////////////////////////////
        // evaluate i,f-parts
        int ival = ( adr.i == null
                     ? 0
                     : eval( new Espresso( adr.i ).Analyze()));
        var instr = Instruction.map.get( op );
        int code = instr.x;
        int fld  = ( adr.f == null
                     ? instr.y
                     : eval( new Espresso( adr.f ).Analyze()));
        Word w = vm.memory[ pc ];
        w.setvalue( Word.INSTR_ADR, aval ); // 0:2
        w.setvalue( Word.INSTR_IDX, ival ); // 3:3
        w.setvalue( Word.INSTR_FLD, fld  ); // 4:4
        w.setvalue( Word.INSTR_OPC, code ); // 5:5
    }
    ////////////////////////////////////////////////////////////
    static Word encode( String alf ){
        int n = alf.length();
        if( n > Word.BYTES ) throw new Error( "encode" );
        Word w = new Word();
        for( int j = 1; j <= n; ++j ){
            w.setvalue( Word.F( j, j ),
                        charmap.get( alf.charAt(j - 1)));
        }
        return w;
    }
    ////////////////////////////////////////////////////////////
    static String decode( Word w ){
        char c[] = new char[ Word.BYTES ];
        for( int j = 1; j <= Word.BYTES; j++ ){
            c[j - 1] = mapchar.get( w.getfld( j, j ));
        }
        return new String(c);
    }
    ////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////
    void loadFile( String fileName ){
        ska = new Skanner( fileName );
    }
    ////////////////////////////////////////////////////////
    void firstPass() throws Exception {
        while( ska.getnext()) {
            pc++;
            // LOC
            if( !ska.loc.isEmpty()) {
                int d = checkLoc( ska.loc );
                if( d >= 0 ) lab[ d ].add( pc );
                else tab.put( ska.loc, pc );
            }
            // OP
            if( ska.op.isEmpty()) continue;
            Word w;
            switch( getop( ska.op )) {
            case MIX:
                asm( ska.op, new Address( ska.adr ));
                line_number_map[ pc - 1 ] = ska.line_number;
                break;
            case EQU:
                w = walue.ewal( ska.adr );
                tab.put( ska.loc, (int) w.getval());
                pc--;
                break;
            case ORIG:
                w = walue.ewal( ska.adr );
                pc = (int) w.getval() - 1;
                break;
            case CON:
                w = walue.ewal( ska.adr );
                vm.memory[ pc ] = w;
                break;
            case ALF:
                vm.memory[ pc ] = encode( ska.adr );
                break;
            case END:
                vm.start = ( int )walue.ewal( ska.adr ).getval();
                vm.end = pc;
                break;
            default: throw new Error( "8/" );
            }
        }
    }
    ////////////////////////////////////////////////////////////
    enum Optype {
        NOPE, // Error 
        MIX,  // MIX Instruction
        EQU,  // mixal pseudo instructions
        ORIG, //
        CON,  //
        ALF,  //
        END,  //
    };
    ////////////////////////////////////////////////////////////
    static Optype getop( String op ){
        if( Instruction.map.containsKey( op )) return Optype.MIX;
        if( op.equals( "EQU" ))  return Optype.EQU;
        if( op.equals( "ORIG" )) return Optype.ORIG;
        if( op.equals( "CON" ))  return Optype.CON;
        if( op.equals( "ALF" ))  return Optype.ALF;
        if( op.equals( "END" ))  return Optype.END;
        return Optype.NOPE;
    }
    ////////////////////////////////////////////////////////////
    // Assembly involving literal constant
    void asmli( Snapshot cheese ){
        var adr = cheese.adr;
        if( litab.containsKey( adr.a )){
            adr.a = litab.get( adr.a );
        } else {
            Word w = vm.memory[ vm.end ];
            w.setvalue( Long.valueOf( adr.a ));
            String a = String.valueOf( vm.end++ );
            litab.put( adr.a, a );
            adr.a = a;
        }
        // assemble the instruction at cheese.pc here
        pc = cheese.pc;
        asm( cheese.op, adr );
    }
//////////////////////////////////////////////////////////////
    void asmfr( Snapshot snapshot ){
        Address adr = snapshot.adr;
        String a = adr.a;
        Pair<Character,Integer> p;
        try{ // dF
            p = checkLab( a );
            int d = p.y;
            var thelab = lab[ d ];
            // thelab should be sorted, but no need for binary
            // search, m is the first number in thelab bigger
            // than snapshot.pc.
            int m = -1;
            int key = snapshot.pc;
            for( int n: thelab ){
                if( n > key ){
                    m = n;
                    break;
                }
            }
            if( m == -1 ){
                throw new Error( "label not found" );
            }
            adr.a = String.valueOf( m );
        } catch( RuntimeException e ){ // normal symbol
            // Not defined in the LOC field.
            if( !tab.containsKey( a )) adr.a = "0";
        }
        pc = snapshot.pc;
        asm( snapshot.op, adr );
    }
//////////////////////////////////////////////////////////////
    void secondPass() {
    // Figure _out first _the literal constants. They must be in
    // the timeshift array flagged as false, use asmli on them.
        // Ok sort all labels here
        for( int j = 0; j < DIGITS; j++ ){
            Collections.sort( lab[ j ]);
        }
        int backup = vm.end;
        for( Snapshot snapshot: timeshift ){
            if( snapshot.futureRef ){
                asmfr( snapshot );
            } else {
                asmli( snapshot );
            }
        }
        vm.end = backup;
    }
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
    void compile( String fileName ){
        loadFile( fileName );
        try{ 
            firstPass();
            secondPass();
        } catch( Throwable t ){
            out.println( t.getMessage() );
        }
    }
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
    public static void main( String[] args ) throws Exception {
        Parser parser = new Parser();
        try {
            parser.compile( "src.mixal" );
            out.println( parser );
            parser.vm.dumpMemory( 0, 20 );
            out.println( parser.ska.file );
            out.println( Arrays.toString( parser.line_number_map ));
        } catch( Exception e ){
            out.println( e );
        }
    }
}
//////////////  ////////////////////////////////////////////////
class Walue { // W-Value
    static final Pattern COMP = Pattern.compile
        ( "^([^()]+)+(\\((.+)\\))*$" ); // E(F)
    static final Pattern SPLITPAT = Pattern.compile
        ( "([^,]+),*" ); // comp1,comp2,..,compN
    //
    Parser parser;
    Walue( Parser parser ){
        this.parser = parser;
    }
    //////////////////////////////////////////////////////////////////
    // Check Component E1(F1),[E2(F2)],...,EN(FN)
    static Pair<String,String> checkcomp( String comp ){
        Matcher mat = COMP.matcher( comp );
        if( !mat.find()) throw new Error( "comp[haha]not[good]" );
        return new Pair<>( mat.group(1),
                           mat.group(3) == null ? "0:5" : mat.group(3));
    }
    // E1(F1),E2(F2),...,EN(FN)
    static ArrayList<Pair<String,String>> split( String walue ){
        var mat = SPLITPAT.matcher( walue );
        var ls = new ArrayList<Pair<String,String>>();
        while( mat.find()) {
            ls.add( checkcomp( mat.group(1)));
        }
        return ls;
    }
    ////////////////////////////////////////////////////////////
    static boolean checkField( int F ){
        if( F < 0 ) return false; 
        // F = 8L + R
        if( F/8 > Word.BYTES ||
            F%8 > Word.BYTES ) return false;
        return true;
    }
    ////////////////////////////////////////////////////////////
    Word ewal( String walue ){
        Word w = new Word();
        try {
            for( var comp: split( walue )){
                // Here comp is an <E,F> pair.
                int F = parser.eval( new Espresso( comp.y ).Analyze());
                if(! checkField( F )){
                    throw new Error( "Not valid field" );
                }
                int E = parser.eval( new Espresso( comp.x ).Analyze());
                w.setvalue( F, E ); 
            }
        } catch( Exception e ) {
            throw new Error( "Alarm!" );
        }
        return w;
    }
    ////////////////////////////////////////////////////////////
    public static void main( String[] args ) throws Exception {
        Walue walue = new Walue( new Parser());
        out.println( walue.ewal( "45(1:1),30(2:4)" ));
    }
}
//////////////////////////////////////////////////////////////
// log:
