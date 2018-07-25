package com.suidifu.sync;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.CyclicBarrier;
import org.apache.commons.dbcp.BasicDataSource;

public class DataSyncMain {

	static DataSyncDataSourceParameter dataSourceParameters;
	static DataSyncSessionParameter sessionParameter;
	static final String configFileName = "config.properties";

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// initialize the parameters
		String progPath = args[0];
		//String progPath = "D://work//MyEclipse 8.5//Workspaces//dataSync";
		String confFilePath = progPath + "//conf";
		if (setparameters(confFilePath)) {
			// start to call thread to sync the data
			syncData();
		}
	}

	private static void syncData() {
		BasicDataSource sDataSource = null;
		BasicDataSource tDataSource = null;
		try {
			// for source driver
			sDataSource = new BasicDataSource();

			sDataSource.setInitialSize(dataSourceParameters
					.getSourceInitialSize());

			sDataSource.setMaxActive(dataSourceParameters.getSourceMaxActive());
			sDataSource.setDriverClassName(dataSourceParameters
					.getSourceDriverClassName());
			sDataSource.setUrl(dataSourceParameters.getSourceUrl());
			sDataSource.setUsername(dataSourceParameters.getSourceUser());
			sDataSource.setPassword(dataSourceParameters.getSourcePassword());
			sDataSource.setMaxWait(dataSourceParameters.getSourceMaxWait());
			sDataSource.setMaxIdle(dataSourceParameters.getSourceMaxIdle());
			sDataSource.setMinIdle(dataSourceParameters.getSourceMinIdle());
			// for target database
			tDataSource = new BasicDataSource();
			tDataSource.setInitialSize(dataSourceParameters
					.getTargetInitialSize());
			tDataSource.setMaxActive(dataSourceParameters.getTargetMaxActive());
			tDataSource.setDriverClassName(dataSourceParameters
					.getTargetDriverClassName());
			tDataSource.setUrl(dataSourceParameters.getTargetUrl());
			tDataSource.setUsername(dataSourceParameters.getTargetUser());
			tDataSource.setPassword(dataSourceParameters.getTargetPassword());
			tDataSource.setMaxWait(dataSourceParameters.getTargetMaxWait());
			tDataSource.setMaxIdle(dataSourceParameters.getTargetMaxIdle());
			tDataSource.setMinIdle(dataSourceParameters.getTargetMinIdle());

			// call thread to sync the data from source to target
			if (sessionParameter.getSourceThreadNum() > 1) {
				CyclicBarrier barrier = new CyclicBarrier(sessionParameter
						.getSourceThreadNum() + 2);
				for (int i = 0; i <= sessionParameter.getSourceThreadNum(); i++) {
					DataSyncThread dataSync = new DataSyncThread(barrier, i,
							sDataSource, tDataSource, sessionParameter);
					dataSync.start();
				}
				long start = System.currentTimeMillis();
				barrier.await();
				barrier.await();
				long end = System.currentTimeMillis();
				long duration = end - start;
				System.out.println("Concurrency "
						+ sessionParameter.getSourceThreadNum()
						+ " threads,duration=" + duration + "ms");
			} else {
				DataSyncSingle dataSync = new DataSyncSingle(sDataSource,
						tDataSource, sessionParameter);
				long start = System.currentTimeMillis();
				dataSync.start();
				long end = System.currentTimeMillis();
				long duration = end - start;
				System.out.println("Concurrency "
						+ sessionParameter.getSourceThreadNum()
						+ " threads,duration=" + duration + "ms");
			}
			if (null != sDataSource)
				sDataSource.close();
			if (null != tDataSource)
				tDataSource.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			try {
				if (null != sDataSource)
					sDataSource.close();
				if (null != tDataSource)
					tDataSource.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static boolean setparameters(String filePath) {
		dataSourceParameters = new DataSyncDataSourceParameter();
		sessionParameter = new DataSyncSessionParameter();
		Properties props = new Properties();
		try {
			InputStream in = new BufferedInputStream(new FileInputStream(
					filePath + "//" + configFileName));
			props.load(in);

			// for source database
			dataSourceParameters.setSourceInitialSize(Integer.parseInt(props
					.getProperty("source.dataSource.initialSize")));
			dataSourceParameters.setSourceMaxIdle(Integer.parseInt(props
					.getProperty("source.dataSource.maxIdle")));
			dataSourceParameters.setSourceMinIdle(Integer.parseInt(props
					.getProperty("source.dataSource.minIdle")));
			dataSourceParameters.setSourceMaxActive(Integer.parseInt(props
					.getProperty("source.dataSource.maxActive")));
			dataSourceParameters.setSourceMaxWait(Integer.parseInt(props
					.getProperty("source.dataSource.maxWait")));
			dataSourceParameters.setSourceDriverClassName(props
					.getProperty("source.jdbc.driverClassName"));
			dataSourceParameters.setSourceUrl(props
					.getProperty("source.jdbc.url"));
			dataSourceParameters.setSourceUser(props
					.getProperty("source.jdbc.username"));
			dataSourceParameters.setSourcePassword(props
					.getProperty("source.jdbc.password"));
			sessionParameter.setSourceSelectSql(props
					.getProperty("source.database.selectSql"));
			sessionParameter.setSourceThreadNum(Integer.parseInt(props
					.getProperty("source.database.threadNum")));
			sessionParameter.setSourceCommand(props
					.getProperty("source.database.sessionCommand"));

			// for target database
			dataSourceParameters.setTargetInitialSize(Integer.parseInt(props
					.getProperty("target.dataSource.initialSize")));
			dataSourceParameters.setTargetMaxIdle(Integer.parseInt(props
					.getProperty("target.dataSource.maxIdle")));
			dataSourceParameters.setTargetMinIdle(Integer.parseInt(props
					.getProperty("target.dataSource.minIdle")));
			dataSourceParameters.setTargetMaxActive(Integer.parseInt(props
					.getProperty("target.dataSource.maxActive")));
			dataSourceParameters.setTargetMaxWait(Integer.parseInt(props
					.getProperty("target.dataSource.maxWait")));
			dataSourceParameters.setTargetDriverClassName(props
					.getProperty("target.jdbc.driverClassName"));
			dataSourceParameters.setTargetUrl(props
					.getProperty("target.jdbc.url"));
			dataSourceParameters.setTargetUser(props
					.getProperty("target.jdbc.username"));
			dataSourceParameters.setTargetPassword(props
					.getProperty("target.jdbc.password"));
			sessionParameter.setTargetInsertSql(props
					.getProperty("target.database.insertSql"));
			sessionParameter.setTargetCommitNum(Integer.parseInt(props
					.getProperty("target.database.commitNum")));
			return true;
		} catch (Exception ex) {
			System.out.println("Load configure parameter error: "
					+ ex.getMessage());
			return false;
		}
	}

}
