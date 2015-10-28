package sichris.androidDemo.util;

public class FastByteBuffer
{
	private byte[] data;
	private int position;
	private int limit;
	private int capacity;

	public FastByteBuffer(int cap) throws OutOfMemoryError
	{
		data = new byte[cap];
		capacity = cap;
		limit = cap;
		position = 0;
	}

	public byte get()
	{
		return data[position++];
	}

	public byte get(int pos)
	{
		return data[pos];
	}

	public short getShort()
	{
		return (short) ((((int) data[position++]) << 8) | 
				(((int) data[position++]) & 0xff));
	}

	public short getShort(int pos)
	{
		return (short) ((((int) data[pos++]) << 8) | 
				(((int) data[pos++]) & 0xff));
	}

	public int getInt()
	{
		return ((((int) data[position++]) << 24) |
				((((int) data[position++]) & 0xff) << 16) |
				((((int) data[position++]) & 0xff) << 8) |
				(((int) data[position++]) & 0xff));
	}

	public int getInt(int pos)
	{
		return ((((int) data[pos++]) << 24) |
				((((int) data[pos++]) & 0xff) << 16) |
				((((int) data[pos++]) & 0xff) << 8) |
				(((int) data[pos++]) & 0xff));
	}

	public void put(byte[] arr, int off, int len)
	{
		System.arraycopy(arr, off, data, position, len);
		position += len;
	}

	public void put(FastByteBuffer fb)
	{
		int len = fb.limit - fb.position;
		System.arraycopy(fb.data, fb.position, data, position, len);

		position += len;
	}

	public void put(byte b)
	{
		data[position++] = b;
	}

	public void put(int pos, byte b)
	{
		data[pos] = b;
	}

	public void putShort(short s)
	{
		data[position++] = (byte) (s >>> 8);
		data[position++] = (byte) (s & 0xff);
	}

	public void putShort(int pos, short s)
	{
		data[pos++] = (byte) (s >>> 8);
		data[pos++] = (byte) (s & 0xff);
	}

	public void putInt(int i)
	{
		data[position++] = (byte) (i >>> 24);
		data[position++] = (byte) ((i >>> 16) & 0xff);
		data[position++] = (byte) ((i >>> 8) & 0xff);
		data[position++] = (byte) (i & 0xff);
	}

	public void putInt(int pos, int i)
	{
		data[pos++] = (byte) (i >>> 24);
		data[pos++] = (byte) ((i >>> 16) & 0xff);
		data[pos++] = (byte) ((i >>> 8) & 0xff);
		data[pos++] = (byte) (i & 0xff);
	}

	public byte[] array()
	{
		return data;
	}

	public int capacity()
	{
		return capacity;
	}

	public int limit()
	{
		return limit;
	}

	public void limit(int lim)
	{
		limit = lim;
	}

	public int position()
	{
		return position;
	}

	public void position(int pos)
	{
		position = pos;
	}

	public int remaining()
	{
		return limit - position;
	}

	public void clear()
	{
		position = 0;
		limit = capacity;
	}

	public void finalize()
	{
		data=null;
	}
}
