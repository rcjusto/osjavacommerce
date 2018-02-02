package org.store.ip2country.webhosting.ip2c;

public class Country
{
	private short m_2c;
	private int m_3c;
	private String m_name;

	public Country(short id2c, int id3c, String name)
	{
		m_2c = id2c;
		m_3c = id3c;	
		m_name = name;
	}
	
	public Country(String id2c, String id3c, String name)
	{
		if (id2c.length() != 2) throw new IllegalArgumentException("Invalid id2c : '" + id2c + "'");
		if (id3c.length() > 3) throw new IllegalArgumentException("Invalid id3c : '" + id3c + "'");
		m_2c = (short)(id2c.charAt(0) << 8 | id2c.charAt(1));
		if (id3c.length() == 0) id3c = "   ";
		if (id3c.length() == 3)
		{
			m_3c = id3c.charAt(0) << 16 | id3c.charAt(1) << 8 | id3c.charAt(2);	
		}
		else
		if (id3c.length() == 2) // can happen for EU on software77 database 
		{
			m_3c = id3c.charAt(0) << 8 | id3c.charAt(1) << 0;
		}
		m_name = name;
	}

	public short get2c()
	{
		return m_2c;
	}

	public int get3c()
	{
		return m_3c;
	}

	public String getName()
	{
		return m_name;
	}
	
	public String toString()
	{
		String id2c = get2cStr(m_2c);
		String id3c = get3cStr(m_3c);
		return "id2=" + id2c + ", id3c=" + id3c+ ", name=" + m_name;
	}

	public String get3cStr()
	{
		return get3cStr(m_3c);
	}
	
	public String get2cStr()
	{
		return get2cStr(m_2c);
	}

	
	public static String get3cStr(int id3c)
	{
		String s = new String(new byte[]{(byte)(id3c >> 16), (byte) (id3c >> 8), (byte)id3c});
		if (s.equals("   ")) return "";
		return s;
	}

	public static String get2cStr(short id2c)
	{
		return new String(new byte[]{(byte) (id2c >> 8), (byte) (id2c & 0x00ff)});
	}
	
	public boolean equals(Object obj)
	{
		if (obj instanceof Country)
		{
			Country other = (Country) obj;
			return other.m_2c == m_2c && other.m_3c == other.m_3c && other.m_name.equals(m_name);
		}
		return false;
	}
	
}

