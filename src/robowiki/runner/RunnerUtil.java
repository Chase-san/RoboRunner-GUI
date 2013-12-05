package robowiki.runner;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * A set of utilities that RoboRunner makes use of.
 * @author Voidious
 *
 */
public class RunnerUtil {
	public static String[] getCombinedArgs(String[] args) {
		List<String> argsList = Lists.newArrayList();
		String nextArg = "";
		for (String arg : args) {
			if (arg.startsWith("-")) {
				if (!nextArg.equals("")) {
					argsList.add(nextArg);
					nextArg = "";
				}
				argsList.add(arg);
			} else {
				nextArg = (nextArg + " " + arg).trim();
			}
		}
		if (!nextArg.equals("")) {
			argsList.add(nextArg);
		}
		return argsList.toArray(new String[0]);
	}

	/**
	 * Returns the string value of the given argument.
	 * @param flagName The argument name.
	 * @param args The list of arguments.
	 * @return the string value of the argument, null otherwise.
	 */
	public static String parseStringArgument(String flagName, String[] args) {
		return parseStringArgument(flagName, args, null);
	}

	/**
	 * Returns the string value of the given argument, prints the missingError to stdout if the flag is not found.
	 * @param flagName The argument name.
	 * @param args The list of arguments.
	 * @param missingError The error to print if the argument is not found.
	 * @return the string value of the argument, null otherwise.
	 */
	public static String parseStringArgument(String flagName, String[] args, String missingError) {
		for (int x = 0; x < args.length - 1; x++) {
			if (args[x].equals("-" + flagName)) {
				return args[x + 1];
			}
		}
		if (missingError != null) {
			System.out.println(missingError);
		}
		return null;
	}

	/**
	 * Returns true if the given argument exists, false otherwise.
	 * @param flagName The argument name.
	 * @param args The list of arguments.
	 * @return true if flag exists, false otherwise.
	 */
	public static boolean parseBooleanArgument(String flagName, String[] args) {
		for (int x = 0; x < args.length; x++) {
			if (args[x].equals("-" + flagName)) {
				return true;
			}
		}
		return false;
	}

	public static double round(double d, int i) {
		long powerTen = 1;
		for (int x = 0; x < i; x++) {
			powerTen *= 10;
		}
		return ((double) Math.round(d * powerTen)) / powerTen;
	}

	public static double standardDeviation(List<Double> values) {
		double avg = average(values);
		double sumSquares = 0;
		for (double value : values) {
			sumSquares += square(avg - value);
		}
		return Math.sqrt(sumSquares / values.size());
	}

	/**
	 * Returns the square of the given value.
	 * @param d The value to square.
	 * @return The square of the value.
	 */
	public static double square(double d) {
		return d * d;
	}

	/**
	 * Calculates the mean average of a list of values.
	 * @param values Values to get the average of.
	 * @return The mean average of the values. For example input of {10,20}, it would return 15.
	 */
	public static double average(List<Double> values) {
		double sum = 0;
		for (double value : values) {
			sum += value;
		}
		return (sum / values.size());
	}
}
