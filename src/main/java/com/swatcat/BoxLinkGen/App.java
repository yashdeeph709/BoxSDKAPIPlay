package com.swatcat.BoxLinkGen;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxCollection.Info;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;
import com.box.sdk.BoxSharedLink;

public class App 
{	
    public static void main( String[] args ) throws Exception
    {
    	int depth=1;
    	BufferedReader br=new BufferedReader(new InputStreamReader(System.in)); 
    	BoxAPIConnection api = new BoxAPIConnection("eQ6pKHi25UlVYpyid7f5vIznbRTqKsOj");
    	BoxFolder folder = new BoxFolder(api, "0");
    	ArrayList<BoxFolder.Info> infoList=new ArrayList<com.box.sdk.BoxFolder.Info>();
    	while(1==1){
	    	System.out.println("Enter a folder number:");
    		for (BoxItem.Info itemInfo : folder) {
	    	    if (itemInfo instanceof BoxFile.Info) {
	    	        BoxFile.Info fileInfo = (BoxFile.Info) itemInfo;
	    	        System.out.printf("File:%s \n",fileInfo.getName());
	    	    } else if (itemInfo instanceof BoxFolder.Info) {
	    	        BoxFolder.Info folderInfo = (BoxFolder.Info) itemInfo;
	    	        System.out.printf(depth+") Folder:%s\n",folderInfo.getName());
	    	        infoList.add(folderInfo);
	    	        depth++;
	    	    }
	    	}    
	    	int folderNumber=Integer.parseInt(br.readLine());
	    	System.out.println("Do you want to dump this folder:(y/n)");
	    	char answer=br.readLine().charAt(0);
	    	if(answer=='y'){
	    		break;
	    	}
	    	if(answer=='n'){
	    		folder = new BoxFolder(api,infoList.get(folderNumber-1).getID());
	    	}
    	}
    	
    }
    public static void printFolder(BoxAPIConnection api,BoxFolder.Info folderInfo){
        System.out.printf("Folder %s \n",folderInfo.getName());
        BoxFolder innerFolder = new BoxFolder(api,folderInfo.getID());
        BoxSharedLink.Permissions permissions = new BoxSharedLink.Permissions();
        permissions.setCanDownload(true);
        permissions.setCanPreview(true);
        
        Iterable<com.box.sdk.BoxItem.Info> items = innerFolder.getChildren();
        for (BoxItem.Info item : items) {
            if (item instanceof BoxFile.Info) {
               BoxFile.Info fileInto = (BoxFile.Info) item;
               BoxFile file = new BoxFile(api, fileInto.getID());
               BoxSharedLink link = file.createSharedLink(BoxSharedLink.Access.OPEN, null, permissions);
               System.out.println(link.getDownloadURL());
            }
            if (item instanceof BoxFolder.Info){
            	BoxFolder.Info folded=(com.box.sdk.BoxFolder.Info) item;
            	printFolder(api,folded);
            }
        }
        
    }
}
