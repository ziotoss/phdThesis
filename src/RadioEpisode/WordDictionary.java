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

public class WordDictionary {

	/**
	 * @param args
	 */
	private static final String DATAPATH = "D:/Data/RadioEpisode/";
	private static ArrayList<WordDictObject> dicWords = new ArrayList<WordDictObject>();
	
	private void insertWord(String file) {
		Scanner scanner;
		try {
			scanner = new Scanner(new FileInputStream(file));
			ArrayList<String> docList = new ArrayList<String>();
			ArrayList<WordDictObject> uniqueWordsInDoc = new ArrayList<WordDictObject>();
			int startIdx = -1;
			
			while(scanner.hasNext()) {
				String line = scanner.nextLine();
				docList.add(line);
			}
			
			for(int i = 0; i < docList.size(); i++) {
				if(docList.get(i).contains("CONTENT"))
					startIdx = i + 1;
			}
						
			for(int i = startIdx; i < docList.size(); i++) {
				if(docList.get(i).matches(".*[:].*[:].*")) {
					String[] tmp = docList.get(i).split(":");
					String type = tmp[1].trim();
					String word = tmp[2].trim();
	
					WordDictObject wordObject = new WordDictObject();
					wordObject.setWord(word);
	
					if(!uniqueWordsInDoc.contains(wordObject)) {
						wordObject.setDocCount(1);
						wordObject.setWordCount(1);
						wordObject.setType(type);
						uniqueWordsInDoc.add(wordObject);
					} else {
						uniqueWordsInDoc.get(uniqueWordsInDoc.indexOf(wordObject)).setWordCount(uniqueWordsInDoc.get(uniqueWordsInDoc.indexOf(wordObject)).getWordCount() + 1);
					}
				}
			}
			
			for(WordDictObject wdo : uniqueWordsInDoc) {
				if(!dicWords.contains(wdo)) {
					dicWords.add(wdo);
				} else {
					dicWords.get(dicWords.indexOf(wdo)).setDocCount(dicWords.get(dicWords.indexOf(wdo)).getDocCount() + wdo.getDocCount());
					dicWords.get(dicWords.indexOf(wdo)).setWordCount(dicWords.get(dicWords.indexOf(wdo)).getWordCount() + wdo.getWordCount());
				}
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unused")
	private void validateWordCount(String program) {
		String prefix = "sbs";
		String postfix = "_201407_hts";
		List<String> wordsInDocuments = new ArrayList<String>();
		
		String rootFolder = prefix + program + postfix;
		File folder = new File(DATAPATH + rootFolder);
		File[] listOfFiles = folder.listFiles();
		for(File file:listOfFiles) {
			Scanner scanner;
			try {
				scanner = new Scanner(new FileInputStream(file));
				ArrayList<String> docList = new ArrayList<String>();
				int startIdx = -1;
				
				while(scanner.hasNext()) {
					String line = scanner.nextLine();
					docList.add(line);
				}
				
				for(int i = 0; i < docList.size(); i++) {
					if(docList.get(i).contains("CONTENT"))
						startIdx = i + 1;
				}
				
				int endIdx = docList.size();
				List<String> tmpList = docList.subList(startIdx, endIdx);
				for(String s : tmpList) {
					String[] tmp = s.split(":");
					String word = tmp[2].trim();
					wordsInDocuments.add(word);	
				}
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		int[] wordCount = new int[dicWords.size()];
		for(int i = 0; i < wordCount.length; i++)
			wordCount[i] = 0;
		
		for(int i = 0; i < dicWords.size(); i++) {
			for(String s : wordsInDocuments) {
				if(dicWords.get(i).getWord().equals(s))
					wordCount[i] = wordCount[i] + 1;
			}
		}
		
		List<String> errorWords = new ArrayList<String>();
		List<Integer> errorWordCount = new ArrayList<Integer>();
		List<Integer> errorDictCount = new ArrayList<Integer>();
		for(int i = 0; i < wordCount.length; i++) {
			if(wordCount[i] != dicWords.get(i).getWordCount()) {
				errorWords.add(dicWords.get(i).getWord());
				errorWordCount.add(wordCount[i]);
				errorDictCount.add(dicWords.get(i).getWordCount());
			}
		}
		
		if(!errorWords.isEmpty()) {
			System.out.println("ERROR: Mismatch in word count. Check debug_word_check.txt for details.");
			BufferedWriter writer = null;
			File logFile = new File("debug_word_check.txt");
			String NL = System.getProperty("line.separator");
			try {
				writer = new BufferedWriter(new FileWriter(logFile, true));
				writer.write("Error in program " + program + NL);
				for(int i = 0; i < errorWords.size(); i++) {
					writer.write(errorWords.get(i) + " : word Count - " + errorWordCount.get(i) + " , dict Count = " + errorDictCount.get(i) + NL);
				}
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				try {
					writer.close();
				} catch(Exception e) {
					
				}
			}
		} else {
			System.out.println("PASSED: Word count check.");
		}
	}

	@SuppressWarnings("unused")
	private void validateDocCount(String program) {
		int[] docCount = new int[dicWords.size()];
		for(int i = 0; i < docCount.length; i++)
			docCount[i]	 = 0;
		String prefix = "sbs";
		String postfix = "_201407_hts";
		
		String rootFolder = prefix + program + postfix;
		File folder = new File(DATAPATH + rootFolder);
		File[] listOfFiles = folder.listFiles();
		for(int i = 0; i < listOfFiles.length; i++) {
			Scanner scanner;
			try {
				scanner = new Scanner(new FileInputStream(listOfFiles[i]));
				ArrayList<String> docWords = new ArrayList<String>();
				
				while(scanner.hasNext()) {
					String line = scanner.nextLine();
					docWords.add(line);
				}
				
				int startIdx = -1;
				for(int j = 0; j < docWords.size(); j++) {
					if(docWords.get(j).contains("CONTENT"))
						startIdx = j + 1;
				}
				
				int endIdx = docWords.size();
				List<String> tmpList = docWords.subList(startIdx, endIdx);
				List<String> validWords = new ArrayList<String>();
				for(String s : tmpList) {
					String[] tmp = s.split(":");
					String word = tmp[2].trim();
					validWords.add(word);
				}
				
				for(int j = 0; j < dicWords.size(); j++) {
					if(validWords.contains(dicWords.get(j).getWord()))
						docCount[j] = docCount[j] + 1;
				}
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		List<String> errorWords = new ArrayList<String>();
		List<Integer> errorDocCount = new ArrayList<Integer>();
		List<Integer> errorDictCount = new ArrayList<Integer>();
		for(int i = 0; i < docCount.length; i++) {
			if(docCount[i] != dicWords.get(i).getDocCount()) {
				errorWords.add(dicWords.get(i).getWord());
				errorDocCount.add(docCount[i]);
				errorDictCount.add(dicWords.get(i).getDocCount());
			}
		}

		if(!errorWords.isEmpty()) {
			System.out.println("ERROR: Mismatch in document count. Check debug_doc_check.txt for details.");
			BufferedWriter writer = null;
			File logFile = new File("debug_doc_check.txt");
			String NL = System.getProperty("line.separator");
			try {
				writer = new BufferedWriter(new FileWriter(logFile, true));
				writer.write("Error in program " + program + NL);
				for(int i = 0; i < errorWords.size(); i++) {
					writer.write(errorWords.get(i) + " : doc Count - " + errorDocCount.get(i) + " , dict Count = " + errorDictCount.get(i) + NL);
				}
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				try {
					writer.close();
				} catch(Exception e) {
					
				}
			}
		} else {
			System.out.println("PASSED: Document count check.");
		}
	}
	
	private void writeToFile(String fileName) {
		BufferedWriter writer = null;
		File outFile = new File(fileName);
		String NL = System.getProperty("line.separator");
		try {
			writer = new BufferedWriter(new FileWriter(outFile));
			for(WordDictObject wdo : dicWords) {
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
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		WordDictionary dao = new WordDictionary();
		String prefix = "sbs";
		String[] programs = {"12pm", "2pm", "4pm", "6pm", "8pm", "10pm", "12am"};
		String postfix = "_201407_hts";
		String outFile = "word_dictionary_";
		String outFileExt = ".txt";		
		
		for(String program:programs) {
			System.out.println("Processing program " + program);
			String rootFolder = prefix + program + postfix;
			File folder = new File(DATAPATH + rootFolder);
			File[] listOfFiles = folder.listFiles();
			for(int i = 0; i < listOfFiles.length; i++) {
				if((i + 1) % (listOfFiles.length / 10) == 0)
					System.out.print(".");
				dao.insertWord(listOfFiles[i].getAbsolutePath());
			}
			
			dao.writeToFile(outFile + program + outFileExt);
			System.out.println();
			//dao.validateWordCount(program);
			//dao.validateDocCount(program);
		}
	}
}
