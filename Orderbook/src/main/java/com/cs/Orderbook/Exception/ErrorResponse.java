package com.cs.Orderbook.Exception;

import java.util.List;

public class ErrorResponse {
	private int status;
	private String message;
	private List<String> details;
	
	public ErrorResponse(int status, String message, List<String> details) {
		super();
		this.status = status;
		this.message = message;
		this.details = details;
	}

}
