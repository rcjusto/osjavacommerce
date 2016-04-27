package org.store.ip2country.webhosting.ip2c.input;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class MemoryMappedRandoeAccessFile implements RandomAccessInput
{
	private ByteBuffer m_roBuf;
	public MemoryMappedRandoeAccessFile(String file, String mode) throws IOException
	{
		RandomAccessFile ra = new RandomAccessFile(file, mode); 
        FileChannel roChannel = ra.getChannel();
        m_roBuf = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, (int)roChannel.size());		
	}
	
	public int readInt() throws IOException
	{
		return m_roBuf.getInt();
	}
	
	public short readShort() throws IOException
	{
		return m_roBuf.getShort();
	}
	
	public void readFully(byte[] a) throws IOException
	{
		m_roBuf.get(a);
	}

	public void seek(long n) throws IOException
	{
		m_roBuf.position((int)n);
	}
	
}
