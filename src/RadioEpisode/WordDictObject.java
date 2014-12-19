package RadioEpisode;

public class WordDictObject {

	private int wordCount;
	private int docCount;
	private String word;
	private String type;
	
	public WordDictObject() {
		this.wordCount = 0;
		this.docCount = 0;
		this.word = null;
		this.type = null;
	}
	
	public int getWordCount() {
		return wordCount;
	}
	
	public void setWordCount(int wordCount) {
		this.wordCount = wordCount;
	}
	
	public int getDocCount() {
		return docCount;
	}
	
	public void setDocCount(int docCount) {
		this.docCount = docCount;
	}
	
	public String getWord() {
		return word;
	}
	
	public void setWord(String word) {
		this.word = word;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((word == null) ? 0 : word.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WordDictObject other = (WordDictObject) obj;
		if (word == null) {
			if (other.word != null)
				return false;
		} else if (!word.equals(other.word))
			return false;
		return true;
	}	
}
