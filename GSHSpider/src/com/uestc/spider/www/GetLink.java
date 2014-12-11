package com.uestc.spider.www;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

/*
 * ����ò����ƿ�� �������ڴ治������
 * 2014.10.21��Ҫ�޸ĵĵط���1. �޷����ʵ����ӱ�������
 *                       2.���ʳ�ʱ�����ӱ������� �ȵ����ʵ�ʱ���������
 *                       3.
 * */
public class GetLink {

	// public String url;
	// ��һ�ڣ�Ϊ����չ
	public Queue<String> linkLast = new LinkedList<String>();
	// ��һ�� ��չ
	public Queue<String> linkNext = new LinkedList<String>();
	// ������������
	public Queue<String> linkTheme = new LinkedList<String>();
	// pdf ��չ
	public Queue<String> linkPdf = new LinkedList<String>();
	// ����ÿ���������ݵ�����
	public Queue<String> linkContent = new LinkedList<String>();
	// �����Ѿ����ʹ�������
	public Queue<String> linkVisit = new LinkedList<String>();

	// ƥ������link theme
	private String newThemeLink; // =
									// "http://www.chinamil.com.cn/jfjbmap/content/[0-9]{4}-[0-9]{2}/[0-9]{2}/node_[0-9]{1,2}.htm";
	// ƥ������link
	private String newContentLink; // ="http://www.chinamil.com.cn/jfjbmap/content/[0-9]{4}-[0-9]{2}/[0-9]{2}/content_[0-9]{5,6}.htm";
	// ƥ��PDF link Ԥ��
	private String newPdfLink;

	// ��ȡ��������
	private String newurl1; // "http://e.chengdu.cn/html/"
	private String newurl2; // "-"
	private String newurl3; // "/"
	private String newurl4; // "/node_2.htm"

	private int getLinkCount;

	public GetLink(String newthemelink, String newcontentlink, String s1,
			String s2, String s3, String s4) {
		this.newThemeLink = newthemelink;
		this.newContentLink = newcontentlink;
		this.newurl1 = s1;
		this.newurl2 = s2;
		this.newurl3 = s3;
		this.newurl4 = s4;
		this.getLinkCount = -1;
	}

