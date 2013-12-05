package robowiki.runner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;

public class BattleRunner {
	private static final Joiner COMMA_JOINER = Joiner.on(",");

	private Queue<Process> _processQueue;
	private ExecutorService _threadPool;
	private ExecutorService _callbackPool;
	private int _numRounds;
	private int _battleFieldWidth;
	private int _battleFieldHeight;
	private boolean _roundSignals;

	public BattleRunner(Set<String> robocodeEnginePaths, String jvmArgs, int numRounds, int battleFieldWidth, int battleFieldHeight,
			boolean requestRoundSignals) {
		_numRounds = numRounds;
		_battleFieldWidth = battleFieldWidth;
		_battleFieldHeight = battleFieldHeight;
		
		_roundSignals = requestRoundSignals;

		_threadPool = Executors.newFixedThreadPool(robocodeEnginePaths.size(), new BattleThreadFactory());
		_callbackPool = Executors.newFixedThreadPool(1);
		_processQueue = Queues.newConcurrentLinkedQueue();
		for (String enginePath : robocodeEnginePaths) {
			initEngine(enginePath, jvmArgs);
		}
	}

	private void initEngine(String enginePath, String jvmArgs) {
		try {
			List<String> command = Lists.newArrayList();
			command.add("java");
			command.addAll(Lists.newArrayList(jvmArgs.trim().split(" +")));
			command.addAll(Lists.newArrayList("-cp", System.getProperty("java.class.path"), "robowiki.runner.BattleProcess", "-rounds", ""
					+ _numRounds, "-width", "" + _battleFieldWidth, "-height", "" + _battleFieldHeight, "-path", enginePath));
			if(_roundSignals) {
				command.add("-rounds");
			}

			System.out.print("Initializing engine: " + enginePath + "... ");
			ProcessBuilder builder = new ProcessBuilder(command);
			builder.redirectErrorStream(true);
			Process battleProcess = builder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(battleProcess.getInputStream()));
			String processOutput;
			do {
				processOutput = reader.readLine();
			} while (!processOutput.equals(BattleProcess.READY_SIGNAL));
			System.out.println("done!");
			_processQueue.add(battleProcess);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void runBattles(List<BotList> botLists, BattleOutputHandler handler) {
		List<Future<String>> futures = Lists.newArrayList();
		for (final BotList botList : botLists) {
			futures.add(_threadPool.submit(new BattleCallable(botList, handler)));
		}
		getAllFutures(futures);
	}

	public void runBattles(BattleSelector selector, BattleOutputHandler handler, int numBattles) {
		List<Future<String>> futures = Lists.newArrayList();
		for (int x = 0; x < numBattles; x++) {
			futures.add(_threadPool.submit(new BattleCallable(selector, handler)));
		}
		getAllFutures(futures);
	}

	private void getAllFutures(List<Future<String>> futures) {
		for (Future<String> future : futures) {
			try {
				future.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
	}

	private List<RobotScore> getRobotScoreList(String battleResults) {
		List<RobotScore> robotScores = Lists.newArrayList();
		String[] botScores = battleResults.replaceFirst(BattleProcess.RESULT_SIGNAL, "").replaceAll("\n", "")
				.split(BattleProcess.BOT_DELIMITER);
		for (String scoreString : botScores) {
			String[] scoreFields = scoreString.split(BattleProcess.SCORE_DELIMITER);
			String botName = scoreFields[0];
			int score = Integer.parseInt(scoreFields[1]);
			int firsts = Integer.parseInt(scoreFields[2]);
			int survivalScore = Integer.parseInt(scoreFields[3]);
			double bulletDamage = Double.parseDouble(scoreFields[4]);
			RobotScore robotScore = new RobotScore(botName, score, firsts, survivalScore, bulletDamage);
			robotScores.add(robotScore);
		}
		return ImmutableList.copyOf(robotScores);
	}

	public void shutdown() {
		_threadPool.shutdown();
		_callbackPool.shutdown();
	}

	public interface BattleOutputHandler {
		void processNewBattle(int id, BotList list);
		void processRound(int id, int round);
		/**
		 * Processes the scores from a battle.
		 * 
		 * @param robotScores
		 *            scores for each robot in the battle
		 * @param elapsedTime
		 *            elapsed time of the battle, in nanoseconds
		 */
		void processResults(int id, List<RobotScore> robotScores, long elapsedTime);
	}

	public interface BattleSelector {
		BotList nextBotList();
	}
	
	private static class BattleThreadFactory implements ThreadFactory {
		int _counter = 0;
		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r,""+_counter++);
		}
	}

	/**
	 * Calls and handles the actual battle.
	 * @author Voidious
	 */
	private class BattleCallable implements Callable<String> {
		private BotList _botList;
		private BattleSelector _selector;
		private BattleOutputHandler _listener;
		private int _id = 0;

		public BattleCallable(BotList botList, BattleOutputHandler listener) {
			_botList = botList;
			_listener = listener;
		}

		public BattleCallable(BattleSelector selector, BattleOutputHandler listener) {
			_selector = selector;
			_listener = listener;
		}
		
		/**
		 * Determines if the given line is a battle result.
		 * @param line The input line.
		 * @return <code>true</code> if it is a battle result, <code>false</code> otherwise.
		 */
		private boolean isBattleResult(String line) {
			return line != null && line.startsWith(BattleProcess.RESULT_SIGNAL);
		}
		
		/**
		 * Determines if the given line is a round signal.
		 * @param line The input line.
		 * @return <code>true</code> if it is a round signal, <code>false</code> otherwise.
		 */
		private boolean isRoundSignal(String line) {
			return line != null && line.startsWith(BattleProcess.ROUND_SIGNAL);
		}

		@Override
		public String call() throws Exception {
			final long startTime = System.nanoTime();
			Process battleProcess = _processQueue.poll();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(battleProcess.getOutputStream()));
			BufferedReader reader = new BufferedReader(new InputStreamReader(battleProcess.getInputStream()));
			final BotList botList;
			
			_id = Integer.parseInt(Thread.currentThread().getName());
			
			if (_selector == null) {
				botList = _botList;
			} else {
				botList = _callbackPool.submit(new Callable<BotList>() {
					@Override
					public BotList call() throws Exception {
						return _selector.nextBotList();
					}
				}).get();
			}
			
			_callbackPool.submit(new Runnable() {
				@Override
				public void run() {
					_listener.processNewBattle(_id, botList);
				}
			});
			
			writer.append(COMMA_JOINER.join(botList.getBotNames()) + "\n");
			writer.flush();
			String input;
			do {
				// TODO: How to handle other output, errors etc?
				input = reader.readLine();
				if(_roundSignals && isRoundSignal(input)) {
					final int round = Integer.parseInt(input.substring(BattleProcess.ROUND_SIGNAL.length()));
					_callbackPool.submit(new Runnable() {
						@Override
						public void run() {
							//TODO check if the callback pool runs quick enough to allow this, otherwise...
							_listener.processRound(_id, round);
						}
					});
				}
			} while (!isBattleResult(input));
			
			final String result = input;
			_processQueue.add(battleProcess);
			_callbackPool.submit(new Runnable() {
				@Override
				public void run() {
					_listener.processResults(_id,getRobotScoreList(result), System.nanoTime() - startTime);
				}
			}).get();
			return result;
		}
	}
}
