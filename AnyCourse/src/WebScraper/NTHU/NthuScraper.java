package WebScraper.NTHU;

import WebScraper.Output.CourseList;
import WebScraper.Output.OutputFormat;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Method;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

//清華大學
public class NthuScraper implements CourseList{
	private ArrayList<OutputFormat> outputs = new ArrayList<OutputFormat>();
	private ArrayList<String> courseURLLists;
	private ArrayList<String> unitNameLists;
	//頁數網址
	private static final String TARGET_URL = "http://ocw.nthu.edu.tw/ocw/index.php?page=courseList&classid=0&order=time&p=";
	//每一個課程網址
	private static final String COURSE_URL = "http://ocw.nthu.edu.tw/ocw/";
	//影片的圖片網址
	private static final String VIDEO_IMG_URL = "http://ocw.nthu.edu.tw/videosite/assert/";
	
	public NthuScraper() throws IOException {
		outputs = new ArrayList<OutputFormat>();
		courseURLLists = new ArrayList<String>();
		unitNameLists = new ArrayList<String>();
		Connection.Response res;
		Connection.Response res2;
		Connection.Response res3;
		
		Document document = null;
		Document document2 = null;
		Document document3 = null;
		
		Elements courseURLElement = null;
		Elements unitList = null;
		Elements tableListTd = null;

		String courseURLToken = null;
		String videoURLToken = null;
		String unitURLToken = null;
		String courseName = null;
		String unitNameFirstHalf = null;
		String unitNameSecondHalf = null;

		String teacher = null;
		String videoIframe = null;
		
		String URLId = null;
		String URLFilename = null;
		
		String otherUnitURLToken = null;
		String otherUnitURL = null;
		String otherImgURLToken = null;
		String otherImgURL = null;
		
		int x;
		//總共14頁
		for (int i = 1; i <= 14; ++i) {
			res = Jsoup.connect(TARGET_URL + i)
					.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:26.0) Gecko/20100101 Firefox/26.0")
					.method(Method.GET).execute();
			document = res.parse();
			try {
				courseURLElement = document.select("#content > div > div > div > div.courseListBlock > div");
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			x = 0;
			for (Element URL : courseURLElement) {
				courseURLToken = courseURLElement.get(x).attr("onclick");
				if(courseURLToken!="") {
					courseURLLists.add(courseURLToken.split("\\'")[1]);
				}
				else {
					System.out.println(i+"-"+x);
				}
				x++;
			}
		}
		
		for (int i = 0; i < courseURLLists.size(); ++i) {
			OutputFormat output = new OutputFormat();
			videoURLToken = null;
			unitURLToken = null;
			System.out.println(i);
			try {
				res = Jsoup.connect(COURSE_URL + courseURLLists.get(i))
						.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:26.0) Gecko/20100101 Firefox/26.0")
						.method(Method.GET).execute();
				document = res.parse();
			
				courseName = document.select("#content > div > h2").get(0).text();
				
				teacher = document.select("#content > div.content-left > div:nth-child(1) > div > div > div.title").get(0).text();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			
			try {
				unitList = document.select("#search2 > ul > li");
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			//每個單元影片裡的td下的結構不一樣，從"第三世界政治"開始不一樣
			if(i < 114) {
				for(Element count : unitList) {
					try {
						videoURLToken = count.select("div > a").get(0).attr("href");
						unitNameFirstHalf = count.select("div > a").get(0).text();
					}
					catch(Exception e) {
						e.printStackTrace();
					}
					
					unitNameLists.add(unitNameFirstHalf);
					
					try {
						res2 = Jsoup.connect(COURSE_URL + videoURLToken)
								.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:26.0) Gecko/20100101 Firefox/26.0")
								.method(Method.GET).execute();
						document2 = res2.parse();
						tableListTd = document2.select("#page > div.content-left2 > div:nth-child(1) > div > div > div:nth-child(5) > table > tbody > tr > td");
					}
					catch(Exception e) {
						e.printStackTrace();
					}
					
					for(Element td : tableListTd) {
						try {
							unitNameSecondHalf = td.select("label").get(0).text();
							output.setUnitName(unitNameFirstHalf+"_"+unitNameSecondHalf);
							unitURLToken = td.select("a:nth-child(2)").get(0).attr("href");
							res3 = Jsoup.connect(COURSE_URL + unitURLToken)
									.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:26.0) Gecko/20100101 Firefox/26.0")
									.method(Method.GET).execute();
							document3 = res3.parse();
							videoIframe = document3.select("#videoFrame").get(0).attr("src");
						}
						catch(Exception e) {
							e.printStackTrace();
						}
						
						try {
							URLId = videoIframe.split("=")[2].split("&")[0];
							URLFilename = videoIframe.split("=")[3].split("&")[0];
							
							output.setUnitURL(VIDEO_IMG_URL + URLId + "/" + URLFilename);
							
						}
						catch(Exception e) {
							output.setUnitURL(null);
							output.setUnitImgSrc(null);
						}
					}
					
				}
				output.setUnitImgSrc(VIDEO_IMG_URL + URLId + "/cover.png");
			}
			//每個單元影片裡的td下的結構不一樣
			else {
				
				otherImgURLToken = document.select("#content > div.content-left > div:nth-child(1) > div > div > div:nth-child(3) > img").get(0).attr("src");
				otherImgURL = COURSE_URL + otherImgURLToken.split("\\./")[1];
				
				for(Element count : unitList) {
					try {
						videoURLToken = count.select("div > a").get(0).attr("href");
						unitNameFirstHalf = count.select("div > a").get(0).text();
					}
					catch(Exception e) {
						e.printStackTrace();
					}
					
					unitNameLists.add(unitNameFirstHalf);
					
					try {
						res2 = Jsoup.connect(COURSE_URL + videoURLToken)
								.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:26.0) Gecko/20100101 Firefox/26.0")
								.method(Method.GET).execute();
						document2 = res2.parse();
						tableListTd = document2.select("#page > div.content-left2 > div:nth-child(1) > div > div > div:nth-child(5) > table > tbody > tr > td");
					}
					catch(Exception e) {
						e.printStackTrace();
					}
					for(Element td : tableListTd) {
						output.setUniversity("國立清華大學");
						output.setCourseName(courseName);
						output.setTeacher(teacher);
						output.setUnitName(unitNameFirstHalf);
						output.setUnitImgSrc(otherImgURL);
						//後面十幾個課程都是例外，要從下載這個網址切出影片網址
						try {
							otherUnitURLToken = td.select("a:nth-child(4)").get(0).attr("href");
							otherUnitURL = VIDEO_IMG_URL + "old_video/" 
									+ otherUnitURLToken.split("2F")[6].split("%")[0] +"/"
									+ otherUnitURLToken.split("2F")[7].split("%")[0] +"/"
									+ otherUnitURLToken.split("2F")[8].split("%")[0] +"/"
									+ otherUnitURLToken.split("2F")[9].split("%")[0].split("\\.")[0]
									+ "_320k."
									+ otherUnitURLToken.split("2F")[9].split("%")[0].split("\\.")[1];
							output.setUnitURL(otherUnitURL);
						}
						catch(Exception e) {
							output.setUnitURL(null);
							output.setUnitImgSrc(null);
							e.printStackTrace();
						}
					}
				}
			}
			outputs.add(output);
		}
	}
	@Override
	public ArrayList<OutputFormat> getCourseList() {
		return outputs;
	}
}
