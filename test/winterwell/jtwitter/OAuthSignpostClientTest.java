package winterwell.jtwitter;

import java.util.List;

import junit.framework.TestCase;

public class OAuthSignpostClientTest 
extends TestCase
{

	public void testSimple() {
		OAuthSignpostClient client = new OAuthSignpostClient(OAuthSignpostClient.JTWITTER_OAUTH_KEY, OAuthScribeClient.JTWITTER_OAUTH_SECRET, "oob");
		Twitter jtwit = new Twitter(TwitterTest.TEST_USER, client);
		// open the authorisation page in the user's browser
		client.authorizeDesktop();
		// get the pin
		String v = OAuthSignpostClient.askUser("Please enter the verification PIN from Twitter");
		client.setAuthorizationCode(v);	
		// use the API!
		// This works
		assert jtwit.isValidLogin();
		// This works
		List<Message> dms = jtwit.getDirectMessages();
		// This fails with a 401?!
		jtwit.setStatus("Using OAuth to tweet");
	}
	
	public void testRecreatingScribe() {
		OAuthSignpostClient client = new OAuthSignpostClient(OAuthSignpostClient.JTWITTER_OAUTH_KEY, OAuthSignpostClient.JTWITTER_OAUTH_SECRET, "oob");
		Twitter jtwit = new Twitter(TwitterTest.TEST_USER, client);
		// open the authorisation page in the user's browser
		client.authorizeDesktop();
		// get the pin
		String v = OAuthSignpostClient.askUser("Please enter the verification PIN from Twitter");
		client.setAuthorizationCode(v);		
		String[] token = client.getAccessToken();
		
		// remake client
		OAuthSignpostClient client2 = new OAuthSignpostClient(OAuthSignpostClient.JTWITTER_OAUTH_KEY, OAuthSignpostClient.JTWITTER_OAUTH_SECRET,
											token[0], token[1]);		
		Twitter jtwit2 = new Twitter(TwitterTest.TEST_USER, client2);
		
		// use the API!
		assert jtwit2.isValidLogin();
		jtwit2.setStatus("Using reconstituted OAuth to tweet");
	}
	
}
