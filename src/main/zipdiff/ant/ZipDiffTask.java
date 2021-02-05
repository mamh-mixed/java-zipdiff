/*
 * 
 * 
 */

package zipdiff.ant;

import zipdiff.DifferenceCalculator;
import zipdiff.Differences;
import zipdiff.output.*;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import java.io.File;
import java.io.FileOutputStream;

/**
 * 
 * 
 * 
 * @author Sean C. Sullivan
 *
 * 
 */
public class ZipDiffTask extends Task 
{
	private String filename1;
	private String filename2;
	private String destfile;
	
	public void setFilename1(String name)
	{
		filename1 = name;
	}
	
	public void setFilename2(String name)
	{
		filename2 = name;
	}
	
	public void execute() throws BuildException
	{
		validate();
		
		// this.log("Filename1=" + filename1, Project.MSG_DEBUG);
		// this.log("Filename2=" + filename2, Project.MSG_DEBUG);
		// this.log("destfile=" + getDestFile(), Project.MSG_DEBUG);
		
		Differences d = calculateDifferences();
		
		try
		{
			writeDestFile(d);
		}
		catch (java.io.IOException ex)
		{
			throw new BuildException(ex);
		}
		
	}

	protected void writeDestFile(Differences d)
		throws java.io.IOException
	{
		String destfilename = getDestFile();
		
		File f = new File(destfilename);
		
		FileOutputStream fos = null;
		
		Builder builder = null;
		
		if (destfilename.endsWith(".html"))
		{
			builder = new HtmlBuilder();
		}
		else if (destfilename.endsWith(".xml"))
		{
			builder = new XmlBuilder();
		}
		else
		{
			builder = new TextBuilder();
		}
		
		boolean bSuccessful = false;
		
		try
		{
			fos = new FileOutputStream(f);
			builder.build(fos, d);
			fos.flush();
			bSuccessful = true;
		}
		finally
		{
			if (fos != null)
			{
				fos.close();
			}
			if ( ! bSuccessful )
			{
				f.delete();
			}
		}
	}
	
	public String getDestFile()
	{
		return destfile;
	}
	
	public void setDestFile(String name)
	{
		destfile = name;
	}
	
	protected Differences calculateDifferences()
		throws BuildException
	{
		DifferenceCalculator calculator;
		
		Differences d = null;
		
		try
		{
			calculator = new DifferenceCalculator(filename1, filename2);
			d = calculator.getDifferences();
		}
		catch (java.io.IOException ex)
		{
			throw new BuildException(ex);
		}
		
		return d;
	}
	
	protected void validate() throws BuildException
	{
		if ( (filename1 == null) || (filename1.length() < 1) )
		{
			throw new BuildException("filename1 is required");
		}
		
		if ( (filename2 == null) || (filename2.length() < 1) )
		{
			throw new BuildException("filename2 is required");
		}
		
		String destinationfile = getDestFile();
		
		if ( (destinationfile == null) || (destinationfile.length() < 1) )
		{
			throw new BuildException("destfile is required");
		}
	}
	
}
