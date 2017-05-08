package com.swatcat.BoxLinkGen;

public class AuthData {
	private String authData;
	private String refreshToken;
	private boolean refreshed;
	
	public String getAuthData() {
		return authData;
	}
	public void setAuthData(String authData) {
		this.authData = authData;
	}
	public String getRefreshToken() {
		return refreshToken;
	}
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
	public AuthData(String authData, String refreshToken) {
		super();
		this.authData = authData;
		this.refreshToken = refreshToken;
	}
	public synchronized boolean refreshed() {
		return refreshed;
	}
	public synchronized void setRefreshed(boolean state){
		refreshed=state;
	}
}
