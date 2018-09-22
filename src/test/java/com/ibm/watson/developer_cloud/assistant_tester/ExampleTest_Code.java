package com.ibm.watson.developer_cloud.assistant_tester;

import static com.ibm.watson.developer_cloud.assistant_tester.util.Assert.assertContains;
import static com.ibm.watson.developer_cloud.assistant_tester.util.Assert.assertContext;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.watson.developer_cloud.assistant.v1.Assistant;
import com.ibm.watson.developer_cloud.assistant.v1.model.MessageResponse;

public class ExampleTest_Code {

	private Conversation conversation = null;
	
	private static String USERNAME = System.getProperty("ASSISTANT_USERNAME");
	private static String PASSWORD = System.getProperty("ASSISTANT_PASSWORD");
	private static String VERSION = "2018-02-16";
	private static String WORKSPACE_ID = System.getProperty("WORKSPACE_ID");
	
    @BeforeClass
    public static void checkEnvironment() {
    	if(USERNAME == null || PASSWORD == null || WORKSPACE_ID == null) {
    		System.err.println("Required environment variables are ASSISTANT_USERNAME, ASSISTANT_PASSWORD, and WORKSPACE_ID");
    		System.exit(-1);
    	}
    }
	
	@Before
	public void setup() {
	    Assistant service = new Assistant(VERSION);
	    service.setUsernameAndPassword(USERNAME, PASSWORD);

	    conversation = new Conversation(service, WORKSPACE_ID);
	}
	
	@After
	public void teardown() {
		conversation.reset();
	}
	
	@Test
	public void one_turn() {
		MessageResponse response = null;

		//turn 1, goes to node: anything_else
	    response = conversation.turn("Can I text you a question?");
	    assertContains("turn 1 state text", response, "I didn't understand");
	}
	
	@Test
	public void two_turns_no_outside_context() {
		MessageResponse response = null;

		//turn 1, goes to node Schedule appointment
		response = conversation.turn("Can someone support me at home?");
	    assertContext("turn 1 state var 1", response, "ask_for_contact", "false");
	    
	    //turn 2, within appointment goes to "contact us"
	    response = conversation.turn("Can I text you a question?");
	    assertContext("turn 2 state var 1", response, "ask_for_contact", "true");
	    assertContext("turn 2 state var 2", response, "appointment_request", "true");
	    assertContains("turn 2 state text", response, "Contact me via email");
	}
	
	@Test
	public void two_turns_with_outside_context() {
		MessageResponse response = null;

		conversation.getContext().put("goldMember", "true");
		
		//turn 1, goes to node Schedule appointment
		response = conversation.turn("Can someone support me at home?");
	    assertContext("turn 1 state var 1", response, "ask_for_contact", "false");
	    
	    //turn 2, within appointment goes to Gold Member "contact us"
	    response = conversation.turn("Can I text you a question?");
	    assertContains("turn 2 state text", response, "No, we'll call you!");
	}
}
