/*
 * 
 * 
 */
package zipdiff;

import java.util.*;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.regex.*;
import java.io.*;

/**
 * 
 * @author Sean C. Sullivan
 *
 * 
 * 
 */
public class DifferenceCalculator {
	private ZipFile file1;
	private ZipFile file2;
	private boolean ignoreTimestamps = false;
	private boolean compareCRCValues = true;
	private Pattern filesToIgnorePattern;
	private boolean bVerbose = false;
	
	protected void debug(Object msg)
	{
		if (isVerboseEnabled())
		{
			System.out.println("["
					+ DifferenceCalculator.class.getName()
					+ "] " 
					+ String.valueOf(msg));
		}
	}
	
	public void setVerbose(boolean b)
	{
		bVerbose = b;
	}
	
	protected boolean isVerboseEnabled()
	{
		return bVerbose;
	}
	
	public DifferenceCalculator(String filename1, String filename2)
		throws java.io.IOException
	{
		this(new File(filename1), new File(filename2));
	}
	public DifferenceCalculator(File f1, File f2)
		throws java.io.IOException
	{
		this(new ZipFile(f1), new ZipFile(f2));
    }

	/**
	 * 
	 * @param patterns a List of zero or more strings
	 * 
	 */
	public void setFilenamesToIgnore(Set patterns)
	{
		if (patterns == null)
		{
			filesToIgnorePattern = null;
		}
		else if (patterns.isEmpty())
		{
			filesToIgnorePattern = null;
		}
		else
		{
			String regex = "";
			
			Iterator iter = patterns.iterator();
			while (iter.hasNext())
			{
				String pattern = (String) iter.next();
				if (regex.length() > 0)
				{
					regex += "|";
				}
				regex += "(" + pattern + ")";
			}
			filesToIgnorePattern = Pattern.compile(regex);
		}
	}
	
	protected boolean ignoreThisFile(String filename)
	{
		if (filename == null)
		{
			return false;
		}
		else if (filesToIgnorePattern == null)
		{
			return false;
		}
		else
		{
			Matcher m = filesToIgnorePattern.matcher(filename);
			return m.matches();
		}
	}
	
	public DifferenceCalculator(ZipFile zf1, ZipFile zf2)
    {
		file1 = zf1;
		file2 = zf2;
    }
	
	public void setCompareCRCValues(boolean b)
	{
		compareCRCValues = b;
	}
	
	public boolean getCompareCRCValues()
	{
		return compareCRCValues;
	}
	
	protected Map buildZipEntryMap(ZipFile zf)
		throws java.io.IOException
	{
		Map m = new HashMap();
		
		Enumeration entries = zf.entries();
		while (entries.hasMoreElements())
		{
			ZipEntry entry = (ZipEntry) entries.nextElement();
			InputStream is = null;
			try
			{
				is = zf.getInputStream(entry);
				processZipEntry("", entry, is, m);
			}
			finally
			{
				if (is != null)
				{
					is.close();
				}
			}
		}
		
		return m;
	}

	
	protected void processZipEntry(String prefix, ZipEntry entry, InputStream is, Map m)
		throws IOException
	{
		if (ignoreThisFile(entry.getName()))
		{
			debug("ignoring file: " + entry.getName());
		}
		else
		{
			String name = prefix + entry.getName();
			
			debug("processing ZipEntry: " + name);
			
			if (entry.isDirectory())
			{
				m.put(name, entry);
			}
			else if ( isZipFile(name) )
			{
				processEmbeddedZipFile(
						entry.getName() + "/", 
						is, 
						m);
				m.put(name, entry);
			}
			else
			{
				m.put(name, entry);
			}
		}
	}
	
	protected void processEmbeddedZipFile(String prefix, InputStream is, Map m) 
		throws java.io.IOException
	{
		ZipInputStream zis = new ZipInputStream(is);
		
		ZipEntry entry = zis.getNextEntry();
		
		while ( entry != null )
		{
			processZipEntry(prefix, entry, zis, m);
			zis.closeEntry();
			entry = zis.getNextEntry();
		}
		
	}
	protected boolean isZipFile(String filename)
	{
		boolean result;
		
		if (filename == null)
		{
			result = false;
		}
		else
		{
			String lowercaseName = filename.toLowerCase();
			if (lowercaseName.endsWith(".zip"))
			{
				result = true;
			}
			else if (lowercaseName.endsWith(".ear"))
			{
				result = true;
			}
			else if (lowercaseName.endsWith(".war"))
			{
				result = true;
			}
			else if (lowercaseName.endsWith(".rar"))
			{
				result = true;
			}
			else if (lowercaseName.endsWith(".jar"))
			{
				result = true;
			}
			else
			{
				result = false;
			}
		}
		
		return result;
	}
	
	protected Differences calculateDifferences(ZipFile zf1, ZipFile zf2)
		throws java.io.IOException
	{
		Map map1 = buildZipEntryMap(zf1);
		Map map2 = buildZipEntryMap(zf2);

		return calculateDifferences(map1, map2);
	}
	
	protected Differences calculateDifferences(Map m1, Map m2)
	{
		Differences d = new Differences();
		
		Set names1 = m1.keySet();
		Set names2 = m2.keySet();
		
		Set allNames = new HashSet();
		allNames.addAll(names1);
		allNames.addAll(names2);
		
		Iterator iterAllNames = allNames.iterator();
		while (iterAllNames.hasNext())
		{
			String name = (String) iterAllNames.next();
			if (names1.contains(name) && ( ! names2.contains(name)) )
			{
				d.fileRemoved( name, (ZipEntry) m1.get(name) );
			}
			else if ( names2.contains(name) && ( ! names1.contains(name)) )
			{
				d.fileAdded( name, (ZipEntry) m2.get(name));
			}
			else if (names1.contains(name) && (names2.contains(name)) )
			{
				ZipEntry entry1 = (ZipEntry) m1.get(name);
				ZipEntry entry2 = (ZipEntry) m2.get(name);
				if ( ! entriesMatch(entry1, entry2) )
				{
					d.fileChanged(name, entry1, entry2);
				}
			}
			else
			{
				throw new IllegalStateException("todo");
			}
		}
		
		return d;
	}

	protected boolean entriesMatch(ZipEntry entry1, ZipEntry entry2)
	{
		boolean result;
		
		result = (entry1.isDirectory() == entry2.isDirectory())
			&& (entry1.getSize() == entry2.getSize())
			&& (entry1.getCompressedSize() == entry2.getCompressedSize())
			&& (entry1.getName().equals(entry2.getName()));
		
		if (! isIgnoringTimestamps())
		{
			result = result && (entry1.getTime() == entry2.getTime());
		}
		
		if (getCompareCRCValues())
		{
			result = result && (entry1.getCrc() == entry2.getCrc());
		}
		return result;
	}
	
	public void setIgnoreTimestamps(boolean b)
	{
		ignoreTimestamps = b;
	}
	
	public boolean isIgnoringTimestamps()
	{
		return ignoreTimestamps;
	}
	
	public Differences getDifferences() throws java.io.IOException
	{
		Differences d = calculateDifferences(file1, file2);
		d.setFilename1(file1.getName());
		d.setFilename2(file2.getName());
		
		return d;
	}
}
