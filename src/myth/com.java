////////////////////////////////////////////////////////////////
package myth;
////////////////////////////////////////////////////////////////
import static java.lang.System.out;
////////////////////////////////////////////////////////////////
import java.util.Scanner;
import java.util.Arrays;
////////////////////////////////////////////////////////////////
class Comsat {
    Parser pasr = new Parser();
    // how to make here hash string to function pointer
    // com -> method
    void Debug() {
        out.println( pasr );
    }
}
////////////////////////////////////////////////////////////////
class Com{
    Comsat station = new Comsat();
    Com(){}
    void exec( String s ){
        String[] argv = s.split( "\\s+" );
        String com = argv[ 0 ];
        int argc = argv.length;
        String args[] = Arrays.copyOfRange( argv, 1, argc );
        if( com.equals( "debug" )){
            station.Debug();
        }
    }
    void launch(){ // firefox
        Scanner scanner = new Scanner( System.in );
        while( true ){
            out.print( "> " );
            if( !scanner.hasNextLine()){
                break;
            }
            exec( scanner.nextLine());
        }
        scanner.close();        
    }
    public static void main( String[] args ){
        Com com = new Com();
        com.launch();
    }
}
////////////////////////////////////////////////////////////////
