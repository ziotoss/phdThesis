package RadioEpisode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import DataTypes.EpisodeObject;
import DataTypes.SongObject;

class SearchRequestSongThread extends Thread {
	private Thread t;
	private String startIdx;
	private ArrayList<EpisodeObject> epiList;
	private final String OUTPUTPATH = "D:/Data/RadioEpisode/sbs_request_songs2/";
	private static Connection kpopConnect = null;
	
	SearchRequestSongThread(String idx, ArrayList<EpisodeObject> epiL) {
		startIdx = idx;
		epiList = epiL;
		System.out.println("Creating thread for index " + startIdx);
	}
	
	private void connectToDb() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			// Setup the connection with the DB
			kpopConnect = DriverManager.getConnection("jdbc:mysql://localhost/koreansongs?useUnicode=yes&characterEncoding=EUCKR", "root", "apmsetup");
			kpopConnect.setAutoCommit(false);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private ArrayList<SongObject> retrieveSongInfo() {
		System.out.println("Retrieving song info from Db.");
		ArrayList<SongObject> songInfo = new ArrayList<SongObject>();
		try {
			PreparedStatement select = kpopConnect.prepareStatement("SELECT * FROM kpop_archive LIMIT " + startIdx + ", 10000");
			ResultSet rs = select.executeQuery();
			
			while(rs.next()) {
				String artist = rs.getString(2);
				
				String title = rs.getString(3);
				title = title.replaceAll("[(](.*?)[)]", "").replaceAll("[(](.*)", "").replaceAll("[\\[](.*?)[\\]]", "");
				title = title.replaceAll(" ", "").replaceAll("\\p{Punct}", "");

				artist = artist.trim();
				title = title.trim();
				if(!title.isEmpty()) {
					SongObject obj = new SongObject();
					obj.setArtist(artist.toLowerCase());
					obj.setTitle(title.toLowerCase());
					if(!songInfo.contains(obj))
						songInfo.add(obj);
				}
			}
			rs.close();
			select.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return songInfo;
	}

	private void retrieveRequestSong(ArrayList<SongObject> songObj, ArrayList<EpisodeObject> epiList) {
		
		for(SongObject rtObj : songObj) {
			System.out.println("Processing artist = " + rtObj.getArtist() + " title = " + rtObj.getTitle());
			String artist = rtObj.getArtist().toLowerCase().replaceAll(" ", "").replaceAll("\\p{Punct}", "").trim();
			String title = rtObj.getTitle().toLowerCase().replaceAll(" ", "").replaceAll("\\p{Punct}", "").trim();
			if(!processedSong(artist, title)) {
				ArrayList<String> requestedEpisodes = searchRequestSongs(rtObj.getArtist(), rtObj.getTitle(), epiList);
				writeToFile(artist, title, requestedEpisodes);
			} else
				System.out.println("Already processed.");
		}
	}
	
	private ArrayList<String> searchRequestSongs(String artist, String title, ArrayList<EpisodeObject> epiList) {
		ArrayList<String> toReturn = new ArrayList<String>();
		ArrayList<String> artistNames = new ArrayList<String>();

		if(artist.contains("(") && artist.contains(")")) {
			artistNames.add(artist.substring(0, artist.indexOf("(")));
			artistNames.add(artist.substring(artist.indexOf("(") + 1, artist.indexOf(")")));
		} else if(artist.contains("(") && !artist.contains(")")){
			artistNames.add(artist.substring(0, artist.indexOf("(")));
			artistNames.add(artist.substring(artist.indexOf("(") + 1));
		} else {
			artistNames.add(artist);
		}

		for(EpisodeObject epiObj : epiList) {
			String epiId = epiObj.getEpisodeId();
			List<String> sentences = epiObj.getSentences();
			for(String name : artistNames) {
				String nameTmp = name.toLowerCase().replaceAll(" ", "").replaceAll("\\p{Punct}", "").trim();
				
				for(String s : sentences) {
					String sentenceTmp = s.toLowerCase().replaceAll(" ", "").replaceAll("\\p{Punct}", "").trim();
					if(title.length() < 2) {
						/*if(sentenceTmp.contains(title) && sentenceTmp.contains(nameTmp)
								&& (sentenceTmp.contains("신청") || sentenceTmp.contains("듣고") || sentenceTmp.contains("틀어") || sentenceTmp.contains("들려"))) {
							if(!toReturn.contains(epiId))
								toReturn.add(epiId);
						}*/
						if(sentenceTmp.matches(".*" + nameTmp + ".*" + title + ".*")
								&& (sentenceTmp.contains("듣고") || sentenceTmp.contains("신청") || sentenceTmp.contains("틀어") || sentenceTmp.contains("들려"))) {
							if(!toReturn.contains(epiId))
								toReturn.add(epiId);
						}

						else if(sentenceTmp.matches(".*" + title + ".*" + nameTmp + ".*")
								&& (sentenceTmp.contains("듣고") || sentenceTmp.contains("신청") || sentenceTmp.contains("틀어") || sentenceTmp.contains("들려"))) {
							if(!toReturn.contains(epiId))
								toReturn.add(epiId);
						}
								
					} else {
						/*if(sentenceTmp.contains(title) && s.toLowerCase().contains(nameTmp)) {
							if(!toReturn.contains(epiId))
								toReturn.add(epiId);
						}*/
						if(sentenceTmp.matches(".*" + nameTmp + ".*" + title + ".*")) {
							if(!toReturn.contains(epiId))
								toReturn.add(epiId);
						} else if(sentenceTmp.matches(".*" + title + ".*" + nameTmp + ".*")) {
							if(!toReturn.contains(epiId))
								toReturn.add(epiId);
						}
					}
				}
			}
		}
		
		return toReturn;
	}
	
	private boolean processedSong(String artist, String title) {
		File outFile = new File(OUTPUTPATH + artist + "_" + title + ".txt");
		return outFile.exists();
	}
	
	private void writeToFile(String artist, String title, ArrayList<String> requestedEpisodes) {

		BufferedWriter writer = null;
		
		String outputPath = OUTPUTPATH;
		File outDir = new File(outputPath);
		if(!outDir.exists()) {
			try {
				outDir.mkdir();
			} catch(SecurityException se) {
				se.printStackTrace();
			}
		}
		
		File outFile = new File(outDir.getAbsolutePath() + "/" + artist.replaceAll("\\p{Punct}", "").trim() + "_" + title.replaceAll("\\p{Punct}", "").trim() + ".txt");
		String NL = System.getProperty("line.separator");
		try {
			writer = new BufferedWriter(new FileWriter(outFile));
			for(String s : requestedEpisodes)
				writer.write(s + NL);
			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch(Exception e) {
				
			}
		}
	}
	
	private void close() {
		try {
			if(kpopConnect != null)
				kpopConnect.close();
		} catch(Exception e) {}
	}
	
	public void run() {
		connectToDb();
		ArrayList<SongObject> songs = retrieveSongInfo();
		retrieveRequestSong(songs, epiList);
		close();
	}
	
	public void start() {
		if (t == null) {
			t = new Thread(this, startIdx);
			t.start();
		}
	}
}


class SearchRequestSong {

