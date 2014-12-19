package RadioEpisode;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CrawlKpopDb {

	private final String BASICURL = "http://www.k-pop.or.kr";
	private final String ARTISTURL = "/history2014/directory_part.jsp?pagenum=";
	private final String TITLEURL = "/history2014/directory_part_detail.jsp?pagenum=";
	private final String URLPOSTFIX = "&eid=107&role_name=노래";
	
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
	
	private List<String> getExistingDates(String artist, String title) throws Exception {
		ArrayList<String> dateList = new ArrayList<String>();
		
		try {
			PreparedStatement select = kpopConnect.prepareStatement("SELECT * FROM kpop_archive WHERE artist = ? AND title = ?");
			select.setString(1, artist);
			select.setString(2, title);
			
			ResultSet rs = select.executeQuery();
			while(rs.next())
				dateList.add(rs.getString(4));
			
			select.close();
			rs.close();
			
		} catch(SQLException e) {
			throw e;
		}
		
		return dateList;
	}
	
	private void insertSongToDb(String artist, String title, String issueDate) {
		try {
			PreparedStatement insertStatement = kpopConnect.prepareStatement("INSERT INTO kpop_archive (artist, title, issue_date) VALUES (?, ?, ?)");
			insertStatement.setString(1, artist);
			insertStatement.setString(2, title);
			insertStatement.setString(3, issueDate);
			insertStatement.addBatch();
			insertStatement.executeBatch();
			kpopConnect.commit();
			
			insertStatement.close();
			
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void updateSongDb(String artist, String title, String issueDate, List<String> existingDates) {
		String pattern = "yyyy.MM.dd";
		SimpleDateFormat formatter = new SimpleDateFormat(pattern);
		String oldestDate = issueDate;
		
		for(String existingDate : existingDates) {
			if(oldestDate.equals("NOT SPECIFIED") && !existingDate.equals("NOT SPECIFIED")) {
				oldestDate = existingDate;
			} else if(!oldestDate.equals("NOT SPECIFIED") && existingDate.equals("NOT SPECIFIED")) {
			} else if(oldestDate.equals("NOT SPECIFIED") && existingDate.equals("NOT SPECIFIED")) {
				oldestDate = "NOT SPECIFIED";
			} else {
				try {
					Date curDate = formatter.parse(oldestDate);
					Date prevDate = formatter.parse(existingDate);
					if(curDate.after(prevDate))
						oldestDate = formatter.format(prevDate);
					
				} catch(ParseException e) {
					e.printStackTrace();
				}
			}
		}
		
		try {
			PreparedStatement update = kpopConnect.prepareStatement("UPDATE kpop_archive SET issue_date = ? WHERE artist = ? AND title = ?");
			update.setString(1, oldestDate);
			update.setString(2, artist);
			update.setString(3, title);
			update.addBatch();
			update.executeBatch();
			kpopConnect.commit();
			
			update.close();
			
		} catch(SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	private String parseDate(String issueDate) {
		String pattern = "yyyy.MM.dd";
		SimpleDateFormat formatter = new SimpleDateFormat(pattern);
		Date tmp = null;
		String toReturn = null;

		if(issueDate.equals("0000") || issueDate.equals("null")) {
			toReturn = "NOT SPECIFIED";
		}
		else {
			try {
				if(issueDate.split("\\.").length == 1)
					issueDate = issueDate + ".01.01";
				else if(issueDate.split("\\.").length == 2)
					issueDate = issueDate + ".01";
				
				tmp = formatter.parse(issueDate);
				toReturn = formatter.format(tmp);

			} catch(ParseException e) {
				e.printStackTrace();
			}
		}		
		
		return toReturn;
	}
	
	public void crawlSongs(String artistUrl) {
		Document doc = null;
		String artist = artistUrl.substring(artistUrl.indexOf("name_kor=") + 9);
		try {
			doc = Jsoup.connect(BASICURL + TITLEURL + String.valueOf(1) + URLPOSTFIX + artistUrl).get();
			Element songCountElem = doc.getElementsByClass("sn").first();
			int totalSongs = Integer.parseInt(songCountElem.text().replaceAll("[^0-9]", ""));
			System.out.println(totalSongs);
			int endPage = -1;
			if(totalSongs % 30 == 0)
				endPage = totalSongs / 30;
			else
				endPage = totalSongs / 30 + 1;
			for(int i = 1; i <= endPage; i++) {
				if(endPage < 10) {
					System.out.print(".");
				} else {
					if(i % (endPage / 10) == 0)
						System.out.print(".");
				}
				doc = Jsoup.connect(BASICURL + TITLEURL + String.valueOf(i) + URLPOSTFIX + artistUrl).get();
				
				Element titlesPerPage = doc.getElementsByClass("boardlist").first();
				
				Elements links = titlesPerPage.getElementsByTag("tr");
				for(int j = 1; j < links.size(); j++) {
					Elements info = links.get(j).getElementsByTag("td");
					String title = info.get(1).text();
					String issueDate = parseDate(info.get(3).text());
					
					List<String> existingDates = getExistingDates(artist, title);
					if(existingDates.size() == 0)
						insertSongToDb(artist, title, issueDate);
					else
						updateSongDb(artist, title, issueDate, existingDates);
				}
			}
			System.out.println();
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("Retrying song crawling for artist <" + artistUrl + "> in 1 minute.");
			try {
				Thread.sleep(1000 * 60);
				crawlSongs(artistUrl);
			} catch(InterruptedException e2) {
				e2.printStackTrace();
			}
		}
	}
	
	private void crawlArtists(String pageNum) {
		Document doc = null;
		try {
			doc = Jsoup.connect(BASICURL + ARTISTURL + pageNum).get();
			
			Element artistPerPage = doc.getElementsByClass("boardlist").first();
			
			Elements links = artistPerPage.getElementsByAttribute("href");
			for(Element e : links) {
				String artistLink = e.outerHtml().replaceAll("&amp;", "&");
				artistLink = artistLink.substring(artistLink.indexOf("&aid="));
				//artistLink = artistLink.substring(0, artistLink.indexOf("\""));
				artistLink = artistLink.substring(0, artistLink.indexOf("&total"));
				System.out.println("Crawling data for artist: " + artistLink);
				crawlSongs(artistLink);
			}
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("Retrying artist crawling for page <" + pageNum + "> in 1 minute.");
			try {
				Thread.sleep(1000 * 60);
				crawlArtists(pageNum);
			} catch(InterruptedException e2) {
				e2.printStackTrace();
			}
		}
	}
	
	private void close() {
		try {
			if(kpopConnect != null)
				kpopConnect.close();
		} catch(Exception e) {}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CrawlKpopDb dao = new CrawlKpopDb();
		dao.connectToDb();
		
		int endPage = 1012;
		for(int i = 529; i <= endPage; i++) {
			System.out.println("Currently processing page: " + String.valueOf(i));
			dao.crawlArtists(String.valueOf(i));
		}
		//dao.crawlSongs("&aid=20370&name_kor=박진석");
		dao.close();
	}

}
