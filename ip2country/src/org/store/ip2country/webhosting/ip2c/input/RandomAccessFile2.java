package org.store.ip2country.webhosting.ip2c.input;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

public class RandomAccessFile2 extends RandomAccessFile implements RandomAccessInput
{
	public RandomAccessFile2(String file, String mode) throws FileNotFoundException
	{
		super(file, mode);
	}
}
