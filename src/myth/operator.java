////////////////////////////////////////////////////////////////
package myth;
////////////////////////////////////////////////////////////////
import static java.lang.System.out;
import static java.util.Arrays.asList;
////////////////////////////////////////////////////////////////
import java.util.ArrayList;
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
    void enta( int adr, Word reg ){
        reg.setvalue( 5, adr );
    }
    void entaneg( int adr, Word reg ){
    	enta( -adr, reg ); // switch the flip
    }
    class ENTA implements Service {
        public void exec( int adr, int fld ){
            enta( adr, vm.rA );
        }
    }
    class ENTX implements Service {
        public void exec( int adr, int fld ){
            enta( adr, vm.rX );
        }
    }
    class ENT1 implements Service {
        public void exec( int adr, int fld ){
            enta( adr, vm.rI[0] );
        }
    }
    class ENT2 implements Service {
        public void exec( int adr, int fld ){
            enta( adr, vm.rI[1] );
        }
    }
    class ENT3 implements Service {
        public void exec( int adr, int fld ){
            enta( adr, vm.rI[2] );
        }
    }
    class ENT4 implements Service {
        public void exec( int adr, int fld ){
            enta( adr, vm.rI[3] );
        }
    }
    class ENT5 implements Service {
        public void exec( int adr, int fld ){
            enta( adr, vm.rI[4] );
        }
    }
    class ENT6 implements Service {
        public void exec( int adr, int fld ){
            enta( adr, vm.rI[5] );
        }
    }
    ////////////////////////////////////////////////////////____
    //                                                      ENNA
    class ENNA implements Service {
        public void exec( int adr, int fld ){
            enta( -adr, vm.rA );
        }
    }
    class ENNX implements Service {
        public void exec( int adr, int fld ){
            enta( -adr, vm.rX );
        }
    }
    class ENN1 implements Service {
        public void exec( int adr, int fld ){
            enta( -adr, vm.rI[0] );
        }
    }
    class ENN2 implements Service {
        public void exec( int adr, int fld ){
            enta( -adr, vm.rI[1] );
        }
    }
    class ENN3 implements Service {
        public void exec( int adr, int fld ){
            enta( -adr, vm.rI[2] );
        }
    }
    class ENN4 implements Service {
        public void exec( int adr, int fld ){
            enta( -adr, vm.rI[3] );
        }
    }
    class ENN5 implements Service {
        public void exec( int adr, int fld ){
            enta( -adr, vm.rI[4] );
        }
    }
    class ENN6 implements Service {
        public void exec( int adr, int fld ){
            enta( -adr, vm.rI[5] );
        }
    }
    // INCA
