/*
 * 
 * 
 */
package zipdiff.output;

import java.io.OutputStream;
import zipdiff.Differences;

/**
 * 
 * @author Sean C. Sullivan
 *
 * 
 * 
 */
public interface Builder {
	public void build(OutputStream out, Differences d);
}
