package RadioEpisode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import DataTypes.WordDictObject;

public class CombineDictionary {

	private final String INDIVIDUALDICTPATH = "C:/Users/user/Documents/Thesis_Workspace/PhdThesis/";
	private final String OUTFILENAME = "Combined_Dictionary.txt";
	
	private void insertToDictionary(String fileName, List<WordDictObject> dictionary) {
		try {
			Scanner scanner = new Scanner(new FileInputStream(INDIVIDUALDICTPATH + fileName));
			while(scanner.hasNext()) {
				WordDictObject wdo = new WordDictObject();
				String line = scanner.nextLine();
				String[] tmp = line.split("\t");
				wdo.setWord(tmp[0].trim());
				wdo.setType(tmp[1].trim());
				wdo.setWordCount(Integer.parseInt(tmp[2].trim()));
				wdo.setDocCount(Integer.parseInt(tmp[3].trim()));
				if(!dictionary.contains(wdo)) {
					dictionary.add(wdo);
				} else {
					WordDictObject existingWdo = dictionary.remove(dictionary.indexOf(wdo));
					existingWdo.setWordCount(existingWdo.getWordCount() + wdo.getWordCount());
					existingWdo.setDocCount(existingWdo.getDocCount() + wdo.getDocCount());
					dictionary.add(existingWdo);
				}
			}
			
			scanner.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void writeToFile(List<WordDictObject> dictionary) {
		BufferedWriter writer = null;
		File outFile = new File(OUTFILENAME);
		String NL = System.getProperty("line.separator");
		try {
			writer = new BufferedWriter(new FileWriter(outFile));
			for(WordDictObject wdo : dictionary) {
				writer.write(wdo.getWord() + "\t" + wdo.getType() + "\t" + wdo.getWordCount() + "\t" + wdo.getDocCount() + NL);
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch(Exception e) {
				
			}
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		List<WordDictObject> dictionary = new ArrayList<WordDictObject>();
		String dictFilePrefix = "word_dictionary_";
		String dictFilePostfix = ".txt";
		String[] programs = {"12pm", "2pm", "4pm", "6pm", "8pm", "10pm", "12am"};
		
		CombineDictionary dao = new CombineDictionary();
		
		for(String s : programs) {
			System.out.println("Processing dictionary " + dictFilePrefix + s + dictFilePostfix);
			dao.insertToDictionary(dictFilePrefix + s + dictFilePostfix, dictionary);
		}
		
		dao.writeToFile(dictionary);
		
	}

}
