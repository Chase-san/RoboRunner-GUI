package robowiki.runner;

import static robowiki.runner.RunnerUtil.getCombinedArgs;
import static robowiki.runner.RunnerUtil.parseStringArgument;
import static robowiki.runner.RunnerUtil.parseBooleanArgument;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import robocode.control.BattleSpecification;
import robocode.control.BattlefieldSpecification;
import robocode.control.RobocodeEngine;
import robocode.control.RobotResults;
import robocode.control.RobotSpecification;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * This class is run in a separate JVM, interfaces directly with robocode and
 * actually runs the battles that are passed to it by it's stdin and stdout. It
 * is not recommended this class be used directly, but rather interfaced by
 * means of BattleRunner.
 * 
 * @author Voidious
 */
public class BattleProcess {
	public static final String READY_SIGNAL = "BattleProcess ready";
	public static final String ROUND_SIGNAL = "ROUND: ";
	public static final String RESULT_SIGNAL = "BATTLE RESULT: ";
	public static final String BOT_DELIMITER = ":::";
	public static final String SCORE_DELIMITER = "::";

	private static final Joiner COMMA_JOINER = Joiner.on(",");
	private static final Joiner COLON_JOINER = Joiner.on(BOT_DELIMITER);

	private BattlefieldSpecification _battlefield;
	private RobocodeEngine _engine;
	private BattleListener _listener;
	private int _numRounds;

	public static void main(String[] args) {
		args = getCombinedArgs(args);
		String robocodePath = parseStringArgument("path", args, "Pass a path to Robocode with -path");
		int numRounds = Integer.parseInt(parseStringArgument("rounds", args, "Pass number of rounds width with -rounds"));
		int width = Integer.parseInt(parseStringArgument("width", args, "Pass battlefield width with -width"));
		int height = Integer.parseInt(parseStringArgument("height", args, "Pass battlefield height with -height"));
		boolean sendRoundSignals = parseBooleanArgument("srounds", args);

		BattleProcess process = new BattleProcess(robocodePath, numRounds, width, height, sendRoundSignals);
		System.out.println(READY_SIGNAL);
		BufferedReader stdin = new BufferedReader(new java.io.InputStreamReader(System.in));

		/* Main processing loop */
		while (true) {
			try {
				String line = stdin.readLine();
				System.out.println("Processing " + line);
				String result = process.runBattle(getBotList(line));
				System.out.println(RESULT_SIGNAL + result);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static BotList getBotList(String line) {
		return new BotList(Lists.newArrayList(line.split(",")));
	}

	public BattleProcess(String robocodePath, int numRounds, int battleFieldWidth, int battleFieldHeight, boolean sendRoundSignals) {
		_numRounds = numRounds;
		_battlefield = new BattlefieldSpecification(battleFieldWidth, battleFieldHeight);
		_engine = new RobocodeEngine(new File(robocodePath));
		_listener = new BattleListener(sendRoundSignals);
		_engine.addBattleListener(_listener);
		_engine.setVisible(false);
	}

	public String runBattle(BotList botList) {
		RobotSpecification[] robots = _engine.getLocalRepository(COMMA_JOINER.join(botList.getBotNames()));
		BattleSpecification battleSpec = new BattleSpecification(_numRounds, _battlefield, robots);
		_engine.runBattle(battleSpec, true);
		Multimap<String, RobotResults> resultsMap = _listener.getRobotResultsMap();
		_listener.clear();
		return battleResultString(resultsMap);
	}

	private String battleResultString(Multimap<String, RobotResults> resultsMap) {
		Set<String> resultStrings = Sets.newHashSet();
		for (Map.Entry<String, RobotResults> resultsEntry : resultsMap.entries()) {
			RobotResults results = resultsEntry.getValue();
			resultStrings.add(resultsEntry.getKey() + SCORE_DELIMITER + results.getScore() + SCORE_DELIMITER + results.getFirsts()
					+ SCORE_DELIMITER + results.getSurvival() + SCORE_DELIMITER + results.getBulletDamage());
		}
		return COLON_JOINER.join(resultStrings);
	}
}
