package robowiki.runner.gui;

import java.util.List;

import robowiki.runner.BotList;
import robowiki.runner.ChallengeConfig;
import robowiki.runner.RoboRunner;
import robowiki.runner.ScoreLog;

public class QueueItem {
	private String title;
	private List<BotList> battleList;
	private ChallengeConfig challenge;
	private ScoreLog scoreLog;
	private String challenger;
	private int seasons;
	
	public QueueItem(ChallengeConfig challenge, String challenger, int seasons) {
		this.challenge = challenge;
		this.challenger = challenger;
		this.scoreLog = new ScoreLog(challenger);
		this.seasons = seasons;
		updateBattleList();
	}
	
	protected void updateBattleList() {
		this.battleList = RoboRunner.getBattleList(scoreLog, challenge, challenger, seasons);
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setSeasons(int seasons) {
		this.seasons = seasons;
	}

	public List<BotList> getBattleList() {
		return battleList;
	}

	public ChallengeConfig getChallenge() {
		return challenge;
	}

	public ScoreLog getScoreLog() {
		return scoreLog;
	}

	public String getChallenger() {
		return challenger;
	}

	public int getSeasons() {
		return seasons;
	}
	
	public String toString() {
		return title;
	}
	
}
