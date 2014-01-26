package robowiki.runner.gui;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

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
	
	protected QueueItem(ChallengeConfig challenge, ScoreLog scoreLog, int seasons) {
		this.challenge = challenge;
		this.scoreLog = scoreLog;
		this.seasons = seasons;
		updateBattleList();
	}
	
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

	public void save(int queueIndex) throws IOException {
		String queueID = Integer.toString(queueIndex);
		
		//save title/seasons
		Files.write(
				title + "\n" + Integer.toString(seasons) + "\n", 
				new File(Options.dataQueueDir + File.separator + queueID + ".txt"),
				Charsets.UTF_8
			);
		
		//save Challenge
		challenge.save(Options.dataQueueDir + File.separator + queueID + ".rrc");
		
		//Save Score Log
		scoreLog.saveXMLScoreLog(Options.dataQueueDir + File.separator + queueID + ".xml.gz");
	}
	
	public static QueueItem load(int queueIndex) {
		try {
			String queueID = Integer.toString(queueIndex);
			List<String> lines = Files.readLines(new File(Options.dataQueueDir + File.separator + queueID + ".txt"), Charsets.UTF_8);
			String name = lines.get(0);
			int seasons = Integer.parseInt(lines.get(1));
			
			ChallengeConfig challenge = ChallengeConfig.load(new File(Options.dataQueueDir + File.separator + queueID + ".rrc"));
			
			ScoreLog log = null;
			log = ScoreLog.loadXMLScoreLog(Options.dataQueueDir + File.separator + queueID + ".xml.gz");
			
			
			//log.challenger;
			QueueItem item = new QueueItem(challenge,log,seasons);
			item.setTitle(name);
			
			return item;
		} catch(IOException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
