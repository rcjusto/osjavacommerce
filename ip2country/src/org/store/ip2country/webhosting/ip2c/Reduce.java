
package org.store.ip2country.webhosting.ip2c;

import java.io.*;
import java.util.*;

/**
 * Attempts to consolidate ip ranges where possible
 */
public class Reduce
{
	/**
	 * @param args
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	public static void main(String[] args) throws NumberFormatException,
			IOException
	{
		if(args.length < 2)
		{
			System.err.println("Reduce input-csv output-csv");
			return;
		}
		reduce(args[0], args[1]);
	}

	public static void reduce(String in_file, String out_file)throws IOException
	{
		OutputStream out = null;
		InputStream in = null;
		
		try
		{
			in = new FileInputStream(in_file);
			out = new FileOutputStream(out_file);
			reduce(in, out);
		}
		finally
		{
			if (in != null) in.close();
			if (out != null) out.close();
		}
	}
	
	public static void reduce(InputStream in, OutputStream out)throws IOException
	{
		List lines = Utils.readCSV(in);
		List reduced = reduce(lines);
		Utils.writeCSV(reduced, out);
	}
	
	public static List reduce(List lines)throws IOException
	{
		Collections.sort(lines, new Comparator()
		{
			// result will be increasing based on the start of the IP
			// range
			public int compare(Object first, Object second)
			{
				LineInfo l1 = (LineInfo) first;
				LineInfo l2 = (LineInfo) second;
				return l2.getStartIP() - l1.getStartIP() > 0 ? -1 : (l2
						.getStartIP()
						- l1.getStartIP() == 0 ? 0 : 1);
			}
		});		

		for (int i = 0; i < lines.size(); i++)
		{
			LineInfo last;
			if(i > 0)
			{
				last = (LineInfo) lines.get(i - 1);
			}
			else
			{
				last = null;
			}
			LineInfo li = (LineInfo) lines.get(i);

			// the end of the IP range must not be smaller the start of the
			// range
			if(li.getEndIP() < li.getStartIP())
			{
				throw new IllegalArgumentException("start > end at line " + li);
			}

			if(last != null && li.getStartIP() < last.getEndIP())
			{
				System.out.println("Conflict between lines: \n" + last + " : " + li);

				if(li.getEndIP() <= last.getEndIP())
				{
					System.out.println("Dropping inner range : " + li);
					lines.remove(i);
					continue;
				}
				else if(li.getEndIP() > last.getEndIP())
				{
					// in fact we can resolve it by splitting ranges.
					// but I`ll only do it if I see actual cases where this
					// happens
					String msg = "Conflict type 2, can't resolve";
					throw new IllegalArgumentException(msg);
				}
			}
		}
		
		// consolidate ranges
		for (int i = 0; i < lines.size(); i++)
		{
			LineInfo last;
			if(i > 0)
			{
				last = (LineInfo) lines.get(i - 1);
			}
			else
			{
				last = null;
			}
			LineInfo li = (LineInfo) lines.get(i);
			if(li == null)
				throw new IllegalStateException();

			if(last != null)
			{
				long lastEnd = last.getEndIP();
				long startIP = li.getStartIP();
				if(last.getId2c().equals(li.getId2c()) && (lastEnd == startIP || lastEnd + 1 == startIP))
				{
//					System.out.println("Merging '" + last + "' and '" + li + "'");
					last.setEndIP(li.getEndIP());
					lines.remove(i);
					i--;
				}
			}
		}
		
		return lines;
	}
}