	// ��url����������ĳһ����themeurl ����ʹĳһ���������ŵ�themeurl
	public void getLink(String themeUrl, int year, int month, int day,
			String encode) {
		int state;
		try {
			HttpURLConnection httpUrlConnection = (HttpURLConnection) new URL(
					themeUrl).openConnection(); // ��������
			state = httpUrlConnection.getResponseCode();
			// System.out.println("StateCode:"+state);
			httpUrlConnection.disconnect();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			System.out.println("���������Ѿ��޷��������ӣ��޷���ȡ����");
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("���糬�������Ѿ��޷��������ӣ��޷���ȡ����");
			return;
		}
		if (state != 200 && state != 201) {
			System.out.println(themeUrl + "�޷��򿪸�ҳ��" + "\n״̬��:" + state);
			return;
		}
		try {
			if (themeUrl.contains("http://shrb.qlwb.com.cn/shrb") // �����ձ�
					|| themeUrl.contains("http://epaper.qlwb.com.cn/qlwb") // ��³��
			) {
				Parser localparser = new Parser(themeUrl);
				localparser.setEncoding(encode);
				NodeIterator iterator = localparser.elements();
				String pagetext = "";
				while (iterator.hasMoreNodes()) {
					Node node = iterator.nextNode();
					pagetext += node.toHtml();
				}
				Pattern localpattern = Pattern.compile("(?<=URL=)(.*?(?=\"))");
				Matcher localmatcher = localpattern.matcher(pagetext);
				if (localmatcher.find()) {
					themeUrl = themeUrl.replace("issueindex.htm",
							localmatcher.group());
				}
			}
			Parser parser = new Parser(themeUrl);
			parser.setEncoding(encode);
			NodeList nodeList = parser
					.extractAllNodesThatMatch(new NodeFilter() {
						public boolean accept(Node node) {
							if (node instanceof LinkTag)// ���
								return true;
							return false;
						}

					});

			//
			// ���Ű���������ʽ
			Pattern newPage = Pattern.compile(newThemeLink); // "http://e.chengdu.cn/html/[0-9]{4}-[0-9]{2}/[0-9]{2}/node_[0-9]{1,2}.htm"
			// �������ݵ�������ʽ
			Pattern newContent = Pattern.compile(newContentLink); // "http://e.chengdu.cn/html/[0-9]{4}-[0-9]{2}/[0-9]{2}/content_[0-9]{1,6}.htm"
			// PDF������ʽ
			// Pattern newPdf =
			// Pattern.compile("http://e.chengdu.cn/page/[0-9]{1}/[0-9]{4}-[0-9]{2}/[0-9]{2}/[0-9]{2}/[0-9]{10}_pdf.pdf");

			// ��ȡһ����ҳ���е�����url ����url pdf url
			for (int i = 0; i < nodeList.size(); i++) {
				LinkTag n = (LinkTag) nodeList.elementAt(i);
				// System.out.print(n.getStringText() + "==>> ");
				// System.out.println(n.extractLink());
				// ĳһ��
				Matcher themeMatcher = newPage.matcher(n.extractLink());
				// ���������
				Matcher contentMatcher = newContent.matcher(n.extractLink());
				// PDF
				// Matcher pdfMatcher = newPdf.matcher(n.extractLink());

				if (!linkVisit.contains(n.extractLink())) {
					if (themeMatcher.find()) {
						String themeurl = n.extractLink();
						if (themeurl.contains("/shtml/jnsb/")) { // ����ʱ��
							themeurl = themeurl.replace("/shtml/jnsb/",
									"http://jnsb.e23.cn/shtml/jnsb/");
						}
						if (themeurl.contains("http://yzwb.sjzdaily.com.cn/")) { // ������  ������ҳ���ݰ���ǰһ�����ҳ��ַ
							if (!themeurl.contains(String.format(
									"/%04d-%02d/%02d/", year, month, day))) {
								continue;
							}
						}
						linkTheme.offer(themeurl);
						linkVisit.offer(themeurl);
						// System.out.println("#############"+n.extractLink());
					}
					if (contentMatcher.find()) {
						String contenturl = n.extractLink();
						if (contenturl.contains("/shtml/jnsb/")) { // ����ʱ��
							contenturl = contenturl.replace("/shtml/jnsb/",
									"http://jnsb.e23.cn/shtml/jnsb/");
						}

						linkContent.offer(contenturl);
						linkVisit.offer(contenturl);
						// System.out.println("!!!!!!!!"+n.extractLink());
					}
					// if(pdfMatcher.find()){
					// linkPdf.offer(n.extractLink());
					// linkVisit.offer(n.extractLink());
					// }

				}
				themeMatcher = null;
				contentMatcher = null;
				// pdfMatcher = null;
			}
			if (themeUrl.contains("http://ctdsb.cnhubei.com")) // ���춼�б�
			{
				// ���춼�б�themeҳ�������themeҳ����ϢΪ��̬����,��HTMLParser��������
				// ���ֶ����themeҳ��
				// Ŀǰ�۲��ֵ��Ϊ2-24 ��ʱΪ2-32δ���ֳ���40��
				this.getLinkCount++;
				if (this.getLinkCount < 1) {
					for (int i = 2; i <= 40; i++) {
						String themepage = "http://ctdsb.cnhubei.com/HTML/ctdsb/"
								+ String.format("%04d%02d%02d", year, month,
										day) + "/ctdsb" + i + ".html";
						if (!linkVisit.contains(themepage)) {
							linkTheme.offer(themepage);
							linkVisit.offer(themepage);
						}
					}
				}

				Parser ctdsbparser = new Parser(themeUrl);
				System.out.println("themeUrl" + themeUrl);
				NodeList tdList = ctdsbparser
						.extractAllNodesThatMatch(new TagNameFilter("td"));

				for (int i = 0; i < tdList.size(); i++) {
					Node td = tdList.elementAt(i);
					String tdText = td.getText();
					Matcher ctdsbcontent = newContent.matcher(tdText);
					if (ctdsbcontent.find()) {
						// ../../../html/ctdsb/20141117/ctdsb2476017.html�滻��
						// http://ctdsb.cnhubei.com/html/ctdsb/20141117/ctdsb2476017.html
						String contentURL = ctdsbcontent.group();
						if (contentURL.contains("../../../html/ctdsb/")) {
							contentURL = contentURL.replace("../../../",
									"http://ctdsb.cnhubei.com/");
						}
						// System.out.println("2222222"+contentURL);
						if (!linkVisit.contains(contentURL)) {
							linkContent.offer(contentURL);
							linkVisit.offer(contentURL);
						}
					}
				}
			} else if (themeUrl.contains("http://www.shangbw.com/"))// �����̱�
			{
				parser.reset();
				NodeFilter localNodeFilter = new TagNameFilter("Area");
				NodeList localNodeList = parser
						.extractAllNodesThatMatch(localNodeFilter);
				for (int i = 0; i < localNodeList.size(); i++) {
					Node n = (Node) localNodeList.elementAt(i);
					Matcher localcontentMatcher = newContent
							.matcher(n.toHtml());
					if (localcontentMatcher.find()) {
						String contenturl = localcontentMatcher.group();
						if (contenturl.contains("/epaper")) {
							contenturl = contenturl.replace("/epaper",
									"http://www.shangbw.com/epaper");
						}
						if (!linkVisit.contains(contenturl)) {
							linkContent.offer(contenturl);
							linkVisit.offer(contenturl);
						}
					}
					localcontentMatcher = null;
					// pdfMatcher = null;
				}
			} else if (themeUrl.contains("http://zzwb.zynews.com/")) // ֣����
			{
				URL url = new URL(themeUrl);
				HttpURLConnection connection = (HttpURLConnection) url
						.openConnection();
				connection.setRequestProperty("Content-type", encode);
				connection.setRequestProperty("Accept-Charset", encode);
				connection.connect();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(connection.getInputStream(),
								encode));
				String lines = null;
				String htmltext = "";
				while ((lines = reader.readLine()) != null) {
					htmltext += lines + "\n";
				}
				Matcher zzwbThemeMatcher = newPage.matcher(htmltext);
				Matcher zzwbContentMatcher = newContent.matcher(htmltext);
				while (zzwbThemeMatcher.find()) {
					String nodehtml = zzwbThemeMatcher.group();
					String themeurl = String.format(
							"http://zzwb.zynews.com/html/%04d-%02d/%02d/%s",
							year, month, day, nodehtml);
					if (!linkVisit.contains(themeurl)) {
						System.out.println("themeurl:" + themeurl);
						linkTheme.offer(themeurl);
						linkVisit.offer(themeurl);
					}
				}
				while (zzwbContentMatcher.find()) {
					String contenthtml = zzwbContentMatcher.group();
					String contenturl = String.format(
							"http://zzwb.zynews.com/html/%04d-%02d/%02d/%s",
							year, month, day, contenthtml);
					if (!linkVisit.contains(contenturl)) {
						System.out.println("contenturl:" + contenturl);
						linkContent.offer(contenturl);
						linkVisit.offer(contenturl);
					}
				}
			}
		} catch (ParserException e) {
			e.printStackTrace();
			return;
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	public void allWeWillDo(String themeUrl, String[] bqtitle,
			String[] bqcontent, String[] bqdate, String[] bqnewsource,
			String[] bqcategroy, String bqbuf, String encode, String DBName,
			String DBTable, String photourl, String imageurl, String imagescr,
			String imagebuf, int year, int month, int day) throws Exception {

		int i = 0;
		linkTheme.offer(themeUrl);
		// linkVisit.offer(n.extractLink());
		while (!linkTheme.isEmpty()) {
			String themeLink = linkTheme.poll();
			System.out.println("Theme URL:" + themeLink);
			getLink(themeLink, year, month, day, encode);
			while (!linkContent.isEmpty()) {
				StringBuffer s = new StringBuffer(linkContent.poll());
				i++;
				System.out.println("ContentURL:" + s);
				CDSB cdsb = new CDSB(s.toString(), bqtitle, bqcontent, bqdate,
						bqnewsource, bqcategroy, bqbuf, encode, photourl,
						imageurl, imagescr, imagebuf);
				cdsb.memory(s.toString(), bqtitle, bqcontent, bqdate,
						bqnewsource, bqcategroy, bqbuf, encode, DBName,
						DBTable, photourl, imageurl, imagescr, imagebuf);
			}
			// System.out.println("�����ڰѻ�ȡ�����Ŵ������ݿ�...");
		}
		System.out.println("���ֵ�����������" + i);

	}

	// ��ȡһ�������
	public void result(int year, int month, int day, String[] bqtitle,
			String[] bqcontent, String[] bqdate, String[] bqnewsource,
			String[] bqcategroy, String bqbuf, String encode, String DBName,
			String DBTable, String photourl, String imageurl, String imagescr,
			String imagebuf) throws Exception {
		Calendar now = Calendar.getInstance();
		int year1 = now.get(Calendar.YEAR);
		if (year > year1)
			return;
		if (month > 12 && month < 1)
			return;
		if (day > 31 && day < 1)
			return;

		StringBuffer s1 = new StringBuffer(newurl1); // newurl1 =
														// "http://e.chengdu.cn/html/"
														// newurl2 = -
		StringBuffer s2 = new StringBuffer(newurl3); // newurl3 = "/"
		StringBuffer s3 = new StringBuffer(newurl4); // newurl4 = "/node_2.htm"
		for (int j = 1; j < 13; j++) {

			if (j < 10)
				s1 = s1.append(year).append(newurl2).append("0"); // http://e.chengdu.cn/html/2014-0
			else
				s1 = s1.append(year).append(newurl2);
			StringBuffer url = new StringBuffer();
			for (int i = 1; i < 32; i++) {
				// String url;
				if (i < 10)
					url = url.append(s1).append(j).append(s2).append("0")
							.append(i).append(s3);
				else
					url = url.append(s1).append(s2).append(i).append(s3); // url.append(s1).append(j).append(s3).append(i).append(s2);

				// System.out.println(url);
				allWeWillDo(url.toString(), bqtitle, bqcontent, bqdate,
						bqnewsource, bqcategroy, bqbuf, encode, DBName,
						DBTable, photourl, imageurl, imagescr, imagebuf, year,
						month, day);
				// ����Ѿ����ʵ�link�б���ÿ���������ȡ�洢��Ҫ�����з��ʹ������ӽ���������Լ�ڴ�
				linkVisit.clear();

			}
			url = null;
			System.gc();
		}
	}

	// ��ȡһ�������
	public void resultForOneDay(int year, int month, int day, String[] bqtitle,
			String[] bqcontent, String[] bqdate, String[] bqnewsource,
			String[] bqcategroy, String bqbuf, String encode, String DBName,
			String DBTable, String photourl, String imageurl, String imagescr,
			String imagebuf) {
		// StringBuffer s1 = new StringBuffer(newurl1);
		// StringBuffer s2 = new StringBuffer(newurl3);
		// StringBuffer s3 = new StringBuffer(newurl4);
		// if (month < 10)
		// s1 = s1.append(year).append(newurl2).append("0"); //
		// http://e.chengdu.cn/html/2014-0
		// else
		// s1 = s1.append(year).append(newurl2);
		// StringBuffer url = new StringBuffer();
		// if (day < 10)
		// url = url.append(s1).append(month).append(s2).append("0")
		// .append(day).append(s3);
		// else
		// url = url.append(s1).append(s2).append(day).append(s3);
		String url = String.format("%s%04d%s%02d%s%02d%s", newurl1, year,
				newurl2, month, newurl3, day, newurl4);
		System.out.println("Merged url:" + url);
		try {
			allWeWillDo(url, bqtitle, bqcontent, bqdate, bqnewsource,
					bqcategroy, bqbuf, encode, DBName, DBTable, photourl,
					imageurl, imagescr, imagebuf, year, month, day);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// ����Ѿ����ʵ�link�б���ÿ���������ȡ�洢��Ҫ�����з��ʹ������ӽ���������Լ�ڴ�
		linkVisit.clear();
		url = null;
		System.gc();
	}

	public void hxdsb(String[] bqtitle, String[] bqcontent, String[] bqdate,
			String[] bqnewsource, String[] bqcategroy, String bqbuf,
			String encode, String newsource, String newtable, int year,
			int month, int day) {

		StringBuffer s1 = new StringBuffer(
				"http://www.wccdaily.com.cn/shtml/hxdsb/20141024/va");
		StringBuffer s2 = new StringBuffer(".shtml");

		for (int i = 1; i < 37; i++) {
			StringBuffer theme = new StringBuffer();
			if (i < 10)
				theme = theme.append(s1).append(0).append(i).append(s2);
			else
				theme = theme.append(s1).append(i).append(s2);
			System.out.println(theme);
			getLink(theme.toString(), year, month, day, encode);
			while (!linkContent.isEmpty()) {
				StringBuffer s = new StringBuffer(linkContent.poll());
				// i++;
				System.out.println(s);
				// CDSB cdsb = new CDSB(s.toString(),bqtitle ,bqcontent ,bqdate
				// ,bqnewsource ,bqcategroy ,bqbuf,encode);
				// cdsb.memory(s.toString(),bqtitle ,bqcontent ,bqdate
				// ,bqnewsource ,bqcategroy ,bqbuf,encode ,newsource,newtable);
				// s = null;
				// cdsb = null;
			}
			// theme = null;
		}

	}

	public static void main(String args[]) throws Exception {
		long start = System.currentTimeMillis();
		String url = "http://www.wccdaily.com.cn/shtml/hxdsb/20141029/va01.shtml";
		String url1 = "http://www.chinamil.com.cn/jfjbmap/content/2014-10/27/node_2.htm";
		String test1 = "http://zqb.cyol.com/html/2014-10/29/nbs.D110000zgqnb_01.htm";
		// GetLink test = new GetLink();
		// test.allWeWillDo(url1);
		// test.hxdsb();
		// test.getLink(url);
		// test.result(0, 0, 0);
		// String url = "http://e.chengdu.cn/html/2014-10/16/node_2.htm";
		// System.out.println(" ������Ŭ����������...");
		// test.allWeWillDo(url);
		// System.out.println("����ִ�����...");
		String theme = "http://www.chinamil.com.cn/jfjbmap/content/[0-9]{4}-[0-9]{2}/[0-9]{2}/node_[0-9]{1,2}.htm";
		String content = "http://www.chinamil.com.cn/jfjbmap/content/[0-9]{4}-[0-9]{2}/[0-9]{2}/content_[0-9]{5,6}.htm";
		String s1 = "http://www.chinamil.com.cn/jfjbmap/content/";
		String s2 = "-";
		String s3 = "/";
		String s4 = "/node_2.htm";
		String[] bqtitle = { "style", "line-height:140%;" };
		String[] bqcontent = { "id", "ozoom" };
		String[] bqdate = { "height", "25" };
		String[] bqnewsource = { "��ž���", "....." };
		String[] bqcategroy = { "class", "info" };
		String bqbuf = "";
		String encode = "utf-8";
		String DBName = "jfjb1";
		String DBTable = "cg";
		String photourl = "http://www.chinamil.com.cn/jfjbmap/";
		String imageurl = "IMG src=\"(.*?)res(.*?)attpic_brief.jpg\""; // "img src=\"(.*?)res(.*?)attpic_brief.jpg\""
		String imagescr = "http:\"?(.*?)(\"|>|\\s+)"; // "http:\"?(.*?)(\"|>|\\s+)"
		String imagebuf = "../../../";
		GetLink test = new GetLink(theme, content, s1, s2, s3, s4);
		// ��ȡ����ĳһ������������ url�̶�
		// test.allWeWillDo(url1,bqtitle,bqcontent,
		// bqdate,bqnewsource ,bqcategroy ,bqbuf,encode,DBName ,
		// DBTable,photourl,imageurl,imagescr,imagebuf);
		// ��ȡ�ƶ�ĳ�����������
		test.resultForOneDay(2014, 11, 8, bqtitle, bqcontent, bqdate,
				bqnewsource, bqcategroy, bqbuf, encode, DBName, DBTable,
				photourl, imageurl, imagescr, imagebuf);

		long end = System.currentTimeMillis();
		System.out.println(end - start);
	}

}
