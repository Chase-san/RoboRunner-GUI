package robowiki.runner;

import robocode.control.RobotResults;
import robocode.control.events.BattleAdaptor;
import robocode.control.events.BattleCompletedEvent;
import robocode.control.events.BattleErrorEvent;
import robocode.control.events.RoundStartedEvent;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

/**
 * This class is used by BattleProcess to process input from robocode.
 * 
 * @author Voidious
 * 
 */
public class BattleListener extends BattleAdaptor {
	private Multimap<String, RobotResults> _botResults;
	private boolean _sendRoundSignals;

	public BattleListener(boolean sendRoundSignals) {
		_botResults = ArrayListMultimap.create();
		_sendRoundSignals = sendRoundSignals;
	}

	public void onRoundStarted(RoundStartedEvent roundEvent) {
		if(_sendRoundSignals) {
			System.out.println(BattleProcess.ROUND_SIGNAL + roundEvent.getRound());
		}
	}

	public void onBattleCompleted(BattleCompletedEvent completedEvent) {
		RobotResults[] robotResultsArray = RobotResults.convertResults(completedEvent.getIndexedResults());
		for (RobotResults robotResults : robotResultsArray) {
			_botResults.put(robotResults.getTeamLeaderName(), robotResults);
		}
	}

	public void onBattleError(BattleErrorEvent battleErrorEvent) {
		System.out.println("Robocode error: " + battleErrorEvent.getError());
	}

	public Multimap<String, RobotResults> getRobotResultsMap() {
		return ImmutableMultimap.copyOf(_botResults);
	}

	public void clear() {
		_botResults.clear();
	}
}
