/*
Copyright (c) 2012 Ronald Tsang, ronaldtsang@orochis-den.com

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package orochi.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class FileUtils {
    public static void mkdirIfNeeded(String directoryName)
    {
      File theDir = new File(directoryName);

      // if the directory does not exist, create it
      if (!theDir.exists())
      {
        Log.d("Webtop", "Creating Directory: "+directoryName);
        theDir.mkdir();
      }
    }
    
	public static String getRealPathFromMediaURI(Context context, Uri contentUri) {
        String [] proj={MediaStore.MediaColumns.DATA};
        Cursor cursor = context.getContentResolver().query(contentUri,
                        proj, // Which columns to return
                        null,       // WHERE clause; which rows to return (all rows)
                        null,       // WHERE clause selection arguments (none)
                        null); // Order-by clause (ascending by name)
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();

        return cursor.getString(column_index);
	}
	
	public static boolean renameFile(String source, String dest) {

		File file =new File(source);
		
 	   if(file.renameTo(new File(dest))){
 		   return true;
   	   }else{
   		   return false;
   	   }
	}
	
	public static boolean moveFile(Context context, String source, String dest){
		InputStream sourceStream = null;
		OutputStream destStream = null;
		File sourceFile = new File(source);
		try {
			sourceStream = context.getContentResolver().openInputStream(Uri.fromFile(sourceFile));
			destStream = context.getContentResolver().openOutputStream(Uri.fromFile(new File(dest)));
		} catch (FileNotFoundException e) {
			Log.e("Orochi", "FileUtils: File not found");
		}
		if(sourceStream!=null && destStream!=null)
			cloneFile(sourceStream, destStream);

        try {
        	sourceStream.close();
        	destStream.close();
        } catch (IOException e) {
            Log.e("Orochi", "FileUtils: Exception occured while closing filestream ", e);
            return false;
        }			
		return sourceFile.delete();		
	}

	public static boolean cloneFile(InputStream inputFileStream, OutputStream outFileStream) {				

		if(inputFileStream==null || outFileStream==null)
			return false;

	    try {
	        byte[] bytesArray = new byte[1024];
	        int length;
	        while ((length = inputFileStream.read(bytesArray)) > 0) {
	        	outFileStream.write(bytesArray, 0, length);
	        }

	        outFileStream.flush();

	    } catch (IOException e) {
	        Log.e("Orochi", "FileUtils: Exception while copying file " + inputFileStream + " to "
	                + outFileStream, e);
	        return false;
	    }
        return true;
	}
	
    public static String getExtension(java.io.File file) {
        String extension = "";
        String filename = file.getName();
        int dotPos = filename.lastIndexOf(".");
        if (dotPos >= 0) {
            extension = filename.substring(dotPos);
        }
        return extension.toLowerCase();
    }	
	
}
