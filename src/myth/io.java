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
    int    content_ptr = 0;
    //
    Device( int unit, String fileName, int block_size, VM vm ){
        this.unit = unit;
        this.fileName = fileName;
        this.block_size = block_size * Word.BYTES;
        this.vm = vm;
    }
    void load() {
        try {
            Path filePath = Path.of( BASEDIR + fileName );
            content = Files.readString( filePath )
                           .replaceAll( "\\R", "" ); // new lines
        } catch( Exception e ){
            throw new Error( e );
        }
    }
    void in( int adr ){
        if( content == null ) load();
        try {
            out.println( content.substring( content_ptr,
                                            content_ptr + block_size ));
        } catch( Throwable t ){
            throw new Error( "<<<EOF" );
        }
        content_ptr += block_size;
    }
    static public void main( String args[] ){
        VM vm = new VM();
        Device line_printer = new Device( 18, "printer", 24, vm );
        line_printer.in( 0 );
        line_printer.in( 0 );
        line_printer.in( 0 );
        line_printer.in( 0 );
    }
}
////////////////////////////////////////////////////////////////
// log:
//
