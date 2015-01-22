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
import java.util.Scanner;

import DataTypes.EpisodeObject;
import DataTypes.SongObject;


public class SearchRequestSong {

	private final static String DATAPATH = "D:/Data/RadioEpisode/";
	private final String OUTPUTPATH = DATAPATH + "sbs_request_songs/";
	private Connection kpopConnect = null;
	
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
			PreparedStatement select = kpopConnect.prepareStatement("SELECT * FROM kpop_archive LIMIT 0, 30000");
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

	private void retrieveRequestSong(ArrayList<SongObject> songObj) {

		ArrayList<EpisodeObject> epiList = retrieveEpisodeInfo();
		
		for(SongObject rtObj : songObj) {
			System.out.println("Processing artist = " + rtObj.getArtist() + " title = " + rtObj.getTitle());
			String artist = rtObj.getArtist();
			String title = rtObj.getTitle();
			if(!processedSong(artist, title)) {
				ArrayList<String> requestedEpisodes = searchRequestSongs(rtObj.getArtist(), rtObj.getTitle(), epiList);
				writeToFile(artist, title, requestedEpisodes);
			} else
				System.out.println("Already processed.");
		}
	}
	
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
						sentences.add(scanner.nextLine().trim());
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
	
	private ArrayList<String> searchRequestSongs(String artist, String title, ArrayList<EpisodeObject> epiList) {
		ArrayList<String> toReturn = new ArrayList<String>();
		ArrayList<String> artistNames = new ArrayList<String>();

		if(artist.contains("(")) {
			artistNames.add(artist.substring(0, artist.indexOf("(")));
			artistNames.add(artist.substring(artist.indexOf("(") + 1, artist.indexOf(")")));
		} else {
			artistNames.add(artist);
		}

		for(EpisodeObject epiObj : epiList) {
			String epiId = epiObj.getEpisodeId();
			ArrayList<String> sentences = epiObj.getSentences();
			for(String name : artistNames) {
				
				for(String s : sentences) {
					if(title.length() < 2) {
						if(s.toLowerCase().replaceAll("\\p{Punct}", "").replaceAll(" ", "").contains(title) 
								&& s.toLowerCase().contains(name)
								&& (s.contains("신청") || s.contains("듣고") || s.contains("틀어") || s.contains("들려"))) {
							if(!toReturn.contains(epiId))
								toReturn.add(epiId);
						}
					} else {
						if(s.toLowerCase().replaceAll("\\p{Punct}", "").replaceAll(" ", "").contains(title)
								&& s.toLowerCase().contains(name)) {
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
		
		File outFile = new File(outDir.getAbsolutePath() + "/" + artist + "_" + title + ".txt");
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
	
	public static void main(String[] args) {
		SearchRequestSong dao = new SearchRequestSong();
		dao.connectToDb();
		ArrayList<SongObject> songs = dao.retrieveSongInfo();
		dao.retrieveRequestSong(songs);		
		dao.close();
	}

}
