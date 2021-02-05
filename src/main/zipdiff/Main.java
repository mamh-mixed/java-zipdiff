/*
 * 
 * 
 */
package zipdiff;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author Sean C. Sullivan
 * 
 */
public class Main {
	private static final int EXITCODE_ERROR = 1;
	
	public static void main(String[] args) 
	{
		if (args.length != 2)
		{
			printHelp();
			System.exit(EXITCODE_ERROR);
		}
		try
		{
			String filename1 = args[0];
			String filename2 = args[1];
			
			File f1 = new File(filename1);
			File f2 = new File(filename2);
			
			checkFile(f1);
			checkFile(f2);
			
			System.out.println("File 1 = " + filename1);
			System.out.println("File 2 = " + filename2);
			
			DifferenceCalculator calc = new DifferenceCalculator(
					f1, f2);
			
			Set patterns = new HashSet();
			patterns.add(".*ignore.*");
			
			calc.setFilenamesToIgnore(patterns);
			calc.setCompareCRCValues(true);
			calc.setIgnoreTimestamps(true);
			
			Differences d = calc.getDifferences();
			if (d.hasDifferences())
			{
				System.out.println(d);
				System.out.println(
						d.getFilename1()
						+ " and "
						+ d.getFilename2()
						+ " are different.");
			}
			else
			{
				System.out.println("No differences found.");
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			System.exit(EXITCODE_ERROR);
		}
		
	}
	
	private static void printHelp()
	{
		System.out.println("Parameters: filename1 filename2");
	}
	
	private static void checkFile(java.io.File f)
	{
		String filename = f.toString();
		
		if ( ! f.exists() )
		{
			System.err.println(
					"'"
					+ filename 
					+ "' does not exist");
			System.exit(EXITCODE_ERROR);
		}
		
		if ( ! f.canRead() )
		{
			System.err.println(
					"'"
					+ filename 
					+ "' is not readable");
			System.exit(EXITCODE_ERROR);
		}
		
		if (f.isDirectory())
		{
			System.err.println(
					"'"
					+ filename
					+ "' is a directory");
			System.exit(EXITCODE_ERROR);
		}
		
	}
}
