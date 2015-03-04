package RadioEpisode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import DataTypes.EpisodeObject;

public class FilterHtsFiles {
	
	private final String inputPath = "D:/Data/RadioEpisode/sbs_hts_new";

	private void getEpisodes(String program) {
		
		File f = new File(inputPath + "/" + program);
		String postFix = "_preprocessed";
		String[] htsFiles = f.list();
		
		for(String s : htsFiles) {
			File tmp = new File(f.getAbsolutePath() + postFix + "/" + s);
			if(!tmp.exists()) {
				System.out.println("Processing file " + s + ".");
				List<String> lines = new ArrayList<String>();
				try {
					Scanner scanner = new Scanner(new FileInputStream(f.getAbsolutePath() + "/" + s));
					boolean trigger = false;
					while(scanner.hasNext()) {
						String line = scanner.nextLine();
						if(line.contains("###TITLE###"))
							trigger = true;
						else if(line.contains("###AUTHOR###"))
							trigger = false;
						else if(line.contains("###CONTENT###"))
							trigger = true;
						
						if(trigger) {
							if(line.matches(".*[:][a-zA-Z][:].+"))
								lines.add(line.trim());
						}
					}
		
					scanner.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				EpisodeObject epiObj = new EpisodeObject();
				epiObj.setEpisodeId(s);
				epiObj.setSentences(lines);
				
				EpisodeObject epiPreprocessed = preprocessEpisodes(epiObj);
				if(epiPreprocessed != null)
					writeToFile(program, epiPreprocessed);
			} else {
				System.out.println("File " + s + " already processed.");
			}
		}
	}
	
	// Remove hts with K label if K is followed by a P. Also remove C components while leaving the P components.
	// Remove words that have length 1.
	// Remove words with [ぁ-ぞた-び].
	private EpisodeObject preprocessEpisodes(EpisodeObject epi) {
		List<String> hts = epi.getSentences();
		List<String> toAdd = new ArrayList<String>();
		if(hts.size() > 0) {
			for(int i = 0; i < hts.size() - 1; i++) {
				String[] tmp = hts.get(i).split(":");
				if(!tmp[1].equals("C")) {
					if(tmp[1].equals("K")) {
						String[] tmp2 = hts.get(i + 1).split(":");
						if(!tmp2[1].equals("P")) {
							if(tmp2[2].length() > 1) {
								toAdd.add(hts.get(i).replaceAll("[ぁ-ぞた-び]", ""));
							}
						}
					} else {
						if(tmp[2].length() > 1) {
							toAdd.add(hts.get(i).replaceAll("[ぁ-ぞた-び]", ""));
						}
					}
				}
			}
			
			String lastElem = hts.get(hts.size() - 1);
			String tmp[] = lastElem.split(":");
			if(tmp[2].length() > 1) {
				toAdd.add(lastElem.replaceAll("[ぁ-ぞた-び]", ""));
			}
			
			EpisodeObject obj = new EpisodeObject();
			obj.setEpisodeId(epi.getEpisodeId());
			obj.setSentences(toAdd);
			return obj;
		} else
			return null;
	}
	
	private void writeToFile(String program, EpisodeObject epi) {
		BufferedWriter writer = null;
		String NL = System.getProperty("line.separator");
		String postFix = "_preprocessed";
		String outputPath = inputPath + "/" + program + postFix;
		File outDir = new File(outputPath);
		if(!outDir.exists()) {
			try {
				outDir.mkdir();
			} catch(SecurityException se) {
				se.printStackTrace();
			}
		}

		String outputFile = outDir.getAbsolutePath() + "/" + epi.getEpisodeId();
		File outFile = new File(outputFile);
		try {
			writer = new BufferedWriter(new FileWriter(outFile));
			List<String> hts = epi.getSentences();
			for(String s : hts) {
				writer.write(s + NL);
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

		FilterHtsFiles dao = new FilterHtsFiles();
		String[] folders = {"sbs2pm_201412_hts", "sbs4pm_201412_hts", "sbs6pm_201412_hts",
							"sbs8pm_201412_hts", "sbs10pm_201412_hts", "sbs12am_201412_hts"};
		
		for(String s : folders) {
			System.out.println("Processing program " + s + ".");
			dao.getEpisodes(s);
		}

		
/*		for(EpisodeObject epiObj : preprocessedEpisodes) {
			List<String> hts = epiObj.getSentences();
			String id = epiObj.getEpisodeId();
			System.out.println(id);
			for(String s : hts)
				System.out.println(s);
		}*/
	}

}
