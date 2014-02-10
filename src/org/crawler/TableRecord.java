package org.crawler;

import java.util.Date;

public class TableRecord {
	/**
	 * id
	 */
	private Integer id;
	/**
	 * 記事中の日付
	 */
	private Date presume_date;
	/**
	 * 記事の本文
	 */
	private String report_content;
	/**
	 * 記事のURL
	 */
	private String report_url;

	/**
	 * 日付情報のありフラグ
	 */
	private boolean date_exit;
	/**
	 * 　記事取得日付
	 */
	private Date crawle_date;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Date getPresume_date() {
		return presume_date;
	}

	public void setPresume_date(Date presume_date) {
		this.presume_date = presume_date;
	}

	public String getReport_content() {
		return report_content;
	}

	public void setReport_content(String report_content) {
		this.report_content = report_content;
	}

	public String getReport_url() {
		return report_url;
	}

	public void setReport_url(String report_url) {
		this.report_url = report_url;
	}

	public Date getCrawle_date() {
		return crawle_date;
	}

	public void setCrawle_date(Date crawle_date) {
		this.crawle_date = crawle_date;
	}

	public boolean isDate_exit() {
		return date_exit;
	}

	public void setDate_exit(boolean date_exit) {
		this.date_exit = date_exit;
	}

}
