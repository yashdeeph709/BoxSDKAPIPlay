package com.swatcat.BoxLinkGen;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;
import com.box.sdk.BoxSharedLink;
import com.box.sdk.PartialCollection;

public class BoxThread implements Callable<Long> {
	private BoxAPIConnection api;
	private BufferedWriter bw;
	private FileWriter fw;
	private String folderId;
	private long offset;
	private long limit;
	private String fileName;
	private Logger logger;
	private boolean reuse;

	public BoxThread(String folderId, BoxAPIConnection api, String name) throws IOException {
		super();
		this.api = api;
		this.fileName = name;
		if((new File(name)).exists()){
			reuse=true;
		}
		this.fw = new FileWriter(fileName,true);
		this.bw = new BufferedWriter(fw);
		this.folderId = folderId;
		logger=Logger.getLogger(BoxThread.class.getName());
	}

	public long getStartRange() {
		return offset;
	}

	public void setStartRange(long l) {
		this.offset = l;
	}
	
	public long getLimit() {
		return limit;
	}

	public void setLimit(long limit) {
		this.limit = limit;
	}
	
	public Long call() throws IOException {
		if(!reuse){
			fw.write("Name,Link\n");
		}
		BoxFolder innerFolder = new BoxFolder(api, folderId);
		BoxSharedLink.Permissions permissions = new BoxSharedLink.Permissions();
		permissions.setCanDownload(true);
		permissions.setCanPreview(true);
		PartialCollection<BoxItem.Info> pc = innerFolder.getChildrenRange(offset, limit, "name", "shared_link");
		logger.info("Got "+pc.size()+" Files for offset:"+pc.offset()+" limit:"+pc.limit());
		String line = "";
		for (BoxItem.Info item : pc) {
			if (item instanceof BoxFile.Info) {
				BoxFile.Info fileInto = (BoxFile.Info) item;
				BoxFile file = new BoxFile(api, fileInto.getID());
				BoxSharedLink link = file.createSharedLink(BoxSharedLink.Access.DEFAULT, null, permissions);
				line += item.getName() + ",";
				line += link.getDownloadURL() + "\n";
				if (line.length() > 500) {
					this.bw.write(line);
					line="";
				}
			}
		}
		this.bw.write(line);
		this.bw.flush();
		this.fw.close();
		this.bw.close();				
		return (long) pc.size();
	}

}