////////////////////////////////////////////////////////////////
    class INCA implements Service {
        public void exec( int adr, int fld ){
            try {
                vm.rA.inc( new Word( adr ));
            } catch( Exception e ){
                vm.overflowToggle = true;
            }
        }
    }
    class INCX implements Service {
        public void exec( int adr, int fld ){
            try {
                vm.rX.inc( new Word( adr ));
            } catch( Exception e ){
                vm.overflowToggle = true;
            }
        }
    }
    class INC1 implements Service {
        public void exec( int adr, int fld ){
            try {
                vm.rI[0].inc( new Word( adr ));
            } catch( Exception e ){} // No overflow 
        }
    }
    class INC2 implements Service {
        public void exec( int adr, int fld ){
            try {
                vm.rI[1].inc( new Word( adr ));
            } catch( Exception e ){}
        }
    }
    class INC3 implements Service {
        public void exec( int adr, int fld ){
            try {
                vm.rI[2].inc( new Word( adr ));
            } catch( Exception e ){}
        }
    }
    class INC4 implements Service {
        public void exec( int adr, int fld ){
            try {
                vm.rI[3].inc( new Word( adr ));
            } catch( Exception e ){}
        }
    }
    class INC5 implements Service {
        public void exec( int adr, int fld ){
            try {
                vm.rI[4].inc( new Word( adr ));
            } catch( Exception e ){}
        }
    }
    class INC6 implements Service {
        public void exec( int adr, int fld ){
            try {
                vm.rI[5].inc( new Word( adr ));
            } catch( Exception e ){}
        }
    }
    ////////////////////////////////////////////////////////////
    //                                                      DECX
    class DECA implements Service {
        public void exec( int adr, int fld ){
            try {
                vm.rA.inc( new Word( -adr ));
            } catch( Exception e ){
                vm.overflowToggle = true;
            }
        }
    }
    class DECX implements Service {
        public void exec( int adr, int fld ){
            try {
                vm.rX.inc( new Word( -adr ));
            } catch( Exception e ){
                vm.overflowToggle = true;
            }
        }
    }
    class DEC1 implements Service {
        public void exec( int adr, int fld ){
            try {
                vm.rI[0].inc( new Word( -adr ));
            } catch( Exception e ){}
        }
    }
    class DEC2 implements Service {
        public void exec( int adr, int fld ){
            try {
                vm.rI[1].inc( new Word( -adr ));
            } catch( Exception e ){}
        }
    }
    class DEC3 implements Service {
        public void exec( int adr, int fld ){
            try {
                vm.rI[2].inc( new Word( -adr ));
            } catch( Exception e ){}
        }
    }
    class DEC4 implements Service {
        public void exec( int adr, int fld ){
            try {
                vm.rI[3].inc( new Word( -adr ));
            } catch( Exception e ){}
        }
    }
    class DEC5 implements Service {
        public void exec( int adr, int fld ){
            try {
                vm.rI[4].inc( new Word( -adr ));
            } catch( Exception e ){}
        }
    }
    class DEC6 implements Service {
        public void exec( int adr, int fld ){
            try {
                vm.rI[5].inc( new Word( -adr ));
            } catch( Exception e ){}
        }
    }
    ////////////////////////////////////////////////////////////
    void comp( int adr, int fld, Word reg ) {
        Word w = new Word( 0 );
        load( adr, fld, w );
        int a = reg.getfld( Word.L( fld ), Word.R( fld ));
        int b = (int) w.getval();
        if( a < b ) {
            vm.comparizonIndykate = VM.LESS;
        } else if ( a == b ) {
            vm.comparizonIndykate = VM.EQUAL;
        } else {
            vm.comparizonIndykate = VM.GREATER;
        }            
    }
    class CMPA implements Service {
        public void exec( int adr, int fld ){
            comp( adr, fld, vm.rA );
        }
    }
    class CMPX implements Service {
        public void exec( int adr, int fld ){
            comp( adr, fld, vm.rX );
        }
    }
    class CMP1 implements Service {
        public void exec( int adr, int fld ){
            comp( adr, fld, vm.rI[0] );
        }
    }
    class CMP2 implements Service {
        public void exec( int adr, int fld ){
            comp( adr, fld, vm.rI[1] );
        }
    }
    class CMP3 implements Service {
        public void exec( int adr, int fld ){
            comp( adr, fld, vm.rI[2] );
        }
    }
    class CMP4 implements Service {
        public void exec( int adr, int fld ){
            comp( adr, fld, vm.rI[3] );
        }
    }
    class CMP5 implements Service {
        public void exec( int adr, int fld ){
            comp( adr, fld, vm.rI[4] );
        }
    }
    class CMP6 implements Service {
        public void exec( int adr, int fld ){
            comp( adr, fld, vm.rI[5] );
        }
    }
    ////////////////////////////////////////////////////////////
    // To continue program execution ONE SHOULD RE-ESTABLISH the
    // program counter pc from the jump register rJ when exiting
    // from a function, and jump to it.
    void backup() {
        vm.rJ.setvalue( Word.F( 0, 2 ), vm.pc + 1 );
    }
    void setpc( int adr ){
        vm.pc = adr - 1; // why is that?
        // bcoz the cpu cycle will execute a jump and than
        // increment the pc.
    }
    void jump( int adr ){
        backup();
        setpc( adr );
    }
    void jump_nobackup( int adr ){
        setpc( adr );
    }
    class JMP implements Service {
        public void exec( int adr, int fld ){
            jump( adr );
        }
    }
    class JSJ implements Service { // First jump and than shoot!
        public void exec( int adr, int fld ){
            jump_nobackup( adr );
        }
    }
    class JOV implements Service {
        public void exec( int adr, int fld ){
            if( vm.overflowToggle ){
                vm.overflowToggle = false;
                jump( adr );
            }
        }
    }
    class JNOV implements Service {
        public void exec( int adr, int fld ){
            if( vm.overflowToggle ){
                vm.overflowToggle = false;
            } else {
                jump( adr );
            }
        }
    }
    class JL implements Service {
        public void exec( int adr, int fld ){
            if( vm.comparizonIndykate == VM.LESS ){
                jump( adr );
            }
        }
    }
    class JE implements Service {
        public void exec( int adr, int fld ){
            if( vm.comparizonIndykate == VM.EQUAL ){
                jump( adr );
            }
        }
    }
    class JG implements Service {
        public void exec( int adr, int fld ){
            if( vm.comparizonIndykate == VM.GREATER ){
                jump( adr );
            }
        }
    }
    class JGE implements Service {
        public void exec( int adr, int fld ){
            if( vm.comparizonIndykate == VM.GREATER ||
                vm.comparizonIndykate == VM.EQUAL ){
                jump( adr );
            }
        }
    }
    class JNE implements Service {
        public void exec( int adr, int fld ){
            if( vm.comparizonIndykate != VM.EQUAL ){
                jump( adr );
            }
        }
    }
    class JLE implements Service {
        public void exec( int adr, int fld ){
            if( vm.comparizonIndykate == VM.LESS ||
                vm.comparizonIndykate == VM.EQUAL ){
                jump( adr );
            }
        }
    }
    // JAN, JAZ, JAP, JANN, JANZ, JANP; C = 40, F = 0,1,2,3,4,5
    class JAN implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.sign && vm.rA.bufr > 0 ){
                jump( adr );
            }
        }
    }
    class JAZ implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.bufr == 0 ){
                jump( adr );
            }
        }
    }
    class JAP implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.sign == false && vm.rA.bufr > 0 ){
                jump( adr );
            }
        }
    }
    class JANN implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.sign == false || vm.rA.bufr == 0 ){
                jump( adr );
            }
        }
    }
    class JANZ implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.bufr != 0 ){
                jump( adr );
            }
        }
    }
    class JANP implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.sign || vm.rA.bufr == 0 ){
                jump( adr );
            }
        }
    }
    // JXN, JXZ, JXP, JXNN, JXNZ, JXNP; C = 47, F = 0,1,2,3,4,5
    class JXN implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.sign && vm.rA.bufr > 0 ){
                jump( adr );
            }
        }
    }
    class JXZ implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.bufr == 0 ){
                jump( adr );
            }
        }
    }
    class JXP implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.sign == false && vm.rA.bufr > 0 ){
                jump( adr );
            }
        }
    }
    class JXNN implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.sign == false || vm.rA.bufr == 0 ){
                jump( adr );
            }
        }
    }
    class JXNZ implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.bufr != 0 ){
                jump( adr );
            }
        }
    }
    class JXNP implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.sign || vm.rA.bufr == 0 ){
                jump( adr );
            }
        }
    }
    // J1N, J1Z, J1P, J1NN, J1NZ, J1NP; C = 41, F = 0,1,2,3,4,5
    class J1N implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.sign && vm.rA.bufr > 0 ){
                jump( adr );
            }
        }
    }
    class J1Z implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.bufr == 0 ){
                jump( adr );
            }
        }
    }
    class J1P implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.sign == false && vm.rA.bufr > 0 ){
                jump( adr );
            }
        }
    }
    class J1NN implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.sign == false || vm.rA.bufr == 0 ){
                jump( adr );
            }
        }
    }
    class J1NZ implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.bufr != 0 ){
                jump( adr );
            }
        }
    }
    class J1NP implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.sign || vm.rA.bufr == 0 ){
                jump( adr );
            }
        }
    }
    // J2N, J2Z, J2P, J2NN, J2NZ, J2NP; C = 42, F = 0,1,2,3,4,5
    class J2N implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.sign && vm.rA.bufr > 0 ){
                jump( adr );
            }
        }
    }
    class J2Z implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.bufr == 0 ){
                jump( adr );
            }
        }
    }
    class J2P implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.sign == false && vm.rA.bufr > 0 ){
                jump( adr );
            }
        }
    }
    class J2NN implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.sign == false || vm.rA.bufr == 0 ){
                jump( adr );
            }
        }
    }
    class J2NZ implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.bufr != 0 ){
                jump( adr );
            }
        }
    }
    class J2NP implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.sign || vm.rA.bufr == 0 ){
                jump( adr );
            }
        }
    }
    // J3N, J3Z, J3P, J3NN, J3NZ, J3NP; C = 43, F = 0,1,2,3,4,5
    class J3N implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.sign && vm.rA.bufr > 0 ){
                jump( adr );
            }
        }
    }
    class J3Z implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.bufr == 0 ){
                jump( adr );
            }
        }
    }
    class J3P implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.sign == false && vm.rA.bufr > 0 ){
                jump( adr );
            }
        }
    }
    class J3NN implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.sign == false || vm.rA.bufr == 0 ){
                jump( adr );
            }
        }
    }
    class J3NZ implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.bufr != 0 ){
                jump( adr );
            }
        }
    }
    class J3NP implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.sign || vm.rA.bufr == 0 ){
                jump( adr );
            }
        }
    }
    // J4N, J4Z, J4P, J4NN, J4NZ, J4NP; C = 44, F = 0,1,2,3,4,5
    class J4N implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.sign && vm.rA.bufr > 0 ){
                jump( adr );
            }
        }
    }
    class J4Z implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.bufr == 0 ){
                jump( adr );
            }
        }
    }
    class J4P implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.sign == false && vm.rA.bufr > 0 ){
                jump( adr );
            }
        }
    }
    class J4NN implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.sign == false || vm.rA.bufr == 0 ){
                jump( adr );
            }
        }
    }
    class J4NZ implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.bufr != 0 ){
                jump( adr );
            }
        }
    }
    class J4NP implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.sign || vm.rA.bufr == 0 ){
                jump( adr );
            }
        }
    }
    // J5N, J5Z, J5P, J5NN, J5NZ, J5NP; C = 45, F = 0,1,2,3,4,5
    class J5N implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.sign && vm.rA.bufr > 0 ){
                jump( adr );
            }
        }
    }
    class J5Z implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.bufr == 0 ){
                jump( adr );
            }
        }
    }
    class J5P implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.sign == false && vm.rA.bufr > 0 ){
                jump( adr );
            }
        }
    }
    class J5NN implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.sign == false || vm.rA.bufr == 0 ){
                jump( adr );
            }
        }
    }
    class J5NZ implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.bufr != 0 ){
                jump( adr );
            }
        }
    }
    class J5NP implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.sign || vm.rA.bufr == 0 ){
                jump( adr );
            }
        }
    }
    // J6N, J6Z, J6P, J6NN, J6NZ, J6NP; C = 46, F = 0,1,2,3,4,5
    class J6N implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.sign && vm.rA.bufr > 0 ){
                jump( adr );
            }
        }
    }
    class J6Z implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.bufr == 0 ){
                jump( adr );
            }
        }
    }
    class J6P implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.sign == false && vm.rA.bufr > 0 ){
                jump( adr );
            }
        }
    }
    class J6NN implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.sign == false || vm.rA.bufr == 0 ){
                jump( adr );
            }
        }
    }
    class J6NZ implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.bufr != 0 ){
                jump( adr );
            }
        }
    }
    class J6NP implements Service {
        public void exec( int adr, int fld ){
            if( vm.rA.sign || vm.rA.bufr == 0 ){
                jump( adr );
            }
        }
    }
    ////////////////////////////////////////////////////////////
    // Shift Operators: SLA, SRA, SLAX, SRAX, SLC, SRC
    // C = 6, F = 0, 1, 2, 3, 4, 5
    ////////////////////////////////////////////////////////////
    class SLA implements Service {
        public void exec( int adr, int fld ){
            vm.rA.shiftleft( adr );
        }
    }
    class SRA implements Service {
        public void exec( int adr, int fld ){
            vm.rA.shiftryte( adr );
        }
    }
    // make rX buffer extended rAX
    void preshift() {
        vm.rA.shiftleft( Word.BYTES );
        vm.rX.bufr |= vm.rA.bufr;
    }
    // re-establish
    void postshift() {
        vm.rA.bufr = vm.rX.bufr; // copy
        vm.rA.shiftryte( Word.BYTES ); // rA
        vm.rX.bufr &= Word.WORD_MASK;  // rX
    }
    class SLAX implements Service {
        public void exec( int adr, int fld ){
            preshift();
            vm.rX.shiftleft( adr );
            postshift();
        }
    }
    class SRAX implements Service {
        public void exec( int adr, int fld ){
            preshift();
            vm.rX.shiftryte( adr );
            postshift();
        }
    }
    class SLC implements Service {
        public void exec( int adr, int fld ){
            preshift();
            vm.rX.cycle( 10 - adr );
            postshift();
        }
    }
    class SRC implements Service {
        public void exec( int adr, int fld ){
            preshift();
            vm.rX.cycle( adr );
            postshift();
        }
    }
    ////////////////////////////////////////////////////////////
    // There are operations with same C and different F, one way
    // is to forget about the serv variable and use switch state-
    //                                                     .tnem-
    @SuppressWarnings("unchecked")
    ArrayList<Service> serv[] = new ArrayList[ NO_SERVICES ];
    Operator( VM vm ){
        this.vm = vm;
        serv[  8 ] = new ArrayList<>( asList( new LDA() ));
        serv[ 15 ] = new ArrayList<>( asList( new LDX() ));
        serv[  9 ] = new ArrayList<>( asList( new LD1() ));
        serv[ 10 ] = new ArrayList<>( asList( new LD2() ));
        serv[ 11 ] = new ArrayList<>( asList( new LD3() ));
        serv[ 12 ] = new ArrayList<>( asList( new LD4() ));
        serv[ 13 ] = new ArrayList<>( asList( new LD5() ));
        serv[ 14 ] = new ArrayList<>( asList( new LD6() ));
        serv[ 16 ] = new ArrayList<>( asList( new LDAN() ));
        serv[ 23 ] = new ArrayList<>( asList( new LDXN() ));
        serv[ 17 ] = new ArrayList<>( asList( new LD1N() ));
        serv[ 18 ] = new ArrayList<>( asList( new LD2N() ));
        serv[ 19 ] = new ArrayList<>( asList( new LD3N() ));
        serv[ 20 ] = new ArrayList<>( asList( new LD4N() ));
        serv[ 21 ] = new ArrayList<>( asList( new LD5N() ));
        serv[ 22 ] = new ArrayList<>( asList( new LD6N() ));
        serv[ 24 ] = new ArrayList<>( asList( new STA() ));
        serv[ 31 ] = new ArrayList<>( asList( new STX() ));
        serv[ 25 ] = new ArrayList<>( asList( new ST1() ));
        serv[ 26 ] = new ArrayList<>( asList( new ST2() ));
        serv[ 27 ] = new ArrayList<>( asList( new ST3() ));
        serv[ 28 ] = new ArrayList<>( asList( new ST4() ));
        serv[ 29 ] = new ArrayList<>( asList( new ST5() ));
        serv[ 30 ] = new ArrayList<>( asList( new ST6() ));
        serv[ 32 ] = new ArrayList<>( asList( new STJ() ));
        serv[ 33 ] = new ArrayList<>( asList( new STZ() ));
        serv[  1 ] = new ArrayList<>( asList( new ADD() ));
        serv[  2 ] = new ArrayList<>( asList( new SUB() ));
        serv[  3 ] = new ArrayList<>( asList( new MUL() ));
        serv[  4 ] = new ArrayList<>( asList( new DIV() ));
        serv[ 48 ] = new ArrayList<>( asList( new INCA(),
                                              new DECA(),
                                              new ENTA(),
                                              new ENNA() ));
        serv[ 55 ] = new ArrayList<>( asList( new INCX(),
                                              new DECX(),
                                              new ENTX(),
                                              new ENNX() ));
        serv[ 49 ] = new ArrayList<>( asList( new INC1(),
                                              new DEC1(),
                                              new ENT1(),
                                              new ENN1() ));
        serv[ 50 ] = new ArrayList<>( asList( new INC2(),
                                              new DEC2(),
                                              new ENT2(),
                                              new ENN2() ));
        serv[ 51 ] = new ArrayList<>( asList( new INC3(),
                                              new DEC3(),
                                              new ENT3(),
                                              new ENN3() ));
        serv[ 52 ] = new ArrayList<>( asList( new INC4(),
                                              new DEC4(),
                                              new ENT4(),
                                              new ENN4() ));
        serv[ 53 ] = new ArrayList<>( asList( new INC5(),
                                              new DEC5(),
                                              new ENT5(),
                                              new ENN5() ));
        serv[ 54 ] = new ArrayList<>( asList( new INC6(),
                                              new DEC6(),
                                              new ENT6(),
                                              new ENN6() ));
        serv[ 56 ] = new ArrayList<>( asList( new CMPA() ));
        serv[ 63 ] = new ArrayList<>( asList( new CMPX() ));
        serv[ 57 ] = new ArrayList<>( asList( new CMP1() ));
        serv[ 58 ] = new ArrayList<>( asList( new CMP2() ));
        serv[ 59 ] = new ArrayList<>( asList( new CMP3() ));
        serv[ 60 ] = new ArrayList<>( asList( new CMP4() ));
        serv[ 61 ] = new ArrayList<>( asList( new CMP5() ));
        serv[ 62 ] = new ArrayList<>( asList( new CMP6() ));
        serv[ 39 ] = new ArrayList<>( asList( new JMP(),
                                              new JSJ(),
                                              new JOV(),
                                              new JNOV(),
                                              new JL(),
                                              new JE(),
                                              new JG(),
                                              new JGE(),
                                              new JNE(),
                                              new JLE() ));
        serv[ 40 ] = new ArrayList<>( asList( new JAN(),
                                              new JAZ(),
                                              new JAP(),
                                              new JANN(),
                                              new JANZ(),
                                              new JANP() ));
        serv[ 47 ] = new ArrayList<>( asList( new JXN(),
                                              new JXZ(),
                                              new JXP(),
                                              new JXNN(),
                                              new JXNZ(),
                                              new JXNP() ));
        serv[ 41 ] = new ArrayList<>( asList( new J1N(),
                                              new J1Z(),
                                              new J1P(),
                                              new J1NN(),
                                              new J1NZ(),
                                              new J1NP() ));
        serv[ 42 ] = new ArrayList<>( asList( new J2N(),
                                              new J2Z(),
                                              new J2P(),
                                              new J2NN(),
                                              new J2NZ(),
                                              new J2NP() ));
        serv[ 43 ] = new ArrayList<>( asList( new J3N(),
                                              new J3Z(),
                                              new J3P(),
                                              new J3NN(),
                                              new J3NZ(),
                                              new J3NP() ));
        serv[ 44 ] = new ArrayList<>( asList( new J4N(),
                                              new J4Z(),
                                              new J4P(),
                                              new J4NN(),
                                              new J4NZ(),
                                              new J4NP() ));
        serv[ 45 ] = new ArrayList<>( asList( new J5N(),
                                              new J5Z(),
                                              new J5P(),
                                              new J5NN(),
                                              new J5NZ(),
                                              new J5NP() ));
        serv[ 46 ] = new ArrayList<>( asList( new J6N(),
                                              new J6Z(),
                                              new J6P(),
                                              new J6NN(),
                                              new J6NZ(),
                                              new J6NP() ));
        // Lots of Copy and Paste!
        serv[ 6 ] = new ArrayList<>( asList( new SLA(),
                                             new SRA(),
                                             new SLAX(),
                                             new SRAX(),
                                             new SLC(),
                                             new SRC() ));
    }
    void exec( int adr, int fld, int code ){
        if( serv[ code ].size() == 1 ){
            serv[ code ].get( 0 ).exec( adr, fld );
        } else {
            serv[ code ].get( fld ).exec( adr, fld );
        }
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
        int adr = 1;
        int fld = 4;
        int code = 6;
        //
        vm.rA.setvalue( Word.F( 0, 5 ), 13 );
        vm.rX.setvalue( Word.F( 0, 5 ), -24 );
        vm.rX.setvalue( Word.F( 1, 1 ), 35 );
        out.println( vm.rA );
        out.println( vm.rX );
        op.exec( adr, fld, code ); // SLC
        out.println( vm.rA );
        out.println( vm.rX );
        //
    }
}
////////////////////////////////////////////////////////////////
// log:
