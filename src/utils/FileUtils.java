
package utils;

import java.io.File;
import java.util.StringTokenizer;

public class FileUtils{

	public static String getProperPathString(String pathString, char wrongChar){
		
		StringBuffer pathStringBuffer = new StringBuffer(pathString);
		
		int index  = 0;
		int offset = -1;
		while((index = pathString.indexOf(wrongChar, offset + 1)) != -1){
			pathStringBuffer.setCharAt(index, File.separatorChar);	
			offset = index;
		}
		return pathStringBuffer.toString();
	}
	
	public static String[] getClassPathes(){
		String[] classPathes = new String[1];
		String classPath = System.getProperty("java.class.path");

		StringTokenizer tokenizer = new StringTokenizer(classPath, File.pathSeparator);

		int count = 0;
		String[] oldClassPathes;
		String token;
		while(tokenizer.hasMoreTokens()){
			token = tokenizer.nextToken();
			if(token.endsWith(File.separator))
				token = token.substring(0, token.length()-1);
			classPathes[count] = token;
			oldClassPathes = classPathes;
			classPathes = new String[++count + 1];
			System.arraycopy(oldClassPathes, 0, classPathes, 0, count);
		}
		oldClassPathes = classPathes;
		classPathes = new String[count];
		System.arraycopy(oldClassPathes, 0, classPathes, 0, count);
		
		return classPathes;
	}
}