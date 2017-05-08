package com.swatcat.BoxLinkGen;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;

public class BoxBrowser {
	static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	static BoxAPIConnection api;
	static int LIMIT;
	static BoxSession boxsession;
	static Logger logger;
	static int poolSize;
    static FileInputStream fis;
    static Properties prop;

	public static void main(String[] args) throws Exception {
		try{
	    	fis = new FileInputStream(args[0]);
	    	prop = new Properties();
	    	prop.load(fis);
		}catch(FileNotFoundException fnf){
			System.out.println("The config File you are pointing doesn't exist");
		}
		LIMIT = Integer.parseInt(prop.getProperty("LIMIT"));
		poolSize = Integer.parseInt(prop.getProperty("POOLSIZE"));

		BoxSession session=new BoxSession(prop.getProperty("APP_KEY"),prop.getProperty("APP_SECRET"),prop.getProperty("PORT"));
		session.authenticate();
		logger=Logger.getLogger(BoxBrowser.class.getName());	
		boxsession=session;
		api = new BoxAPIConnection(session.getAPP_KEY(), session.getAPP_SECRET(), session.getAccess_token(),session.getRefresh_token());
		br = new BufferedReader(new InputStreamReader(System.in));
		String id = "0";
		while (true) {
			id = selectFolder(api, id);
			if(id.equals("-1")){
				break;
			}
		}
	}

	public static String selectFolder(BoxAPIConnection api, String id) throws Exception {
		BoxFolder folder = new BoxFolder(api, id);
		ArrayList<String> infoList = new ArrayList<String>();
		System.out.printf("(%s)> \n", folder.getInfo().getName());
		int folderNumber = 0;
		for (BoxItem.Info itemInfo : folder) {
			if (itemInfo instanceof BoxFile.Info) {
				BoxFile.Info fileInfo = (BoxFile.Info) itemInfo;
				System.out.printf("File : %s \n", fileInfo.getName());
			} else if (itemInfo instanceof BoxFolder.Info) {
				BoxFolder.Info folderInfo = (BoxFolder.Info) itemInfo;
				System.out.printf((++folderNumber) + ") Folder : %s\n", folderInfo.getName());
				infoList.add(folderInfo.getID());
			} else {
				System.out.println("Not File/Folder:" + itemInfo.getName());
			}
		}
		System.out.println("-1) Go back ");
		System.out.println("0) Exit");
		int selection = Integer.parseInt(br.readLine());
		if (selection == 0) {
			System.exit(0);
		}
		if (selection == -1) {
			return folder.getInfo().getParent().getID();
		}
		if (selection <= folderNumber && selection > 0) {
			System.out.println("Do you want to dump this folder:(y)");
			if (br.readLine().charAt(0) == 'y') {
				dumpManager(folder, infoList.get(selection - 1));
				return "-1";
			}
		} else if (selection > infoList.size()) {
			System.out.println("Errorneos Selection: Please choose a valid selection next time");
			return id;
		}
		return infoList.get(selection - 1);
	}

	public static void dumpManager(BoxFolder folder, String id) throws IOException, InterruptedException, ExecutionException {
		long startRange=0;
		ExecutorService executor=Executors.newFixedThreadPool(poolSize);
		int threadNumber=1,poolRuns=0;
		ArrayList<Future<Long>> futures=new ArrayList<Future<Long>>();
		while(true){
			BoxThread thread=new BoxThread(id, api,"DUMP_"+threadNumber);
			thread.setStartRange(startRange);
			thread.setLimit(LIMIT);
			startRange+=LIMIT;
			futures.add(executor.submit(thread));
			if(threadNumber==poolSize){
				threadNumber=0;
				poolRuns++;
				if(futures.get(futures.size()-1).get()<LIMIT){
					logger.info("Last Page Completed"+startRange);
					break;
				}
			}
			threadNumber++;
		}
		System.out.println("Start The Result Gathering");
		for(int i=0;i<futures.size();i++){
			System.out.println("Thread "+(i+1)+" "+futures.get(i).get());
		}
		executor.shutdown();
  	}
}
