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
		ArrayList<SongObject> songInfo = new ArrayList<SongObject>();
		try {
			PreparedStatement select = kpopConnect.prepareStatement("SELECT * FROM kpop_archive LIMIT 0, 10000");
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

		for(SongObject rtObj : songObj) {
			System.out.println("Processing artist = " + rtObj.getArtist() + " title = " + rtObj.getTitle());
			String artist = rtObj.getArtist();
			String title = rtObj.getTitle();
			ArrayList<String> requestedEpisodes = searchRequestSongs(rtObj.getArtist(), rtObj.getTitle());

			if(requestedEpisodes.size() > 0)
				writeToFile(artist, title, requestedEpisodes);
		}
	}
	
	private ArrayList<String> searchRequestSongs(String artist, String title) {
		ArrayList<String> toReturn = new ArrayList<String>();
		String[] programs = {"sbs8pm_201412"};
		
		for(String program : programs) {

			File root = new File(DATAPATH + program);
			
			for(File f : root.listFiles()) {
	
				try {
					Scanner scanner = new Scanner(new FileReader(f));
					ArrayList<String> artistNames = new ArrayList<String>();
					if(artist.contains("(")) {
						artistNames.add(artist.substring(0, artist.indexOf("(")));
						artistNames.add(artist.substring(artist.indexOf("(") + 1, artist.indexOf(")")));
					} else {
						artistNames.add(artist);
					}
					
					for(String name : artistNames) {
						
						while(scanner.hasNext()) {
							String line = scanner.nextLine();
							if(title.length() < 2) {
								if(line.toLowerCase().replaceAll("\\p{Punct}", "").replaceAll(" ", "").contains(title) 
										&& line.toLowerCase().contains(name)
										&& (line.contains("신청") || line.contains("듣고") || line.contains("틀어") || line.contains("들려"))) {
									if(!toReturn.contains(f.getAbsolutePath()))
										toReturn.add(f.getAbsolutePath());
								}
							} else {
								if(line.toLowerCase().replaceAll("\\p{Punct}", "").replaceAll(" ", "").contains(title)
										&& line.toLowerCase().contains(name)) {
									if(!toReturn.contains(f.getAbsolutePath()))
										toReturn.add(f.getAbsolutePath());
								}
							}
						}
						
						scanner.close();	
					}
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return toReturn;
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
