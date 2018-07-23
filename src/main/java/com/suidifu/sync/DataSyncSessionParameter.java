package com.suidifu.sync;

public class DataSyncSessionParameter {
   
	int    targetCommitNum;
	String targetInsertSql;
	int    sourceThreadNum;
	String sourceSelectSql;
	String sourceCommand;
	
	public int getTargetCommitNum() {
		return targetCommitNum;
	}
	public void setTargetCommitNum(int targetCommitNum) {
		this.targetCommitNum = targetCommitNum;
	}
	public String getTargetInsertSql() {
		return targetInsertSql;
	}
	public void setTargetInsertSql(String targetInsertSql) {
		this.targetInsertSql = targetInsertSql;
	}
	public int getSourceThreadNum() {
		return sourceThreadNum;
	}
	public void setSourceThreadNum(int sourceThreadNum) {
		this.sourceThreadNum = sourceThreadNum;
	}
	public String getSourceSelectSql() {
		return sourceSelectSql;
	}
	public void setSourceSelectSql(String sourceSelectSql) {
		this.sourceSelectSql = sourceSelectSql;
	}
	public String getSourceCommand() {
		return sourceCommand;
	}
	public void setSourceCommand(String sourceCommand) {
		this.sourceCommand = sourceCommand;
	}
}
