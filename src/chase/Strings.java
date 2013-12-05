package chase;

import java.util.ArrayList;

/**
 * Some basic String methods.
 * @author Robert Maupin (Chase)
 */
public class Strings {

	/**
	 * Preserves null sections. "ab..cd" would produce [ab][][cd] with '.'
	 * 
	 * @param src
	 *            The source string
	 * @param delim
	 *            The delimiter string to split by
	 * @return An array of strings.
	 */
	public static final String[] split(final String src, final char delim) {
		final ArrayList<String> output = new ArrayList<String>();
		int index = 0;
		int lindex = 0;
		while ((index = src.indexOf(delim, lindex)) != -1) {
			output.add(src.substring(lindex, index));
			lindex = index + 1;
		}
		output.add(src.substring(lindex));
		return output.toArray(new String[output.size()]);
	}

	/**
	 * Preserves null sections. "ab..cd" would produce [ab][][cd] with "."
	 * 
	 * @param src
	 *            The source string
	 * @param delim
	 *            The delimiter string to split by
	 * @return An array of strings.
	 */
	public static final String[] split(final String src, final String delim) {
		final ArrayList<String> output = new ArrayList<String>();
		final int len = delim.length();
		int index = 0;
		int lindex = 0;
		while ((index = src.indexOf(delim, lindex)) != -1) {
			output.add(src.substring(lindex, index));
			lindex = index + len;
		}
		output.add(src.substring(lindex));
		return output.toArray(new String[output.size()]);
	}
	
	/**
	 * Returns the number of times the given character occurs within the source string.
	 */
	public static final int count(final String src, final char chr) {
		int count = 0;
		int index = 0;
		while ((index = src.indexOf(chr, index)) != -1) {
			++index;
			++count;
		}
		return count;
	}
}
