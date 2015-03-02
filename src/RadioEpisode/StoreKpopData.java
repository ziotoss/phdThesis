package RadioEpisode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import DataTypes.SongObject;

public class StoreKpopData {

	private Connection kpopConnect = null;
	private final String OUTPUTPATH = "D:/Data/RadioEpisode";
	
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
	
	private ArrayList<SongObject> retrieveSongInfo(int startIdx) {
		System.out.println("Retrieving song info from Db.");
		ArrayList<SongObject> songInfo = new ArrayList<SongObject>();
		try {
			PreparedStatement select = kpopConnect.prepareStatement("SELECT * FROM kpop_archive LIMIT " + String.valueOf(startIdx) + ", 30000");
			ResultSet rs = select.executeQuery();
			
			while(rs.next()) {
				String artist = rs.getString(2);				
				String title = rs.getString(3);

				if(!title.isEmpty()) {
					SongObject obj = new SongObject();
					obj.setArtist(artist);
					obj.setTitle(title);
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
	
	private void writeToFile(ArrayList<SongObject> songs, int index) {

		BufferedWriter writer = null;
		File outDir = new File(OUTPUTPATH);
		
		File outFile = new File(outDir.getAbsolutePath() + "/" + "Song_Info_" + String.valueOf(index) + ".txt");
		String NL = System.getProperty("line.separator");
		try {
			writer = new BufferedWriter(new FileWriter(outFile));
			for(SongObject s : songs)
				writer.write(s.getArtist() + "\t" + s.getTitle() + NL);
			
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
		// TODO Auto-generated method stub
		StoreKpopData dao = new StoreKpopData();
		int[] index = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
		
		dao.connectToDb();
		
		for(int i : index) {
			ArrayList<SongObject> sobj = dao.retrieveSongInfo(i * 30000);
			dao.writeToFile(sobj, i);
		}
		
		dao.close();
	}

}
