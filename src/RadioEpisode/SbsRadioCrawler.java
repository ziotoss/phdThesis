package RadioEpisode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SbsRadioCrawler {

	private final String SBSURL = "http://nbbs3.sbs.co.kr/index.jsp?cmd=read&code=tb_";
	private final String OUTPATH = "D:/Data/RadioEpisode/";
	private final String NL = System.getProperty("line.separator");
	
	private void crawlEpisodes(String program, String programId, String startIdx) {
		String prevId = startIdx;
		while(!prevId.equals("DONE")) {
			try {
				Document doc = Jsoup.connect(SBSURL + programId + "&" + prevId).get();
				//System.out.println(doc.html());
				Element title = doc.getElementsByAttributeValueMatching("headers", "con_title").first();
				Element requestDate = doc.getElementsByAttributeValue("headers", "con_date").first();
				Element author = doc.getElementsByAttributeValue("headers", "con_writer").first();
				Element content = doc.getElementsByAttributeValue("id", "content").first();
				Element topBar = doc.getElementsByClass("fl").first();
				Elements topBarElems = topBar.getElementsByAttribute("href");
	
				for(Element e : topBarElems) {
					if(e.outerHtml().contains("¿Ã¿¸")) {
						prevId = e.outerHtml().replace("&amp;", ";");
						prevId = prevId.substring(prevId.indexOf("no="), prevId.indexOf(";page_no"));
						break;
					} else {
						prevId = "DONE";
					}
				}
		
				String contentStr = content.html();
				contentStr = contentStr.replace("<p>", "").replace("</p>", "").replace("&nbsp;", "")
						.replaceAll("[<][s][c][r][i][p][t][>](.*?)[<][/][s][c][r][i][p][t][>]", "")
						.replaceAll("[<][i][m][g](.*?)[/][>]", "");
				
				writeToFile(title.text().trim(), requestDate.text().trim(), author.text().trim(), contentStr.trim(),
							prevId.substring(3).trim(), program);
			} catch(Exception e) {
				e.printStackTrace();
				BufferedWriter writer = null;
				File errLog = new File(OUTPATH + "errLog.txt");
				try {
					writer = new BufferedWriter(new FileWriter(errLog, true));
					writer.write("ERROR PROGRAM: " + program + " ERROR ID: " + prevId + NL);
				} catch(IOException ie) {
					ie.printStackTrace();
				} finally {
					try {
						writer.close();
					} catch(IOException ie) {
						ie.printStackTrace();
					}
				}
			}
		}
	}
	
	private void writeToFile(String title, String requestDate, String author, String content, String fileName, String program) {
		BufferedWriter writer = null;
		String outputPath = OUTPATH + program + "_201412";
		File outDir = new File(outputPath);
		if(!outDir.exists()) {
			try {
				outDir.mkdir();
			} catch(SecurityException se) {
				se.printStackTrace();
			}
		}
		
		File outFile = new File(outDir.getAbsolutePath() + "/" + fileName + ".txt");
		try {
			writer = new BufferedWriter(new FileWriter(outFile));
			writer.write("DATE" + NL);
			writer.write(requestDate + NL + NL);
			writer.write("TITLE" + NL);
			writer.write(title + NL + NL);
			writer.write("AUTHOR" + NL);
			writer.write(author + NL + NL);
			writer.write("CONTENT" + NL);
			writer.write(content + NL);
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SbsRadioCrawler dao = new SbsRadioCrawler();
		String[] programs = {"sbs4pm", "sbs6pm", "sbs8pm", "sbs10pm", "sbs12am"}; //"sbs12pm", "sbs2pm", 
		String[] programIds = {"school01", "lovegame03", "Boom2ayo5", "greatradio15", "todaynight07"}; //"powertime1", "cult16", 
		String[] startIdx = {"no=1", "no=1", "no=1", "no=1", "no=1"}; //"no=108759", "no=1", 
		
		for(int i = 0; i < programs.length; i++) {
			System.out.println("Currently crawling program: " + programs[i]);
			dao.crawlEpisodes(programs[i], programIds[i], startIdx[i]);
		}
	}
}
