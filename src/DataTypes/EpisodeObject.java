package DataTypes;

import java.util.ArrayList;

public class EpisodeObject {
	
	private String episodeId;
	private ArrayList<String> sentences;
	
	
	public String getEpisodeId() {
		return episodeId;
	}
	
	
	public void setEpisodeId(String episodeId) {
		this.episodeId = episodeId;
	}
	
	
	public ArrayList<String> getSentences() {
		return sentences;
	}
	
	
	public void setSentences(ArrayList<String> sentences) {
		this.sentences = sentences;
	}
	
}
