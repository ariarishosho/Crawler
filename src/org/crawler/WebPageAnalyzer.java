package org.crawler;

import java.io.IOException;

import org.crawler.media.YahooPageParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class WebPageAnalyzer {
	public void getContetPage(String url) {
		try {
			Document document = Jsoup.connect(url).get();
			YahooPageParser yahoo = new YahooPageParser(document);
			String contents = yahoo.getContents();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
