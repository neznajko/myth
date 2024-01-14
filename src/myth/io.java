////////////////////////////////////////////////////////////////
package myth;
////////////////////////////////////////////////////////////////
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.nio.channels.FileChannel;
////////////////////////////////////////////////////////////////
import java.util.ArrayList;
////////////////////////////////////////////////////////////////
import static java.lang.System.out;
////////////////////////////////////////////////////////////////
class Device {
    static final String BASEDIR = "./dev/";
	static final int PRINTER = 18;
    int        unit;
    Path       filePath;
    int        noof_words;
    int        block_size;
    VM         vm;
    String     content;
    int        cure_ptr = 0; // current pointer
    Controller ctrl;
    //
    Device( int unit, String fileName, int noof_words, VM vm ){
        this.unit = unit;
        this.filePath = Path.of( BASEDIR + fileName );
        this.noof_words = noof_words;
        this.block_size = noof_words * Word.BYTES;
        this.vm = vm;
    }
    void set_controller( Controller ctrl ){
        this.ctrl = ctrl;
    }
    void perform_rewind() {
        ctrl.rewind( filePath );
    }
    void load_content() {
        try {
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
            load_memory( block, adr );
        } catch( Throwable t ){
            throw new Error( "<<< EOF" );
        }
        cure_ptr = next_ptr;
    }
    void out( int adr ){
        var lines = new ArrayList<String>();
        for( int i = 0; i < 2; i++ ){
            var b = new StringBuilder();
            for( int j = 0; j < noof_words/2; j++ ){
                b.append( Parser.decode( vm.memory[ adr++ ]));
            }
            lines.add( b.toString());
        }
        try {
            Files.write( filePath, lines,
                         StandardCharsets.UTF_8,
                         StandardOpenOption.APPEND );
        } catch( Throwable t ){
            throw new Error( "#8(" );
        }
    }
    static public void main( String args[] ){
        VM vm = new VM();
        Device line_printer = new Device( 18, "printer", 24, vm );
        line_printer.set_controller( new Clear());
        Parser parser = new Parser();
        vm.memory[0] = parser.walue.ewal( "31(4:4),4(5:5)" );
        vm.memory[23] = parser.walue.ewal( "14(1:1),28(3:3)" );
        line_printer.out( 0 );
        vm.memory[0] = parser.walue.ewal( "2(1:1),31(4:4),4(5:5)" );
        line_printer.perform_rewind();
        line_printer.out( 0 );
    }
}
////////////////////////////////////////////////////////////////
interface Controller {
    void rewind( Path filePath );
}
class Clear implements Controller {
    public void rewind( Path filePath ){
        try {
            FileChannel.open( filePath, StandardOpenOption.WRITE)
                       .truncate(0)
                       .close();
        } catch( Throwable t ){
            out.println( "Q:'");
        }
    }
    static public void main( String args[] ){
    }
}
////////////////////////////////////////////////////////////////
// log: 
//
