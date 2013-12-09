package robowiki.runner.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Properties;

import com.google.common.base.Charsets;

public class Options {
	private Options() {}
	
	public static File robocodeLibDir = new File("/robocode/libs");
	public static File challengeDir = new File("challenges");
	public static File runnerDir = new File("robocodes");
	public static File robotsDir = new File("robots");
	public static String jvmArgs = "-Xmx512M";
	
	public static void loadOptions() {
		Properties config = new Properties();
		
		try {
			FileInputStream fis = new FileInputStream("config");
			config.load(new InputStreamReader(fis,Charsets.UTF_8));
			fis.close();
			
			robocodeLibDir = new File(config.getProperty("robocodelib_dir","/robocode/libs"));
			challengeDir = new File(config.getProperty("challenges_dir","challenges"));
			runnerDir = new File(config.getProperty("robots_dir","robocodes"));
			robotsDir = new File(config.getProperty("runner_dir","robots"));
			jvmArgs = config.getProperty("jvm_args","-Xmx512M");
		} catch (IOException e) {}
	}
	
	public static void saveOptions() {
		Properties config = new Properties();
		
		config.setProperty("robocodelib_dir",robocodeLibDir.getPath());
		config.setProperty("challenges_dir",challengeDir.getPath());
		config.setProperty("robots_dir",robotsDir.getPath());
		config.setProperty("runner_dir",robotsDir.getPath());
		config.setProperty("jvm_args",jvmArgs);
		
		
		try {
			FileOutputStream fos = new FileOutputStream("config");
			config.store(new OutputStreamWriter(fos,Charsets.UTF_8), null);
			fos.close();
		} catch (IOException e) {}
		
	}
	
	public static String transformPath(File file) {
		String root = new File(".").getAbsoluteFile().getParent();
		root = root.replace('\\', '/');
		if(!root.endsWith("/"))
			root += "/";
		String path = file.getAbsolutePath();
		path = path.replace('\\', '/');
		
		if(path.startsWith(root)) {
			path = path.substring(root.length());
		}
		
		return path;
	}
	
	public static String getRunnerDirectory(int index) {
		File dir = new File(Options.runnerDir,"r"+index);
		return dir.getPath();
	}
}
