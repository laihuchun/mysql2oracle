package com.suidifu.sync;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;

import org.apache.commons.dbcp.BasicDataSource;

public class DataSyncSingle {

	private final BasicDataSource sDataSource;
	private final BasicDataSource tDataSource;
	private final DataSyncSessionParameter sessionParameter;

	public DataSyncSingle(BasicDataSource sDataSource,
			BasicDataSource tDataSource,
			DataSyncSessionParameter sessionParameter) {

		this.sDataSource = sDataSource;
		this.tDataSource = tDataSource;
		this.sessionParameter = sessionParameter;
	}

	public void start() {
		// start
		Connection sConn = null;
		Connection tConn = null;
		PreparedStatement pstmt = null;
		PreparedStatement targetPstmt = null;
		ResultSet rs = null;
		try {
			sConn = this.sDataSource.getConnection();
			tConn = this.tDataSource.getConnection();
			tConn.setAutoCommit(false);
			// get the source select data SQL.
			String tmpSql = sessionParameter.getSourceSelectSql();
			String insertSql = sessionParameter.getTargetInsertSql();
			int commitNum = sessionParameter.getTargetCommitNum();
			String sessionCommand = sessionParameter.getSourceCommand();
			String exeSql = tmpSql;
			System.out.println("exeSql=" + exeSql);
			String[] command = null;
			if (null != sessionCommand) {
				command = sessionCommand.split(";");
				// load the session command
				for (int i = 0; i < command.length; i++) {
					pstmt = sConn.prepareStatement(command[i]);
					pstmt.execute();
				}
			}
			
			pstmt = sConn.prepareStatement(exeSql);
			rs = pstmt.executeQuery();

			// start to insert the data
			targetPstmt = tConn.prepareStatement(insertSql);

			int cnt = 0;
			while (rs.next()) {
				ResultSetMetaData rsmd = rs.getMetaData();
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					if (rsmd.getColumnType(i) == Types.VARCHAR
							|| rsmd.getColumnType(i) == Types.CHAR) {
						targetPstmt.setString(i, rs.getString(i));
					} else if (rsmd.getColumnType(i) == Types.INTEGER
							|| rsmd.getColumnType(i) == Types.TINYINT) {
						targetPstmt.setInt(i, rs.getInt(i));
					} else if (rsmd.getColumnType(i) == Types.DATE) {
						
						
						targetPstmt.setDate(i, rs.getDate(i));
					} else if (rsmd.getColumnType(i) == Types.DOUBLE) {
						targetPstmt.setDouble(i, rs.getDouble(i));
					} else if (rsmd.getColumnType(i) == Types.CLOB) {
						targetPstmt.setClob(i, rs.getClob(i));
					} else if (rsmd.getColumnType(i) == Types.TIMESTAMP) {
						//targetPstmt.setDate(i, rs.getTimestamp(i));
						targetPstmt.setTimestamp(i, rs.getTimestamp(i));				
					} else if (rsmd.getColumnType(i) == Types.NUMERIC
							|| rsmd.getColumnType(i) == Types.BIGINT) {
						targetPstmt.setLong(i, rs.getLong(i));
					}else if (rsmd.getColumnType(i) == Types.DECIMAL
							|| rsmd.getColumnType(i) == Types.NUMERIC) {
						BigDecimal value = new BigDecimal(rs.getDouble(i));
						value = value.setScale(2,RoundingMode.HALF_UP);
						targetPstmt.setFloat(i, 2);
						targetPstmt.setDouble(i,value.doubleValue());
					//	System.out.println("bigdecimal as double  is "+value.doubleValue());
					}
				}

				targetPstmt.execute();
				cnt = cnt + 1;
				if ((cnt % commitNum) == 0) {
					tConn.commit();
				}

			}
			tConn.commit();
			rs.close();
			pstmt.close();
			sConn.close();
			targetPstmt.close();
			tConn.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			try {
				if (rs != null)
					rs.close();
				if (pstmt != null)
					pstmt.close();
				if (sConn != null)
					sConn.close();
				if (targetPstmt != null)
					if (tConn != null)
						tConn.close();
			} catch (Exception e) {
				System.out.println("error Message : " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

}
