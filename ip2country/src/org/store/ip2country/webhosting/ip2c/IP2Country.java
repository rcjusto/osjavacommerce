
package org.store.ip2country.webhosting.ip2c;

import org.store.ip2country.webhosting.ip2c.input.MemoryMappedRandoeAccessFile;
import org.store.ip2country.webhosting.ip2c.input.RandomAccessBuffer;
import org.store.ip2country.webhosting.ip2c.input.RandomAccessFile2;
import org.store.ip2country.webhosting.ip2c.input.RandomAccessInput;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

public class IP2Country
{
	public static final int WEBHOSTING_INFO_CSV_FORMAT = 0;
	public static final int SOFTWARE77_CSV_FORMAT = 1;

	public static final int NO_CACHE = 0;
	public static final int MEMORY_MAPPED = 1;
	public static final int MEMORY_CACHE = 2;

	private RandomAccessInput m_input;
	private int m_firstTableOffset;
	private int m_secondTableOffset;
	private int m_countriesOffset;
	private int m_numRangesFirstTable;
	private int m_numRangesSecondTable;

	private int m_numCountries;

	public IP2Country() throws IOException
	{
		this(NO_CACHE);
	}

	public IP2Country(int cacheMode) throws IOException
	{
		this("ip-to-country.bin", cacheMode);
	}

	public IP2Country(String file, int cacheMode) throws IOException
	{
		switch (cacheMode)
		{
			case NO_CACHE :
				m_input = new RandomAccessFile2(file, "r");
			break;
			case MEMORY_MAPPED :
				m_input = new MemoryMappedRandoeAccessFile(file, "r");
			break;
			case MEMORY_CACHE :
				m_input = new RandomAccessBuffer(file);
			break;
		}

		byte sig[] = new byte[4];
		readFully(sig);
		if(sig[0] != 'i' || sig[1] != 'p' || sig[2] != '2' || sig[3] != 'c')
			throw new IOException("Invalid file signature");
		if(readInt() != 2)
			throw new IOException("Invalid format version");

		m_firstTableOffset = readInt();
		m_numRangesFirstTable = readInt();
		m_secondTableOffset = readInt();
		m_numRangesSecondTable = readInt();
		m_countriesOffset = readInt();
		m_numCountries = readInt();
	}

	public Country getCountry(String ipStr) throws IOException
	{
		return getCountry(parseIP(ipStr));
	}

	private static int parseIP(String ipStr)
	{
		StringTokenizer tok = new StringTokenizer(ipStr, ".");
		short b1 = Short.parseShort(tok.nextToken());
		short b2 = Short.parseShort(tok.nextToken());
		short b3 = Short.parseShort(tok.nextToken());
		short b4 = Short.parseShort(tok.nextToken());

		int ip = ((b1 << 24) & 0xff000000) | ((b2 << 16) & 0x00ff0000) | ((b3 << 8) & 0x0000ff00) | (b4 & 0x000000ff);
		return ip;
	}

	public Country getCountry(int ip) throws IOException
	{
		short key;
		if(ip >= 0)
		{
			key = findCountyCode(ip, 0, m_numRangesFirstTable, true);
		}
		else
		{
			int nip = ip - Integer.MAX_VALUE;
			key = findCountyCode(nip, 0, m_numRangesSecondTable, false);
		}

		// System.out.println(Country.get2cStr(key));
		if(key == 0)
		{
			return null;
		}
		else
		{
			return findCountry(key, 0, m_numCountries);
		}
	}

	private short findCountyCode(long ip, int startIndex, int endIndex,
			boolean firstTable) throws IOException
	{
		int middle = (startIndex + endIndex) / 2;
		Pair mp = getPair(middle, firstTable);
		long mip = mp.m_ip;
		if(ip < mip)
		{
			if(startIndex + 1 == endIndex)
				return 0; // not found
			return findCountyCode(ip, startIndex, middle, firstTable);
		}
		else if(ip > mip)
		{
			Pair np = getPair(middle + 1, firstTable);
			if(ip < np.m_ip)
			{
				return mp.m_key;
			}
			else
			{
				if(startIndex + 1 == endIndex)
				{
					return 0;
				}
				return findCountyCode(ip, middle, endIndex, firstTable);
			}
		}
		else
		// ip == mip
		{
			return mp.m_key;
		}
	}


	public Country findCountry(short code) throws IOException
	{
		return findCountry(code, 0, m_numCountries);
	}

