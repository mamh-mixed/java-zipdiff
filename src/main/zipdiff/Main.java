/*
 * 
 * 
 */
package zipdiff;

import java.io.File;
import org.apache.commons.cli.*;

/**
 * 
 * @author Sean C. Sullivan
 * 
 */
public class Main {
	private static final int EXITCODE_ERROR = 1;
	private static final String OPTION_COMPARE_CRC_VALUES = "comparecrcvalues";
	private static final String OPTION_COMPARE_TIMESTAMPS = "comparetimestamps";
	private static final String OPTION_FILE1 = "file1";
	private static final String OPTION_FILE2 = "file2";
	private static final Options options;
	
	// static initializer
	static
	{
		options = new Options();

		Option compareTS = new Option(OPTION_COMPARE_TIMESTAMPS, 
				OPTION_COMPARE_TIMESTAMPS, 
				false, 
				"Compare timestamps");
		compareTS.setRequired(false);
		
		Option compareCRC = new Option(OPTION_COMPARE_CRC_VALUES, 
				OPTION_COMPARE_CRC_VALUES, 
				false, 
				"Compare CRC values");
		compareCRC.setRequired(false);
		
		Option file1 = new Option(
				OPTION_FILE1, 
				OPTION_FILE1,
				true, 
				"first file");
		file1.setRequired(true);
		
		Option file2 = new Option(
				OPTION_FILE2,
				OPTION_FILE2,
				true, 
				"second file");
		file2.setRequired(true);
		
		options.addOption(compareTS);
		options.addOption(compareCRC);
		options.addOption(file1);
		options.addOption(file2);
	}

	public static void main(String[] args) 
	{
		CommandLineParser parser = new GnuParser();
		
		try
		{
			CommandLine line = parser.parse(options, args);
			
			String filename1 = null;
			String filename2 = null;
			
			filename1 = line.getOptionValue(OPTION_FILE1);
			filename2 = line.getOptionValue(OPTION_FILE2);
				
			File f1 = new File(filename1);
			File f2 = new File(filename2);
			
			checkFile(f1);
			checkFile(f2);
			
			System.out.println("File 1 = " + f1);
			System.out.println("File 2 = " + f2);
			
			DifferenceCalculator calc = new DifferenceCalculator(
					f1, f2);
			
			// todo - calc.setFilenamesToIgnore();
			
			if (line.hasOption(OPTION_COMPARE_CRC_VALUES))
			{
				calc.setCompareCRCValues(true);
			}
			else
			{
				calc.setCompareCRCValues(false);
			}

			if (line.hasOption(OPTION_COMPARE_TIMESTAMPS))
			{
				calc.setIgnoreTimestamps(false);
			}
			else
			{
				calc.setIgnoreTimestamps(true);
			}
			
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
		catch (UnrecognizedOptionException ex)
		{
			System.err.println(ex.getMessage());
			System.exit(EXITCODE_ERROR);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			System.exit(EXITCODE_ERROR);
		}
		
	}
	
	private static void printHelp()
	{
		System.out.println("Parameters: -file1 filename1 -file2 filename2");
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
