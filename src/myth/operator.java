////////////////////////////////////////////////////////////////
package myth;
////////////////////////////////////////////////////////////////
import static java.lang.System.out;
////////////////////////////////////////////////////////////////
class Operator {
    static final int NO_SERVICES = 64;
    VM vm;
    interface Service {
        void exec( int adr, int fld );
    }
    // loading...
    void load( int adr, int fld, Word reg ){
        Word w = vm.memory[ adr ];
        int left = Word.L( fld );
        int ryte = Word.R( fld );
        int val = w.getfld( left, ryte );
        reg.setvalue( fld, val );
        reg.shiftryte( Word.BYTES - ryte );
    }
    void loadneg( int adr, int fld, Word reg ){
    	load( adr, fld, reg );
    	reg.sign = !reg.sign; // flip the switch
    }
    class LDA implements Service {
        public void exec( int adr, int fld ){
            load( adr, fld, vm.rA );
        }
    }
    class LDX implements Service {
        public void exec( int adr, int fld ){
            load( adr, fld, vm.rX );
        }
    }
    class LD1 implements Service {
        public void exec( int adr, int fld ){
            load( adr, fld, vm.rI[0] );
        }
    }
    class LD2 implements Service {
        public void exec( int adr, int fld ){
            load( adr, fld, vm.rI[1] );
        }
    }
    class LD3 implements Service {
        public void exec( int adr, int fld ){
            load( adr, fld, vm.rI[2] );
        }
    }
    class LD4 implements Service {
        public void exec( int adr, int fld ){
            load( adr, fld, vm.rI[3] );
        }
    }
    class LD5 implements Service {
        public void exec( int adr, int fld ){
            load( adr, fld, vm.rI[4] );
        }
    }
    class LD6 implements Service {
        public void exec( int adr, int fld ){
            load( adr, fld, vm.rI[5] );
        }
    }
    class LDAN implements Service {
        public void exec( int adr, int fld ){
            loadneg( adr, fld, vm.rA );
        }
    }
    class LDXN implements Service {
        public void exec( int adr, int fld ){
            loadneg( adr, fld, vm.rX );
        }
    }
    class LD1N implements Service {
        public void exec( int adr, int fld ){
            loadneg( adr, fld, vm.rI[0] );
        }
    }
    class LD2N implements Service {
        public void exec( int adr, int fld ){
            loadneg( adr, fld, vm.rI[1] );
        }
    }
    class LD3N implements Service {
        public void exec( int adr, int fld ){
            loadneg( adr, fld, vm.rI[2] );
        }
    }
    class LD4N implements Service {
        public void exec( int adr, int fld ){
            loadneg( adr, fld, vm.rI[3] );
        }
    }
    class LD5N implements Service {
        public void exec( int adr, int fld ){
            loadneg( adr, fld, vm.rI[4] );
        }
    }
    class LD6N implements Service {
        public void exec( int adr, int fld ){
            loadneg( adr, fld, vm.rI[5] );
        }
    }
    //////////////////////////////////////////////////// storing
    // This operation is a bit weird if register reg is
    // - 0 1 2 3 4, than store with field (0:2) will update the
    // word at the given adr as - 3 4 ? ? ?
    void store( int adr, int fld, Word reg ){
        // Check if sign is in the field.
        int left = Word.L( fld );
        boolean storeSign = false;
        if( left == 0 ){
            storeSign = true;
            left = 1;
        }
        // Take ryte shifted field value.
        left += Word.BYTES - Word.R( fld );
        int value = reg.getfld( left, Word.BYTES );
        if( storeSign ){
            if( reg.sign ) value = -value;
        }
        // Get ref to memory address.
        Word w = vm.memory[ adr ];
        // Finaly store that dude's value.
        w.setvalue( fld, value );
    }
    class STA implements Service {
        public void exec( int adr, int fld ){
            store( adr, fld, vm.rA );
        }
    }
    class STX implements Service {
        public void exec( int adr, int fld ){
            store( adr, fld, vm.rX );
        }
    }
    class ST1 implements Service {
        public void exec( int adr, int fld ){
            store( adr, fld, vm.rI[0] );
        }
    }
    class ST2 implements Service {
        public void exec( int adr, int fld ){
            store( adr, fld, vm.rI[1] );
        }
    }
    class ST3 implements Service {
        public void exec( int adr, int fld ){
            store( adr, fld, vm.rI[2] );
        }
    }
    class ST4 implements Service {
        public void exec( int adr, int fld ){
            store( adr, fld, vm.rI[3] );
        }
    }
    class ST5 implements Service {
        public void exec( int adr, int fld ){
            store( adr, fld, vm.rI[4] );
        }
    }
    class ST6 implements Service {
        public void exec( int adr, int fld ){
            store( adr, fld, vm.rI[5] );
        }
    }
    class STJ implements Service {
        public void exec( int adr, int fld ){
            store( adr, fld, vm.rJ );
        }
    }
    class STZ implements Service {
        public void exec( int adr, int fld ){
            store( adr, 5, new Word( false, 0 ));
        }
    }
    ////////////////////////////////////////////////////////////
    class ADD implements Service {
        public void exec( int adr, int fld ){
            Word w = new Word( false, 0 );
            load( adr, fld, w );
            //
            try {
                vm.rA.inc( w );
            } catch( Exception e ){
                vm.overflowToggle = true;
            }
        }
    }
    class SUB implements Service {
        public void exec( int adr, int fld ){
            Word w = new Word( false, 0 );
            load( adr, fld, w );
            w.sign = !w.sign; // flip the switch
            try {
                vm.rA.inc( w );
            } catch( Exception e ){
                vm.overflowToggle = true;
            }
        }
    }
    // rAX, both signs
    class MUL implements Service {
        public void exec( int adr, int fld ){
            Word w = new Word( false, 0 );
            load( adr, fld, w );
            try {
                vm.rA.mul( w );
            } catch( Exception e ){
                vm.overflowToggle = true;
            }
            // copy first 30 bits to rX, copy sign as well.
            vm.rX.bufr = vm.rA.bufr & 0x000000003fffffffL;
            vm.rX.sign = vm.rA.sign;
            // shift ryte rA
            vm.rA.shiftryte( 5 );
        }
    }
    // --------------------------------------------------------- ___
    // Here I'm differ from the spec, after thus in rA is the fl oör
    // of the division and in rX is the mod with their respectiv è__
    // signs.                                                    ___
    // --------------------------------------------------------- ___
    class DIV implements Service {
        public void exec( int adr, int fld ){
            Word w = new Word( false, 0 );
            load( adr, fld, w );
            // first of ll put rAX into rA buffer
            vm.rA.shiftleft( 5 ); // fai
            vm.rA.bufr |= vm.rX.bufr;
            // Ok, ??!
            try {
                int mod = (int) vm.rA.div( w );
                vm.rX.setvalue( 5, mod ); // fäj
            } catch( Exception e ){
                // log: leave rAX as it is for now
                vm.overflowToggle = true;
            }
        }
    }
    ////////////////////////////////////////////////////////////
    Service serv[] = new Service[ NO_SERVICES ];
    Operator( VM vm ){
        this.vm = vm;
        serv[  8 ] = new LDA();
        serv[ 15 ] = new LDX();
        serv[  9 ] = new LD1();
        serv[ 10 ] = new LD2();
        serv[ 11 ] = new LD3();
        serv[ 12 ] = new LD4();
        serv[ 13 ] = new LD5();
        serv[ 14 ] = new LD6();
        serv[ 16 ] = new LDAN();
        serv[ 23 ] = new LDXN();
        serv[ 17 ] = new LD1N();
        serv[ 18 ] = new LD2N();
        serv[ 19 ] = new LD3N();
        serv[ 20 ] = new LD4N();
        serv[ 21 ] = new LD5N();
        serv[ 22 ] = new LD6N();
        serv[ 24 ] = new STA();
        serv[ 31 ] = new STX();
        serv[ 25 ] = new ST1();
        serv[ 26 ] = new ST2();
        serv[ 27 ] = new ST3();
        serv[ 28 ] = new ST4();
        serv[ 29 ] = new ST5();
        serv[ 30 ] = new ST6();
        serv[ 32 ] = new STJ();
        serv[ 33 ] = new STZ();
        serv[  1 ] = new ADD();
        serv[  2 ] = new SUB();
        serv[  3 ] = new MUL();
        serv[  4 ] = new DIV();
    }
    void exec( int adr, int fld, int code ){
        serv[ code ].exec( adr, fld );
    }
    void exec( Word w ){
        var insWord = new InsWord( w );
        int i = insWord.idx;
        int adr = insWord.adr + vm.rI[i].getfld( 0, 5 );
        int fld = insWord.fld;
        int code = insWord.cde;
        exec( adr, fld, code );
    }
    public static void main( String[] args ){
        var vm = new VM();
        var op = new Operator( vm );
        // testing...
        int adr = 5;
        Word w = op.vm.memory[ adr ];
        //
        w.setvalue( Word.F( 0, 5 ), 7 );
        w.sign = true;
        op.vm.dumpMemory( 0, 10 );
        //
        op.vm.rA.bufr = 0;
        op.vm.rA.sign = false;
        op.vm.rX.bufr = 200;
        op.vm.rX.sign = false;
        out.println( op.vm.rA );
        out.println( op.vm.rX );
        //
        op.exec( adr, Word.F( 0, 5 ), 4 );
        out.println( op.vm.rA );
        out.println( op.vm.rX );
        //
    }
}
////////////////////////////////////////////////////////////////
// log:
