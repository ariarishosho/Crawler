package org.crawler.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditUtil {

	/**
	 * 記事中の不要タグと削除する
	 * 
	 * @param content
	 * @return
	 */
	public static String trimTags(String content) {
		if (content != null) {
			String regex = "<.*/>";
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(content);

			content = m.replaceAll("");
			content.trim();
			content.replaceAll(" ", "");
		}
		return content;
	}

	/**
	 * 全角アルファベットと半角アルファベットとの文字コードの差
	 */
	private static final int DIFFERENCE = 'Ａ' - 'A';

	/**
	 * 変更対象全角記号配列
	 */
	private static char[] SIGNS2 = { '！', '＃', '＄', '％', '＆', '（', '）', '＊',
			'＋', '，', '−', '．', '／', '：', '；', '＜', '＝', '＞', '？', '＠', '［',
			'］', '＾', '＿', '｛', '｜', '｝' };

	/**
	 * 文字列のアルファベット・数値を半角文字に変換する。
	 * 
	 * @param str
	 * @return
	 */
	public static String convertFulltoHalf(String str) {
		char[] cc = str.toCharArray();
		StringBuilder sb = new StringBuilder();
		for (char c : cc) {
			char newChar = c;
			if ((('Ａ' <= c) && (c <= 'Ｚ')) || (('ａ' <= c) && (c <= 'ｚ'))
					|| (('１' <= c) && (c <= '９')) || is2Sign(c)) {
				// 変換対象のcharだった場合に全角文字と半角文字の差分を引く
				newChar = (char) (c - DIFFERENCE);
			}
			sb.append(newChar);
		}
		return sb.toString();
	}

	/**
	 * 変換対象全角記号かを判定する。
	 * 
	 * @param pc
	 * @return
	 */
	private static boolean is2Sign(char pc) {
		for (char c : SIGNS2) {
			if (c == pc) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 漢数字を英数字に変換する
	 * 
	 * @param str
	 * @return
	 */
	public static String convertKanjiNumtoNum(String str) {
		if (str != null) {
			str = str.replace("〇", "0");
			str = str.replace("一", "1");
			str = str.replace("二", "2");
			str = str.replace("三", "3");
			str = str.replace("四", "4");
			str = str.replace("五", "5");
			str = str.replace("六", "6");
			str = str.replace("七", "7");
			str = str.replace("八", "8");
			str = str.replace("九", "9");
			str = str.replace("度", "");
		}
		return str;
	}
}