	public Country findCountry(short code, int startIndex, int endIndex)
			throws IOException
	{
		int middle = (startIndex + endIndex) / 2;
		short mc = getCountryCode(middle);
		// System.out.println(Country.get2cStr(mc));
		if(mc == code)
		{
			// found.
			return loadCountry(middle);
		}
		else if(code > mc)
		{
			return findCountry(code, middle, endIndex);
		}
		else
		{
			return findCountry(code, startIndex, middle);
		}
	}

	private Country loadCountry(int index) throws IOException
	{
		int offset = m_countriesOffset + index * 10;
		seek(offset);
		short id2c = readShort();
		int id3c = readInt();
		int nameOffset = readInt();
		seek(nameOffset);
		short len = readShort();
		byte nameBuf[] = new byte[len];
		readFully(nameBuf);
		return new Country(id2c, id3c, new String(nameBuf));
	}

	private short getCountryCode(int index) throws IOException
	{
		int offset = m_countriesOffset + index * 10;
		seek(offset);
		return readShort();
	}

	private Pair getPair(int index, boolean firstTable) throws IOException
	{
		int offset;
		if(firstTable)
		{
			if(index > m_numRangesFirstTable)
			{
				return new Pair(); // zero
			}
			offset = m_firstTableOffset + index * 6;
		}
		else
		{
			if(index > m_numRangesSecondTable)
			{
				return new Pair(); // zero
			}
			offset = m_secondTableOffset + index * 6;

		}

		seek(offset);
		Pair p = new Pair();
		byte a[] = new byte[4];
		readFully(a);
		p.m_ip = toInt(a);
		p.m_key = readShort();
		return p;

	}

	static class Pair
	{
		int m_ip; // silly java have no unsigened int32.
		short m_key;
	}

	public static List convertCSVtoBIN(String csvFile, String binFile) throws IOException
	{
		List lines = Utils.readCSV(csvFile);

		int sizeBefore = lines.size();
		Utils.removeOverlappings(lines);
		
		lines = Reduce.reduce(lines);
		if (sizeBefore != lines.size())
		{
			System.out.println("Reduced " + (sizeBefore - lines.size()) + " ranges by consolidating adjacent ranges");
		}
		
		lines = normalizeList(lines);
		convertCSVtoBIN(lines, binFile);
		return lines;
	}
	
