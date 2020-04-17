package com.nucleus.cfi.message.vo;

import java.io.Serializable;


public class WhatsAppMessage implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 8533797011388034807L;

	private String from;
	private String to;
	private String body;
	private String mediaUri;

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getMediaUri() {
		return mediaUri;
	}

	public void setMediaUri(String mediaUri) {
		this.mediaUri = mediaUri;
	}


}
