package com.jsontest;

import java.io.IOException;
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
			String service_url = req.getRequestURL().toString();
			service = service_url.substring(0, service_url.indexOf("."));
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
		 * directly to a String.
		 */
		
		JSONObject response_json = new JSONObject();
		String response_data = null;
		
		try {
			if (service.equals("ip")) {
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
					
					response_json.put(header_name, req.getHeader(header_name));
				}
			}
			else if (service.contains("code")) {
				
			}
			else if (service.contains("date") || service.contains("time")) {
				
			}
			else if (service.contains("echo")) {
				
			}
			else if (service.contains("cookie")) {
				
			}
			else if (service.contains("malform")) {
				
			}
			else if (service.contains("validate")) {
				
			}
			else {
				//None of the above services were a match. Send back an error message.
			}
		}
		catch (JSONException e) {
			System.err.println(e.getMessage());
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
		
		
		
	}
}