	public static void convertCSVtoBIN(List lines, String binFile) throws IOException
	{
		Map h = new TreeMap(new Comparator()
		{
			public int compare(Object a, Object b)
			{
				return ((String) a).compareTo((String) b);
			}
		});

		RandomAccessFile dout = new RandomAccessFile(binFile, "rw");
		dout.setLength(0);
		long lastEndLong = -1;
		
		// write signature
		dout.write("ip2c".getBytes());

		// write version 1.
		dout.writeInt(2);

		// placeholder for the country names offset

		int offsets = (int) dout.getFilePointer();
		dout.writeInt(0xcccccccc); // place holder for first ranges table
		// offset
		dout.writeInt(0xbbbbbbbb); // place holder for first table ranges count
		dout.writeInt(0xcccccccc); // place holder for second ranges table
		// offset
		dout.writeInt(0xbbbbbbbb); // place holder for second table ranges
		// count
		dout.writeInt(0xcccccccc); // place holder for countries offset
		dout.writeInt(0xbbbbbbbb); // place holder for num countries

		int cur;
		cur = (int) dout.getFilePointer();
		dout.seek(offsets);
		dout.writeInt(cur);
		dout.seek(cur);

		boolean firstHalf = true;
		int numRangesInTable = 0;

		long lastEndInt = -1;
		LineInfo li = null;
		// write first element in the ranges array. ip 0.0.0.0
		for (int i = 0; i < lines.size(); i++)
		{
			try
			{
				li = (LineInfo) lines.get(i);

				if(li.getStartIP() > li.getEndIP())
					throw new IllegalArgumentException("start > end");

				if(li.getStartIP() < lastEndLong)
					throw new IllegalArgumentException(
							"File not sorted, start of range "
									+ li.getStartIP()
									+ " and end of previous range "
									+ lastEndLong + " " + li);

				String id2c = li.getId2c();
				String id3c = li.getId3c();
				String name = li.getName();

				Country country = null;
				if (id2c != null) // if not dummy range.
				{
					country = new Country(id2c, id3c, name);
					if(!h.containsKey(id2c))
					{
						h.put(id2c, country);
					}
				}

				if(firstHalf)
				{
					if(li.getStartIP() > Integer.MAX_VALUE)
					{
						// end of first table. close range.
						firstHalf = false;
						numRangesInTable++;
						lastEndLong = -1;
						lastEndInt = -1;

						// update header with info about first table.
						long tableHalfOffset = dout.getFilePointer();
						if(tableHalfOffset > Integer.MAX_VALUE)
							throw new RuntimeException("Invalid offset");
						cur = (int) dout.getFilePointer();
						dout.seek(offsets + 4);
						dout.writeInt(numRangesInTable);
						dout.writeInt(cur);
						dout.seek(cur);

						numRangesInTable = 0;

					}
				}

				int startInt;
				int endInt;
				if(firstHalf)
				{
					startInt = toInt(toBytes(li.getStartIP()));
					endInt = toInt(toBytes(li.getEndIP()));
				}
				else
				{
					long nip = li.getStartIP() - Integer.MAX_VALUE;
					startInt = toInt(toBytes(nip));
					if(li.getEndIP() > (long) Integer.MAX_VALUE * 2)
					{
						li.setEndIP(((long) Integer.MAX_VALUE) * 2);
					}
					endInt = toInt(toBytes(li.getEndIP() - Integer.MAX_VALUE));
				}

				lastEndLong = li.getEndIP();
				lastEndInt = endInt;
				dout.write(toBytes(startInt));
				dout.writeShort(country != null ? country.get2c() : 0);
				numRangesInTable++;

			}
			catch (RuntimeException e)
			{
				System.err.println("Error parsing : " + li.getLineNum());
				throw e;
			}
		}

		// finish up the last element
		dout.write(toBytes(lastEndInt));
		dout.writeShort(0); // dummy

		cur = (int) dout.getFilePointer();
		dout.seek(offsets + 12);
		dout.writeInt(numRangesInTable); // num ranges in second half
		dout.writeInt(cur); // countries offset
		dout.writeInt(h.size()); // num countries
		dout.seek(cur);

		int countryCodesOffset = cur;

		Set keys = h.keySet();
		for (Iterator iter = keys.iterator(); iter.hasNext();)
		{
			String key = (String) iter.next();
			Country country = (Country) h.get(key);
			short id2c = country.get2c();
			int id3c = country.get3c();
			dout.writeShort(id2c);
			dout.writeInt(id3c);
			dout.writeInt(0xcccccccc); // name offset place holder
		}

		int n = 0;
		for (Iterator iter = keys.iterator(); iter.hasNext();)
		{
			String key = (String) iter.next();
			Country country = (Country) h.get(key);
			String name = country.getName();
			long nameOffset = dout.getFilePointer();
			if(nameOffset > Integer.MAX_VALUE)
				throw new RuntimeException("Invalid offset");
			dout.seek(countryCodesOffset + (n * 10) + 6);
			dout.writeInt((int) nameOffset);
			dout.seek(nameOffset);
			dout.writeShort(name.length());
			dout.writeBytes(name);
			n++;
		}

		dout.close();
	}

	/**
	 * This function ensures that there are no holes in the list and that there is no element that overlaps both first and second tables (passes above Integer.MAX_INT).
	 * @param lines
	 */
	private static List normalizeList(List lines)
	{
		List res = new Vector();
		long lastEnd = -1;
		boolean firstHalf = true;
		for (int i = 0; i < lines.size(); i++)
		{
			LineInfo line = (LineInfo) lines.get(i);
			// close holes in the list.
			if(lastEnd + 1 < line.getStartIP())
			{
				long next = lines.size() > i ? ((LineInfo)lines.get(i)).getStartIP() : Integer.MAX_VALUE + 1; // -1 follows.
				LineInfo dummy = new LineInfo(lastEnd+1, next-1, null, null, "dummy", -1); // inserted lines have line number -1
				res.add(dummy);
			}
			res.add(line);
			
			if (firstHalf)
			{
				if (i == lines.size() - 1)
				{
					// if last item in the list is in the first table, close the first table and add a dummy range into the second table.
					LineInfo part1 = new LineInfo(line.getEndIP()+1, Integer.MAX_VALUE, null, null, "dummy", -1);
					LineInfo part2 = new LineInfo((long)Integer.MAX_VALUE+1, Integer.MAX_VALUE * (long)2, null, null, "dummy", -1);
					res.add(part1);
					res.add(part2);
					break;
				}
				else // if crossing to second half.
				if (line.getStartIP() < Integer.MAX_VALUE && line.getEndIP() > Integer.MAX_VALUE)
				{
					// end of first table. close range.
					LineInfo part1 = new LineInfo(line.getStartIP(), Integer.MAX_VALUE-1, line.getId2c(), line.getId3c(), line.getName(), line.getLineNum());
					LineInfo part2 = new LineInfo(Integer.MAX_VALUE, line.getEndIP(), line.getId2c(), line.getId3c(), line.getName(), line.getLineNum());
					res.add(part1);
					res.add(part2);
					firstHalf = false;
				}
				else
				if (line.getStartIP() > Integer.MAX_VALUE)
				{
					firstHalf = false;
				}
			}
			
			lastEnd = line.getEndIP();
		}
		return res;
	}

