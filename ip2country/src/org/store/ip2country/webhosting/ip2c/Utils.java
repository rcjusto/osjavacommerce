
package org.store.ip2country.webhosting.ip2c;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Utils
{
	public static List readCSV(String file) throws IOException
	{
		InputStream in = null;
		try
		{
			return readCSV(in = new FileInputStream(file));
		}
		finally
		{
			if (in != null) in.close();
		}
	}
	
	public static List readCSV(InputStream in) throws IOException
	{
		String line = null;
		int lineNum = -1;
		int format = -1;
		List lines = new ArrayList();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));

		while ((line = reader.readLine()) != null)
		{
			lineNum++;
			if(line.length() == 0 || line.trim().startsWith("#"))
				continue;

			if(format == -1)
			{
				format = detectFormat(line);
				if(format == -1)
					continue;

			}
			LineInfo li = new LineInfo(line, format, lineNum);
			lines.add(li);
		}
		return lines;
	}

	/**
	 * Reduce can parse: Software77 format -
	 * http://software77.net/cgi-bin/ip-country/geo-ip.pl Webhosting format -
	 * http://ip-to-country.webhosting.info/
	 */
	public static int detectFormat(String line)
	{
		if(line.length() == 0 || line.trim().startsWith("#"))
			return -1;
		StringTokenizer tok = new StringTokenizer(line, "\",");
		if(tok.countTokens() == 7)
		{
			System.err.println("Detected format : SOFTWARE77");
			return IP2Country.SOFTWARE77_CSV_FORMAT;
		}
		else if(tok.countTokens() == 5)
		{
			System.err.println("Detected format : WEBHOSTING_INFO");
			return IP2Country.WEBHOSTING_INFO_CSV_FORMAT;
		}
		return -1;
	}

	public static String randomizeIP()
	{
		short b1 = (short) (Math.random() * 256);
		short b2 = (short) (Math.random() * 256);
		short b3 = (short) (Math.random() * 256);
		short b4 = (short) (Math.random() * 256);
		return b1 + "." + b2 + "." + b3 + "." + b4;
	}

	public static void removeOverlappings(List lines)
	{
		List removed = new ArrayList();
		
		for (int i = 0; i < lines.size(); i++)
		{
			LineInfo previousLi;
			if (i > 0)
			{
				previousLi = (LineInfo) lines.get(i-1);
			}
			else
			{
				previousLi = null;
			}
			LineInfo li = (LineInfo) lines.get(i);
			
			// the end of the IP range must not be smaller the start of the range 
			if(li.getEndIP() < li.getStartIP())
			{
				throw new IllegalArgumentException("start > end at line " + li);
			}
			
	
			if(previousLi != null && li.getStartIP() < previousLi.getEndIP())
			{
				System.err.println("Conflict between lines: \n"+ previousLi + "\n" + li);
				
				if (li.getEndIP() <= previousLi.getEndIP()) 
				{
					// duplicate ranges
					if (li.getStartIP() == previousLi.getStartIP() &&
							li.getEndIP() == previousLi.getEndIP())
					{
						System.err.println("Conflicting ranges have identical start and end. removing " + li);
						removed.add(lines.remove(i));
					}
					// if they start from the same startIP we can just
					// start the second range from the end of the first range
					else if (li.getStartIP() == previousLi.getStartIP())
					{
						removed.add(previousLi.clone());
						
						System.err.println("Extending the start of the outer range : ");
						System.err.println("Outer range was:"+previousLi);
						
						previousLi.setStartIP(li.getEndIP()+1);
						System.err.println("Outer range is:"+previousLi);
						System.err.println("Inner range is:"+li);
						// switching their place, to maintain incremental order of start ranges
						lines.set(i, previousLi);
						lines.set(i-1, li);
					}
					else {
						System.err.println("Dropping inner range : " + li);
						removed.add(lines.remove(i));
					}
				}
				else if (li.getEndIP() > previousLi.getEndIP())
				{
					// in fact we can resolve it by splitting ranges.
					// but I`ll only do it if I see actual cases where this happens
					String msg = "Conflict type 2, can't resolve";
					throw new IllegalArgumentException(msg);
				}
			}
		}
	}

	public static void writeCSV(List lines, String outcsv) throws IOException
	{
		OutputStream out = null;
		try
		{
			writeCSV(lines, out = new FileOutputStream(outcsv));
		}
		finally
		{
			if (out != null) out.close();
		}
		
	}
	
	public static void writeCSV(List lines, OutputStream out) throws IOException
	{
		for (int i = 0; i < lines.size(); i++)
		{
			LineInfo li = (LineInfo) lines.get(i);
			out.write(("\"" + li.getStartIP() + "\",\"" + li.getEndIP()
					+ "\",\"" + li.getId2c() + "\",\"" + li.getId3c() + "\",\""
					+ li.getName() + "\"\n").getBytes());
		}
	}
}
