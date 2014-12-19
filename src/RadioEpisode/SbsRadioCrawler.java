package RadioEpisode;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SbsRadioCrawler {

	private final String SBSURL = "http://nbbs3.sbs.co.kr/index.jsp?cmd=read&code=tb_powertime1&no=108759&page_no=16344";
	
	private void crawlEpisodes(String startIdx) {
		try {
			Document doc = Jsoup.connect(SBSURL).get();
			
			//System.out.println(doc.html());
			Element title = doc.getElementsByAttributeValueMatching("headers", "con_title").first();
			Element requestDate = doc.getElementsByAttributeValue("headers", "con_date").first();
			Element writer = doc.getElementsByAttributeValue("headers", "con_writer").first();
			Element content = doc.getElementsByAttributeValue("id", "content").first();
			
			Element topBar = doc.getElementsByClass("fl").first();
			Elements topBarElems = topBar.getElementsByAttribute("href");
			
			for(Element e : topBarElems)
			
			System.out.println(title.text() + requestDate.text() + writer.text());
			System.out.println(topBar.text());
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SbsRadioCrawler dao = new SbsRadioCrawler();
		dao.crawlEpisodes("abc");
	}

}
