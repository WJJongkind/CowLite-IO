/* 
 * Copyright (C) 2018 Wessel Jelle Jongkind.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package cowlite.io.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * This class allows for files on a user's PC to be destroyed and/or removed in
 * such a way that it is impossible to recover the files from the storage device
 * that they were stored on.
 * 
 * @author Wessel Jelle Jongkind
 * @version 2018-08-20 (yyyy-mm-dd)
 */
public class FileOverwriter
{
    /**
     * Placeholder for the default overwrite block.
     */
    private static byte[] defaultBlock;
    
    /**
     * The block that is actively being used by an instance of this class.
     */
    private byte[] block;
    
    /**
     * Default block size for overwriting files. 
     */
    public static final int DEFAULT_BLOCK_SIZE = 512;
    
    /**
     * Instantiates a new FileOverwriter object with the {@code DEFAULT_BLOCK_SIZE}
     * as the block-size used for overwriting files.
     */
    public FileOverwriter() {
        this(DEFAULT_BLOCK_SIZE);
    }
    
    /**
     * Instantiates a new FileOverwriter object which uses blocks of the given
     * size for overwriting files.
     * @param blockSize The size of the blocks used to overwrite files. A larger
     * blocksize may lead to faster file overwriting for large files, but also require
     * more RAM to be used by the system.
     */
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
    
    /**
     * Overwrites the file at the given path. 
     * @param f The file that should be overwritten deleted.
     * @param delete True if you want the file to be deleted afterwards, false if not.
     * @return True if the file was successfully overwritten and (if needed) deleted.
     * @throws IOException If the file could not be found, the given file is a directory
     * or due to other IO errors.
     */
    public boolean overwriteFile(File f, boolean delete) throws IOException {
        FileOutputStream out = null;
        try {
            long len = f.length();

            //Make sure the file is overwritten with data that is atleast the same length.
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
