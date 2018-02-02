
package org.store.ip2country.webhosting.ip2c.tests;

import org.store.ip2country.webhosting.ip2c.Country;
import org.store.ip2country.webhosting.ip2c.IP2Country;
import org.store.ip2country.webhosting.ip2c.Utils;

import java.io.IOException;

public class DatabaseValidityTest
{
	public static void main(String[] args) throws Exception
	{
		test(IP2Country.MEMORY_CACHE);
		// test(IP2Country.MEMORY_MAPPED);
		// test(IP2Country.NO_CACHE);
	}

	private static void test(int cacheMode) throws IOException
	{
		switch (cacheMode)
		{
			case IP2Country.NO_CACHE :
				System.err.println("Testing with NO_CACHE");
			break;
			case IP2Country.MEMORY_MAPPED :
				System.err.println("Testing with MEMORY_MAPPED");
			break;
			case IP2Country.MEMORY_CACHE :
				System.err.println("Testing with MEMORY_CACHE");
			break;
		}
		// webhosting
		IP2Country ip2c_webhosting = new IP2Country(cacheMode);

		// software77
		IP2Country ip2c_software77 = new IP2Country("software77.ip2c.bin",
				cacheMode);
		String ips[] = new String[100000];
		// randomize 100,000 ip addresses.
		for (int i = 0; i < ips.length; i++)
		{
			ips[i] = Utils.randomizeIP();
		}

		for (int i = 0; i < ips.length; i++)
		{
			if(i % 5000 == 0)
				System.err.println((i / 1000) + "% done");
			Country c1 = ip2c_webhosting.getCountry(ips[i]);
			Country c2 = ip2c_software77.getCountry(ips[i]);

			boolean equals = false;
			if((c1 == null && c2 != null) || (c2 == null && c1 != null))
			{
			}
			else if(c1 == null && c2 == null)
			{
				equals = true;
			}
			else
			{
				equals = c1.equals(c2);
			}

			if(!equals)
				System.out.println(c1 + " " + c2);
		}
	}
}
