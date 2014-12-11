package com.uestc.spider.www;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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

public class MainSiteTest {
	private String MainSiteURL;
	private String encoding;
	private String themeReg;
	private String contentReg;

	public MainSiteTest(String MainSiteURL, String encoding, String themeReg,
			String contentReg) {
		this.MainSiteURL = MainSiteURL;
		this.encoding = encoding;
		this.themeReg = themeReg;
		this.contentReg = contentReg;
	}

	private void Test(String HTMLContent) {
		Parser MainSiteParser = null;
		try {
			System.out.println(MainSiteURL);
			if (!HTMLContent.equals("")) {
				MainSiteParser = Parser
						.createParser(HTMLContent, this.encoding);
				MainSiteParser.setEncoding(encoding);
			}
			else{
				MainSiteParser = new Parser(MainSiteURL);
				MainSiteParser.setEncoding(encoding);
			}
			NodeFilter nodeFilter = new NodeFilter() {
				public boolean accept(Node node) {
					if (node instanceof LinkTag) {
						return true;
					}
					return false;
				}
			};
			// NodeFilter nodeFilter2 = new TagNameFilter("Area");
			NodeList nodeList = MainSiteParser
					.extractAllNodesThatMatch(nodeFilter);
			// MainSiteParser.reset();
			// NodeList nodeList2 = MainSiteParser
			// .extractAllNodesThatMatch(nodeFilter2);
			System.out.println("Size:" + nodeList.size());
			Pattern themeURLs = Pattern.compile(themeReg);
			Pattern contentURLs = Pattern.compile(contentReg);
			for (int i = 0; i < nodeList.size(); i++) {
				LinkTag n = (LinkTag) nodeList.elementAt(i);
				System.out.println("Raw text:" + n.extractLink().toString());
				Matcher themeMatcher = themeURLs.matcher(n.extractLink());
				Matcher contentMatcher = contentURLs.matcher(n.extractLink());
				if (themeMatcher.find()) {
					System.out.println("ThemeURL:" + themeMatcher.group());
				} else if (contentMatcher.find()) {
					System.out.println("ContentURL:" + contentMatcher.group());
				}
			}
			// for(int i=0;i<nodeList2.size();i++){
			// Node n=(Node)nodeList2.elementAt(i);
			// System.out.println(n.toHtml());
			// }
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getHTMLTextByParser() {
		Parser MainSiteParser = null;
		try {
			String buf = "";
			MainSiteParser = new Parser(MainSiteURL);
			MainSiteParser.setEncoding(encoding);
			NodeIterator nodeIterator = MainSiteParser.elements();
			while (nodeIterator.hasMoreNodes()) {
				Node n = nodeIterator.nextNode();
				buf += n.toHtml();
			}
			return buf;
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "";

	}

	public String getHTMLTextByHTTP(String PageUrl, String encoding) {
		try {
			URL url = new URL(PageUrl);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setRequestProperty("Content-type", encoding);
			connection.setRequestProperty("Accept-Charset", encoding);
			connection.connect();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					connection.getInputStream(), encoding));
			String lines = null;
			String buf = "";
			while ((lines = reader.readLine()) != null) {
				buf += lines + "\n";
			}
			return buf;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	public void TestWithParser() {
		this.Test(this.getHTMLTextByParser());
	}

	public void TestWithHTTP() {
		this.Test(this.getHTMLTextByHTTP(this.MainSiteURL, this.encoding));
	}
	
	public void RawTest(){
		this.Test("");
	}
}
