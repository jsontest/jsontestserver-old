package com.jsontest;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;

import javax.servlet.http.*;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("serial")
public class JsontestserverServlet extends HttpServlet {
	
	//We pipe any POST requests straight to the doGet handler; no special handling is required.
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		doGet(req, resp);
	}
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		
		//JSONP applications use the callback parameter to define where to send 
		//the JSON data to.
		String callback = req.getParameter("callback");
		
		/**
		 * The service parameter defines which service the client is requesting; i.e. 
		 * whether to respond with IP information, date information, etc. If this parameter 
		 * is null, then the client is sending us service information using the 
		 * servicename.jsontest.com method. We handle that by looking at the request URL 
		 * the client sent us.
		 */
		String service = req.getParameter("service");
		if (service == null) {
			System.out.println("No service defined, reading service from request URL.");
			service = req.getRequestURL().toString();
			service.toLowerCase();
		}
		else {
			service.toLowerCase();
		}
		
		System.out.println("Service is set to: " + service);
		
		/**
		 * Now find which service is being requested, and handle it. 
		 * 
		 * Most services return JSON, so they can write directly to 
		 * the JSONObject response_json. However, some services need 
		 * to directly set the response, so they can write their responses 
		 * directly to the String response_data.
		 */
		
		JSONObject response_json = new JSONObject();
		String response_data = "";
		
		try {
			if (service.contains("ip")) {
				//Send back the user's IP address.
				response_json.put("ip", req.getRemoteAddr());
			}
			else if (service.contains("header")) {
				//Send back all the headers that the client sent us.
				Enumeration<String> headers = req.getHeaderNames();
				while (headers.hasMoreElements()) {
					String header_name = headers.nextElement();
					
					/**
					 * We host on Google App Engine, which sets some special headers 
					 * that we shouldn't be returning to the client. If the header is 
					 * included by GAE, skip it.
					 */
					if ((header_name.toLowerCase()).startsWith("x-appengine")) {
						break;
					}
					else if ((header_name.toLowerCase()).startsWith("x-zoo")) {
						break;
					}
					else if ((header_name.toLowerCase()).startsWith("x-google")) {
						break;
					}
					
					response_json.put(header_name, req.getHeader(header_name));
				}
			}
			else if (service.contains("code")) {
				//Arbitrary JS Code
				String ip = req.getRemoteAddr();
				
				response_data = "alert(\"Your IP: " + ip + "\");";
				
				//If there is a JSONP callback, include a function.
				if (callback != null) {
					response_json.put("ip", ip);
					response_data += callback + "(" + response_json.toString(3) + ");";
				}
				
			}
			else if (service.contains("date") || service.contains("time")) {
				//Date and time
				Date current_date = new Date();
				String date = new java.text.SimpleDateFormat("MM-dd-yyyy").format(current_date);
				String time = new java.text.SimpleDateFormat("hh:mm:ss aa").format(current_date);
				response_json.put("date", date);
				response_json.put("time", time);
				response_json.put("milliseconds", current_date.getTime());
			}
			else if (service.contains("echo")) {
				//Echo back a JSON object based on the URI.
				String request_uri = req.getRequestURI().substring(1);
				String[] components = request_uri.split("/");
				
				for (int i = 0; i < components.length; i++) {
					String key = components[i];
					String value = "";
					try {
						value = components[i + 1];
					}
					catch (ArrayIndexOutOfBoundsException e) {
						//If this exception is thrown, that means there are an odd number of tokens
						//in the request url (in other terms, there is a key value specified, but no 
						//value). It's OK, because we'll just put a blank string into the value component.
					}
					response_json.put(key, value);
					i++;
				}
			}
			else if (service.contains("cookie")) {
				//Set a cookie.
				String millis = ((Long)(new Date()).getTime()).toString();
				Cookie cookie = new Cookie("jsontestdotcom", millis);
				cookie.setMaxAge(2 * 7 * 24 * 60 * 60);//Two weeks to expire, in seconds.
				resp.addCookie(cookie);
				
				response_json.put("cookie", "Cookie added with name jsontestdotcom and value " + millis);
			}
			else if (service.contains("malform")) {
				
			}
			else if (service.contains("validate")) {
				
			}
			else {
				//None of the above services were a match. Send back an error message.
				response_json.put("error", "You did not specify a valid JSONTest.com service.");
				response_json.put("info", "Visit jsontest.com for a list of services available.");
				response_json.put("url", "jsontest.com");
				response_json.put("version", "1.0 Production");
			}
		}
		catch (JSONException e) {
			//JSONObject.put throws a JSONException if the key is null. That shouldn't 
			//happen, but we still have to catch the exception.
			System.err.println("JSONException.put() may have thrown an error. " + e.getMessage());
		}
		
		
		/**
		 * Set up MIME type.
		 * 
		 * The MIME type is set via the parameter mime, where it 
		 * can be set to a integer corresponding to a particular 
		 * mime type. If this parameter is not set, then we set the 
		 * MIME type according to the response: application/json 
		 * for JSON responses, application/javascript for 
		 * JSONP responses.
		 */
		String mime_param = req.getParameter("mime");
		String content_type = "text/plain";
		if ("1".equals(mime_param)) {
			content_type = "application/json";
		}
		else if ("2".equals(mime_param)) {
			content_type = "application/javascript";
		}
		else if ("3".equals(mime_param)) {
			content_type = "text/javascript";
		}
		else if ("4".equals(mime_param)) {
			content_type = "text/html";
		}
		else if ("5".equals(mime_param)) {
			content_type = "text/plain";
		}
		else {
			//The user didn't set up a requested MIME type. We'll set it for them.
			if (callback == null) {
				content_type = "application/json";
			}
			else {
				content_type = "application/javascript";
			}
		}
		
		//And finally set the content type.
		resp.setContentType(content_type);
		
		
		/**
		 * Set up the Access-Control-Allow-Origin header. 
		 * 
		 * The user may shut this header off, but it is required for 
		 * most web apps to be able to use external services.
		 */
		if ("false".equals(req.getParameter("alloworigin"))) {
			//Do nothing, the user does not want this header.
			//Make a note in logging, since this is a very unusual request.
			System.out.println("Access-Control-Allow-Origin header turned off. User's web application may not work.");
		}
		else {
			resp.setHeader("Access-Control-Allow-Origin", "*");
		}
		
		/**
		 * If the String response_data is blank, that means we need to get 
		 * the response_json JSONObject, convert it to a String, and send it back to 
		 * the user. If it isn't blank, then the service above had to directly set 
		 * the response content and we can send back response_data as it is.
		 */
		if (response_data.equals("")) {
			
			try {
				response_data = response_json.toString(3);
			}
			catch (JSONException e) {
				System.err.println("Unable to indent response JSON. " + e.getMessage());
				response_data = response_json.toString();
			}
			
			//If the user requested a callback, we'll wrap the callback here
			if (callback != null) {
				response_data = callback + "(" + response_data + ");";
			}
		}
		
		//And finally send it off to the user.
		resp.getWriter().println(response_data);
		

		
	}//end doGet
}//end file
