package sichris.androidDemo.util;

public class DataTransforms
{
	public static void putInt(byte[] array,int pos,int val)
	{
		array[pos++] = (byte) (val >>> 24);
		array[pos++] = (byte) ((val >>> 16) & 0xff);
		array[pos++] = (byte) ((val >>> 8) & 0xff);
		array[pos] = (byte) (val & 0xff);
	}
	
	public static int getInt(byte[] array,int pos)
	{
		return ((((int) array[pos++]) << 24) |
				((((int) array[pos++]) & 0xff) << 16) |
				((((int) array[pos++]) & 0xff) << 8) |
				(((int) array[pos]) & 0xff));
	}
}
