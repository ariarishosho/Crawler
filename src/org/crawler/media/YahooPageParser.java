package org.crawler.media;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.crawler.DAO;
import org.crawler.TableRecord;
import org.crawler.util.EditUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * ヤフー用のニュース本文解析クラス
 * 
 * @author arichi
 * 
 */
public class YahooPageParser {

	private final String linkClassName = "readAll";

	// Webページオブジェクト
	private Document document;

	/**
	 * Htmlエレメメントを設定
	 * 
	 * @param html
	 */
	public YahooPageParser(Document document) {
		this.document = document;
	}

	/**
	 * ヤフーのページから記事本文を抽出する
	 * 
	 * @return
	 */
	public String getContents() {
		String content = null;

		Elements linkElement = document.getElementsByClass(linkClassName);
		try {
			if (linkElement.size() > 0) {
				Document contentsPage = Jsoup.connect(
						linkElement.get(0).absUrl("href")).get();
				// 記事本文を取得
				content = contentsPage.getElementsByClass("ynDetailText")
						.html();
				if(content.length() == 0){
					content = contentsPage.getElementsByClass("detailHeadline")
							.html();
				}
				content = EditUtil.trimTags(content);
				// 日付抽出
				List<TableRecord> rows = pickPresumeDate(content);
				DAO dao = new DAO();
				dao.insert(rows);
			}
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		System.out.println(content);

		return content;
	}

	// 2000/01/01 or 2000/1/1 or 2000年01月01 or 2000年1日1
	String regex_yyyyMMdd = "([0-9０-９]{4})[-/¥.／−．年]{1}([0-9０-９]{1,2})[-/¥.／−．月]{1}([0-9０-９]{1,2})[¥.．日]?";
	// H24/12/31
	String regex_wareki = "(平成|昭和|大正|明治|H|S|T|M|H|S|T|M)([0-9０-９]{1,2})[-/¥.／−．年]{1}[01０１]?[0-9０-９][-/¥.／−．月]{1}[0-3０−３]?[0-9０-９][¥.．日]?";
	// H1/4/1
	String regex_yyMM = "(平成|昭和|大正|明治|H|S|T|M|H|S|T|M)([0-9０-９]{1,2})[-/¥.／−．年]{1}[01０１]?[0-9０-９][-/¥.／−．月]{1}(?![0-9０-９])";
	// 5月3日
	String regex_MMdd = "(?<![-/¥.／−．年0-9０-９]{1})([0-9０-９]{1,2})[-/¥.／−．月]{1}([0-9０-９]{1,2})[¥.．日]?(?![-/¥.／−．0-9０-９])";
	// 1日
	String regex_dd = "(?<![-/¥.／−．月0-9０-９]{1})([0-9０-９]{1,2})[¥.．日]+(?![-/¥.／−．0-9０-９])";
	// 2005年
	String regex_yyyy = "([0-9０-９]{4})[-/¥.／−．年]{1}(?![-/¥.／−．0-9０-９])";
	// 平成１２年
	String regex_yy = "(平成|昭和|大正|明治|H|S|T|M|H|S|T|M)([0-9０-９]{1,2})[-/¥.／−．年]{1}(?![-/¥.／−．0-9０-９])";
	// 1月
	String regex_MM = "(?<![-/¥.／−．年0-9０-９]{1})([0-9０-９]{1,2})[¥.．月]+(?![-/¥.／−．0-9０-９])";
	// 来年１２月
	String regex_soutaiyyMM = "(本年|今年|来年|翌年|再来年|去年|昨年|一昨年)([0-9０-９]{1,2})[-/¥.／−．月]{1}";
	// 来月６日
	String regex_soutaiMMdd = "(今月|来月|翌月|再来月|先月|先々月)([0-9０-９]{1,2})[-/¥.／−．日]{1}";
	// 明日６日
	String regex_soutaidd = "(本日|今日|明日|翌日|明後日|昨日|一昨日)([0-9０-９]{1,2})[-/¥.／−．日]{1}";
	// 漢数字 二〇〇四年一二月一四日
	String regex_kanjiyyyyMMdd = "[〇一二三四五六七八九十]{3,4}[-/¥.／−．年]{1}([〇一二三四五六七八九十]{1,2})[-/¥.／−．月]{1}([〇一二三四五六七八九十]{1,2})[¥.．日]?";
	// 漢数字 二〇〇四年度一二月 or 二〇〇四年一二月
	String regex_kanjiyyyyMM = "[〇一二三四五六七八九十]{3,4}[-/¥.／−．年]{1}度?([〇一二三四五六七八九十]{1,2})[-/¥.／−．月]{1}(?![〇一二三四五六七八九十])";

	String[] targetPatterns = { regex_yyyyMMdd, regex_wareki, regex_yyMM,
			regex_MMdd, regex_dd, regex_yyyy, regex_yy, regex_MM,
			regex_soutaiyyMM, regex_soutaiMMdd, regex_soutaidd,
			regex_kanjiyyyyMMdd, regex_kanjiyyyyMM };

	/**
	 * 記事中の日付をピックアップして日付を代入 ルール: 日付が無い記事は、本日日付を代入 年月が無い記事は、本日年月を代入
	 * 日付が無い記事は、当月１日を代入
	 * 
	 * @param contents
	 * @return
	 */
	private List<TableRecord> pickPresumeDate(String contents) {

		List<Date> date = null; // 予想日付
		List<TableRecord> record = new ArrayList<TableRecord>();
		Map<String, List<String>> regex_date = new HashMap<String, List<String>>(); // 正規表現で抜き出された文字列
		if (contents != null) {
			// 正規表現抜き出し
			for (String targetPattern : targetPatterns) {
				Pattern pat = Pattern.compile(targetPattern);
				Matcher mat = pat.matcher(contents);
				// 候補日付文字列
				ArrayList<String> arry = new ArrayList<String>();
				while (mat.find()) {
					String matchedString = mat.group();
					System.out.println(targetPattern);
					System.out.println(matchedString);
					arry.add(matchedString);
				}
				if (arry.size() > 0) {
					// 正規パターンごとに対象日を格納
					regex_date.put(targetPattern, arry);
				}
			}
			try {
				date = convertDate(regex_date);
			} catch (ParseException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
			Date today = new Date();
			if (date != null && date.size() > 0) {
				
				for (Date presume_date : date) {
					TableRecord row = new TableRecord();
					row.setCrawle_date(today);
					row.setDate_exit(true);
					row.setPresume_date(presume_date);
					row.setReport_content(contents);
					row.setReport_url("");
					record.add(row);
				}
			} else {
				TableRecord row = new TableRecord();
				row.setCrawle_date(today);
				row.setDate_exit(false);
				row.setPresume_date(today);
				row.setReport_content(contents);
				row.setReport_url("");
				record.add(row);
			}
		}
		return record;
	}

	/**
	 * 記事の予測日付を生成する より完璧にやろうとしたら、日付の出てきた順番を格納しておく必要があるかもしれない
	 * 
	 * @param regex_date
	 * @return
	 * @throws ParseException
	 */
	private List<Date> convertDate(Map<String, List<String>> regex_date)
			throws ParseException {
		List<Date> dates = null;
		String strPresumeDate;
		int yyyy = 0;
		int MM = 0;
		int dd = 0;
		if (regex_date.size() > 0) {
			dates = new ArrayList<Date>();
			if (regex_date.containsValue(regex_yyyyMMdd)
					|| regex_date.containsValue(regex_wareki)
					|| regex_date.containsValue(regex_kanjiyyyyMMdd)) {
				// 基準となる年月日
				if (regex_date.containsValue(regex_yyyyMMdd)) {
					strPresumeDate = regex_date.get(regex_yyyyMMdd).get(0);
				} else if (regex_date.containsValue(regex_wareki)) {
					strPresumeDate = regex_date.get(regex_wareki).get(0);
				} else {
					strPresumeDate = regex_date.get(regex_kanjiyyyyMMdd).get(0);
				}
				Calendar cal = getCalendar(strPresumeDate);
				yyyy = cal.get(Calendar.YEAR);
				MM = cal.get(Calendar.MONTH);
				dd = cal.get(Calendar.DAY_OF_MONTH);
			} else if (regex_date.containsValue(regex_yyMM)
					|| regex_date.containsValue(regex_kanjiyyyyMM)) {
				// 基準となる年月を取得
				if (regex_date.containsValue(regex_yyMM)) {
					strPresumeDate = regex_date.get(regex_yyMM).get(0);
				} else {
					strPresumeDate = regex_date.get(regex_kanjiyyyyMM).get(0);
				}
				Calendar cal = getCalendar(strPresumeDate);
				yyyy = cal.get(Calendar.YEAR);
				MM = cal.get(Calendar.MONTH);
			}
			// 基準となる年を取得
			if (yyyy == 0 && regex_date.containsValue(regex_yy)) {
				strPresumeDate = regex_date.get(regex_yy).get(0);
				Calendar cal = getCalendar(strPresumeDate);
				yyyy = cal.get(Calendar.YEAR);
			}
			// 基準となる月を取得
			if (MM == 0
					&& (regex_date.containsValue(regex_MM) || regex_date
							.containsValue(regex_MMdd))) {
				if (regex_date.containsValue(regex_MM)) {
					strPresumeDate = regex_date.get(regex_MM).get(0);
				} else {
					strPresumeDate = regex_date.get(regex_MMdd).get(0);
				}
				Calendar cal = getCalendar(strPresumeDate);
				MM = cal.get(Calendar.MONTH);
				if (regex_date.containsValue(regex_MMdd)) {
					dd = cal.get(Calendar.DAY_OF_MONTH);
				}
			}
			// 基準となる日を取得
			if (dd == 0 && regex_date.containsValue(regex_dd)) {
				strPresumeDate = regex_date.get(regex_dd).get(0);
				Calendar cal = getCalendar(strPresumeDate);
				dd = cal.get(Calendar.DAY_OF_MONTH);
			}

			// 相対日の判定
			if (regex_date.containsValue(regex_soutaiyyMM)) {

				if (MM == 0) {
					// 月がなかった場合
					Pattern pat = Pattern.compile("[0-9０-９]");
					Matcher mat = pat.matcher(regex_date.get(regex_soutaiyyMM)
							.get(0));
					MM = Integer.parseInt(mat.group());
				}

			}

			if (regex_date.containsValue(regex_soutaiMMdd)) {
				if (dd == 0) {
					// 日がなかった場合
					Pattern pat = Pattern.compile("[0-9０-９]");
					Matcher mat = pat.matcher(regex_date.get(regex_soutaiMMdd)
							.get(0));
					dd = Integer.parseInt(mat.group());
				}
			}
			if (regex_date.containsValue(regex_soutaidd)) {
				if (dd == 0) {
					// 日がなかった場合
					Pattern pat = Pattern.compile("[0-9０-９]");
					Matcher mat = pat.matcher(regex_date.get(regex_soutaiMMdd)
							.get(0));
					dd = Integer.parseInt(mat.group());
				}
			}
			if (yyyy == 0) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date());
				yyyy = cal.get(Calendar.YEAR);
			}
			if (MM == 0) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date());
				MM = cal.get(Calendar.MONTH);
			}
			if (dd == 0) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date());
				dd = cal.get(Calendar.DAY_OF_MONTH);
			}

			for (Entry<String, List<String>> e : regex_date.entrySet()) {
				// 同じ日付ごと
				List<String> strdate = e.getValue();
				for (Iterator<String> iterator = strdate.iterator(); iterator
						.hasNext();) {
					String date = iterator.next();
					SimpleDateFormat frmt = new SimpleDateFormat();

					if (e.getKey() == regex_yyyyMMdd) {
						dates.add(frmt.parse(date));
					} else if (e.getKey() == regex_wareki) {
						dates.add(frmt.parse(date));
					} else if (e.getKey() == regex_yyMM) {
						Date _yyMM = frmt.parse(date);
						Calendar cal = Calendar.getInstance();
						cal.setTime(_yyMM);
						cal.set(Calendar.DAY_OF_MONTH, dd);
						dates.add(cal.getTime());
					} else if (e.getKey() == regex_MMdd) {
						Date _MMdd = frmt.parse(date);
						Calendar cal = Calendar.getInstance();
						cal.setTime(_MMdd);
						cal.set(Calendar.YEAR, yyyy);
						dates.add(cal.getTime());

					} else if (e.getKey() == regex_dd) {
						frmt.applyPattern("d");
						Date _dd = frmt.parse(date);
						Calendar cal = Calendar.getInstance();
						cal.setTime(_dd);
						cal.set(Calendar.YEAR, yyyy);
						cal.set(Calendar.MONTH, MM);
						dates.add(cal.getTime());
					} else if (e.getKey() == regex_yyyy) {
						Date _yyyy = frmt.parse(date);
						Calendar cal = Calendar.getInstance();
						cal.setTime(_yyyy);
						cal.set(Calendar.MONTH, MM);
						cal.set(Calendar.DAY_OF_MONTH, dd);
						dates.add(cal.getTime());

					} else if (e.getKey() == regex_yy) {
						Date _yy = frmt.parse(date);
						Calendar cal = Calendar.getInstance();
						cal.setTime(_yy);
						cal.set(Calendar.MONTH, MM);
						cal.set(Calendar.DAY_OF_MONTH, dd);
						dates.add(cal.getTime());

					} else if (e.getKey() == regex_MM) {
						Date _MM = frmt.parse(date);
						Calendar cal = Calendar.getInstance();
						cal.setTime(_MM);
						cal.set(Calendar.YEAR, yyyy);
						cal.set(Calendar.DAY_OF_MONTH, dd);
						dates.add(cal.getTime());

					} else if (e.getKey() == regex_soutaiyyMM) {
						Calendar cal = Calendar.getInstance();
						cal.set(yyyy, MM, dd);
						if (e.getKey().contains("本年")
								|| e.getKey().contains("今年")) {
							// そのまま
						} else if (e.getKey().contains("来年")
								|| e.getKey().contains("翌年")) {
							cal.add(Calendar.YEAR, 1);
						} else if (e.getKey().contains("再来年")) {
							cal.add(Calendar.YEAR, 2);
						} else if (e.getKey().contains("去年")
								|| e.getKey().contains("昨年")) {
							cal.add(Calendar.YEAR, -1);
						} else if (e.getKey().contains("一昨年")
								|| e.getKey().contains("今日")) {
							cal.add(Calendar.YEAR, -2);
						}

						dates.add(cal.getTime());

					} else if (e.getKey() == regex_soutaiMMdd) {
						Calendar cal = Calendar.getInstance();
						cal.set(yyyy, MM, dd);
						if (e.getKey().contains("今月")) {
							// そのまま
						} else if (e.getKey().contains("来月")
								|| e.getKey().contains("翌月")) {
							cal.add(Calendar.MONTH, 1);
						} else if (e.getKey().contains("再来月")) {
							cal.add(Calendar.MONTH, 2);
						} else if (e.getKey().contains("先月")) {
							cal.add(Calendar.MONTH, -1);
						} else if (e.getKey().contains("先々月")) {
							cal.add(Calendar.MONTH, -2);
						}
						dates.add(cal.getTime());

					} else if (e.getKey() == regex_soutaidd) {
						Calendar cal = Calendar.getInstance();
						cal.set(yyyy, MM, dd);
						if (e.getKey().contains("本日")
								|| e.getKey().contains("今日")) {
							// そのまま
						} else if (e.getKey().contains("明日")
								|| e.getKey().contains("翌日")) {
							cal.add(Calendar.DAY_OF_MONTH, 1);
						} else if (e.getKey().contains("明後日")) {
							cal.add(Calendar.DAY_OF_MONTH, 2);
						} else if (e.getKey().contains("昨日")) {
							cal.add(Calendar.DAY_OF_MONTH, -1);
						} else if (e.getKey().contains("一昨日")) {
							cal.add(Calendar.DAY_OF_MONTH, -2);
						}
						dates.add(cal.getTime());

					} else if (e.getKey() == regex_kanjiyyyyMMdd) {
						dates.add(frmt.parse(date));
					} else if (e.getKey() == regex_kanjiyyyyMM) {
						Date _kanjiyyyyMM = frmt.parse(date);
						Calendar cal = Calendar.getInstance();
						cal.setTime(_kanjiyyyyMM);
						cal.set(Calendar.DAY_OF_MONTH, dd);
						dates.add(cal.getTime());
					}
				}

				System.out.println(e.getKey() + " : " + e.getValue());
			}
		}
		return dates;
	}

	private Calendar getCalendar(String strPresumeDate) {
		Locale locale = new Locale("ja", "JP", "JP");
		DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd", locale);
		Calendar cal = null;
		strPresumeDate = EditUtil.convertFulltoHalf(strPresumeDate);
		strPresumeDate = EditUtil.convertKanjiNumtoNum(strPresumeDate);
		try {
			cal = Calendar.getInstance();
			cal.setTime(format1.parse(strPresumeDate));
		} catch (ParseException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		}
		return cal;
	}
}
