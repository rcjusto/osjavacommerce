package org.store.ip2country.webhosting.ip2c.tests;

import org.store.ip2country.webhosting.ip2c.Country;
import org.store.ip2country.webhosting.ip2c.IP2Country;
import org.store.ip2country.webhosting.ip2c.LineInfo;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Provides means of checking the BIN format against the CSV format.
 * This should prove that the CSV and BIN files are identical in the
 * sense of lookup results. 
 * 
 * Run the main class by providing the CSV file and BIN file as arguments.
 * The class will also rerun the CSV to BIN conversion to avoid the problem
 * of testing wrong BIN to the CSV.
 */
public class CSVBinCompare
{
    public static Logger log = Logger.getLogger(CSVBinCompare.class);
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException
	{
		if (args.length != 1)
		{
			showUsage();
			System.exit(0);
		}
		
		String line = null;
		try
		{
			String csvFile = args[0];
			File temp = File.createTempFile("ip2c",".bin");
			List csvLines = IP2Country.convertCSVtoBIN(csvFile, temp.getAbsolutePath());
		
			IP2Country ip2c = new IP2Country(temp.getAbsolutePath(), IP2Country.MEMORY_CACHE);
			
			for(int i=0;i<csvLines.size();i++)
			{
				LineInfo li = (LineInfo)csvLines.get(i);

				if (li.getName().equals("RESERVED")) continue; // skip reserved.
				if (li.getId2c() == null && li.getName().equals("dummy")) continue; // skip dummy
				
				Country expected = new Country(li.getId2c(), li.getId3c(), li.getName());
				test(ip2c, expected, li.getStartIP());
				
				if (li.getEndIP() - li.getStartIP() > 1)
					test(ip2c, expected, li.getStartIP()+1);
				
				if (li.getEndIP() < (long)Integer.MAX_VALUE * 2)
					test(ip2c, expected, li.getEndIP());
				
				if (li.getEndIP() - li.getStartIP() > 1 && (li.getEndIP()-1) < (long)Integer.MAX_VALUE * 2)
					test(ip2c, expected, li.getEndIP()-1);
				
				test(ip2c, expected, (li.getStartIP()+li.getEndIP())/2);
			}	
			System.out.println("test passed");	
		} 
		catch (Error e)
		{
            log.error(e.getMessage(), e);
		}
		
	}
	
	private static void showUsage() 
	{
//		System.out.println("Tests that the specified CSV file can be converted correctly");
//		System.out.println("Usage: net.firefang.ip2c.IP2CountryTest CSVFILE");
	}

	private static void test(IP2Country ip2c, Country expected, long ip) throws IOException
	{
		String ips = getIPString(ip);
		Country country = ip2c.getCountry(ips);
		
		if (expected == null && country == null) return;
		
		if (expected == null && country != null) {
			throw new RuntimeException("Expected " + expected + ", got " + country + " ||| " + ip);
		}
		
		if (expected != null && country == null) { 
			throw new RuntimeException("Expected " + expected + ", got " + country + " ||| " + ip);
		}
		
		if (!(country.equals(expected)))
		{
			throw new RuntimeException("Expected " + expected + ", got " + country + " ||| " + ip);
		}

	}

	private static String getIPString(long i)
	{
		short a1 = (short) ((i & 0xff000000l) >> 24);
		short a2 = (short) ((i & 0x00ff0000l) >> 16);
		short a3 = (short) ((i & 0x0000ff00l) >> 8);
		short a4 = (short) ((i & 0x000000ffl) >> 0);
		
		return a1 + "." + a2 + "." + a3 + "." + a4;
	}

}
