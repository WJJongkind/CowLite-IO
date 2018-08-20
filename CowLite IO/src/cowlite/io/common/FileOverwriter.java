package cowlite.io.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author Wessel
 */
public class FileOverwriter
{
    private static byte[] defaultBlock;
    private byte[] block;
    private static final int DEFAULT_BLOCK_SIZE = 512;
    
    public FileOverwriter() {
        this(DEFAULT_BLOCK_SIZE);
    }
    
    public FileOverwriter(int blockSize) {
        if(blockSize != DEFAULT_BLOCK_SIZE || defaultBlock == null) {
            block = new byte[blockSize];
            for(int i = 0; i < blockSize; i++){
                block[i] = 0;
            }
            if(blockSize == DEFAULT_BLOCK_SIZE) {
                defaultBlock = block;
            }
        } else {
            block = defaultBlock;
        }
    }
    
    public boolean overwriteFile(File f, boolean delete) throws IOException {
        FileOutputStream out = null;
        try {
            long len = f.length();

            out = new FileOutputStream(f);
            for(int j = 0; j < (double)len / (double)block.length; j++) {
                out.write(block);
            }
            
            if(delete) {
                f.delete();
            }
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if(out != null) {
                out.close();
            }
        }
    }
}