	static int toInt(byte a[])
	{
		return ((a[0] << 24) & 0xff000000) | ((a[1] << 16) & 0x00ff0000)
				| ((a[2] << 8) & 0x0000ff00) | (a[3] & 0x000000ff);
	}

	static byte[] toBytes(long i)
	{
		// break down to bytes
		byte a[] = new byte[4];
		a[0] = (byte) ((i & 0xff000000l) >> 24);
		a[1] = (byte) ((i & 0x00ff0000l) >> 16);
		a[2] = (byte) ((i & 0x0000ff00l) >> 8);
		a[3] = (byte) ((i & 0x000000ffl) >> 0);
		return a;
	}

	public static void main(String[] args) throws IOException
	{
		if(args.length < 2)
		{
			usage();
		}

		String cmd = args[0];
		if (cmd.equals("-r"))
		{
			String ip = args[1];
			IP2Country ip2c = new IP2Country();
			Country c = ip2c.getCountry(ip);
			if(c == null)
			{
				System.out.println("UNKNOWN");
			}
			else
			{
				System.out.println(c.get2cStr() + "," + c.get3cStr() + ","
						+ c.getName());
			}
		}
		else
		if (cmd.equals("-c"))
		{
			String bin;
			String csv = args[1];
			int i = csv.toLowerCase().lastIndexOf(".csv");
			if (i == -1) throw new IllegalArgumentException("Input file name should end with csv");
			if (args.length == 2)
			{
				bin = args[1].substring(0, csv.length() - ".csv".length()) + ".bin";
			}
			else
			{
				bin = args[2];
			}
			System.out.println("converting " + csv + " to " + bin);
			convertCSVtoBIN(csv, bin);
		}
		else
		{
			usage();
		}
	}

	private static void usage()
	{
		System.err.println("Usage : ");
		System.err.println("java -jar ip2c.jar -r|-c ...");
		System.err.println();
		System.err.println("\t-r : Resolve an IP address");	
		System.err.println("\t-c : Build binary file from CSV");	
		System.err.println();
		System.err.println();
		System.err.println("-= Resolve an ip address =-");
		System.err.println("java -jar ip2c.jar -r ip-address");
		System.err.println("Output format:");
		System.err.println("if not found:");
		System.err.println("UNKNOWN");
		System.err.println();
		System.err.println("if found:");
		System.err.println("2C 3C NAME");
		System.err.println();
		System.err.println("Example:");
		System.err.println("java -jar ip2c.jar -r 85.64.225.159");
		System.err.println("Outputs:");
		System.err.println("IL ISR ISRAEL");
		System.err.println();
		System.err.println();
		System.err.println("-= Build binary file from CSV =-");
		System.err.println("java -jar ip2c.jar -c csv_file [bin_file]");
		System.err.println("if bin file is not specified, the name of the csv will be used.");
		System.err.println();
		System.err.println("supported CSV formats:");
		System.err.println("webhosting.info : http://ip-to-country.webhosting.info/");
		System.err.println("software77 : http://software77.net/cgi-bin/ip-country/geo-ip.pl");
		
		System.exit(1);

	}

	private int readInt() throws IOException
	{
		return m_input.readInt();
	}

	private short readShort() throws IOException
	{
		return m_input.readShort();
	}

	private void readFully(byte[] a) throws IOException
	{
		m_input.readFully(a);
	}

	private void seek(int n) throws IOException
	{
		m_input.seek(n);
	}

}
