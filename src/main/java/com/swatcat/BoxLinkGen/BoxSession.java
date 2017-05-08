package com.swatcat.BoxLinkGen;

import com.box.boxjavalibv2.BoxClient;
import com.box.boxjavalibv2.dao.*;
import com.box.boxjavalibv2.exceptions.AuthFatalFailureException;
import com.box.boxjavalibv2.exceptions.BoxServerException;
import com.box.boxjavalibv2.requests.requestobjects.BoxOAuthRequestObject;
import com.box.restclientv2.exceptions.BoxRestException;
import java.util.Properties;
import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class BoxSession {

    private BoxClient client;

    public String APP_KEY ;
    public String APP_SECRET ;
    public String URL ;
    public int PORT ;
    public String access_token;
	public String refresh_token;

    public String getAPP_KEY() {
		return APP_KEY;
	}

	public void setAPP_KEY(String aPP_KEY) {
		APP_KEY = aPP_KEY;
	}

	public String getAPP_SECRET() {
		return APP_SECRET;
	}

	public void setAPP_SECRET(String aPP_SECRET) {
		APP_SECRET = aPP_SECRET;
	}

	public String getURL() {
		return URL;
	}

	public void setURL(String uRL) {
		URL = uRL;
	}

	public int getPORT() {
		return PORT;
	}

	public void setPORT(int pORT) {
		PORT = pORT;
	}

	public String getAccess_token() {
		return access_token;
	}

	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}

	public String getRefresh_token() {
		return refresh_token;
	}

	public void setRefresh_token(String refresh_token) {
		this.refresh_token = refresh_token;
	}


    public BoxSession(String APP_KEY,String APP_SECRET,String PORT) {
	    	this.APP_KEY = APP_KEY;
	    	this.APP_SECRET = APP_SECRET;
	    	this.URL = "https://www.box.com/api/oauth2/authorize?response_type=code&client_id=" + APP_KEY;
	    	this.PORT = Integer.parseInt(PORT);
    }

    public String authenticate(){

        String code = "";

        try {
            Desktop.getDesktop().browse(java.net.URI.create(URL));
            code = getCode();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            client = getAuthenticatedClient(code);
        } catch( BoxRestException e ) {
        	e.printStackTrace();
        } catch( BoxServerException e ) {
        	e.printStackTrace();
        } catch( AuthFatalFailureException e ) {
        	e.printStackTrace();
        }

        System.out.println("We are authenticated");
        
        try {
            refresh_token = client.getAuthData().getRefreshToken();
        } catch( AuthFatalFailureException e ) {
        	e.printStackTrace();
        }

        return refresh_token;
    }


    private BoxClient getAuthenticatedClient(String code) throws BoxRestException,
            BoxServerException, AuthFatalFailureException {
        BoxClient client = new BoxClient(APP_KEY, APP_SECRET);
        BoxOAuthRequestObject obj = BoxOAuthRequestObject.createOAuthRequestObject(
                code, APP_KEY, APP_SECRET, "http://localhost:" + PORT);
        BoxOAuthToken bt =  client.getOAuthManager().createOAuth(obj);
        access_token=bt.getAccessToken();
        refresh_token=bt.getRefreshToken();
        client.authenticate(bt);
        return client;
    }

    private String getCode() throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        Socket socket = serverSocket.accept();
        System.out.println("Request Received ");
        BufferedReader in = new BufferedReader (new InputStreamReader(socket.getInputStream ()));
        while (true)
        {
            String code = "";
            try
            {
                code = in.readLine ();
                System.out.println (code);
                String match = "code";
                int loc = code.indexOf(match);

                if( loc > 0 ) {
                    int httpstr = code.indexOf("HTTP") - 1;
                    code = code.substring(code.indexOf(match), httpstr);
                    String parts[] = code.split("=");
                    code=parts[1];
                }

                return code;
            }
            catch (IOException e) {
                //error ("System: " + "Connection to server lost!");
                System.exit (1);
                break;
            }
        }
        return "";
    }



}