	private final static String DATAPATH = "D:/Data/RadioEpisode/";
	
	private ArrayList<EpisodeObject> retrieveEpisodeInfo() {
		System.out.println("Storing episodes into arraylist.");
		String[] programs = {"sbs12pm_201412", "sbs2pm_201412", "sbs4pm_201412", "sbs6pm_201412", "sbs8pm_201412", "sbs10pm_201412", "sbs12am_201412"};
		ArrayList<EpisodeObject> epiObjList = new ArrayList<EpisodeObject>();
		
		for(String program : programs) {

			File root = new File(DATAPATH + program);
			
			for(File f : root.listFiles()) {
				EpisodeObject epiObj = new EpisodeObject();
				epiObj.setEpisodeId(f.getAbsolutePath());
				
				try {
					Scanner scanner = new Scanner(new FileReader(f));
					ArrayList<String> sentences = new ArrayList<String>();

					while(scanner.hasNext()) {
						String line = scanner.nextLine().trim();
						if(!line.equals(""))
							sentences.add(line);
					}
					scanner.close();
					epiObj.setSentences(sentences);
					epiObjList.add(epiObj);
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return epiObjList;
	}

	public static void main(String[] args) {
		SearchRequestSong dao = new SearchRequestSong();
		//dao.connectToDb();
		
		ArrayList<EpisodeObject> epiObj = dao.retrieveEpisodeInfo();
		
		SearchRequestSongThread T1 = new SearchRequestSongThread("120000", epiObj);
		T1.start();
		
		SearchRequestSongThread T2 = new SearchRequestSongThread("130000", epiObj);
		T2.start();
		
		SearchRequestSongThread T3 = new SearchRequestSongThread("140000", epiObj);
		T3.start();
		
		SearchRequestSongThread T4 = new SearchRequestSongThread("150000", epiObj);
		T4.start();
		//dao1.connectToDb();
		//ArrayList<SongObject> songs = dao.retrieveSongInfo();
		//dao.retrieveRequestSong(songs);		
		//dao.close();
	}

}
