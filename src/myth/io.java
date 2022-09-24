////////////////////////////////////////////////////////////////
package myth;
////////////////////////////////////////////////////////////////
import java.nio.file.Files;
import java.nio.file.Path;
////////////////////////////////////////////////////////////////
import static java.lang.System.out;
////////////////////////////////////////////////////////////////
class Device {
    static final String BASEDIR = "./dev/";
    final int    unit;
    final String fileName;      
    final int    block_size;
    VM     vm;
    String content;
    int    cure_ptr = 0; // current pointer
    //
    Device( int unit, String fileName, int block_size, VM vm ){
        this.unit = unit;
        this.fileName = fileName;
        this.block_size = block_size * Word.BYTES;
        this.vm = vm;
    }
    void load_content() {
        try {
            Path filePath = Path.of( BASEDIR + fileName );
            content = Files.readString( filePath )
                           .replaceAll( "\\R", "" ); // new lines
        } catch( Exception e ){
            throw new Error( e );
        }
    }
    void load_memory( String block, int adr ){
        int i = 0;
        while( i < block_size ){
            int j = i + Word.BYTES;
            vm.memory[ adr++ ] = Parser.encode( block.substring( i, j ));
            i = j;
        }
    }
    void in( int adr ){
        if( content == null ) load_content();
        int next_ptr = cure_ptr + block_size;
        try {
            String block = content.substring( cure_ptr, next_ptr );
            out.println( block );
            load_memory( block, adr );
        } catch( Throwable t ){
            throw new Error( "<<< EOF" );
        }
        cure_ptr = next_ptr;
    }
    static public void main( String args[] ){
        VM vm = new VM();
        Device line_printer = new Device( 18, "printer", 24, vm );
        line_printer.in( 0 );
        vm.dumpMemory( 0, 25 );
    }
}
////////////////////////////////////////////////////////////////
// log:
//
