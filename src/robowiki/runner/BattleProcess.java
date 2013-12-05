package robowiki.runner;

import static robowiki.runner.RunnerUtil.getCombinedArgs;
import static robowiki.runner.RunnerUtil.parseStringArgument;
import static robowiki.runner.RunnerUtil.parseBooleanArgument;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import robocode.control.BattleSpecification;
import robocode.control.BattlefieldSpecification;
import robocode.control.RobocodeEngine;
import robocode.control.RobotResults;
import robocode.control.RobotSpecification;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
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

	private static final Joiner COMMA_JOINER = Joiner.on(',');
	private static final Splitter COMMA_SPLITTER = Splitter.on(',');
	private static final Joiner COLON_JOINER = Joiner.on(BOT_DELIMITER);

	private RobocodeEngine _engine;
	private BattleListener _listener;

	public static void main(String[] args) {
		args = getCombinedArgs(args);
		String robocodePath = parseStringArgument("path", args, "Pass a path to Robocode with -path");
		boolean sendRoundSignals = parseBooleanArgument("srounds", args);

		BattleProcess process = new BattleProcess(robocodePath, sendRoundSignals);
		System.out.println(READY_SIGNAL);
		BufferedReader stdin = new BufferedReader(new java.io.InputStreamReader(System.in));

		/* Main processing loop */
		while (true) {
			try {
				String line = stdin.readLine();
				System.out.println("Processing " + line);
				
				String result = process.runBattle(process.getBattleSpecification(line));
				System.out.println(RESULT_SIGNAL + result);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public BattleProcess(String robocodePath, boolean sendRoundSignals) {
		_engine = new RobocodeEngine(new File(robocodePath));
		_listener = new BattleListener(sendRoundSignals);
		_engine.addBattleListener(_listener);
		_engine.setVisible(false);
	}
	
	public String runBattle(int numRounds, int battlefieldWidth, int battlefieldHeight, BotList botList) {
		BattlefieldSpecification battlefield = new BattlefieldSpecification(battlefieldWidth, battlefieldHeight);
		RobotSpecification[] robots = _engine.getLocalRepository(COMMA_JOINER.join(botList.getBotNames()));
		BattleSpecification battleSpec = new BattleSpecification(numRounds,battlefield,robots);
		_engine.runBattle(battleSpec, true);
		Multimap<String, RobotResults> resultsMap = _listener.getRobotResultsMap();
		_listener.clear();
		return battleResultString(resultsMap);
	}

	public String runBattle(BattleSpecification battleSpec) {
		_engine.runBattle(battleSpec, true);
		Multimap<String, RobotResults> resultsMap = _listener.getRobotResultsMap();
		_listener.clear();
		return battleResultString(resultsMap);
	}
	
	private BattleSpecification getBattleSpecification(String line) {
		List<String> config = COMMA_SPLITTER.splitToList(line);
		int numRounds = Integer.parseInt(config.get(0));
		int width = Integer.parseInt(config.get(1));
		int height = Integer.parseInt(config.get(2));
		
		BattlefieldSpecification battlefield = new BattlefieldSpecification(width, height);
		config = config.subList(3, config.size());
		RobotSpecification[] robots = _engine.getLocalRepository(COMMA_JOINER.join(config));
		
		return new BattleSpecification(numRounds,battlefield,robots);
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
