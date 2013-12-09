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
	private boolean _roundSignals;

	public BattleRunner(Set<String> robocodeEnginePaths, String jvmArgs, boolean requestRoundOutput) {
		_roundSignals = requestRoundOutput;
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
			command.addAll(Lists.newArrayList("-cp", System.getProperty("java.class.path"), "robowiki.runner.BattleProcess", "-path", enginePath));
			if(_roundSignals) {
				command.add("-srounds");
			}

			System.out.print("Initializing engine: " + enginePath + "... ");
			ProcessBuilder builder = new ProcessBuilder(command);
			builder.redirectErrorStream(true);
			Process battleProcess = builder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(battleProcess.getInputStream()));
			String processOutput;
			do {
				processOutput = reader.readLine();
			} while (!BattleProcess.READY_SIGNAL.equals(processOutput));
			System.out.println("done!");
			_processQueue.add(battleProcess);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void runBattlesNonBlocking(List<BotList> botLists, BattleOutputHandler handler,
			int numRounds, int battlefieldWidth, int battlefieldHeight) {
		for (final BotList botList : botLists) {
			_threadPool.submit(new BattleCallable(botList, handler, numRounds, battlefieldWidth, battlefieldHeight));
		}
	}

	public void runBattles(List<BotList> botLists, BattleOutputHandler handler,
			int numRounds, int battlefieldWidth, int battlefieldHeight) {
		List<Future<String>> futures = Lists.newArrayList();
		for (final BotList botList : botLists) {
			futures.add(_threadPool.submit(
						new BattleCallable(botList, handler, numRounds, battlefieldWidth, battlefieldHeight)
					));
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
		/**
		 * Processes a new battle for the given thread.
		 * @param id The thread id.
		 * @param list The list of robots in the battle.
		 */
		void processNewBattle(int id, BotList list);
		/**
		 * Processes a new round from a battle.
		 * @param id The thread id.
		 * @param round The round number.
		 */
		void processRound(int id, int round);
		/**
		 * Processes the scores from a battle.
		 * @param id The thread id.
		 * @param robotScores Scores for each robot in the battle.
		 * @param elapsedTime Elapsed time of the battle, in nanoseconds.
		 */
		void processResults(int id, List<RobotScore> robotScores, long elapsedTime);
	}

	public interface BattleSelector {
		int nextNumRounds();
		int nextBattlefieldWidth();
		int nextBattlefieldHeight();
		BotList nextBotList();
	}
	
	private static class BattleThreadFactory implements ThreadFactory {
		int _counter = 0;
		@Override
		public Thread newThread(Runnable r) {
			Thread thread = new Thread(r,""+_counter++);
			thread.setDaemon(true);
			return thread;
		}
	}

	/**
	 * Calls and handles the actual battle.
	 * @author Voidious
	 */
	private class BattleCallable implements Callable<String> {
		private int _battlefieldWidth;
		private int _battlefieldHeight;
		private int _numRounds;
		private BotList _botList;
		private BattleSelector _selector;
		private BattleOutputHandler _listener;

		public BattleCallable(BotList botList, BattleOutputHandler listener,
				int numRounds, int battlefieldWidth, int battlefieldHeight) {
			_botList = botList;
			_listener = listener;
			_numRounds = numRounds;
			_battlefieldWidth = battlefieldWidth;
			_battlefieldHeight = battlefieldHeight;
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
			
			final int id = Integer.parseInt(Thread.currentThread().getName());
			
			if (_selector == null) {
				botList = _botList;
			} else {
				//Until I can figure out a better way to do it.
				_numRounds = _selector.nextNumRounds();
				_battlefieldWidth = _selector.nextBattlefieldWidth();
				_battlefieldHeight = _selector.nextBattlefieldHeight();
				botList = _selector.nextBotList();
			}
			
			_callbackPool.execute(new Runnable() {
				@Override
				public void run() {
					_listener.processNewBattle(id, botList);
				}
			});

			List<String> battleConfig = Lists.newArrayList("" + _numRounds, "" + _battlefieldWidth, "" + _battlefieldHeight);
			battleConfig.addAll(botList.getBotNames());
			writer.append(COMMA_JOINER.join(battleConfig) + "\n");
			writer.flush();
			String input;
			do {
				// TODO: How to handle other output, errors etc?
				input = reader.readLine();
				if(isRoundSignal(input)) {
					if(_roundSignals) {
						final int round = Integer.parseInt(input.substring(BattleProcess.ROUND_SIGNAL.length()));
						_callbackPool.execute(new Runnable() {
							@Override
							public void run() {
								//TODO check if the callback pool runs quick enough to allow this, otherwise...
								_listener.processRound(id, round);
							}
						});
					}
				} else {
					//Temporary debugging
					System.out.println(input);
				}
			} while (!isBattleResult(input));
			
			final String result = input;
			_processQueue.add(battleProcess);
			_callbackPool.execute(new Runnable() {
				@Override
				public void run() {
					_listener.processResults(id, getRobotScoreList(result), System.nanoTime() - startTime);
				}
			});
			
			return result;
		}
	}
}
