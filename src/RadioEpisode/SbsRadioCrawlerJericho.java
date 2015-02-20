package RadioEpisode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;

public class SbsRadioCrawlerJericho {

	private final String OUTPATH = "D:/Data/RadioEpisode/";
	private final String NL = System.getProperty("line.separator");
	private final String SBSURL = "http://nbbs3.sbs.co.kr/index.jsp?cmd=read&code=tb_";

	private void crawlEpisodes(String program, String programId, String startId, String endId) {

		String prevId = startId;
		while(Integer.valueOf(prevId) <= Integer.valueOf(endId)) {
			try {
				if(Integer.valueOf(prevId) == Integer.valueOf(endId) / 100)
					System.out.print(".");
				Source source = new Source(new URL(SBSURL + programId + "&no=" + prevId));
				
				String contentStr = "";
				String titleStr = "";
				String dateStr = "";
				String authorStr = "";
				
				// Get title, request date, author, and content of the episode
				Element title = source.getFirstElement("headers", "con_title", false);
				if(title != null)
					titleStr = title.getRenderer().toString().trim();
				Element date = source.getFirstElement("headers", "con_date", false);
				if(date != null)
					dateStr = date.getRenderer().toString().trim();				
				Element author = source.getFirstElement("headers", "con_writer", false);
				if(author != null)
					authorStr = author.getRenderer().toString().trim();
				Element content = source.getElementById("content");
				if(content != null)
					contentStr = content.getRenderer().toString().trim();

				writeToFile(titleStr, dateStr, authorStr, contentStr, prevId, program);
				
				Element prevElem = source.getFirstElementByClass("fl");
				List<Element> prevElems = prevElem.getAllElements("a href");	
				for(Element e : prevElems) {
					if(e.toString().contains("btn_prev.gif")) {
						prevId = e.toString().substring(e.toString().indexOf("&no=") + 4, e.toString().indexOf("&page_no"));
						break;
					}
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				BufferedWriter writer = null;
				File errLog = new File(OUTPATH + "errLog.txt");
				try {
					writer = new BufferedWriter(new FileWriter(errLog, true));
					writer.write("ERROR PROGRAM: " + programId + " ERROR ID: " + prevId + NL);
					writer.write("---ERRORMSG---");
					writer.write(e.getMessage());
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
			writer.write("###DATE###" + NL);
			writer.write(requestDate + NL + NL);
			writer.write("###TITLE###" + NL);
			writer.write(title + NL + NL);
			writer.write("###AUTHOR###" + NL);
			writer.write(author + NL + NL);
			writer.write("###CONTENT###" + NL);
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
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SbsRadioCrawlerJericho dao = new SbsRadioCrawlerJericho();
		String[] programs = {"sbs8pm"}; //"sbs12pm", "sbs2pm", "sbs4pm", "sbs6pm", "sbs10pm", "sbs12am" 
		String[] programIds = {"Boom2ayo5"}; //"powertime1", "cult16", "school01", "lovegame03", "greatradio15", "todaynight07" 
		String[] startIdx = {"1"}; //108759, 1, "1", "1", "1", "1" 
		String[] endIdx = {"5747"}; //298775, 155691, "45771", "43399", "4685", "9202" 
		
		for(int i = 0; i < programs.length; i++) {
			System.out.println("Crawling episodes from program: " + programs[i]);
			dao.crawlEpisodes(programs[i], programIds[i], startIdx[i], endIdx[i]);
		}
	}


}
