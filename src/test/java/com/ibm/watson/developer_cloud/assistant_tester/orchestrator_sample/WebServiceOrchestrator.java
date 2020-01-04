package com.ibm.watson.developer_cloud.assistant_tester.orchestrator_sample;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Properties;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.watson.developer_cloud.assistant.v1.Assistant;
import com.ibm.watson.developer_cloud.assistant.v1.model.Context;
import com.ibm.watson.developer_cloud.assistant.v1.model.MessageOptions;
import com.ibm.watson.developer_cloud.assistant.v1.model.MessageResponse;
import com.ibm.watson.developer_cloud.assistant_tester.Conversation;
import com.ibm.watson.developer_cloud.assistant_tester.ExampleTest_Parameterized;

/**
 * Simple Watson Assistant orchestrator for working with the eCommerce intents.
 * This is NOT thread-safe.  Thread-safe instances would not contain specific conversation objects.
 * 
 * This is only a simple example.
 */
public class WebServiceOrchestrator {
	
//	private static String USERNAME = System.getProperty("ASSISTANT_USERNAME");
//	private static String PASSWORD = System.getProperty("ASSISTANT_PASSWORD");
	private static String VERSION = "2018-02-16";
//	private static String WORKSPACE_ID = System.getProperty("WORKSPACE_ID");
	private static String TARGET_URL = System.getProperty("TARGET_URL");

	/** Context key: Should user input be sent to Watson or handled internally in the orchestrator? */
	private static final String GO_TO_WATSON_KEY = "goToWatson";

	/** Context key: ID of Member we are talking to */
	private static final String MEMBER_ID_KEY = "memberId";

	/** Context key: What product order are we talking about? */
	private static final String ORDER_ID_KEY = "orderId";
	
	/*
    static {
    	if(USERNAME == null || PASSWORD == null || WORKSPACE_ID == null) {
    		System.err.println("Required environment variables are ASSISTANT_USERNAME, ASSISTANT_PASSWORD, and WORKSPACE_ID");
    		System.exit(-1);
    	}
    }
*/
   // private final IOrderManagement orderService;
    private final Conversation conversation;
    private String lastIntent;
    
    /**
     * Use of dependency injection is key.  Relying on an interface simplifies our unit testing.
     * @param orderService
     */
	public WebServiceOrchestrator(String sessionId) {
	//	Assistant service = new Assistant(VERSION);
	//    service.setUsernameAndPassword(USERNAME, PASSWORD);
		try {
			
			
			Properties prop = new Properties();
			//ClassLoader loader = Thread.currentThread().getContextClassLoader();           
			InputStream stream = WebServiceOrchestrator.class.getClassLoader().getResourceAsStream("orchestrator.properties");
		//	System.out.println(stream);
			prop.load(stream);
		//	System.out.println("uyser " +prop.getProperty("TARGET_URL"));
			TARGET_URL=prop.getProperty("TARGET_URL");

		} catch (java.io.IOException e) {
			e.printStackTrace();//TODO: handle exception
		}
 /*   	if(USERNAME == null || PASSWORD == null || WORKSPACE_ID == null) {
    		System.err.println("Required environment variables are ASSISTANT_USERNAME, ASSISTANT_PASSWORD, and WORKSPACE_ID");
    		System.exit(-1);
    	}
    	*/
	    conversation = new Conversation();
	    conversation.getContext().put(GO_TO_WATSON_KEY, "true");
	    // initialize VGW here
	    
	}
	

	
	public String onInput(String utterance) {

		MessageOptions options = conversation.buildMessage(utterance);
		System.out.println(TARGET_URL);
		String response = this.executePost(TARGET_URL,options.toString());
		// do things with the response and return the text string
		JsonObject jsonObject =  JsonParser.parseString(response).getAsJsonObject();
		// perhaps can simplify this a bit
		JsonObject result = jsonObject.get("result").getAsJsonObject();
		JsonObject context = result.get("context").getAsJsonObject();
		
		JsonObject output= result.get("output").getAsJsonObject();
		System.out.println(output);
		JsonElement texts = output.get("text");
		String text = "";
		if (texts.isJsonArray()) {
			JsonArray array = texts.getAsJsonArray();
			text = array.get(0).getAsString();
		} else {
		    text= output.get("text").getAsString();
			
		}
	
		// get context from json response
				
		Gson gson = new Gson();
		Context waContext = gson.fromJson(context, Context.class); // cool no need to do a string
		conversation.setContext(waContext); // unsure how to do this otherwise	
		
		//System.out.println(text.toString());
		
		return text;
	}
	
	public String executePost(String targetURL, String urlParameters) {
		  HttpURLConnection connection = null;

		  try {
		    //Create connection
		    URL url = new URL(targetURL);
		    connection = (HttpURLConnection) url.openConnection();
		    connection.setRequestMethod("POST");
		    connection.setRequestProperty("Content-Type", 
		        "application/json");

		    connection.setRequestProperty("Content-Length", 
		        Integer.toString(urlParameters.getBytes().length));
		    connection.setRequestProperty("Content-Language", "en-US");  

		    connection.setUseCaches(false);
		    connection.setDoOutput(true);

		    //Send request
		    DataOutputStream wr = new DataOutputStream (
		        connection.getOutputStream());
		    wr.writeBytes(urlParameters);
		    wr.close();

		    //Get Response  
		    InputStream is = connection.getInputStream();
		    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		    StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
		    String line;
		    while ((line = rd.readLine()) != null) {
		      response.append(line);
		      response.append('\r');
		    }
		    rd.close();
		    return response.toString();
		  } catch (Exception e) {
		    e.printStackTrace();
		    return null;
		  } finally {
		    if (connection != null) {
		      connection.disconnect();
		    }
		  }
		}
	/**
	 * Send response to user. (Stub implementation)
	 * @param response
	 */
	private void sendResponse(String response) {
		System.out.println("Sending response: " + response);
		conversation.getContext().put("lastResponse", response);
	}
	
	/* Non-thread-safe implementation */
	public Context getContext() {
		return conversation.getContext();
	}
	
	
	
}
