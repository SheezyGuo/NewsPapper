package com.uestc.spider.www;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.TableTag;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.HtmlPage;

/*
 * 针对成都商报的新闻爬虫
 * 获取成都商报一天的所有新闻
 * 新闻包括 新闻题目，发布时间，新闻内容，报道记者以及其PDF格式的新闻文件
 * 可配置！！
 * */
public class CDSB implements Runnable {

	HttpURLConnection httpUrlConnection;
	InputStream inputStream;
	BufferedReader bufferedReader;
	String url; // 要处理的url
	String text = ""; // 存储url的html内容
	String nameSource = "cdsb"; // 新闻来源

	public String title; // 新闻标题
	public String titleContent; // 新闻内容标题
	public String originalTitle; // 未处理原始标题

	public String content; // 新闻内容

	public String time; // 新闻发布时间

	public String newSource; // 新闻来源
	public String originalSource; // 未处理原始新闻来源

	public String categroy; // 新闻类别
	public String originalCategroy; // 新闻原始分类

	private String bqTitle[]; // = {"title"}; //新闻标题网页标签"class","bt_title"
	private String[] bqContent; // = {"id","ozoom"} ; // 新闻内容网页标签"bt_con"
								// id="ozoom
	private String[] bqDate; // = {"class","header-today"} ;
								// //时间标签"class","header-today" "riq"
	private String[] bqNewSource; // ={"class","info"} ; //新闻来源标签 name"author"
	private String[] bqCategroy; // = {"width","57%"}; //新闻分类width="57%"
									// "class","s_left"
	private String bqBuf; // = "- 成都商报|成都商报电子版|成都商报官方网站" ;// "华西都市报" ; //过滤内容，如-
							// 成都商报|成都商报电子版|成都商报官方网站 以及

	// 图片配置
	private String photoUrl; // 替换相对路径
	private String imageUrl; // = "IMG src=\"(.*?)res(.*?)attpic_brief.jpg\"";
								// //"img src=\"(.*?)res(.*?)attpic_brief.jpg\""
	private String imageScr; // = "http:\"?(.*?)(\"|>|\\s+)";
								// //"http:\"?(.*?)(\"|>|\\s+)"
	private String imageBuf; // = "../../../";

	private String ENCODE; // = "gb2312"; //gb2312 utf-8

	public int state = 0;

