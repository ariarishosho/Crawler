package org.crawler;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class RssFeedAnalyzer {
	private static final String RSS_URL = "http://rss.dailynews.yahoo.co.jp/fc/rss.xml";

	public void getRssContet() {
		try {

			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(RSS_URL);
			Element root = document.getDocumentElement();

			NodeList channel = root.getElementsByTagName("channel");

			NodeList title = ((Element) channel.item(0))
					.getElementsByTagName("title");

			System.out.println("titel"
					+ title.item(0).getFirstChild().getNodeValue());
			NodeList item_list = root.getElementsByTagName("item");

			WebPageAnalyzer webpage = new WebPageAnalyzer();

			for (int i = 0; i < item_list.getLength(); i++) {

				Element element = (Element) item_list.item(i);

				NodeList item_title = element.getElementsByTagName("title");
				NodeList item_description = element
						.getElementsByTagName("description");
				NodeList item_link = element.getElementsByTagName("link");

				if (item_title.getLength() > 0)
					System.out.println(item_title.item(0).getFirstChild()
							.getNodeValue());
				if (item_description.getLength() > 0)
					System.out.println(item_description.item(0).getFirstChild()
							.getNodeValue());
				if (item_link.getLength() > 0) {
					System.out.println(item_link.item(0).getFirstChild()
							.getNodeValue());
					webpage.getContetPage(item_link.item(0).getFirstChild()
							.getNodeValue());
				}
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
