package com.kanawish.shaderlib.utils;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Created by etiennecaron on 2014-03-02.
 */
public class IOUtils {
	/**
	 * http://stackoverflow.com/questions/326390/how-to-create-a-java-string-from-the-contents-of-a-file
	 * but since we're on android...
	 * http://stackoverflow.com/questions/9095610/android-fileinputstream-read-txt-file-to-string
	 */
	static public String loadStringFromAsset(Context context, String assetName) throws IOException {
		InputStream input = context.getAssets().open(assetName);

		return readFile(new InputStreamReader(input));
	}

	static public String readFile( Reader inputReader ) throws IOException {
		BufferedReader bufferedReader = new BufferedReader( inputReader );
		String         line = null;
		StringBuilder  stringBuilder = new StringBuilder();
		String         ls = System.getProperty("line.separator");

		while( ( line = bufferedReader.readLine() ) != null ) {
			stringBuilder.append( line );
			stringBuilder.append( ls );
		}

		return stringBuilder.toString();
	}


}
