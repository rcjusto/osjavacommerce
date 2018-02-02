package org.store.ip2country.webhosting.ip2c.input;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class RandomAccessBuffer implements RandomAccessInput
{
	private byte m_buffer[];
	private int m_offset;
	
	public RandomAccessBuffer(String file) throws IOException
	{
		File f = new File(file);
		byte buf[] = new byte[(int) f.length()];
		FileInputStream fin = new FileInputStream(f);
		DataInputStream din = new DataInputStream(fin);
		din.readFully(buf);
		fin.close();
		m_buffer = buf;
		m_offset = 0;
	}
	
	public RandomAccessBuffer(byte buffer[])
	{
		m_buffer = buffer;
		m_offset = 0;
	}

	public int readInt() throws IOException
	{
		int b1 = m_buffer[m_offset++];
		int b2 = m_buffer[m_offset++];
		int b3 = m_buffer[m_offset++];
		int b4 = m_buffer[m_offset++];

        return (((b1<< 24)&0xff000000) | ((b2<< 16)&0x00ff0000) | ((b3 << 8)&0x0000ff00) | ((b4 << 0))&0x000000ff);
	}

	public short readShort() throws IOException
	{
		byte b1 = m_buffer[m_offset++];
		byte b2 = m_buffer[m_offset++];
		return (short) (((b1<< 8)&0xff00) | ((b2 << 0)&0x00ff));
	}

	public void readFully(byte[] a) throws IOException
	{
		System.arraycopy(m_buffer, m_offset, a, 0, a.length);
		m_offset += a.length;
	}

	public void seek(long n) throws IOException
	{
		m_offset = (int) n;
	}

}
