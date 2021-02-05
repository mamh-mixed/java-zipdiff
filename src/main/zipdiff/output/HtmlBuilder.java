/*
 * 
 * 
 */
package zipdiff.output;

import zipdiff.Differences;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.util.Set;
import java.util.Iterator;

/**
 * 
 * 
 * 
 * @author Sean C. Sullivan
 *
 * 
 */
public class HtmlBuilder implements Builder 
{
	
	public void build(OutputStream out, Differences d)
	{
		PrintWriter pw = new PrintWriter(out);
		
		pw.println("<html>");
		pw.println("<head>");
		pw.println("<title>File differences</title>");
		pw.println("</head>");
		
		pw.println("<body>");

		pw.print("File 1: ");
		String filename1 = d.getFilename1();
		
		if (filename1 == null)
		{
			filename1 = "filename1.zip";
		}
		pw.print(filename1);
		pw.println("<br>");
		
		pw.print("File 2: ");
		
		String filename2 = d.getFilename2();
		
		if (filename2 == null)
		{
			filename2 = "filename2.zip";
		}
		pw.print(filename2);
		pw.println("<br>");
		
		writeAdded(pw, d.getAdded().keySet());
		writeRemoved(pw, d.getRemoved().keySet());
		writeChanged(pw, d.getChanged().keySet());
		pw.println("<hr>");
		pw.println("Generated at " + new java.util.Date());
		pw.println("</body>");
		
		pw.println("</html");
	
		pw.flush();
		
	}
	
	protected void writeAdded(PrintWriter pw, Set added)
	{
		pw.println("<h3>Added</h3>");
		if (added.size() > 0)
		{
			pw.println("<ul>");
			Iterator iter = added.iterator();
			while (iter.hasNext())
			{
				String key = (String) iter.next();
				pw.print("<li>");
				pw.print(key);
				pw.println("</li>");
			}
			pw.println("</ul>");
		}
		else
		{
			pw.println("None");
		}
		
	}
	
	protected void writeRemoved(PrintWriter pw, Set removed)
	{
		pw.println("<h3>Removed</h3>");
		if (removed.size() > 0)
		{
			pw.println("<ul>");
			Iterator iter = removed.iterator();
			while (iter.hasNext())
			{
				String key = (String) iter.next();
				pw.print("<li>");
				pw.print(key);
				pw.println("</li>");
			}
			pw.println("</ul>");
		}
		else
		{
			pw.println("None");
		}
	
	}

	protected void writeChanged(PrintWriter pw, Set changed)
	{
		pw.println("<h3>Changed</h3>");
		if (changed.size() > 0)
		{
			pw.println("<ul>");
			Iterator iter = changed.iterator();
			while (iter.hasNext())
			{
				String key = (String) iter.next();
				pw.print("<li>");
				pw.print(key);
				pw.println("</li>");
			}
			pw.println("</ul>");
		}
		else
		{
			pw.println("None");
		}
	}

}
