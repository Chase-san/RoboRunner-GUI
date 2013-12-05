package chase;

import javax.swing.UIManager;

public class WindowToolkit {
	public static void setFileChooserReadOnly(boolean readOnly) {
		UIManager.put("FileChooser.readOnly", readOnly);
	}
}
