package com.ibm.watson.developer_cloud.assistant_tester;

import static com.ibm.watson.developer_cloud.assistant_tester.util.Assert.assertContains;
import static com.ibm.watson.developer_cloud.assistant_tester.util.Assert.assertContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.io.InputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.watson.developer_cloud.assistant.v1.Assistant;
import com.ibm.watson.developer_cloud.assistant.v1.model.MessageResponse;
import com.ibm.watson.developer_cloud.assistant_tester.etl.ConversationTestLoader;
import com.ibm.watson.developer_cloud.assistant_tester.orchestrator_sample.WebServiceOrchestrator;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
 
@RunWith(Parameterized.class)
public class OrchestratorParameterizedTest {

	private Conversation conversation = null;
	private WebServiceOrchestrator ucg = null;
	private static String TARGET_URL = System.getProperty(" TARGET_URL");
	
    @Parameters(name="{0}")
    public static Collection<Object[]> data() {
    	ConversationTestLoader ctl = new ConversationTestLoader();
    	List<ConversationTest> tests = ctl.read("simple-assistant-test-cases.csv");
    	List<Object[]> ret = new ArrayList<Object[]>();
    	for(ConversationTest test : tests) {
    		ret.add(new Object[]{test});
    	}
        return ret;
    }
    
    @BeforeClass
    public static void checkEnvironment() {
		try {
			
		
			Properties prop = new Properties();
			//ClassLoader loader = Thread.currentThread().getContextClassLoader();           
			InputStream stream = OrchestratorParameterizedTest.class.getClassLoader().getResourceAsStream("ucg.properties");
			System.out.println(stream);
			prop.load(stream);
			TARGET_URL= prop.getProperty("TARGET_URL");

		} catch (java.io.IOException e) {
			e.printStackTrace();//TODO: handle exception
		}
		if(TARGET_URL == null) {
    		System.err.println("Required environment variables are TARGET_URL");
    		System.exit(-1);
    	}
    }
	
	@Before
	public void setup() {
	    ucg = new WebServiceOrchestrator("foo");
	    //service.setUsernameAndPassword(USERNAME, PASSWORD);
	    conversation = new Conversation();
	}
	
	@After
	public void teardown() {
		conversation.reset();
	}
	
	private final ConversationTest test;
	public OrchestratorParameterizedTest(ConversationTest test) {
		this.test = test;
	}
	
	@Test
	public void conversation_test() {
		System.out.println(">>>>> " + test.getName());
		
		if(test.getInitialContext() != null && test.getInitialContext().length > 0) {
			for(int i = 0; i < test.getInitialContext().length; i += 2) {
				conversation.getContext().put(test.getInitialContext()[i], test.getInitialContext()[i+1]);
			}
		}
		
		
		String response = null;
		int turnCounter = 0;
		for(Turn t : test.getTurns()) {
			turnCounter++;
			response = ucg.onInput(t.getUtterance());

			if(t.getExpectedOutput().length() > 0) {
				assertContains("turn " + turnCounter + " text", response, t.getExpectedOutput());
			}
			if(t.getContext() != null && t.getContext().length > 0) {
				for(int i = 0; i < t.getContext().length; i += 2) {
					assertContext("turn " + turnCounter + " state " + t.getContext()[i], 
							conversation.getContext(), t.getContext()[i], t.getContext()[i+1]);
				}
			}
		}
		
	}
}
