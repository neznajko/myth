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
    void load( int adr, int fld, Word reg ) {
        Word w = vm.memory[adr];
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
        Word reg = op.vm.rA;
        reg.setvalue( Word.F( 0, 5 ), -500 );
        out.println( reg );
        op.exec( 7, Word.F( 0, 2 ), 24 );
        op.vm.dumpMemory( 5, 10 );
        op.exec( 7, 0, 33 );
        op.vm.dumpMemory( 5, 10 );
        //
    }
}
////////////////////////////////////////////////////////////////
