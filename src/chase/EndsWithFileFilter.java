package chase;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * Matches all files that end with the given ending.
 */
public class EndsWithFileFilter extends FileFilter {
	private String end;
	private String desc;
	
	public EndsWithFileFilter(String ending, String description) {
		end = ending;
		desc = description;
	}
	
	@Override
	public boolean accept(File f) {
		if(f.isDirectory() || f.getName().endsWith(end))
			return true;
		return false;
	}

	@Override
	public String getDescription() {
		return desc;
	}

}