	public CDSB(String url, String[] bqtitle, String[] bqcontent,
			String[] bqdate, String[] bqnewsource, String[] bqcategroy,
			String bqbuf, String encode, String photourl, String imageurl,
			String imagescr, String imagebuf) {

		try {
			this.url = url;
			this.bqTitle = bqtitle;
			this.bqContent = bqcontent;
			this.bqDate = bqdate;
			this.bqNewSource = bqnewsource;
			this.bqCategroy = bqcategroy;
			this.bqBuf = bqbuf;
			this.ENCODE = encode;
			// this.baseUrl = ;
			this.photoUrl = photourl;
			this.imageUrl = imageurl;
			this.imageScr = imagescr;
			this.imageBuf = imagebuf;

		} catch (Exception e) {

			// e.printStackTrace();
		}

		try {
			httpUrlConnection = (HttpURLConnection) new URL(url)
					.openConnection(); // 创建连接
			state = httpUrlConnection.getResponseCode();
			httpUrlConnection.disconnect();
		} catch (MalformedURLException e) {
			// e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}

		// System.out.println("---------start-----------");
		if (state == 200 || state == 201) {
			try {
				httpUrlConnection = (HttpURLConnection) new URL(url)
						.openConnection();
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				// e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				// e1.printStackTrace();
			} // 创建连接
			Thread thread = new Thread(this);
			thread.start();
			try {
				thread.join();
			} catch (InterruptedException e) {
				// e.printStackTrace();
			}
		}

		// System.out.println("----------end------------");
	}

	public void run() {
		// TODO Auto-generated method stub
		try {
			httpUrlConnection.setRequestMethod("GET");
		} catch (ProtocolException e) {
			// e.printStackTrace();
		}

		try {
			httpUrlConnection.setUseCaches(true); // 使用缓存
			httpUrlConnection.connect(); // 建立连接 链接超时处理
		} catch (IOException e) {
			// e.printStackTrace();
			// continue;
			System.out.println("该链接访问超�?..");
		}

		try {
			inputStream = httpUrlConnection.getInputStream(); // 读取输入流
			bufferedReader = new BufferedReader(new InputStreamReader(
					inputStream, ENCODE));
			String string;
			StringBuffer sb = new StringBuffer();
			while ((string = bufferedReader.readLine()) != null) {
				sb.append(string);
				sb.append("\n");
			}
			text = sb.toString();
		} catch (IOException e) {
			// e.printStackTrace();
		} // finally {
			// try {
			// bufferedReader.close();
			// inputStream.close();
			// httpUrlConnection.disconnect();
			// } catch (IOException e) {
			// e.printStackTrace();
			// System.out.println("链接关闭出现问题...");
			// }

		// }

	}

	private String getNewsID() {
		return this.url.substring(this.url.lastIndexOf("_") + 1,
				this.url.lastIndexOf("."));
	}

	/*
	 * 只需要一个参数就可以判断的标签，比如title
	 */
	String handle(String html, String one) {
		NodeFilter filter = new HasAttributeFilter(one);
		String buf = "";
		try {
			Parser parser = Parser.createParser(html, ENCODE);
			NodeList nodes = parser.extractAllNodesThatMatch(filter);

			if (nodes != null) {
				for (int i = 0; i < nodes.size(); i++) {
					Node textnode1 = (Node) nodes.elementAt(i);
					buf += textnode1.toPlainTextString();
					if (buf.contains("&nbsp;"))
						buf = buf.replaceAll("&nbsp;", " ");
				}
			}
		} catch (Exception e) {

		}
		return buf;
	}

	/*
	 * �?��两个参数才能判断准确的内容：比如 content-title ，ozoom�?
	 */
	String handle(String html, String one, String two) {
		NodeFilter filter = new HasAttributeFilter(one, two);
		String buf = "";
		try {
			Parser parser = Parser.createParser(html, ENCODE);
			NodeList nodes = parser.extractAllNodesThatMatch(filter);

			if (nodes != null) {
				for (int i = 0; i < nodes.size(); i++) {
					Node textnode1 = (Node) nodes.elementAt(i);
					buf += textnode1.toPlainTextString();
					if (buf.contains("&nbsp;"))
						buf = buf.replaceAll("&nbsp;", " ");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return buf;
	}

	/*
	 * 新闻标题
	 */
	String handleOriginalTitle(String html) {
		if (bqTitle[1].equals(""))
			originalTitle = handle(html, bqTitle[0]); // ,bqTitle[1]
		else
			originalTitle = handle(html, bqTitle[0], bqTitle[1]);
		if (url.contains("http://ehsb.hsw.cn/")) // 华商报
		{
			originalTitle = originalTitle.replace("下一版>", "");
		} else if (url.contains("http://epaper.sanqin.com/")) { // 三秦都市报
			originalTitle = originalTitle.substring(0,
					originalTitle.lastIndexOf("-三秦都市报数字报"));
		}
		originalTitle = originalTitle.trim();
		System.out.println("原始标题:" + originalTitle);
		return originalTitle;
	}

	/*
	 * 新闻内容标题
	 */
	String handleTitleContent(String html) {
		if (bqTitle[1].equals(""))
			titleContent = handle(html, bqTitle[0]);
		else
			titleContent = handle(html, bqTitle[0], bqTitle[1]);

		if (url.contains("http://ehsb.hsw.cn/")) // 华商报
		{
			titleContent = titleContent.replace("下一版>", "");
		} else if (url.contains("http://epaper.sanqin.com/")) { // 三秦都市报
			titleContent = titleContent.substring(0,
					titleContent.lastIndexOf("-三秦都市报数字报"));
		}
		titleContent = titleContent.trim();
		System.out.println("titleContent:" + titleContent);
		return titleContent;
	}

	String handleTitle(String html) {
		if (bqTitle[1].equals(""))
			title = handle(html, bqTitle[0]); // ,bqTitle[1]
		else
			title = handle(html, bqTitle[0], bqTitle[1]);

		if (url.contains("newspaper.jfdaily.com/xwcb")) // 新闻晨报
			title = title.substring(0, title.lastIndexOf(" "));
		else if (url.contains("http://ehsb.hsw.cn/")) { // 华商报
			title = title.replace("下一版>", "");
		} else if (url.contains("http://epaper.sanqin.com/")) { // 三秦都市报
			title = title.substring(0, title.lastIndexOf("-三秦都市报数字报"));
		} else if (url.contains("http://kb.dsqq.cn/") // 现代快报
				|| url.contains("http://jlwb.njnews.cn/") // 金陵晚报
				|| url.contains("http://njcb.jschina.com.cn/") // 南京晨报
				|| url.contains("http://szsb.sznews.com/") // 深圳商报
				|| url.contains("http://sztqb.sznews.com/") // 深圳特区报
				|| url.contains("http://epaper.cqcb.com/") // 重庆晨报
				|| url.contains("http://www.cqwb.com.cn/") // 重庆晚报
				|| url.contains("http://epaper.tianjinwe.com/mrxb") // 每日新报
				|| url.contains("http://epaper.tibet3.com/xhdsb") // 西海都市报
				|| url.contains("http://www.xnwbw.com/") // 西宁晚报
				|| url.contains("http://yzwb.sjzdaily.com.cn/") // 燕赵晚报
		) {
			title = handle(html, "id", "mp" + getNewsID());
			if (title.equals("")) {
				title = this.handle(this.text, bqTitle[0]);
				if (title.equals("")) {
					try {
						throw new Exception("Url:" + this.handleUrl()
								+ "\nTitle not found!");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return "";
					}
				}
			}
		}
		if (url.contains("http://jnsb.e23.cn/shtml/jnsb")) { // 济南时报
			NodeFilter filter = new HasAttributeFilter("name", "Keywords");
			try {
				Parser parser = Parser.createParser(html, ENCODE);
				NodeList nodes = parser.extractAllNodesThatMatch(filter);
				title = nodes.elementAt(0).getText();
				Pattern localpattern = Pattern
						.compile("(?<=content=\")(.*?)(?=\")");
				Matcher localmatcher = localpattern.matcher(title);
				if (localmatcher.find()) {
					title = localmatcher.group();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (url.contains("http://www.shangbw.com/epaper")) // 河南商报
		{
			title = title
					.substring(0, title.substring(0, title.lastIndexOf("-"))
							.lastIndexOf("-"));
			title = title.substring(0, title.lastIndexOf("-"));
		} else if (url.contains("http://epaper.hilizi.com/shtml/bdcb") // 半岛晨报
		) {
			title = title
					.substring(0, title.substring(0, title.lastIndexOf("-"))
							.lastIndexOf("-"));
		} else if (url.contains("http://szb.dlxww.com/dlwb")// 大连晚报
				|| url.contains("http://szb.dlxww.com/xsb") // 新商报
		) {
			title = title.substring(0, title.indexOf("_"));
		}
		if (title != null && title != "")
			title = title.replace(bqBuf, "");
		title = title.trim();
		System.out.println("标题:" + title);
		return title;
	}

	String handleUrl() {
		return url;
	}

	/*
	 * 新闻内容
	 */
	String handleContent(String html) {

		if (bqContent[1].equals(""))
			content = handle(html, bqContent[0]);
		else
			content = handle(html, bqContent[0], bqContent[1]);

		if (url.contains("gzdaily.dayoo.com/html")) {
			content = content.substring(content.indexOf("来源: 广州日报") + 8,
					content.length());
		} else if (url.contains("bjwb.bjd.com.cn/html")) { // 北京晚报
			content = html.substring(html.indexOf("<!--enpcontent-->") + 17,
					html.indexOf("<!--/enpcontent-->"));
			content = content.replaceAll("<P>|<p>|</p>|</P>", "");
			content = content.replaceAll("&nbsp;", "\n");
		} else if (url.contains("http://epaper.jinghua.cn/html") // 京华时报
				|| url.contains("http://whwb.cjn.cn/") // 武汉晚报
				|| url.contains("http://whcb.cjn.cn/") // 武汉晨报
				|| url.contains("http://szb.dlxww.com/dlwb") // 大连晚报
				|| url.contains("http://szb.dlxww.com/xsb") // 新商报
				|| url.contains("http://newpaper.dahe.cn/dhb") // 大河报
		) {
			content = html.substring(html.indexOf("<!--enpcontent-->") + 17,
					html.indexOf("<!--/enpcontent-->"));
			content = content.replaceAll("<P>|<p>|</p>|</P>", "");
			content = content.replaceAll("&nbsp;", " ");
			content = content.trim();
		} else if (url.contains("kb.dsqq.cn/html") // 现代快报
				|| url.contains("http://jlwb.njnews.cn/") // 金陵晚报
				|| url.contains("http://njcb.jschina.com.cn/") // 南京晨报
				|| url.contains("http://szsb.sznews.com/") // 深圳商报
				|| url.contains("http://sztqb.sznews.com/") // 深圳特区报
				|| url.contains("http://epaper.cqcb.com/") // 重庆晨报
				|| url.contains("http://www.cqwb.com.cn/") // 重庆晚报
				|| url.contains("http://epaper.xiancn.com/") // 西安晚报
				|| url.contains("http://epaper.jwb.com.cn/") // 今晚报
				|| url.contains("http://epaper.tianjinwe.com/mrxb") // 每日新报
				|| url.contains("http://epaper.tibet3.com/xhdsb") // 西海都市报
				|| url.contains("http://www.xnwbw.com/") // 西宁晚报
				|| url.contains("http://yzwb.sjzdaily.com.cn/") // 燕赵晚报
		) {
			try {
				content = html.substring(
						html.indexOf("<founder-content>") + 17,
						html.indexOf("</founder-content>"));
				content = content.replaceAll("<P>|<p>|</p>|</P>", "");
				content = content.replaceAll("&nbsp;", "\n");
				if (url.contains("http://yzwb.sjzdaily.com.cn/")) // 燕赵晚报
				{
					content = content.replaceAll("\n+", "\n");
				}
				content = content.trim();
			} catch (Exception e) {
				e.printStackTrace();
				return "";
			}
		} else if (url.contains("http://epaper.nandu.com/")) // 南方都市报
		{
			// content =
			// content.replaceAll("</p>\\s*<p>|</P>\\s*<P>|</p>\\s*<P>|</P>\\s*<p>","\n");
			content = content.replaceAll("<P>|<p>|</p>|</P>", "");
			content = content.replaceAll("\n+", "\n");
			content = content.trim();
		} else if (url.contains("http://ctdsb.cnhubei.com/")) // 楚天都市报
		{
			try {
				Pattern pattern = Pattern
						.compile("(?<=<font style=\"font-size: 14px;line-height: 26px\">)(.*?)(?=</font></div></td>)");
				Matcher matcher = pattern.matcher(html);
				if (!matcher.find()) {
					try {
						throw new Exception("Url " + this.handleUrl()
								+ ":Content not found");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						System.out.println(e.toString());
					}
				}
				content = matcher.group();
				content = content.replaceAll("<br>|<BR>|<P>|<p>|</p>|</P>", "");
				content = content.replaceAll("(&nbsp;){2,}", "\n");
				content = content.replaceAll("&nbsp;", " ");
				content = content.trim();
			} catch (Exception e) {
				e.printStackTrace();
				return "";
			}
		} else if (url.contains("http://epaper.sanqin.com/")) { // 三秦都市报
			content = content.substring(0, content.indexOf("           评论"));
		} else if (url.contains("http://epaper.qlwb.com.cn/qlwb")// 齐鲁晚报
				|| url.contains("http://shrb.qlwb.com.cn/shrb") // 生活日报
		) {
			content = content.replaceAll("\\s{2,}", "");
		} else if (url.contains("http://zzwb.zynews.com/")) // 郑州晚报
		{
			try {
				content = content.substring(0,
						content.lastIndexOf("if(picResCount>0)"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		;
		content = content.trim();
		System.out.println("内容:" + content);
		return content;
	}

	/*
	 * 新闻图片 图片名为：时间+后缀（比如：20140910.jpg） 命名处理待改进
	 */
	public String handleImage(String html) {
		// 图片配置：http://www.chinamil.com.cn/jfjbmap/
		// 正则表达式："IMG src=\"(.*?)res(.*?)attpic_brief.jpg\""
		// 路径表达式："http:\"?(.*?)(\"|>|\\s+)" 辅助:"?../../../"
		// String url = "http://www.chinamil.com.cn/jfjbmap/";
		// String imageurl = "IMG src=\"(.*?)res(.*?)attpic_brief.jpg\"";
		// //"img src=\"(.*?)res(.*?)attpic_brief.jpg\""
		// String imagescr = "http:\"?(.*?)(\"|>|\\s+)";
		// //"http:\"?(.*?)(\"|>|\\s+)"
		// String imageBuf = "../../../";
		StringBuffer buf = new StringBuffer("");
		StringBuffer load = new StringBuffer(
				"C:\\Users\\Administrator\\git\\Spider\\Spider\\image\\");
		StringBuffer symbol = new StringBuffer(";");
		GetImage image = new GetImage(photoUrl, imageUrl, imageScr, imageBuf); // 图片命名正则表达
		image.fileName = handleTime(html).replaceAll("[^0-9]", "") + nameSource;
		Vector<String> dateSourceNumNum = image.getImage(html);
		for (String s : dateSourceNumNum) {
			buf = buf.append(load).append(new StringBuffer(s)).append(symbol);
		}
		if (buf.toString() == "" || buf.toString() == null)
			buf = new StringBuffer("No Images");
		return buf.toString();

	}

	/*
	 * 新闻pdf
	 */
	void handlePDF(String url) {

		new GetPdf(url);
	}

	/*
	 * 新闻发布时间
	 */
	public String handleTime(String html) {

		if (bqDate[1].equals(""))
			time = handle(html, bqDate[0]);
		else
			time = handle(html, bqDate[0], bqDate[1]);
		// time = time.substring(0,12); //只获取时间
		time = time.replaceAll("[^0-9]", ""); // 新闻晨报
		if (url.contains("newspaper.jfdaily.com/xwcb"))
			time = time.substring(0, 8);
		else if (url.contains("http://ehsb.hsw.cn/")) { // 华商报
			time = this.url.substring(this.url.lastIndexOf("hsb/") + 4,
					this.url.lastIndexOf("/"));
		} else if (url.contains("http://epaper.sanqin.com/")) { // 三秦都市报
			time = this.url.substring(this.url.lastIndexOf("sqdsb/") + 6,
					this.url.lastIndexOf("/"));
		} else if (url.contains("http://jnsb.e23.cn/shtml/jnsb")) { // 济南时报
			time = this.url.substring(this.url.lastIndexOf("jnsb/") + 5,
					this.url.lastIndexOf("/"));
		} else if (url.contains("http://epaper.qlwb.com.cn/qlwb")) { // 齐鲁晚报
			time = url.substring(this.url.indexOf("qlwb/content/") + 13,
					this.url.lastIndexOf("/"));
		} else if (url.contains("http://shrb.qlwb.com.cn/shrb")) { // 生活日报
			time = url.substring(this.url.indexOf("shrb/content/") + 13,
					this.url.lastIndexOf("/"));
		} else if (url.contains("http://epaper.hilizi.com/shtml/bdcb")) // 半岛晨报
		{
			time = url.substring(this.url.indexOf("shtml/bdcb/") + 11,
					this.url.lastIndexOf("/"));
		} else if (url.contains("http://gzdaily.dayoo.com/html")) {
			time = time.substring(0, 8);
		} else if (url.contains("http://kb.dsqq.cn/html") // 现代快报
				|| url.contains("http://jlwb.njnews.cn/html") // 金陵晚报
				|| url.contains("http://njcb.jschina.com.cn/") // 南京晨报
				|| url.contains("http://szsb.sznews.com/") // 深圳商报
				|| url.contains("http://sztqb.sznews.com/") // 深圳特区报
				|| url.contains("http://epaper.nandu.com/") // 南方都市报
				|| url.contains("http://whwb.cjn.cn/") // 武汉晚报
				|| url.contains("http://whcb.cjn.cn/") // 武汉晨报
				|| url.contains("http://epaper.cqcb.com/") // 重庆晨报
				|| url.contains("http://www.cqwb.com.cn/") // 重庆晚报
				|| url.contains("http://epaper.xiancn.com/") // 西安晚报
				|| url.contains("http://epaper.jwb.com.cn/") // 今晚报
				|| url.contains("http://epaper.tianjinwe.com/mrxb") // 每日新报
				|| url.contains("http://szb.dlxww.com/dlwb") // 大连晚报
				|| url.contains("http://szb.dlxww.com/xsb") // 新商报
				|| url.contains("http://epaper.tibet3.com/xhdsb") // 西海都市报
				|| url.contains("http://www.xnwbw.com/") // 西宁晚报
				|| url.contains("http://newpaper.dahe.cn/dhb") // 大河报
				|| url.contains("http://zzwb.zynews.com/") // 郑州晚报
				|| url.contains("http://yzwb.sjzdaily.com.cn/") // 燕赵晚报
		) {
			Pattern pattern = Pattern.compile("\\d{4}-\\d{2}/\\d{2}");
			Matcher matcher = pattern.matcher(this.handleUrl());
			if (!matcher.find()) {
				try {
					throw new Exception("Time not found in page:"
							+ this.handleUrl());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			time = matcher.group().replaceAll("\\D", "");
		} else if (url.contains("http://www.shangbw.com/epaper")) // 河南商报
		{
			Pattern pattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
			Matcher matcher = pattern.matcher(this.handleUrl());
			if (!matcher.find()) {
				try {
					throw new Exception("Time not found in page:"
							+ this.handleUrl());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			time = matcher.group().replaceAll("\\D", "");
		} else if (url.contains("http://ctdsb.cnhubei.com/")) // 楚天都市报
		{
			Pattern pattern = Pattern.compile("(?<=ctdsb/)\\d+(?=/)");
			Matcher matcher = pattern.matcher(this.handleUrl());
			if (!matcher.find()) {
				try {
					throw new Exception("Time not found in page:"
							+ this.handleUrl());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			time = matcher.group();
		}
		else if(url.contains("http://epaper.yzdsb.com.cn/")) { //燕赵都市报 
			Pattern pattern = Pattern.compile("\\d{6}/\\d{2}");
			Matcher matcher = pattern.matcher(this.handleUrl());
			if (!matcher.find()) {
				try {
					throw new Exception("Time not found in page:"
							+ this.handleUrl());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			time = matcher.group().replaceAll("\\D", "");
		}
		System.out.println("时间:" + time);
		return time;
	}

	/*
	 * 获取原始报社名称 有待改进，目前无法改进啊。。。貌似有点麻烦
	 */
	String handleNewSource(String html) {

		// newSource = handle(html,bqNewSource[0],bqNewSource[1]);
		// if(newSource.length() >= 4)
		// newSource = newSource.substring(0, 4);
		// System.out.println(newSource);
		// newSource = html ;
		System.out.println("新闻来源:" + bqNewSource[0]);
		return bqNewSource[0];
	}

	/*
	 * 新闻来源
	 */
	public String handleOriginalSource(String html) {
		// TODO Auto-generated method stub
		// originalSource = handle(html,bqNewSource[0],bqNewSource[1]);
		// originalSource = html;
		// System.out.println(cgSource);
		System.out.println("原始新闻来源:" + bqNewSource[0] + " , " + bqNewSource[1]);
		return bqNewSource[0] + " , " + bqNewSource[1];
	}

	/*
	 * 版面属性 "："之后 即为所需
	 */
	String handleCategroy(String html) {
		if (bqCategroy[1].equals(""))
			categroy = handle(html, bqCategroy[0]);
		else
			categroy = handle(html, bqCategroy[0], bqCategroy[1]);
		if (url.contains("http://e.chengdu.cn/")) { // 商报
			categroy = categroy.substring(categroy.lastIndexOf(":") + 1,
					categroy.lastIndexOf("»") - 1);
		} else if (url.contains("/www.chinamil.com.cn")) { // 解放军报
			categroy = categroy.substring(categroy.lastIndexOf(":") + 1,
					categroy.length());// categroy.lastIndexOf(" "));
		} else if (url.contains("newspaper.jfdaily.com/xwcb")) { // 新闻晨报
			categroy = categroy.replaceAll("\\s*", "");
			categroy = categroy.substring(categroy.indexOf(":") + 1,
					categroy.indexOf(":"));
		} else if (url.contains("gzdaily.dayoo.com/html")) { // 广州日报
			categroy = categroy.replaceAll("本版新闻", "");
		} else if (url.contains("www.ycwb.com/ePaper/ycwb/html")) { // 羊城晚报
			if (categroy.contains(":"))
				categroy = categroy.substring(categroy.indexOf(":") + 1,
						categroy.indexOf("按日期检索"));
		} else if (url.contains("http://epaper.nandu.com/")) { // 南方都市报
			categroy = categroy.replaceAll("\\s", "");
			categroy = categroy.substring(categroy.lastIndexOf("："), // 中文冒号!!
					categroy.length());
		} else if (url.contains("epaper.jinghua.cn/html")) {
			categroy = categroy.replaceAll("&gt;", "");
			categroy = categroy.substring(18, categroy.length());
		} else if (url.contains("http://www.cqwb.com.cn/")) { // 重庆晚报
			categroy = categroy.substring(categroy.indexOf("："),
					categroy.indexOf("\n", categroy.indexOf("：")));
		} else if (url.contains("http://epaper.sanqin.com/")) { // 三秦都市报
			categroy = categroy.substring(categroy.indexOf(" ") + 1,
					categroy.length());
		} else if (url.contains("http://jnsb.e23.cn/shtml/jnsb")) { // 济南日报
			int head = categroy.indexOf(":");
			categroy = categroy
					.substring(head + 1, categroy.indexOf(" ", head));
		} else if (url.contains("http://epaper.qlwb.com.cn/qlwb")// 齐鲁晚报
				|| url.contains("http://shrb.qlwb.com.cn/shrb") // 生活日报
		) {
			Pattern localpattern = Pattern
					.compile("(?<=Articel)[A-Z]\\d{2}(?=\\d{3}[A-Z]{2})");
			Matcher localmatcher = localpattern.matcher(url);
			if (localmatcher.find()) {
				String tag = localmatcher.group();
				categroy = html.substring(html.indexOf("第" + tag + "版：")
						+ ("第" + tag + "版：").length(),
						html.indexOf(" ", html.indexOf("第" + tag + "版：")));
				categroy = categroy.replaceAll("<(.*?)>", "");
			}
		} else if (url.contains("http://epaper.hilizi.com/shtml/bdcb") // 半岛晨报
		) {
			Pattern localpattern = Pattern
					.compile("(?<=<a href=\"[a-zA-z]{2}\\d{2}\\.shtml\">)(.*?)(?=</a>)");
			Matcher localmatcher = localpattern.matcher(html);
			if (localmatcher.find()) {
				categroy = localmatcher.group();
				categroy = categroy.substring(categroy.indexOf(":") + 1,
						categroy.length());
			}
		}
		;

		if (categroy.contains("："))
			categroy = categroy.substring(categroy.indexOf("：") + 1,
					categroy.length());
		categroy = categroy.replaceAll("\\s*", "");
		System.out.println("类别:" + categroy);
		return categroy; // .substring(categroy.lastIndexOf("：")+1,categroy.length());

	}

	/*
	 * 新闻原始类别
	 */
	String handleOriginalCategroy(String html) {

		if (bqCategroy[1].equals(""))
			originalCategroy = handle(html, bqCategroy[0]);
		else
			originalCategroy = handle(html, bqCategroy[0], bqCategroy[1]);

		if (url.contains("www.ycwb.com/ePaper/ycwb/html")) { // 羊城晚报
			originalCategroy = originalCategroy.replaceAll("\\s*", "");
			originalCategroy = originalCategroy.substring(6,
					originalCategroy.indexOf("按日期检索"));
		} else if (url.contains("epaper.jinghua.cn/html")) { // 京华时报
			categroy = categroy.replaceAll("&gt;", "");
		} else if (url.contains("http://epaper.nandu.com/")) // 南方都市报
		{
			originalCategroy = originalCategroy.substring(
					originalCategroy.lastIndexOf("版次："), // 中文冒号!
					originalCategroy.length());
		} else if (url.contains("http://ctdsb.cnhubei.com/")) // 楚天都市报
		{
			originalCategroy = this.handle(html, "class", "STYLE1");
		} else if (url.contains("http://www.cqwb.com.cn/")) {
			originalCategroy = originalCategroy.substring(
					0,
					originalCategroy.indexOf("\n",
							originalCategroy.indexOf("：")));
		} else if (url.contains("http://jnsb.e23.cn/shtml/jnsb")) { // 济南日报
			int head = originalCategroy.indexOf(":");
			String front = originalCategroy.substring(0, head);
			originalCategroy = originalCategroy
					.substring(front.lastIndexOf(" "),
							originalCategroy.indexOf(" ", head));
		} else if (url.contains("http://epaper.qlwb.com.cn/qlwb") // 齐鲁晚报
				|| url.contains("http://shrb.qlwb.com.cn/shrb") // 生活日报
		) {
			Pattern localpattern = Pattern
					.compile("(?<=Articel)[A-Z]\\d{2}(?=\\d{3}[A-Z]{2})");
			Matcher localmatcher = localpattern.matcher(url);
			if (localmatcher.find()) {
				String tag = localmatcher.group();
				originalCategroy = html.substring(
						html.indexOf("第" + tag + "版："),
						html.indexOf(" ", html.indexOf("第" + tag + "版：")));
				originalCategroy = originalCategroy.replaceAll("<(.*?)>", "");
			}
		} else if (url.contains("http://epaper.hilizi.com/shtml/bdcb") // 半岛晨报
		) {
			Pattern localpattern = Pattern
					.compile("(?<=<a href=\"[a-zA-z]{2}\\d{2}\\.shtml\">)(.*?)(?=</a>)");
			Matcher localmatcher = localpattern.matcher(html);
			if (localmatcher.find()) {
				originalCategroy = localmatcher.group();
			}
		}
		;

		originalCategroy = originalCategroy.replaceAll("\\s*", "");
		System.out.println("原始类别:" + originalCategroy);

		return originalCategroy;
	}

	/*
	 * 新闻相关内容的存储 标题 时间 内容 报社名称 newsource 为创建的数据库名称 ；newtable 为创建的数据库表名
	 */

	public void memory(String url, String[] bqtitle, String[] bqcontent,
			String[] bqdate, String[] bqnewsource, String[] bqcategroy,
			String bqbuf, String encode, String DBName, String DBTable,
			String photourl, String imageurl, String imagescr, String imagebuf) {
		int state;
		try {
			HttpURLConnection httpUrlConnection = (HttpURLConnection) new URL(
					url).openConnection(); // 创建连接
			state = httpUrlConnection.getResponseCode();
			httpUrlConnection.disconnect();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			System.out.println("网络慢，已经无法正常链接，无法获取新闻");
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("网络超级慢，已经无法正常链接，无法获取新闻");
			return;
		}
		if (state != 200 && state != 201) {
			System.out.println(url + "无法打开该页面" + "\n状态码:" + state);
			return;
		}
		CRUT crut = new CRUT(DBName, DBTable);
		CDSB cdsb = new CDSB(url, bqtitle, bqcontent, bqdate, bqnewsource,
				bqcategroy, bqbuf, encode, photourl, imageurl, imagescr,
				imagebuf);
		try {
			crut.add(cdsb.handleTitle(cdsb.text),
					cdsb.handleOriginalTitle(cdsb.text),
					cdsb.handleTitleContent(cdsb.text),
					cdsb.handleTime(cdsb.text), cdsb.handleContent(cdsb.text),
					cdsb.handleNewSource(cdsb.text),
					cdsb.handleOriginalSource(cdsb.text),
					cdsb.handleCategroy(cdsb.text),
					cdsb.handleOriginalCategroy(cdsb.text), cdsb.handleUrl(),
					cdsb.handleImage(cdsb.text));
		} catch (Exception e) {
			System.out.println("***" + url + "*** curt.add failed!");
			e.printStackTrace();
		}
		System.out.println("\n\n");
		crut = null;
		cdsb = null;
	}

	public static void main(String[] args) throws Exception {
		/*
		 * String url,String[] bqtitle,String[] bqcontent, String[]
		 * bqdate,String[] bqnewsource ,String[] bqcategroy ,String bqbuf
		 * ,String newsource ,String newtable
		 */
		String myurl1 = "http://kb.dsqq.cn/html/2014-11/13/content_370554.htm";
		String myurl2 = "http://kb.dsqq.cn/html/2014-11/13/content_370557.htm";
		String myurl3 = "http://kb.dsqq.cn/html/2014-11/13/content_370461.htm";
		String myurl4 = "http://jlwb.njnews.cn/html/2014-11/14/content_1716929.htm";
		String myurl5 = "http://jlwb.njnews.cn/html/2014-11/14/content_1716945.htm";
		String myurl6 = "http://jlwb.njnews.cn/html/2014-11/08/content_1714233.htm";
		String myurl7 = "http://njcb.jschina.com.cn/mp3/html/2014-11/15/content_1152720.htm";
		String szsb1 = "http://szsb.sznews.com/html/2014-11/15/content_3062848.htm";
		String szsb2 = "http://szsb.sznews.com/html/2014-11/08/content_3055962.htm";
		String sztqb1 = "http://sztqb.sznews.com/html/2014-11/16/content_3063321.htm";
		String nfdsb1 = "http://epaper.nandu.com/epaper/A/html/2014-11/17/content_3345218.htm";
		String nfdsb2 = "http://epaper.nandu.com/epaper/A/html/2014-11/08/content_3340861.htm?div=-1";
		String whwb1 = "http://whwb.cjn.cn/html/2014-11/17/content_5389113.htm";
		String ctdsb1 = "http://ctdsb.cnhubei.com/HTML/ctdsb/20141117/ctdsb2476013.html";
		String ctdsb2 = "http://ctdsb.cnhubei.com/HTML/ctdsb/20141101/index.html";
		String whcb1 = "http://whcb.cjn.cn/html/2014-11/19/content_5389882.htm";
		String cqcb1 = "http://epaper.cqcb.com/html/2014-11/19/content_143365.htm";
		String cqwb1 = "http://www.cqwb.com.cn/cqwb/html/2012-03/28/content_306808.htm";
		String hsb1 = "http://ehsb.hsw.cn/shtml/hsb/20141120/192856.shtml";
		String xawb1 = "http://epaper.xiancn.com/xawb/html/2014-11/20/content_335219.htm";
		String sqdsb1 = "http://epaper.sanqin.com/shtml/sqdsb/20141120/434796.shtml";
		String jwb1 = "http://epaper.jwb.com.cn/jwb/html/2014-11/24/content_1184177.htm";
		String tjrb1 = "http://epaper.tianjinwe.com/mrxb/mrxb/2014-11/25/content_7198433.htm";
		String jnsb1 = "http://jnsb.e23.cn/shtml/jnsb/20141125/1371311.shtml";
		String qlwb1 = "http://epaper.qlwb.com.cn/qlwb/content/20141126/ArticelU07003FM.htm";
		String shrb1 = "http://shrb.qlwb.com.cn/shrb/content/20141126/ArticelA02002JQ.htm";
		String bdcb1 = "http://epaper.hilizi.com/shtml/bdcb/20141127/139050.shtml";
		String dlwb1 = "http://szb.dlxww.com/dlwb/html/2014-11/27/content_1090875.htm?div=-1";
		String xsb1 = "http://szb.dlxww.com/xsb/html/2014-11/27/content_1090832.htm?div=0";
		String xhdsb1 = "http://epaper.tibet3.com/xhdsb/html/2014-09/01/content_166967.htm";
		String xnwb1 = "http://www.xnwbw.com/html/2014-12/01/content_25391.htm";
		String dhb1 = "http://newpaper.dahe.cn/dhb/html/2014-12/01/content_1187313.htm?div=0";
		String hnsb1 = "http://www.shangbw.com/epaper/2014-12-01/content_1187061.htm";
		String zzwb1 = "http://zzwb.zynews.com/html/2014-12/02/content_618323.htm";
		String yzwb1 = "http://yzwb.sjzdaily.com.cn/html/2014-12/04/content_1127533.htm";
		String yzdsb1="http://epaper.yzdsb.com.cn/201212/05/187465.html";

		String[] bqtitle = { "title", "" };
		String[] bqcontent = { "class", "cont" };
		String[] bqdate = { "any", "any" };
		String[] bqnewsource = { "燕赵都市报", "....." };
		String[] bqcategroy = { "none", "none" };
		String bqbuf = "-燕赵都市报数字报";
		String encode = "utf-8";
		String DBName = "cqcb";
		String DBTable = "cg";
		String photourl = "";
		String imageurl = "(?<=<img src=\')http://epaper.yzdsb.com.cn/\\d{6}/\\d{2}/(.*?)\\.jpg(?=\' />)";
		String imagescr = "http://epaper.yzdsb.com.cn/\\d{6}/\\d{2}/(.*?)\\.jpg";
		String imageBuf = "";
		String themeReg = "http://epaper.yzdsb.com.cn/\\d{6}/\\d{2}/e\\d+\\.html";
		String contentReg = "http://epaper.yzdsb.com.cn/\\d{6}/\\d{2}/\\d+.html";

		CDSB T = new CDSB(yzdsb1, bqtitle, bqcontent, bqdate, bqnewsource,
				bqcategroy, bqbuf, encode, photourl, imageurl, imagescr,
				imageBuf);

		// System.out.println(T.text);
		boolean flag = true;
		if (flag) {
			T.handleTitle(T.text);
			T.handleContent(T.text);
			T.handleTime(T.text);
			T.handleCategroy(T.text);
			T.handleOriginalCategroy(T.text);
			T.handleNewSource(T.text);
			T.handleImage(T.text);
		}
		// System.out.println(String.format("%s%04d%s%02d%s%02d%s", "a", 1, "b",
		// 2, "c", 3, "d"));
		if (!flag) {
			String PageUrl = "http://epaper.yzdsb.com.cn/201212/05/";
			MainSiteTest mytest = new MainSiteTest(PageUrl, encode, themeReg,
					contentReg);
			// System.out.println("HTML Content:\n"
			// + mytest.getHTMLTextByHTTP(PageUrl, encode));
			mytest.RawTest();
		}
	}
}
