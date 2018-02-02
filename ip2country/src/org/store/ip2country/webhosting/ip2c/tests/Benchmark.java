package org.store.ip2country.webhosting.ip2c.tests;

import org.store.ip2country.webhosting.ip2c.IP2Country;
import org.store.ip2country.webhosting.ip2c.Utils;

import java.io.IOException;

public class Benchmark
{
	public static void main(String[] args) throws IOException
	{
		test(IP2Country.MEMORY_CACHE);
		test(IP2Country.MEMORY_MAPPED);
		test(IP2Country.NO_CACHE);
	}
	
	private static void test(int cacheMode) throws IOException
	{
		switch(cacheMode)
		{
			case IP2Country.NO_CACHE:
				System.err.println("Testing with NO_CACHE");
				break;
			case IP2Country.MEMORY_MAPPED:
				System.err.println("Testing with MEMORY_MAPPED");
				break;
			case IP2Country.MEMORY_CACHE:
				System.err.println("Testing with MEMORY_CACHE");
				break;
		}		
		IP2Country ip2c = new IP2Country(cacheMode);
		String ips[] = new String[100000];
		// randomize 100,000 ip addresses.
		for (int i = 0; i < ips.length; i++)
		{
			ips[i] = Utils.randomizeIP();
		}
		
		long now = System.currentTimeMillis();
		for (int i = 0; i < ips.length; i++)
		{
			if (i % 5000 == 0) System.err.println((i / 1000) + "% done");
			ip2c.getCountry(ips[i]);
		}
		long t = System.currentTimeMillis() - now;
		float sec = t / 1000f;
		
		System.err.println("Took " + t + " for 100,000 searches ("+100000f / sec+" searches/sec)");

	}
}
