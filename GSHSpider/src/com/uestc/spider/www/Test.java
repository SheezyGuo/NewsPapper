package com.uestc.spider.www;

import java.lang.Thread.UncaughtExceptionHandler;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.Bytes;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.QueryOperators;
import com.mongodb.util.JSON;
import com.uestc.spider.www.TaskThreadPool;

import com.uestc.spider.www.ThreadUncaughtExceptionHandler;

/*
 * 1.先把华西都市报趴下来
 * 2.利用多线程爬取
 * 3.利用线程获取当天的新闻
 * 4.找到内存使用增加的原因！！
 * 
 * 2014.10.27
 * 1.实现多线程爬取：一个控制线程，多个爬取线程（每个线程负责一个报社新闻）
 * 
 * 2.尽可能的模板化
 * */
public class Test {

	public static void main(String args[]) throws UnknownHostException {

		TaskThreadPool xdkb = new TaskThreadPool("xdkb", "cg", new String[] {
				"any", "any" }, new String[] { "id", "ozoom" }, new String[] {
				"class", "default" }, new String[] { "现代快报", "" },
				new String[] { "width", "160" }, "现代快报", "utf-8",
				"http://kb.dsqq.cn/",
				"IMG src=\"(.*?)res(.*?)attpic_brief.jpg(?=\")",
				"http://(.*?)_attpic_brief.jpg", "../../../",
				"http://kb.dsqq.cn/html/\\d{4}-\\d{2}/\\d{2}/node_\\d+.htm",
				"http://kb.dsqq.cn/html/\\d{4}-\\d{2}/\\d{2}/content_\\d+.htm",
				"http://kb.dsqq.cn/html/", "-", "/", "/node_41.htm");
		TaskThreadPool jlwb = new TaskThreadPool(
				"jlwb",
				"cg",
				new String[] { "any", "any" },
				new String[] { "any", "any" },
				new String[] { "class", "default" },
				new String[] { "金陵晚报", "" },
				new String[] { "width", "145" },
				"",
				"utf-8",
				"http://jlwb.njnews.cn/",
				"IMG src=\"(.*?)res(.*?)attpic_brief.jpg(?=\")",
				"http://(.*?)_attpic_brief.jpg",
				"../../../",
				"http://jlwb.njnews.cn/html/\\d{4}-\\d{2}/\\d{2}/node_\\d+.htm",
				"http://jlwb.njnews.cn/html/\\d{4}-\\d{2}/\\d{2}/content_\\d+.htm",
				"http://jlwb.njnews.cn/html/", "-", "/", "/node_322.htm");
		TaskThreadPool njcb = new TaskThreadPool(
				"njcb",
				"cg",
				new String[] { "any", "any" },
				new String[] { "any", "any" },
				new String[] { "any", "any" },
				new String[] { "南京晨报", "" },
				new String[] { "width", "145" },
				"",
				"utf-8",
				"http://njcb.jschina.com.cn/mp3/",
				"IMG src=\"../../../res/\\d/\\d{8}/\\d+.jpg\"",
				"http://njcb.jschina.com.cn/mp3/res/\\d/\\d{8}/\\d+.jpg",
				"../../../",
				"http://njcb.jschina.com.cn/mp3/html/\\d{4}-\\d{2}/\\d{2}/node_\\d+.htm",
				"http://njcb.jschina.com.cn/mp3/html/\\d{4}-\\d{2}/\\d{2}/content_\\d+.htm",
				"http://njcb.jschina.com.cn/mp3/html/", "-", "/",
				"/node_162.htm");
		TaskThreadPool szsb = new TaskThreadPool(
				"szsb",
				"cg",
				new String[] { "title", "" },
				new String[] { "any", "any" },
				new String[] { "any", "any" },
				new String[] { "深圳商报", "" },
				new String[] { "width", "140" },
				"---深圳商报多媒体数字报刊平台",
				"utf-8",
				"http://szsb.sznews.com/",
				"IMG src=\"(.*?)res(.*?)attpic_brief.jpg(?=\")",
				"http://(.*?)_attpic_brief.jpg",
				"../../../",
				"http://szsb.sznews.com/html/\\d{4}-\\d{2}/\\d{2}/node_\\d+.htm",
				"http://szsb.sznews.com/html/\\d{4}-\\d{2}/\\d{2}/content_\\d+.htm",
				"http://szsb.sznews.com/html/", "-", "/", "/node_1388.htm");
		TaskThreadPool sztqb = new TaskThreadPool(
				"sztqb",
				"cg",
				new String[] { "title", "" },
				new String[] { "any", "any" },
				new String[] { "any", "any" },
				new String[] { "深圳特区报", "....." },
				new String[] { "width", "163" },
				"---深圳特区报",
				"utf-8",
				"http://sztqb.sznews.com/",
				"IMG src=\"(.*?)/res/(.*?)attpic_brief.jpg(?=\")",
				"http://(.*?)_attpic_brief.jpg",
				"../../../",
				"http://sztqb.sznews.com/html/\\d{4}-\\d{2}/\\d{2}/node_\\d+.htm",
				"http://sztqb.sznews.com/html/\\d{4}-\\d{2}/\\d{2}/content_\\d+.htm",
				"http://sztqb.sznews.com/html/", "-", "/", "/node_642.htm");
		TaskThreadPool nfdsb = new TaskThreadPool(
				"nfdsb",
				"cg",
				new String[] { "title", "" },
				new String[] { "id", "fontzoom" },
				new String[] { "any", "any" },
				new String[] { "南方都市报", "....." },
				new String[] { "class", "fl" },
				"_南方都市报数字报TT",
				"utf-8",
				"http://epaper.nandu.com/epaper/A/",
				"IMG src=\"(.*?)/res/(.*?)attpic_brief.jpg(?=\")",
				"http://(.*?)_attpic_brief.jpg",
				"../../../",
				"http://epaper.nandu.com/epaper/\\w/html/\\d{4}-\\d{2}/\\d{2}/node_\\d+.htm",
				"http://epaper.nandu.com/epaper/A/html/\\d{4}-\\d{2}/\\d{2}/content_\\d+.htm",
				"http://epaper.nandu.com/epaper/A/html/", "-", "/",
				"/node_2731.htm");
		TaskThreadPool whwb = new TaskThreadPool(
				"whwb",
				"cg",
				new String[] { "title", "" },
				new String[] { "id", "ozoom" },
				new String[] { "any", "any" },
				new String[] { "武汉晚报", "....." },
				new String[] { "width", "145" },
				" 长江日报报业集团_长江网_长江日报_武汉晚报_武汉晨报_电子报_数字报",
				"utf-8",
				"http://whwb.cjn.cn/",
				"img src=\"(.*?)/images/(.*?)attpic_brief.jpg(?=\")",
				"http://(.*?)_attpic_brief.jpg",
				"../../../",
				"http://whwb.cjn.cn/html/\\d{4}-\\d{2}/\\d{2}/node_\\d+.htm",
				"http://whwb.cjn.cn/html/\\d{4}-\\d{2}/\\d{2}/content_\\d+.htm",
				"http://whwb.cjn.cn/html/", "-", "/", "/node_22.htm");
		TaskThreadPool ctdsb = new TaskThreadPool(
				"ctdsb",
				"cg",
				new String[] { "title", "" },
				new String[] { "any", "any" },
				new String[] { "any", "any" },
				new String[] { "楚天都市报", "....." },
				new String[] { "nowrap", "" },
				"_楚天都市报_多媒体报",
				"gb2312",
				"",
				"img src='http://ctdsb.cnhubei.com/ctdsb/\\d{8}/(.*?).jpg'",
				"http://ctdsb.cnhubei.com/ctdsb/\\d{8}/(.*?).jpg",
				"",
				"abcdefghijklmnopqrstuvwxyz",
				"(?<=window\\.open\\(')(\\.\\./){3}html/ctdsb/\\d{8}/ctdsb\\d{3,}\\.html(?=')",
				"http://ctdsb.cnhubei.com/HTML/ctdsb/", "", "", "/index.html");
		TaskThreadPool whcb = new TaskThreadPool(
				"whcb",
				"cg",
				new String[] { "title", "" },
				new String[] { "any", "any" },
				new String[] { "any", "any" },
				new String[] { "武汉晨报", "....." },
				new String[] { "width", "145" },
				" 长江日报报业集团_长江网_长江日报_武汉晚报_武汉晨报_电子报_数字报",
				"utf-8",
				"http://whcb.cjn.cn/",
				"img src=\"(\\.\\./){3}images/\\d{4}-\\d{2}/\\d{2}/\\d+/res\\d+_attpic_brief.jpg\"",
				"http://(.*?)/images/(.*?)attpic_brief.jpg(?=\")",
				"../../../",
				"http://whcb.cjn.cn/html/\\d{4}-\\d{2}/\\d{2}/node_\\d+.htm",
				"http://whcb.cjn.cn/html/\\d{4}-\\d{2}/\\d{2}/content_\\d+.htm",
				"http://whcb.cjn.cn/html/", "-", "/", "/node_43.htm");
		TaskThreadPool cqcb = new TaskThreadPool(
				"cqcb",
				"cg",
				new String[] { "any", "any" },
				new String[] { "any", "any" },
				new String[] { "any", "any" },
				new String[] { "重庆晨报", "....." },
				new String[] { "class", "bbt" },
				"",
				"utf-8",
				"http://epaper.cqcb.com/",
				"IMG src=\"(\\.\\./){3}res/(.*?)res(.*?)_attpic_brief.jpg\"",
				"http://(.*?)/res/(.*?)attpic_brief.jpg(?=\")",
				"../../../",
				"http://epaper.cqcb.com/html/\\d{4}-\\d{2}/\\d{2}/node_\\d+.htm",
				"http://epaper.cqcb.com/html/\\d{4}-\\d{2}/\\d{2}/content_\\d+.htm",
				"http://epaper.cqcb.com/html/", "-", "/", "/node_2.htm");
		TaskThreadPool cqwb = new TaskThreadPool(
				"cqwb",
				"cg",
				new String[] { "any", "any" },
				new String[] { "any", "any" },
				new String[] { "any", "any" },
				new String[] { "重庆晚报", "....." },
				new String[] { "align", "left" },
				"",
				"utf-8",
				"http://www.cqwb.com.cn/cqwb/",
				"img src=(\\.\\./){3}res/(.*?)\\.jpg",
				"http://(.*?)/res/(.*?)\\.jpg",
				"../../../",
				"http://www.cqwb.com.cn/cqwb/html/\\d{4}-\\d{2}/\\d{2}/node_\\d+.htm",
				"http://www.cqwb.com.cn/cqwb/html/\\d{4}-\\d{2}/\\d{2}/content_\\d+.htm",
				"http://www.cqwb.com.cn/cqwb/html/", "-", "/", "/node_2.htm");
		TaskThreadPool hsb = new TaskThreadPool(
				"hsb",
				"cg",
				new String[] { "title", "" },
				new String[] { "id", "content_div" },
				new String[] { "any", "any" },
				new String[] { "华商报", "....." },
				new String[] { "class", "pgUp" },
				"",
				"utf-8",
				"http://ehsb.hsw.cn/paperdata",
				"(?<=img id=\"img\\[\\d\\]\" src=\")/paperdata/hsb/\\d{8}/(.*?).jpg(?=\")",
				"http://ehsb.hsw.cn/paperdata/hsb/\\d{8}/(.*?)\\.jpg",
				"/paperdata",
				"http://ehsb.hsw.cn/shtml/hsb/\\d{8}/[a-zA-Z]+\\d+\\.shtml",
				"http://ehsb.hsw.cn/shtml/hsb/\\d{8}/\\d+\\.shtml",
				"http://ehsb.hsw.cn/shtml/hsb/", "", "", "/index.shtml");
		TaskThreadPool xawb = new TaskThreadPool(
				"xawb",
				"cg",
				new String[] { "title", "" },
				new String[] { "id", "ozoom" },
				new String[] { "any", "any" },
				new String[] { "西安晚报", "....." },
				new String[] { "height", "18" },
				"",
				"utf-8",
				"http://epaper.xiancn.com/xawb/",
				"img src=\"(.*?)/res/(.*?)attpic_brief.jpg(?=\")",
				"http://(.*?)_attpic_brief.jpg",
				"../../../",
				"http://epaper.xiancn.com/xawb/html/\\d{4}-\\d{2}/\\d{2}/node_\\d+.htm",
				"http://epaper.xiancn.com/xawb/html/\\d{4}-\\d{2}/\\d{2}/content_\\d+.htm",
				"http://epaper.xiancn.com/xawb/html/", "-", "/", "/node_55.htm");
		// TaskThreadPool sqdsb = new TaskThreadPool(
		// "sqdsb",
		// "cg",
		// new String[] { "title", "" },
		// new String[] { "id", "content_div" },
		// new String[] { "any", "any" },
		// new String[] { "三秦都市报", "....." },
		// new String[] { "class", "paper_left" },
		// "-三秦都市报数字报",
		// "utf-8",
		// "http://epaper.sanqin.com/paperdata",
		// "(?<=img id=\"img\\[\\d\\]\" src=\")/paperdata/sqdsb/\\d{8}/(.*?).jpg(?=\")",
		// "http://epaper.sanqin.com/paperdata/sqdsb/\\d{8}/(.*?)\\.jpg",
		// "/paperdata",
		// "http://epaper.sanqin.com/shtml/sqdsb/\\d{8}/[a-zA-Z]+\\d+.shtml",
		// "http://epaper.sanqin.com/shtml/sqdsb/\\d{8}/\\d+.shtml",
		// "http://epaper.sanqin.com/shtml/sqdsb/", "", "", "/index.shtml");
		// ThreadUncaughtExceptionHandler uehHandle = new
		// ThreadUncaughtExceptionHandler();
		// 2014.10.28开始变成Flash版 作废
		TaskThreadPool jwb = new TaskThreadPool(
				"jwb",
				"cg",
				new String[] { "title", "" },
				new String[] { "any", "any" },
				new String[] { "any", "any" },
				new String[] { "今晚报", "....." },
				new String[] { "id", "m_1_2_4" },
				"- -今晚报",
				"utf-8",
				"http://epaper.jwb.com.cn/jwb/",
				"(?<=<img src=)../../../res/(.*?)/(.*?)_attpic_brief.jpg(?=>)",
				"http://epaper.jwb.com.cn/jwb/res/(.*?)/(.*?)_attpic_brief.jpg",
				"../../../",
				"http://epaper.jwb.com.cn/jwb/html/\\d{4}-\\d{2}/\\d{2}/node_\\d+.htm",
				"http://epaper.jwb.com.cn/jwb/html/\\d{4}-\\d{2}/\\d{2}/content_\\d+.htm",
				"http://epaper.jwb.com.cn/jwb/html/", "-", "/", "/node_2.htm");
		TaskThreadPool mrxb = new TaskThreadPool(
				"mrxb",
				"cg",
				new String[] { "any", "any" },
				new String[] { "any", "any" },
				new String[] { "any", "any" },
				new String[] { "每日新报", "....." },
				new String[] { "width", "145" },
				"",
				"utf-8",
				"http://epaper.tianjinwe.com/mrxb/",
				"(?<=<IMG src=\")../../../images/(.*?)/(.*?)_attpic_brief.jpg(?=\">)",
				"http://epaper.tianjinwe.com/mrxb/images/(.*?)/(.*?)_attpic_brief.jpg",
				"../../../",
				"http://epaper.tianjinwe.com/mrxb/mrxb/\\d{4}-\\d{2}/\\d{2}/node_\\d+.htm",
				"http://epaper.tianjinwe.com/mrxb/mrxb/\\d{4}-\\d{2}/\\d{2}/content_\\d+.htm",
				"http://epaper.tianjinwe.com/mrxb/mrxb/", "-", "/",
				"/node_521.htm");
		TaskThreadPool jnsb = new TaskThreadPool("jnsb", "cg", new String[] {
				"title", "" }, new String[] { "class", "article" },
				new String[] { "any", "any" },
				new String[] { "济南时报", "....." }, new String[] { "title", "" },
				"", "gbk", "http://jnsb.e23.cn/jnsb",
				"(?<=src=\")/jnsb/\\d{8}/(.*?).jpg(?=\")",
				"http://jnsb.e23.cn/jnsb/\\d{8}/(.*?).jpg", "/jnsb",
				"/shtml/jnsb/\\d{8}/[a-zA-Z]{2}\\d+.shtml",
				"/shtml/jnsb/\\d{8}/\\d+.shtml",
				"http://jnsb.e23.cn/shtml/jnsb/", "", "", "/index.shtml");
		TaskThreadPool qlwb = new TaskThreadPool(
				"qlwb",
				"cg",
				new String[] { "title", "" },
				new String[] { "class", "xx" },
				new String[] { "any", "any" },
				new String[] { "齐鲁晚报", "....." },
				new String[] { "any", "any" },
				" - 齐鲁晚报数字报刊",
				"utf-8",
				"http://epaper.qlwb.com.cn/qlwb/",
				"(?<=SRC=\")(\\.\\./){2}IMAGE/\\d{8}/(.*?).jpg(?=\")",
				"http://epaper.qlwb.com.cn/qlwb/IMAGE/\\d{8}/(.*?).jpg",
				"../../",
				"http://epaper.qlwb.com.cn/qlwb/content/\\d{8}/Page[A-Za-z]+\\d+(.*?).htm",
				"http://epaper.qlwb.com.cn/qlwb/content/\\d{8}/Articel[a-zA-Z]\\d+(.*?).htm",
				"http://epaper.qlwb.com.cn/qlwb/content/", "", "",
				"/issueindex.htm");
		TaskThreadPool shrb = new TaskThreadPool(
				"shrb",
				"cg",
				new String[] { "title", "" },
				new String[] { "class", "xx" },
				new String[] { "any", "any" },
				new String[] { "生活日报", "....." },
				new String[] { "any", "any" },
				" - 生活日报数字报刊",
				"utf-8",
				"http://shrb.qlwb.com.cn/shrb/",
				"(?<=SRC=\")(\\.\\./){2}IMAGE/\\d{8}/(.*?).jpg(?=\")",
				"http://shrb.qlwb.com.cn/shrb/IMAGE/\\d{8}/(.*?).jpg",
				"../../",
				"http://shrb.qlwb.com.cn/shrb/content/\\d{8}/Page[A-Za-z]+\\d+(.*?).htm",
				"http://shrb.qlwb.com.cn/shrb/content/\\d{8}/Articel[a-zA-Z]+\\d+(.*?).htm",
				"http://shrb.qlwb.com.cn/shrb/content/", "", "",
				"/issueindex.htm");
		TaskThreadPool bdcb = new TaskThreadPool(
				"bdcb",
				"cg",
				new String[] { "title", "" },
				new String[] { "id", "reci_a" },
				new String[] { "any", "any" },
				new String[] { "半岛晨报", "....." },
				new String[] { "any", "any" },
				"",
				"gb2312",
				"http://epaper.hilizi.com/bdcb/",
				"(?<=img  src=\")/bdcb/\\d{8}/(.*?).jpg(?=\")",
				"http://epaper.hilizi.com/bdcb/\\d{8}/(.*?).jpg",
				"/bdcb/",
				"http://epaper.hilizi.com/shtml/bdcb/\\d{8}/[a-zA-Z]+\\d+.shtml",
				"http://epaper.hilizi.com/shtml/bdcb/\\d{8}/\\d+.shtml",
				"http://epaper.hilizi.com/shtml/bdcb/", "", "", "/vA01.shtml");
		TaskThreadPool dlwb = new TaskThreadPool(
				"dlwb",
				"cg",
				new String[] { "title", "" },
				new String[] { "any", "any" },
				new String[] { "any", "any" },
				new String[] { "大连晚报", "....." },
				new String[] { "class", "t1" },
				"",
				"utf-8",
				"http://szb.dlxww.com/dlwb/",
				"(?<=<IMG src=\")(\\.\\./){3}res/\\d{4}-\\d{2}/\\d{2}/(.*?)/(.*?)_attpic_brief.jpg(?=\">)",
				"http://szb.dlxww.com/dlwb/res/\\d{4}-\\d{2}/\\d{2}/(.*?)/(.*?)_attpic_brief.jpg",
				"../../../",
				"http://szb.dlxww.com/dlwb/html/\\d{4}-\\d{2}/\\d{2}/node_\\d+.htm",
				"http://szb.dlxww.com/dlwb/html/\\d{4}-\\d{2}/\\d{2}/content_\\d+.htm",
				"http://szb.dlxww.com/dlwb/html/", "-", "/", "/node_581.htm");
		TaskThreadPool xsb = new TaskThreadPool(
				"xsb",
				"cg",
				new String[] { "title", "" },
				new String[] { "any", "any" },
				new String[] { "any", "any" },
				new String[] { "新商报", "....." },
				new String[] { "class", "t1" },
				"",
				"utf-8",
				"http://szb.dlxww.com/xsb/",
				"(?<=<IMG src=\")(\\.\\./){3}res/\\d{4}-\\d{2}/\\d{2}/(.*?)/(.*?)_attpic_brief.jpg(?=\">)",
				"http://szb.dlxww.com/xsb/res/\\d{4}-\\d{2}/\\d{2}/(.*?)/(.*?)_attpic_brief.jpg",
				"../../../",
				"http://szb.dlxww.com/xsb/html/\\d{4}-\\d{2}/\\d{2}/node_\\d+.htm",
				"http://szb.dlxww.com/xsb/html/\\d{4}-\\d{2}/\\d{2}/content_\\d+.htm",
				"http://szb.dlxww.com/xsb/html/", "-", "/", "/node_34.htm");
		TaskThreadPool xhdsb = new TaskThreadPool(
				"xhdsb",
				"cg",
				new String[] { "any", "any" },
				new String[] { "any", "any" },
				new String[] { "any", "any" },
				new String[] { "西海都市报", "....." },
				new String[] { "width", "145" },
				"",
				"utf-8",
				"http://epaper.tibet3.com/xhdsb/",
				"(?<=<IMG src=\")(\\.\\./){3}res/(.*?)_attpic_brief.jpg(?=\">)",
				"http://epaper.tibet3.com/xhdsb/res/(.*?)_attpic_brief.jpg",
				"../../../",
				"http://epaper.tibet3.com/xhdsb/html/\\d{4}-\\d{2}/\\d{2}/node_\\d+.htm",
				"http://epaper.tibet3.com/xhdsb/html/\\d{4}-\\d{2}/\\d{2}/content_\\d+.htm",
				"http://epaper.tibet3.com/xhdsb/html/", "-", "/",
				"/node_29.htm");
		TaskThreadPool xnwb = new TaskThreadPool(
				"xnwb",
				"cg",
				new String[] { "any", "any" },
				new String[] { "any", "any" },
				new String[] { "any", "any" },
				new String[] { "西宁晚报", "....." },
				new String[] { "class", "font08" },
				"",
				"utf-8",
				"http://www.xnwbw.com/",
				"(?<=<IMG src=\")(\\.\\./){3}res/(.*?)_attpic_brief.jpg(?=\">)",
				"http://www.xnwbw.com/res/(.*?)_attpic_brief.jpg",
				"../../../",
				"http://www.xnwbw.com/html/\\d{4}-\\d{2}/\\d{2}/node_\\d+.htm",
				"http://www.xnwbw.com/html/\\d{4}-\\d{2}/\\d{2}/content_\\d+.htm",
				"http://www.xnwbw.com/html/", "-", "/", "/node_2.htm");
		TaskThreadPool dhb = new TaskThreadPool(
				"dhb",
				"cg",
				new String[] { "title", "" },
				new String[] { "any", "any" },
				new String[] { "any", "any" },
				new String[] { "大河晚报", "....." },
				new String[] { "width", "160" },
				"",
				"utf-8",
				"http://newpaper.dahe.cn/dhb/",
				"(?<=<IMG src=\")(\\.\\./){3}images/\\d{4}-\\d{2}/\\d{2}/(.*?)\\.jpg(?=\">)",
				"http://newpaper.dahe.cn/dhb/images/\\d{4}-\\d{2}/\\d{2}/(.*?)\\.jpg",
				"../../../",
				"http://newpaper.dahe.cn/dhb/html/\\d{4}-\\d{2}/\\d{2}/node_\\d+.htm",
				"http://newpaper.dahe.cn/dhb/html/\\d{4}-\\d{2}/\\d{2}/content_\\d+.htm",
				"http://newpaper.dahe.cn/dhb/html/", "-", "/", "/node_66.htm");
		TaskThreadPool hnsb = new TaskThreadPool(
				"hnsb",
				"cg",
				new String[] { "title", "" },
				new String[] { "class", "text" },
				new String[] { "any", "any" },
				new String[] { "河南商报", "....." },
				new String[] { "none", "none" },
				"",
				"utf-8",
				"",
				"(?<=<IMG src=\")http://newpaper.dahe.cn/hnsb/images/\\d{4}-\\d{2}/\\d{2}/(.*?)\\.jpg(?=\">)",
				"http://newpaper.dahe.cn/hnsb/images/\\d{4}-\\d{2}/\\d{2}/(.*?)\\.jpg",
				"",
				"http://www.shangbw.com/epaper/\\d{4}-\\d{2}-\\d{2}-\\d+\\.html",
				"/epaper/\\d{4}-\\d{2}-\\d{2}/content_\\d+.htm",
				"http://www.shangbw.com/epaper/", "-", "-", "-0.html");
		TaskThreadPool zzwb = new TaskThreadPool(
				"zzwb",
				"cg",
				new String[] { "title", "" },
				new String[] { "class", "font6" },
				new String[] { "any", "any" },
				new String[] { "郑州晚报", "....." },
				new String[] { "width", "168" },
				"－郑州晚报数字报-中原网-省会首家数字报",
				"utf-8",
				"http://zzwb.zynews.com/",
				"(?<=<IMG src=\")(\\.\\./){3}res/(.*?)_attpic_brief.jpg(?=\">)",
				"http://zzwb.zynews.com/res/(.*?)_attpic_brief.jpg",
				"../../../", "node_\\d+.htm", "content_\\d+.htm",
				"http://zzwb.zynews.com/html/", "-", "/", "/node_102.htm");
		TaskThreadPool yzwb = new TaskThreadPool(
				"yzwb",
				"cg",
				new String[] { "any", "any" },
				new String[] { "any", "any" },
				new String[] { "any", "any" },
				new String[] { "燕赵晚报", "....." },
				new String[] { "width", "209" },
				"",
				"utf8",
				"http://yzwb.sjzdaily.com.cn/",
				"(?<=<IMG src=\")(\\.\\./){3}res/(.*?)_attpic_brief.jpg(?=\">)",
				"http://yzwb.sjzdaily.com.cn/res/(.*?)_attpic_brief.jpg",
				"../../../",
				"http://yzwb.sjzdaily.com.cn/html/\\d{4}-\\d{2}/\\d{2}/node_\\d+.htm",
				"http://yzwb.sjzdaily.com.cn/html/\\d{4}-\\d{2}/\\d{2}/content_\\d+.htm",
				"http://yzwb.sjzdaily.com.cn/html/", "-", "/", "/node_29.htm");
		TaskThreadPool yzdsb = new TaskThreadPool(
				"yzdsb",
				"cg",
				new String[] { "title", "" },
				new String[] { "class", "cont" },
				new String[] { "any", "any" },
				new String[] { "燕赵都市报", "....." },
				new String[] { "none", "none" },
				"-燕赵都市报数字报",
				"utf-8",
				"",
				"(?<=<img src=\')http://epaper.yzdsb.com.cn/\\d{6}/\\d{2}/(.*?)\\.jpg(?=\' />)",
				"http://epaper.yzdsb.com.cn/\\d{6}/\\d{2}/(.*?)\\.jpg", "",
				"http://epaper.yzdsb.com.cn/\\d{6}/\\d{2}/e\\d+\\.html",
				"http://epaper.yzdsb.com.cn/\\d{6}/\\d{2}/\\d+.html",
				"http://epaper.yzdsb.com.cn/", "", "/", "/");
		Thread t1 = new Thread(xdkb);
		Thread t2 = new Thread(jlwb);
		Thread t3 = new Thread(njcb);
		Thread t4 = new Thread(szsb);
		Thread t5 = new Thread(sztqb);
		Thread t6 = new Thread(nfdsb);
		Thread t7 = new Thread(whwb);
		Thread t8 = new Thread(ctdsb);
		Thread t9 = new Thread(whcb);
		Thread t10 = new Thread(cqcb);
		Thread t11 = new Thread(cqwb);
		Thread t12 = new Thread(hsb);
		Thread t13 = new Thread(xawb);
		// Thread t14 = new Thread(sqdsb);
		Thread t15 = new Thread(jwb); // =
		Thread t16 = new Thread(mrxb);
		Thread t17 = new Thread(jnsb);
		Thread t18 = new Thread(qlwb);
		Thread t19 = new Thread(shrb);
		Thread t20 = new Thread(bdcb);
		Thread t21 = new Thread(dlwb);
		Thread t22 = new Thread(xsb);
		Thread t23 = new Thread(xhdsb);
		Thread t24 = new Thread(xnwb);
		Thread t25 = new Thread(dhb);
		Thread t26 = new Thread(hnsb);
		Thread t27 = new Thread(zzwb);
		Thread t28 = new Thread(yzwb);
		Thread t29 = new Thread(yzdsb);
		t29.start();
	}
}