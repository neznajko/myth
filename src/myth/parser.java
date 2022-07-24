////////////////////////////////////////////////////////////////
package myth;
////////////////////////////////////////////////////////////////
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
////////////////////////////////////////////////////////////////
import static java.lang.System.out;
////////////////////////////////////////////////////////////////
// All evaluations are within Parser because of symbol table and
// program counter, not to mention the lab arrays, zo all fields
// for that reason are strings. 
class Address {
    // a,i(f)
    String a = "0",
           i =  "",
           f =  "";
    Address( String a, String i, String f ){
        this.a = a;
        this.i = i;
        this.f = f;
    }
    Address( String adr ){
        if( adr.isEmpty() )return;
        int p = adr.indexOf( '(' );
        if( p > -1 ) { // found
            int q = adr.length() - 1;
            if( adr.charAt( q ) != ')' ){
                throw new Error( "boom" );
            }
            f = adr.substring( p + 1, q );
        } else {
            p = adr.length();
        }
        int c = adr.indexOf( ',' );
        if( c > -1 ){ // checked
            i = adr.substring( c + 1, p );
        } else {
            c = p;
        }
        a = adr.substring( 0, c );
    }
    @Override
    public String toString() {
        return "[" + a + "," + i + "(" + f + ")]";
    }
    public static void main( String args[]) {
        var adr = new Address( args[0] );
        out.println( adr );
    }
}
////////////////////////////////////////////////////////////////
// After Parser's first pas create list of Snapshots with Future
// references or Literals for second re-evaluation.
class Snapshot {
    int     pc;
    String  op;
    Address adr;
    boolean futureRef; // if false a-part is literal
    Snapshot( int pc, String op, Address adr, boolean futureRef ){
        this.pc        = pc;
        this.op        = op;
        this.adr      = adr;
        this.futureRef = futureRef;
    }
    @Override
    public String toString() {
        return pc + ":" + op + "," + adr.toString() + "|" + futureRef;
    }
    public static void main( String args[] ){
        out.println( new Snapshot( 12,
                                   "ADD",
                                   new Address( "3,5(1)" ),
                                   false ));
    }
}
//```` | {@@|  :@@;  }@@             | _ _ | _ _ _ _ _ _ _ _ _ _
class Parser { /////////////////////////////////////////////////
    // Number of local symbol labels.
    static final int DIGITS = 10;
    // Label's type.
    enum Lab {
        NOPE, // YEEA
        BKWD, // 3B
        HERE, // 1H
        FRWD, // 2F
    };
    // Label to program counter error handling pair.
    static final Pair<Lab,Integer> LABERR = new Pair<>( Lab.NOPE, -1 );
    static HashMap<Character,Integer> charmap;
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
    }
    // This is the famous Symbol Table.
    HashMap<String,Integer> tab;
    // The program counter.
    int pc; 
    // Labels( local symbols ) stacks.
    @SuppressWarnings("unchecked")
    ArrayList<Integer>[] lab = new ArrayList[ DIGITS ];
    ArrayList<Snapshot> timeshift;
    VM vm; // Virtual Machine
    // Literal Constant to Address mapping
    HashMap<String,String> litab;
    ////////////////////////////////////////////////////////////
    // Cons
    Parser(){
        tab = new HashMap<>();
        pc = 0;
        for( int j = 0; j < DIGITS; j++ ){
            lab[ j ] = new ArrayList<>();
        }
        timeshift = new ArrayList<>();
        vm = new VM();
        litab = new HashMap<>();
    }
    ////////////////////////////////////////////////////////////
    // Check if token tok is in the form dB, dH, or dF,
    // where d is 0-9 digit.
    static Pair<Lab,Integer> checkLab( String tok ){
        if( tok.length() != 2 ||
            false == Espresso.estDigit( tok.charAt( 0 ))){
            return LABERR;
        }
        int d = tok.charAt( 0 ) - '0';
        switch( tok.charAt( 1 )){
            case 'B': return new Pair<>( Lab.BKWD, d );
            case 'H': return new Pair<>( Lab.HERE, d );
            case 'F': return new Pair<>( Lab.FRWD, d );
        }
        return LABERR;
    }
    ////////////////////////////////////////////////////////////
    // Check the correctness of the LOC field. On error return 
    // -1, if label return its number(0-9), otherwise return 10.
    static int checkLoc( String loc ){
        var espresso = Espresso.Analyze( loc );
        if( espresso.size() != 1 ) return -1;
        var tok = espresso.get( 0 );
        if( tok.key != Token.Type.SAMBO ) return -1;
        // Check if local symbol, dH, etc.
        var p = checkLab( tok.value );
        if( p.x == Lab.HERE ){
            return p.y;
        } else if( p.x == Lab.NOPE ){
            return DIGITS;
        }
        return -1;
    }
    ////////////////////////////////////////////////////////////
    // Print for debugging and stuff.
    @Override
    public String toString() {
        var b = new StringBuilder();
        b.append( "tab: " + tab.toString() );
        b.append( "\npc: " + pc );
        for( int j = 0; j < DIGITS; j++ ){
            b.append( "\n" + j + ": " );
            b.append( Arrays.toString( lab[ j ].toArray() ));
        }
        b.append( "\ntimeshift: " +
                  Arrays.toString( timeshift.toArray() )); 
        return b.toString();
    }
    ////////////////////////////////////////////////////////////
    int getTokenValue( Token tok ) throws Exception {
        int val;
        if( tok.key == Token.Type.OPERA ){ //              OPERA
            throw new Error( tok.toString());
        } else if( tok.key == Token.Type.NAMBA ){ //       NAMBA
            val = Integer.parseInt( tok.value );
        } else if( tok.key == Token.Type.ASTER ){ //       ASTER
            val = pc;
        } else {
            // Check vhether it's backward local symbol.
            final var ck = checkLab( tok.value );
            if( ck.x == Lab.BKWD ){ // tok.value: 2B
                final int n = ck.y; // n: 2
                final int back = lab[ n ].size() - 1;
                final int mostRecent = lab[ n ].get( back );
                if( pc == mostRecent ){
                    throw new Error( "Not backward compatible!" );
                }
                val = mostRecent;
            } else {
                if(! tab.containsKey( tok.value )){ // SAMBO
                    // Ok Future Reference / Literal Constant
                    throw new Exception( tok.toString());
                }
                val = tab.get( tok.value );
            }
        }
        return val;
    }
    ////////////////////////////////////////////////////////////
    // coffee:
    // <opera,-><namba,5><opera,+><aster,*><opera,-><sambo,2B>
    int eval( ArrayList<Token> coffee ) throws Exception {
        // Get first the sign if present, and then othr operatrs
        // the evaluation is straight from left to right e.g.:
        // -8+1/2+3-2 evaluates to -7/2+3-2 = -3+3-2 = 0-2 = -2
        boolean sign = false;
        Token tok = coffee.get( 0 );
        int n = coffee.size();
        int j = 0; 
        if( tok.key == Token.Type.OPERA ){
            if( tok.value.equals( "-" )){
                sign = true;
                j = 1;
            } else if( tok.value.equals( "+" )){
                j = 1;
            } else {
                throw new Error( tok.toString());
            }
        }
        // check here coffee's length
        // a) no sign
        // a+b-c/d
        // 0123456: 6 - 0 + 1 = 7 is odd
        // b) sign
        // -3/5*7-*: 7 - 1 + 1 = 7 is not even!
        // 01234567
        if( 0 == (( n - j ) & 1 )){ // Check the odd bit
            throw new Error( "Expression!" );
        }
        // initialize return value
        tok = coffee.get( j );
        int rv = getTokenValue( tok );
        if( sign ) rv = -rv;
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
                rv -= val;
            } else if( op.equals( "+" )){
                rv += val;
            } else if( op.equals( "*" )){
                rv *= val;
            } else if( op.equals( "/" )){
                rv /= val;
            } else if( op.equals( "//" )){
                Word w = new Word( rv < 0, rv );
                w.shiftleft( Word.BYTES );
                w.div( val );
                rv = (int) w.bufr;
                if( w.sign ) rv = -rv;
            } else if( op.equals( ":" )){
                rv *= 8;
                rv += val;
            } else {
                throw new Error( ";)" );
            }
        }
        return rv;
    }
    ////////////////////////////////////////////////////////////
    static boolean checkField( int F ){
        if( F < 0 )return false; 
        // F = 8L + R
        int L = F/8;
        int R = F%8;
        if( L > 5 )return false;
        if( R > 5 )return false;
        return true;
    }
    ////////////////////////////////////////////////////////////
    Word evalWalue( String walue ){
        Word w = new Word( false, 0 );
        try {
            for( final var walueComp: Word.walueSplit( walue )){
                // Here walueComp is an <E,F> pair.
                int F = eval( Espresso.Analyze( walueComp.y ));
                if(! checkField( F )){
                    throw new Error( "Not valid field" );
                }
                int E = eval( Espresso.Analyze( walueComp.x ));
                w.setvalue( F, E ); 
            }
        } catch (Exception e ) {
            throw new Error( "Alarm!" );
        }
        return w;
    }
    ////////////////////////////////////////////////////////////
    String isLiteral( String a ){
        final int n = a.length();
        if( a.charAt(0)     == '=' &&
            a.charAt(n - 1) == '=' ){
            String e = a.substring( 1, n - 1 ); // discard ='s
            if( e.length() >= DIGITS ){
                throw new Error( "long long long is too long for gcc" );
            }
            Word w = evalWalue( e );
            int val = w.getfld( 0, Word.BYTES );
            return String.valueOf( val );
        }
        return ""; // nope
    }
    ////////////////////////////////////////////////////////////
    // Pass a-field string, obtained from Address constructor,
    // try running eval on it, if there is an exception, check
    // if there is only one token. In such a case this is a 
    // future reference, return MAX_VALUE to indicate positive
    // result, otherwise return the expression's evaluation.
    ////////////////////////////////////////////////////////////
    int isFutureRef( String a ){
        if( a.isEmpty()) return 0;
        final var coffee = Espresso.Analyze( a );
        int val = Integer.MAX_VALUE;
        try {
            val = eval( coffee );
        } catch ( Exception e ){
            if( coffee.size() > 1 ){
                throw new Error( "Future Reference" );
            }
        }
        return val;
    }
    ////////////////////////////////////////////////////////////
    // called by firstPass
    void asm( String op, final Address adr ) throws Exception {
        // check if a-Part is Literal
        String a = isLiteral( adr.a );
        if(! a.isEmpty()) { // literal constant
            Address adr1 = new Address( a, adr.i, adr.f );
            timeshift.add( new Snapshot( pc, op, adr1, false ));
            return;
        }
        // evaluate a-Part
        int aval = isFutureRef( adr.a );
        if( aval == Integer.MAX_VALUE ){
            // Future refernce
            timeshift.add( new Snapshot( pc, op, adr, true ));
            return;
        }
        ////////////////////////////////////////////////////////        
        // evaluate i,f-parts
        int ival = 0;
        if(! adr.i.isEmpty() ){
            ival = eval( Espresso.Analyze( adr.i ));
        }
        final var instr = Instruction.map.get( op );
        int code = instr.x;
        int fld  = instr.y; // default
        if(! adr.f.isEmpty() ){
            fld = eval( Espresso.Analyze( adr.f ));
        }
        Word w = vm.memory[ pc ];
        w.setvalue( Word.INSTR_ADR, aval ); // 0:2
        w.setvalue( Word.INSTR_IDX, ival ); // 3:3
        w.setvalue( Word.INSTR_FLD, fld  ); // 4:4
        w.setvalue( Word.INSTR_OPC, code ); // 5:5
    }
    ////////////////////////////////////////////////////////////
    // Assembly involving literal constant
    void asmli( Snapshot cheese ){
        var adr = cheese.adr;
        if( litab.containsKey( adr.a )){
            adr.a = litab.get( adr.a );
        } else {
            Word w = vm.memory[ vm.end ];
            w.setvalue( 5, Integer.valueOf( adr.a ));
            String a = String.valueOf( vm.end++ );
            litab.put( adr.a, a );
            adr.a = a;
        }
        // assemble the instruction at cheese.pc here
        int backup = pc;
        pc = cheese.pc;
        try {
            asm( cheese.op, adr );
        } catch( Exception e ){
            throw new Error( "Literal Assembly" );
        }
        pc = backup;
    }
    ////////////////////////////////////////////////////////////
    void encode( String alf ){
        final int n = alf.length();
        if( n > Word.BYTES ){
            throw new Error( "encode" );
        }
        Word w = new Word( false, 0 );
        for( int j = 0; j < n; ++j ){
            w.setvalue( Word.F( j + 1, j + 1 ),
                        charmap.get( alf.charAt( j )));
        }
        vm.memory[ pc ] = w;
    }
    ////////////////////////////////////////////////////////////
    // log: 0
    void firstPass( String fileName ) throws Exception {
        Scanner sc = new Scanner( fileName );
        Word w;
        while( true ){
            Fields line = sc.getnext();
            if( line == null ) break; // EOF
            out.print( pc + ": " + line );
            // LOC
            if( line.loc.isEmpty() == false ){
                int d = checkLoc( line.loc );
                if( d == -1 ){
                    throw new Error( "LOC" );
                } else if( d == DIGITS ){
                    tab.put( line.loc, pc );
                } else {
                    lab[ d ].add( pc );
                }
            }
            // OP
            if( line.op.isEmpty() == false ){
                switch( checkOp( line.op )) {
                case MIX:
                    asm( line.op, new Address( line.adr ));
                    break;
                case EQU:
                    w = evalWalue( line.adr );
                    tab.put( line.loc, w.getfld( 0, Word.BYTES ));
                    pc--;
                    break;
                case ORIG:
                    w = evalWalue( line.adr );
                    pc = w.getfld( 0, Word.BYTES ) - 1;
                    break;
                case CON:
                    w = evalWalue( line.adr );
                    vm.memory[ pc ] = w;
                    break;
                case ALF:
                    // log: allow spaces to appear in line.adr
                    encode( line.adr );
                    break;
                case END:
                    vm.end = pc;
                    break;
                case NOPE:
                    throw new Error( "8/" );
                default:
                    break;  
                }
            }
            pc++;
        }
    }
    ////////////////////////////////////////////////////////////
    enum Op {
        NOPE, // Error 
        MIX,  // MIX Instruction
        EQU,  // mixal pseudo instructions
        ORIG, //
        CON,  //
        ALF,  //
        END,  //
    };
    ////////////////////////////////////////////////////////////
    static Op checkOp( String op ){
        if( Instruction.map.containsKey( op )){
            return Op.MIX;
        }
        if( op.equals( "EQU" )){
            return Op.EQU;
        }
        if( op.equals( "ORIG" )){
            return Op.ORIG;
        }
        if( op.equals( "CON" )){
            return Op.CON;
        }
        if( op.equals( "ALF" )){
            return Op.ALF;
        }
        if( op.equals( "END" )){
            return Op.END;
        }
        return Op.NOPE;
    }
