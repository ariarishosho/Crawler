package org.crawler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

/**
 * 
 * @author arichi
 */
public class DAO {

	/**
	 * 記事データをDBから取得する テーブルをちゃん正規化したらJOINやり方考える
	 * 
	 * @return
	 */
	public List<TableRecord> select() {
		try {
			DataTransaction dt = new DataTransaction();
			Connection conn = dt.getConnection();
			QueryRunner run = new QueryRunner(dt.getDataSorce());
			ResultSetHandler<List<TableRecord>> h = new BeanListHandler<TableRecord>(
					TableRecord.class);
			List<TableRecord> result = run.query(conn,
					"select firstname,lastname from person", h);
			DbUtils.close(conn);
			System.out.println(DataTransaction.connectionCount);
			return result;
		} catch (Exception e) {
			System.out.println("Erroror : " + e);
			return null;
		}
	}

	/**
	 * 記事データをDBへ格納する
	 */
	public void insert(List<TableRecord> reportData) {
		DataTransaction dt = new DataTransaction();
		Connection conn = dt.getConnection();
		QueryRunner run = new QueryRunner(dt.getDataSorce());
		for (Iterator<TableRecord> i = reportData.iterator(); i.hasNext();) {
			TableRecord t = i.next();
			try {
				run.update(
						conn,
						"INSERT INTO CONTENTS(presume_date,report_content,report_url,crawle_date) VALUES(?,?,?,?)",
						t.getPresume_date(), t.getReport_content(),
						t.getReport_url(), t.getCrawle_date());
			} catch (SQLException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		}

	}
}