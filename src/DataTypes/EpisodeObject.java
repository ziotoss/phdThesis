package DataTypes;

import java.util.List;

public class EpisodeObject {
	
	private String episodeId;
	private List<String> sentences;
	
	
	public String getEpisodeId() {
		return episodeId;
	}
	
	
	public void setEpisodeId(String episodeId) {
		this.episodeId = episodeId;
	}
	
	
	public List<String> getSentences() {
		return sentences;
	}
	
	
	public void setSentences(List<String> sentences) {
		this.sentences = sentences;
	}
	
}