////////////////////////////////////////////////////////////////
    void asmfr( Snapshot snapshot ){
        Address adr = snapshot.adr;
        String a = adr.a;
        final var p = checkLab( a );
        if( p == LABERR ){
            // check if is in the table
            if( tab.containsKey( a ) == false ){
                // replace with 0
                adr.a = "0";
            }
        } else {
            int d = p.y;
            final var thelab = lab[ d ];
            // Ok no need for binary search, thelab should be
            // sorted.
            int m = -1; // first number in thelab bigger than
                        // snapshot.pc
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
        }
        int backup = pc;
        pc = snapshot.pc;
        try {
            asm( snapshot.op, adr );
        } catch( Exception e ){
            throw new Error( "Future Reference Assembly" );
        }
        pc = backup;
    }
////////////////////////////////////////////////////////////////
    void secondPass() {
    // Figure _out first _the literal constants. They must be in
    // the timeshift array flagged as false, use asmli on them.
        // Ok sort all labels here
        for( int j = 0; j < DIGITS; j++ ){
            Collections.sort( lab[ j ]);
        }
        for( final Snapshot snapshot: timeshift ) {
            if( snapshot.futureRef == false ) {
                asmli( snapshot );
            } else {
                asmfr( snapshot );
            }
        }
    }
////////////////////////////////////////////////////////////////
    public static void main( String[] args ) throws Exception {
        Parser parser = new Parser();
        if( false ){
        } else {
            parser.firstPass( "./src.mixal" );
            out.println( parser );
            parser.vm.dumpMemory( 0, 10 );
            parser.secondPass();
            parser.vm.dumpMemory( 0, 10 );
        }
    }
}
////////////////////////////////////////////////////////////////
// log: - Why not create class Walue?
//      - figure evalWalue( "-200/5(2:4)" ) case
//      - =200/5(2:4)=,2(0:3) will cause problems in Address
//        constructor, that means isLiteral, and possibly
//        isFutureRef should be called in the Address cons.
//      - make fst, snd pass, debug a little and push to git
//      - it's not very clear to me if local labels are chosen
//        with respect of program counter or source line?
