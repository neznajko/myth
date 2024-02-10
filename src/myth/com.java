////////////////////////////////////////////////////////////////
package myth;
////////////////////////////////////////////////////////////////
import static java.lang.System.out;
////////////////////////////////////////////////////////////////
import java.util.Scanner;
import java.util.Arrays;
////////////////////////////////////////////////////////////////
class Com{
    Com(){}
    void exec( String s ){
        String[] argv = s.split( "\\s+" );
        String com = argv[ 0 ];
        String args[] = Arrays.copyOfRange( argv, 1, argv.length );
        out.println( com + " " + Arrays.toString( args ));
    }
    void launch(){
        Scanner scanner = new Scanner( System.in );
        while( true ){
            out.print( "> " );
            if( !scanner.hasNextLine() ){
                break;
            }
            exec( scanner.nextLine() );
        }
        scanner.close();        
    }
    public static void main( String[] args ){
        Com com = new Com();
        com.launch();
    }
}
////////////////////////////////////////////////////////////////
