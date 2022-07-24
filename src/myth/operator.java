////////////////////////////////////////////////////////////////
package myth;
////////////////////////////////////////////////////////////////
import static java.lang.System.out;
////////////////////////////////////////////////////////////////
class Operator {
    VM vm;
    interface Service {
        void exec( int adr, int fld );
    }
    class LDA implements Service {
        public void exec( int adr, int fld ) {
            Word w = vm.memory[adr];
            int left = Word.L( fld );
            int ryte = Word.R( fld );
            int val = w.getfld( left, ryte );
            vm.rA.setvalue( fld, val );
            // right shift
            vm.rA.shiftryte( Word.BYTES - ryte );
        }
    }
    Service serv[] = new Service[ 1 ];
    Operator( VM vm ){
        this.vm = vm;
        serv[0] = new LDA();
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
        Word w = vm.memory[0];
        w.setvalue( 2, -10 );
        op.vm.dumpMemory( 0, 10 );
        op.exec( 0, 2, 0 );
        out.println( op.vm.rA );
    }
}
////////////////////////////////////////////////////////////////
