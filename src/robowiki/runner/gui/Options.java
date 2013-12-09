package robowiki.runner.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Properties;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class Options {
	private Options() {}
	
	private static final String JVM_ARGS_PROPERTY = "jvmArgs";
	private static final String BOTS_DIR_PROPERTY = "botsDir";
	private static final String RUNNER_DIR_PROPERTY = "runnerDir";
	private static final String CHALLENGES_DIR_PROPERTY = "challengeDir";
	private static final String ROBOCODE_PATH_PROPERTY = "robocodeLibDir";
	private static final String THREADS_PROPERTY = "threads";
	
	public static File robocodeLibDir = new File("/robocode/libs");
	public static File challengeDir = new File("challenges");
	public static File runnerDir = new File("robocodes");
	public static File robotsDir = new File("robots");
	public static String jvmArgs = "-Xmx512M";
	public static int threads = 0;
	
	public static void loadOptions() {
		Properties config = new Properties();
		
		try {
			FileInputStream fis = new FileInputStream("config");
			config.load(new InputStreamReader(fis,Charsets.UTF_8));
			fis.close();
			
			robocodeLibDir = new File(config.getProperty(ROBOCODE_PATH_PROPERTY,"/robocode/libs"));
			challengeDir = new File(config.getProperty(CHALLENGES_DIR_PROPERTY,"challenges"));
			runnerDir = new File(config.getProperty(RUNNER_DIR_PROPERTY,"robocodes"));
			robotsDir = new File(config.getProperty(BOTS_DIR_PROPERTY,"robots"));
			jvmArgs = config.getProperty(JVM_ARGS_PROPERTY,"-Xmx512M");
			threads = Integer.parseInt(config.getProperty(THREADS_PROPERTY,"1"));
		} catch (IOException e) {}
	}
	
	public static void saveOptions() {
		Properties config = new Properties();
		
		config.setProperty(ROBOCODE_PATH_PROPERTY,robocodeLibDir.getPath());
		config.setProperty(CHALLENGES_DIR_PROPERTY,challengeDir.getPath());
		config.setProperty(BOTS_DIR_PROPERTY,robotsDir.getPath());
		config.setProperty(RUNNER_DIR_PROPERTY,runnerDir.getPath());
		config.setProperty(JVM_ARGS_PROPERTY,jvmArgs);
		config.setProperty(THREADS_PROPERTY,""+threads);
		
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
	
	public static void copyRobotToRunners(String robot) {
		String name = robot.replaceAll(" ", "_") + ".jar";
		File jar = new File(robotsDir,name);
		
		for(int i = 0; i < threads; ++i) {
			File targetJar = new File(new File(getRunnerDirectory(i),"robots"),name);
			
			if(!targetJar.exists()) {
				try {
					Files.copy(jar, targetJar);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	public static void createRunners(int total) {
		if(!robocodeLibDir.exists()) {
			throw new RuntimeException("Robocode library directory does not exist!");
		}
		
		//copy libs
		for(int i = 0; i < total; ++i) {
			String directory = getRunnerDirectory(i);
			File dir = new File(directory);
			
			if(!dir.exists()) {
				new File(dir,"robots").mkdirs();
				File libs = new File(dir,"libs");
				libs.mkdirs();
				
				//copy libs
				for(File lib : robocodeLibDir.listFiles()) {
					String name = lib.getName();
					if(name.endsWith(".jar")) {
						try {
							Files.copy(lib, new File(libs,name));
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		
		threads = total;
	}
}
