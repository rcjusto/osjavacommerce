package org.store.ip2country.webhosting.ip2c;

import java.util.StringTokenizer;

public class LineInfo implements Cloneable
{
	private long startIP;
	private long endIP;
	private String id2c;
	private String id3c;
	private String name;
	private int lineNum;

	public LineInfo(long startIP, long endIP, String id2c, String id3c, String name, int lineNum)
	{
		super();
		this.endIP = endIP;
		this.id2c = id2c;
		this.id3c = id3c;
		this.lineNum = lineNum;
		this.name = name;
		this.startIP = startIP;
	}

	
	public LineInfo(String line, int format, int lineNum)
	{
		this.lineNum = lineNum;
		StringTokenizer tok = new StringTokenizer(line, "\",");
		if (format == IP2Country.SOFTWARE77_CSV_FORMAT)
		{
			startIP = Long.parseLong(tok.nextToken());
			endIP = Long.parseLong(tok.nextToken());
			tok.nextToken(); // skip authority
			tok.nextToken(); // skip issued
			id2c = tok.nextToken();
			id3c = tok.nextToken();
			if(tok.hasMoreElements())
			{
				// name can contain a comma, which messes things up.
				int i = line.lastIndexOf('\"', line.length() - 2);
				name = line.substring(i + 1, line.length() - 1);
			}
		}
		else if (format == IP2Country.WEBHOSTING_INFO_CSV_FORMAT)
		{
			startIP = Long.parseLong(tok.nextToken());
			endIP = Long.parseLong(tok.nextToken());
			id2c = tok.nextToken();
			id3c = "";
			name = "";
			if(tok.hasMoreElements())
			{
				id3c = tok.nextToken();
				if(tok.hasMoreElements())
				{
					// name can contain a comma, which messes things up.
					int i = line.lastIndexOf('\"', line.length() - 2);
					name = line.substring(i + 1, line.length() - 1);
				}
			}
		}
	}
	
	public long getStartIP() 
	{
		return startIP;
	}

	public long getEndIP() 
	{
		return endIP;
	}

	public String getId2c() 
	{
		return id2c;
	}

	public String getId3c() 
	{
		return id3c;
	}

	public String getName() 
	{
		return name;
	}

	public int getLineNum() 
	{
		return lineNum;
	}
	
	
	
	public void setEndIP(long endIP)
	{
		this.endIP = endIP;
	}
	
	public void setStartIP(long startIP)
	{
		this.startIP = startIP;
	}

	public boolean equals(Object obj) 
	{
		if((obj == null) || (obj.getClass() != this.getClass())) return false;
		LineInfo li = (LineInfo)obj;
		return (this.startIP == li.getStartIP() && this.endIP == li.getEndIP()
				&& this.id2c != null && this.id2c.equals(li.getId2c()) 
				&& this.id3c != null && this.id3c.equals(getId3c())
				&& this.name.equals(li.getName()));
	}
	
	public int hashCode() {
		int result = 0;
		
		if (id2c != null)
			result = id2c.hashCode();
		
		if (id3c != null)
			result = 42 * result + id3c.hashCode();
		
		if (name != null)
			result = 15 * result + name.hashCode();
		
		result+=startIP;
		result+=endIP;
		
		return result;

	}
	
	public String toString()
	{
		return "Line #" + lineNum + ": " + startIP + "," + endIP + "," + id2c + "," + id3c + "," + name;
	}
	
	public Object clone() {
	    return new LineInfo(this.startIP, this.endIP, this.id2c, this.id3c, this.name,this.lineNum); 
	}
}