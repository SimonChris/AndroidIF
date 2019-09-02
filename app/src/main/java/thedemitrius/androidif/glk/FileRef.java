package thedemitrius.androidif.glk;

import java.io.File;

import android.content.Context;
import android.os.Environment;

public class FileRef extends File 
{
	private static final long serialVersionUID = -5759247943453865390L;

	private int rock;
	
	private FileRef(String dir,String name,int rock,File filesystem)
	{
		super(filesystem, dir+"/"+name);
		
		this.rock=rock;
	}
	
	public static FileRef createFileRef(Context context,String dir,String name,int rock)
	{
		if (isExternalStorageAvailable()&&!isExternalStorageReadOnly())
			return (new FileRef(dir,name,rock,context.getExternalFilesDir(null)));
		else
			return (new FileRef(dir,name,rock,context.getFilesDir()));
	}

	int getRock()
	{
		return (rock);
	}
	
	public static boolean isExternalStorageAvailable() {
	    boolean state = false;
	    String extStorageState = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
	        state = true;
	    }
	    return state;
	}	
	
	public static boolean isExternalStorageReadOnly() {
	    boolean state = false;
	    String extStorageState = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
	        state = true;
	    }
	    return state;
	}
}
