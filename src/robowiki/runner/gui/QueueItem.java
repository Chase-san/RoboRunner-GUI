package robowiki.runner.gui;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import robowiki.runner.BotList;
import robowiki.runner.ChallengeConfig;
import robowiki.runner.RoboRunner;
import robowiki.runner.ScoreLog;

public class QueueItem {
	private String title;
	private List<BotList> battleList;
	public final ChallengeConfig challenge;
	public final ScoreLog scoreLog;
	private int seasons;
	
	public QueueItem(ChallengeConfig challenge, String challenger, int seasons) {
		this.challenge = challenge;
		this.scoreLog = new ScoreLog(challenger);
		this.seasons = seasons;
		updateBattleList();
	}
	
	protected void updateBattleList() {
		this.battleList = Collections.synchronizedList(Lists.newArrayList(RoboRunner.getBattleList(scoreLog, challenge, seasons)));
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

	public int getSeasons() {
		return seasons;
	}
	
	public String toString() {
		return title;
	}

	public void save() {
		//save title
		//save Challenge
		//save seasons
		//save ScoreLog
	}
}
