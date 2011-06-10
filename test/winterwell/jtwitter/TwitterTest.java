package winterwell.jtwitter;


import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

import winterwell.jtwitter.Twitter.IHttpClient;
import winterwell.jtwitter.Twitter.KRequestType;
import winterwell.jtwitter.Twitter.Message;
import winterwell.jtwitter.Twitter.Status;
import winterwell.jtwitter.Twitter.User;
import winterwell.jtwitter.TwitterException.E401;
import winterwell.jtwitter.TwitterException.E403;
import winterwell.jtwitter.TwitterException.SuspendedUser;
import winterwell.utils.Printer;
import winterwell.utils.Utils;
import winterwell.utils.io.XStreamBinaryConverter;

/**
 * Unit tests for JTwitter.
 * These only provide partial testing -- sorry.
 *
 *
 * @author daniel
 */
public class TwitterTest
extends TestCase // Comment out to remove the JUnit dependency
{

	public void testGetListsContaining() {
		Twitter jtwit = newTestTwitter();
		List<TwitterList> lists = jtwit.getListsContaining("patrickharvie", false);
		System.out.println(lists);
	}

	public void testParsing() {
//		String json = "[252059223,19082904,12435562,18881316,72806554,213104665]";
//		JSONArray arr = new JSONArray(json);

		String locn = "ÜT: 37.892943,-122.270439";
		Matcher m = Twitter.latLongLocn.matcher(locn);
		assert m.matches();
		assert m.group(2).equals("37.892943");
		assert m.group(3).equals("-122.270439");
	}

	public void testStopFollowing() {
		Twitter tw = newTestTwitter();
		{
			User bieber = new User("justinbieber");
			tw.follow(bieber);
			tw.stopFollowing(bieber);
		}
		{
			User bieber = new User("charliesheen");
			tw.stopFollowing(bieber);
			User nul = tw.stopFollowing(bieber);
			assert nul == null : nul;
		}
	}

	public void testRateLimits() {
		Twitter tw = newTestTwitter();
		{
			tw.search("stuff");
			Object rateLimit = tw.getRateLimit(KRequestType.SEARCH);
			System.out.println(rateLimit);
		}
		{
			tw.show("winterstein");
			Object rateLimit = tw.getRateLimit(KRequestType.SHOW_USER);
			System.out.println(rateLimit);
		}
		{
			tw.getStatus("winterstein");
			Object rateLimit = tw.getRateLimit(KRequestType.NORMAL);
			System.out.println(rateLimit);
		}
	}

	public void testDeletedUser() {
		Twitter tw = newTestTwitter();
		// NB Once Twitter delete an account, it will 404 (rather than 403)
		try {
			tw.show("radio_kulmbach");
			assert false;
		} catch (TwitterException.SuspendedUser ex) {
			// OK
		} catch (TwitterException.E404 ex) {
			// OK
		}
	}

	public void testNewestFirstSorting() {
		Twitter tw = newTestTwitter();
		List<Status> tweets = tw.getUserTimeline("winterstein");
		Collections.sort(tweets, Twitter.NEWEST_FIRST);
		Date prev=null;
		Printer.out(tweets);
		for (Status status : tweets) {
			assert prev==null || status.getCreatedAt().before(prev) : prev+" vs "+status.getCreatedAt();
			prev = status.getCreatedAt();
		}
	}

	public void testSinceId() {
//		investigating URI uri = new URI("http://api.twitter.com/1/statuses/replies.json?since_id=22090245178&?since_id=22090245178&");
		Twitter tw = newTestTwitter();
		tw.setSinceId(22090245178L);
		tw.setMaxResults(30);
		List<Status> tweets = tw.getUserTimeline();
		assert tweets.size() != 0;
	}

	public void testSuspendedAccounts() throws JSONException {
		Twitter tw = newTestTwitter();
		try {
			tw.show("ykarya35a4wr");
		} catch (SuspendedUser e) {
		}
		List<User> users = tw.bulkShow(Arrays.asList("winterstein", "ykarya35a4wr"));
		assert ! users.isEmpty();
		try {
			tw.isFollowing("ykarya35a4wr");
		} catch (SuspendedUser e) {
		}
		try {
			tw.follow("ykarya35a4wr");
		} catch (SuspendedUser e) {
		}
		try {
			tw.stopFollowing("ykarya35a4wr");
		} catch (SuspendedUser e) {
		}
		try {
			tw.getUserTimeline("ykarya35a4wr");
		} catch (SuspendedUser e) {
		}
	}

	public void testProtectedAccounts() {
		Twitter tw = newTestTwitter();
		try {
			tw.show("acwright");
		} catch (SuspendedUser e) {
			assert false;
		} catch (E403 e) {
		}
		try {
			tw.isFollowing("acwright");
		} catch (SuspendedUser e) {
			assert false;
		} catch (E403 e) {
		}
		try {
			tw.isFollower("acwright", "stephenfry");
		} catch (SuspendedUser e) {
			assert false;
		} catch (E403 e) {
		}
		try {
			tw.getUserTimeline("acwright");
		} catch (SuspendedUser e) {
			assert false;
		} catch (E403 e) {
		} catch (E401 e) {
		}
	}

	public void testJSON() throws JSONException {
		String lng = "10765432100123456789";
//		Long itsLong = 10765432100123456789L;
		BigInteger bi = new BigInteger(lng);
		long bil = bi.longValue();
//		Long itsLong2 = Long.parseLong(lng);
		String s = "{\"id\": 10765432100123456789, \"id_str\": \"10765432100123456789\"}";
//		Map map = (Map) JSON.parse(s);
		JSONObject jo = new JSONObject(s);
//		Object joid = jo.get("id");
//		String ids = jo.getString("id_str");
		assertEquals(""+new BigInteger(lng), jo.getString("id_str"));
	}

	public void testTwitLonger() {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><twitlonger>\n"
	+"<post>\n"
	+"	<id>448efb379cea1098ebe2c1fe453a1cdc</id>\n"
+"		<link>http://www.twitlonger.com/show/448efb379cea1098ebe2c1fe453a1cdc</link>\n"
+"		<short>http://tl.gd/2lc0qb</short>\n"
+"		<content>This is a long test status. Sorry if I ramble. But sometimes 140 characters is just too short.\n"
+"You know what I (cont) http://tl.gd/2lc0qb</content>\n"
+"	</post>\n"
+"</twitlonger>";

		assert Twitter.contentTag.matcher(xml).find();

		Twitter twitter = newTestTwitter();
		twitter.setupTwitlonger("sodash", "MyTwitlongerApiKey"); // FIXME
		Status s = twitter.updateLongStatus(
				"This is a long test status. Sorry if I ramble. But sometimes 140 characters is just too short.\n"
				+"You know what I mean?\n\n"
				+"So thank-you to TwitLonger for providing this service.\n"
				+":)", 0);
		System.out.println(s);
	}

	public void testGetSetFavorite() throws InterruptedException {
		Twitter twitter = newTestTwitter();
		int salt = new Random().nextInt(100);
		Status s = twitter.getStatus("winterstein");
		if (s.isFavorite()) {
			twitter.setFavorite(s, false);
			Thread.sleep(5000);
			assert !s.isFavorite();
		}
		twitter.setFavorite(s, true);
		Thread.sleep(5000);
		Status s2 = twitter.getStatus("winterstein");
		assert s2.isFavorite();
	}



	public void testRepetitionSetStatus() {
		Twitter twitter = newTestTwitter();
		int salt = new Random().nextInt(100);
		Status s1 = twitter.updateStatus("repetitive tweet "+salt);
		try {
			Status s2 = twitter.updateStatus("repetitive tweet "+salt);
			assert false;
		} catch (TwitterException.Repetition e) {
			assert true;
		}
	}

	public void testRepetitionRetweet() {
		Twitter twitter = newTestTwitter();
		Status tweet = twitter.getStatus("winterstein");
		assert tweet != null;
		try {
			Status s = twitter.getStatus();
			Status s1 = twitter.retweet(tweet);
			Status sb = twitter.getStatus();
			Status s2 = twitter.retweet(tweet);
			assert false;
		} catch (TwitterException.Repetition e) {
			assert true;
		}
	}

	public void testRetweetsByMe() {
		Twitter twitter = newTestTwitter();
		Status original = twitter.getStatus("stephenfry");
		Status retweet = twitter.retweet(original);

		List<Status> rtsByMe = twitter.getRetweetsByMe();
//		List<Status> rtsOfMe = source.getRetweetsOfMe();

		assert retweet.inReplyToStatusId == original.id;
		assert retweet.getOriginal().equals(original);
		assert retweet.getText().startsWith("RT @spoonmcguffin: ");
		assert ! rtsByMe.isEmpty();
		assert rtsByMe.contains(retweet);
//		assert ! rtsOfMe.isEmpty();
//		assert rtsOfMe.contains(original);

		// retweeters
//		List<User> retweeters = source.getRetweeters(rtsOfMe.get(0));
//		System.out.println(retweeters);
	}

	public void testMisc() {
		//
	}

	public void testOldSearch() {
		try {
			Twitter twitter = new Twitter();
			twitter.setSinceId(13415168197L);
			List<Status> results = twitter.search("dinosaurs");
		} catch (TwitterException.BadParameter e) {
			String m = e.getMessage();
			boolean old = m.contains("too old");
			assert old;
		}
	}

	static final String TEST_USER = "jtwit";

	public static final String TEST_PASSWORD = "notsofast";

	public static final String[] TEST_ACCESS_TOKEN = new String[]{
		"59714113-kiyrSzrCqsmGLl0RXlEak8rnvzGVtJFc9e8TbxLBU",
		"COBn8690I3JdfivVsr14mphbvOTjIFRmwEUU8Tygi4"
	};

	public static void main(String[] args) {
		TwitterTest tt = new TwitterTest();
		Method[] meths = TwitterTest.class.getMethods();
		for(Method m : meths) {
			if ( ! m.getName().startsWith("test")
					|| m.getParameterTypes().length != 0) continue;
			try {
				m.invoke(tt);
				System.out.println(m.getName());
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				System.out.println("TEST FAILED: "+m.getName());
				System.out.println("\t"+e.getCause());
			}
		}
	}

	public void testSearchUsers() {
		Twitter tw = newTestTwitter();

		List<User> users = tw.searchUsers("Nigel Griffiths");
		System.out.println(users);

		// AND Doesn't work!
		List<User> users2 = tw.searchUsers("Fred near:Scotland");
		assert ! users.isEmpty();
	}

	public void testBulkShow() {
		Twitter tw = newTestTwitter();
		List<User> users = tw.bulkShow(Arrays.asList("winterstein", "joehalliwell", "annettemees"));
		assert users.size() == 3 : users;
		assert users.get(1).description != null;
	}

	public void testBulkShowById() {
		Twitter tw = newTestTwitter();
		List<Long> userIds = Arrays.asList(32L, 34L, 45L, 12435562L);
		List<User> users = tw.bulkShowById(userIds);
		assert users.size() == 2 : users;
	}

	// slow, as we have to wade through a lot of misses
//	public void testBulkShowById2() {
//		Twitter tw = newTestTwitter();
//		List<Long> userIds = new ArrayList<Long>();
//		for(int i=0; i<5000; i++) {
//			userIds.add(12435562L + i);
//		}
//		List<User> users = tw.bulkShowById(userIds);
//		System.out.println(users.size());
//		assert users.size() > 100 : users;
//	}

	/**
	 * Check that you can send 160 chars if you wants
	 */
	public void canSend160() {
		String s = "";
		for(int i=0; i<15; i++) {
			s += i+"23456789 ";
		}
		Twitter tw = newTestTwitter();
		tw.setStatus(s);
	}

	public void testSerialisation() throws IOException {
		Twitter tt = newTestTwitter();
		IHttpClient client = tt.getHttpClient();
		XStreamBinaryConverter conv = new XStreamBinaryConverter();
		{// serialise
			String s = conv.toString(client);
			IHttpClient c2 = (IHttpClient) conv.fromString(s);
		}
		{// serialise
			String s = conv.toString(tt);
			Twitter tt2 = (Twitter) conv.fromString(s);
		}
	}

	/**
	 *  NONDETERMINISTIC! Had to increase sleep time to make it more reliable.
	 * @throws InterruptedException
	 */
	public void testDestroyStatus() throws InterruptedException {
		Twitter tw = newTestTwitter();
		Status s1 = tw.getStatus();
		tw.destroyStatus(s1.getId());
		Status s0 = tw.getStatus();
		assert s0.id != s1.id : "Status id should differ from that of destroyed status";
	}

	public void testDestroyStatusBad() {
		// Check security failure
		Twitter tw = newTestTwitter();
		Status hs = tw.getStatus("winterstein");
		try {
			tw.destroyStatus(hs);
			assert false;
		} catch (Exception ex) {
			// OK
		}
	}

	/**
	 * This tested a bug in {@link OAuthSignpostClient}
	 * @throws InterruptedException
	 */
	public void tstFollowFollow() throws InterruptedException {
		int lag = 2000; //300000;
		OAuthSignpostClient client = new OAuthSignpostClient(
				OAuthSignpostClient.JTWITTER_OAUTH_KEY, OAuthSignpostClient.JTWITTER_OAUTH_SECRET, "oob");
		Twitter tw = new Twitter(TEST_USER, client);
		// open the authorisation page in the user's browser
		client.authorizeDesktop();
		// get the pin
		String v = client.askUser("Please enter the verification PIN from Twitter");
		client.setAuthorizationCode(v);

		User u = tw.follow("winterstein");

		Thread.sleep(lag);

		User u2 = tw.follow("winterstein");
	}

	/**
	 * Test method for {@link winterwell.jtwitter.Twitter#follow(java.lang.String)}.
	 */
	public void testFollowAndStopFollowing() throws InterruptedException {
		int lag = 1000; //300000;
		Twitter tw = newTestTwitter();
		tw.flush();
		List<User> friends = tw.getFriends();
		if ( ! tw.isFollowing("winterstein")) {
			tw.follow("winterstein");
			Thread.sleep(lag);
		}
		assert tw.isFollowing("winterstein") : friends;

		// Stop
		User h = tw.stopFollowing("winterstein");
		assert h != null;
		Thread.sleep(lag);
		assert ! tw.isFollowing("winterstein") : friends;

		// break where no friendship exists
		User h2 = tw.stopFollowing("winterstein");
		assert h2==null;

		// Follow
		tw.follow("winterstein");
		Thread.sleep(lag);
		assert tw.isFollowing("winterstein") : friends;

		try {
			User suspended = tw.follow("Alysha6822");
			assert false : "Trying to follow a suspended user should throw an exception";
		} catch (TwitterException e) {
		}
	}

	public void testIdenticaAccess() throws InterruptedException {
		Twitter jtwit = new Twitter(TEST_USER, TEST_PASSWORD);
		jtwit.setAPIRootUrl("http://identi.ca/api");
		int salt = new Random().nextInt(1000);
		System.out.println(salt);
		Status s1 = null;
		try {
			s1 = jtwit.updateStatus(salt+" Hello to you shiny open source people");
		} catch (TwitterException.Timeout e) {
			// identi.ca has problems
		}
		Thread.sleep(2000);
		Status s2 = jtwit.getStatus();
		assertEquals(s1.toString(), s2.toString());
		assert s1.equals(s2);
	}

	/**
	 * Test method for {@link winterwell.jtwitter.Twitter#getFollowerIDs()}
	 * and {@link winterwell.jtwitter.Twitter#getFollowerIDs(String)}.
	 *
	 */
	public void testFollowerIDs() {
		Twitter tw = newTestTwitter();
		List<Number> ids = tw.getFollowerIDs();
		for (Number id : ids) {
			// Getting a 403 Forbidden error here - not sure what that means
			// user id = 33036740 is causing the problem
			// possibly to do with protected updates?
			try {
				assert tw.isFollower(id.toString(), TEST_USER) : id;
			} catch (E403 e) {
				// this seems to be a corner issue with Twitter's API rather than a bug in JTwitter
				System.out.println(id+" "+e);
			}
		}
		List<Number> ids2 = tw.getFollowerIDs(TEST_USER);
		assert ids.equals(ids2);
	}

	/**
	 * Test the new cursor-based follower/friend methods.
	 */
	public void testManyFollowerIDs() {
		Twitter tw = newTestTwitter();
		tw.setMaxResults(50000);
		List<Number> ids = tw.getFollowerIDs("stephenfry");
		assertTrue(ids.size() >= 50000);
	}

	/**
	 * Test method for {@link winterwell.jtwitter.Twitter#getFriendIDs()}
	 * and {@link winterwell.jtwitter.Twitter#getFriendIDs(String)}.
	 */
	public void testFriendIDs() {
		Twitter tw = newTestTwitter();
		List<Number> ids = tw.getFriendIDs();
		for (Number id : ids) {
			try {
				assert tw.isFollower(TEST_USER, id.toString());
			} catch (E403 e) {
				// ignore
				e.printStackTrace();
			}
		}
		List<Number> ids2 = tw.getFriendIDs(TEST_USER);
		assert ids.equals(ids2);
	}

	/**
	 * Test method for {@link winterwell.jtwitter.Twitter#getDirectMessages()}.
	 */
	public void testGetDirectMessages() {
		// send one to make sure there is one
//		Twitter tw0 = new Twitter("winterstein", "");
//		String salt = Utils.getRandomString(4);
//		String msg = "Hello "+TEST_USER+" "+salt;
//		tw0.sendMessage(TEST_USER, msg);

		Twitter tw = newTestTwitter();
		List<Message> msgs = tw.getDirectMessages();
		for (Message message : msgs) {
			User recipient = message.getRecipient();
			assert recipient.equals(new User(TEST_USER));
		}
		assert msgs.size() != 0;
	}

	/**
	 * Test method for {@link winterwell.jtwitter.Twitter#getDirectMessagesSent()}.
	 */
	public void testGetDirectMessagesSent() {
		Twitter tw = newTestTwitter();
		List<Message> msgs = tw.getDirectMessagesSent();
		for (Message message : msgs) {
			assert message.getSender().equals(new User(TEST_USER));
		}
		assert msgs.size() != 0;
	}

	/**
	 * Test method for {@link winterwell.jtwitter.Twitter#getFeatured()}.
	 */
	public void testGetHomeTimeline() {
		Twitter tw = newTestTwitter();
		List<Status> ts = tw.getHomeTimeline();
		assert ts.size() > 0;
		assert ts.get(0).text != null;
	}

	/**
	 * Test method for {@link winterwell.jtwitter.Twitter#getFeatured()}.
	 */
	public void testGetFeatured() {
		Twitter tw = newTestTwitter();
		List<User> f = tw.getFeatured();
		assert f.size() > 0;
		assert f.get(0).status != null;
	}


	/**
	 * Test method for {@link winterwell.jtwitter.Twitter#getFollowers()}.
	 */
	public void testGetFollowers() {
		Twitter tw = newTestTwitter();
		List<User> f = tw.getFollowers();
		assert f.size() > 0;
		assert Twitter.getUser("winterstein", f) != null;
	}

	/**
	 * Test method for {@link winterwell.jtwitter.Twitter#getFriends()}.
	 */
	public void testGetFriends() {
		Twitter tw = newTestTwitter();
		List<User> friends = tw.getFriends();
		assert friends != null && ! friends.isEmpty();
	}

	public void testGetFriendIDs() {
		{
			Twitter tw = newTestTwitter();
			List<Number> friends = tw.getFriendIDs();
			assert friends != null && ! friends.isEmpty();
		}
		{
			Twitter tw = new Twitter();
			List<Number> friends = tw.getFriendIDs("winterstein");
			assert friends != null && ! friends.isEmpty();
		}
	}
	/**
	 * Test the cursor-based API for getting many followers.
	 * Slightly intermittent
	 */
	public void testGetManyFollowers() {
		Twitter tw = newTestTwitter();
		tw.setMaxResults(10000); // we don't want to run the test for ever.
		String victim = "psychovertical";
		User user = tw.getUser(victim);
		assertFalse("More than 10000 followers; choose a different victim or increase the maximum results",
				user.followersCount > 10000);
		Set<User> followers = new HashSet(tw.getFollowers(victim));
		Set<Long> followerIDs = new HashSet(tw.getFollowerIDs(victim));
		// psychovertical has about 600 followers, as of 14/12/09
		assertEquals(user.followersCount, followers.size());
		assertEquals(user.followersCount, followerIDs.size());
	}

	/**
	 * Test method for {@link winterwell.jtwitter.Twitter#getFriends(java.lang.String)}.
	 */
	public void testGetFriendsString() {
		Twitter tw = newTestTwitter();
		List<User> friends = tw.getFriends("winterstein");
		assert friends != null;
	}
	/**
	 * Test method for {@link winterwell.jtwitter.Twitter#getFriendsTimeline()}.
	 */
	public void testGetFriendsTimeline() {
		Twitter tw = newTestTwitter();
		List<Status> ft = tw.getFriendsTimeline();
		assert ft.size() > 0;
	}

	public void testTooOld() {
		Twitter tw = newTestTwitter();
		try {
			tw.setSinceId(10584958134L);
			tw.setSearchLocation(55.954151,-3.20277,"18km");
			List<Status> tweets = tw.search("stuff");
			assert false;
		} catch (TwitterException.E403 e) {
			String msg = e.getMessage();
		}
	}


	/**
	 * Test method for {@link winterwell.jtwitter.Twitter#getPublicTimeline()}.
	 */
	public void testGetPublicTimeline() {
		Twitter tw = newTestTwitter();
		List<Status> pt = tw.getPublicTimeline();
		assert pt.size() > 5;
	}

	public void testGetRateLimitStats() throws InterruptedException {
		{
			Twitter tw = newTestTwitter();
			int i = tw.getRateLimitStatus();
			if (i<1) return;
			tw.getStatus();
			Thread.sleep(1000);
			int i2 = tw.getRateLimitStatus();
			assert i - 1 == i2;
		}
		{
			Twitter tw = new Twitter();
			int i = tw.getRateLimitStatus();
		}
//		{
//			Twitter twitter = newTestTwitter();
//			while (true)
//			{
//			   int rate = twitter.getRateLimitStatus();
//			   System.out.println(rate+" "+twitter.getHomeTimeline().get(0));
//			}
//		}
	}

	public static Twitter newTestTwitter() {
		OAuthSignpostClient client = new OAuthSignpostClient(
				OAuthSignpostClient.JTWITTER_OAUTH_KEY,
				OAuthSignpostClient.JTWITTER_OAUTH_SECRET,
				TEST_ACCESS_TOKEN[0], TEST_ACCESS_TOKEN[1]);
		return new Twitter(TEST_USER, client);
	}

	/**
	 * Test method for {@link winterwell.jtwitter.Twitter#getMentions()}.
	 */
	public void testGetMentions() {
		{
			Matcher m = Status.AT_YOU_SIR.matcher("@dan hello");
			assert m.find();
			m.group(1).equals("dan");
		}
		//		{	// done in code
		//			Matcher m = Status.atYouSir.matcher("dan@email.com hello");
		//			assert ! m.find();
		//		}
		{
			Matcher m = Status.AT_YOU_SIR.matcher("hello @dan");
			assert m.find();
			m.group(1).equals("dan");
		}

		Twitter tw = newTestTwitter();
		List<Status> r = tw.getMentions();
		for (Status message : r) {
			List<String> ms = message.getMentions();
			assert ms.contains(TEST_USER) : message;
		}
		System.out.println("Replies "+r);
	}

	// Test written to flush out a problem with the paging code
	public void testPaging() {
		Twitter tw = newTestTwitter();
		tw.setMaxResults(100);
		List<Status> stati = tw.getUserTimeline("joehalliwell");
		assert stati.size() > 100 : stati.size();

		// To see the bug we need a status ID that's within
		// maxResults
		BigInteger sinceId = stati.get(50).id;
		tw.setSinceId(sinceId);
		tw.setMaxResults(100);

		// Previously this would hang
		stati = tw.getUserTimeline("joehalliwell");
	}

	public void testAagha() {
		Twitter tw = newTestTwitter();
		Status s = tw.getStatus("aagha");
		assert s != null;
	}

	/**
	 * Test method for {@link winterwell.jtwitter.Twitter#getStatus(int)}.
	 */
	public void testGetStatus() {
		Twitter tw = newTestTwitter();
		Status s = tw.getStatus();
		assert s != null;
		System.out.println(s);

		// source field
		assert s.source.contains("<") : s.source;
		assert ! s.source.contains("&lt;") : s.source;

		//		// test no status
		//		tw = new Twitter(ANOther Account);
		//		s = tw.getStatus();
		//		assert s == null;
	}



	/**
	 * Test method for {@link winterwell.jtwitter.Twitter#getStatus(long)}.
	 */
	public void testGetStatusLong() {
		Twitter tw = newTestTwitter();
		Status s = tw.getStatus();
		Status s2 = tw.getStatus(s.getId());
		assert s.text.equals(s2.text) : "Fetching a status by id should yield correct text";
	}

	/**
	 * Test method for {@link winterwell.jtwitter.Twitter#getUserTimeline()}.
	 */
	public void testGetUserTimeline() {
		Twitter tw = newTestTwitter();
		List<Status> ut = tw.getUserTimeline();
		assert ut.size() > 0;
	}


	/**
	 * Test method for {@link winterwell.jtwitter.Twitter#getUserTimeline(java.lang.String, java.lang.Integer, java.util.Date)}.
	 */
	public void testGetUserTimelineString() {
		Twitter tw = newTestTwitter();
		List<Status> ns = tw.getUserTimeline("anonpoetry");
		System.out.println(ns.get(0));
	}

	public void testTweetEntities() {
		Twitter tw = newTestTwitter();
		tw.setIncludeTweetEntities(true);
		try {
			Status s = tw.setStatus("@jtwit423gg see http://bit.ly/cldEfd #cool :)");
		} catch (Exception e) {
			// TODO: handle exception
		}
		List<Status> statuses = tw.getUserTimeline();

	}

	public void testGetUserTimelineWithRetweets() {
		Twitter tw = newTestTwitter();
		Status ws = tw.getStatus("stephenfry");
		tw.retweet(ws);
		List<Status> ns = tw.getUserTimelineWithRetweets(null);
		System.out.println(ns.get(0));
		Status rt = ns.get(0);
		assert rt != null;
		assert ws.equals(rt.getOriginal()) : rt.getOriginal();
	}


	/**
	 * Test method for {@link winterwell.jtwitter.Twitter#isFollower(String)}
	 * and {@link winterwell.jtwitter.Twitter#isFollower(String, String)}.
	 */
	public void testIsFollower() throws InterruptedException {
		Twitter tw = newTestTwitter();

		assert tw.isFollower("winterstein");
		int LAG = 5000;
		User u = tw.stopFollowing("winterstein");
		Thread.sleep(LAG);
		assert ! tw.isFollowing("winterstein");
		tw.follow("winterstein");
		Thread.sleep(LAG);
		assert tw.isFollowing("winterstein");
	}

	public void testUserFollowingProperty() throws InterruptedException {
		// test the user property
		Twitter tw = newTestTwitter();
		tw.follow("stephenfry");
		User sf = tw.getUser("stephenfry");
		assert sf.isFollowedByYou();
		assert ! sf.isFollowingYou();
		List<User> followers = tw.getFollowers();
		List<User> fBy = tw.getFriends();
		Printer.out(fBy);
	}

	public void testRetweet() {
		Twitter tw = newTestTwitter();
		String[] tweeps = new String[]{
				"winterstein", "joehalliwell", "spoonmcguffin", "forkmcguffin"};
		Status s = tw.getStatus(tweeps[new Random().nextInt(tweeps.length)]);
		Status rt1 = tw.retweet(s);
		assert rt1.text.contains(s.text) : rt1+ " vs "+s;
		Status s2 = tw.getStatus("joehalliwell");
		Status rt2 = tw.updateStatus("RT @"+s2.user.screenName+" "+s2.text);
		assert rt2.text.contains(s2.text) : rt2;

		Status original = rt1.getOriginal();
		assert original != null;
		User user = original.getUser();

		List<User> rters = tw.getRetweeters(s);
		assert rters.contains(new User(TEST_USER)) : rters;

	}

	public void testSearch() {
		{
			Twitter tw = newTestTwitter();
			List<Status> javaTweets = tw.search("java");
			assert javaTweets.size() != 0;
		}
//		{	// long search - This is too complex, gets an exception
//			Twitter tw = newTestTwitter();
//			List<Status> javaTweets = tw.search("santander -banco -de -minutos -en -por -el -hola -buenos -hoy -la -este -esta -nueva");
//			assert javaTweets.size() != 0;
//		}
		{	// few results
			Twitter tw = new Twitter();
			tw.setMaxResults(10);
			List<Status> tweets = tw.search(":)");
			assert tweets.size() == 10;
		}
		{	// Lots of results
			Twitter tw = new Twitter();
			tw.setMaxResults(300);
			List<Status> tweets = tw.search(":)");
			assert tweets.size() > 100 : tweets.size();
		}
	}

	public void testSearchWithLocation() {
		{	// location
			Twitter tw = new Twitter();
			tw.setSearchLocation(51.5, 0, "20km");
			List<Status> tweets = tw.search("the");
			assert tweets.size() > 10 : tweets.size();
		}

	}

	/**
	 * Test method for {@link winterwell.jtwitter.Twitter#sendMessage(java.lang.String, java.lang.String)}.
	 */
	public void testSendMessage() {
		Twitter tw = newTestTwitter();
		Message sent = tw.sendMessage("winterstein", "Please ignore this message");
		System.out.println(""+sent);
	}

	/**
	 * Test method for {@link winterwell.jtwitter.Twitter#show(java.lang.String)}.
	 */
	public void testShow() {
		Twitter tw = new Twitter(); //TEST_USER, TEST_PASSWORD);
		User show = tw.show(TEST_USER);
		assert show != null;
		User show2 = tw.show("winterstein");
		assert show2 != null;

		// a protected user
		User ts = tw.show("tassosstevens");
		assert ts.isProtectedUser() : ts;
	}

	public void testTrends() {
		Twitter tw = newTestTwitter();
		List<String> trends = tw.getTrends();
		Printer.out(trends);
		assert trends.size() > 0;
	}

	/**
	 * Test method for {@link winterwell.jtwitter.Twitter#updateStatus(java.lang.String)}.
	 */
	public void testUpdateStatus() {
		Twitter tw = newTestTwitter();
		String s = "Experimenting (http://winterwell.com at "+new Date().toString()+")";
		Status s2a = tw.updateStatus(s);
		Status s2b = tw.getStatus();
		assert s2b.text.equals(s) : s2b.text;
		assert s2a.id == s2b.id;
		//		assert s2b.source.equals("web") : s2b.source;
	}


	/**
	 * This crashes out at above 140, which is correct
	 * @throws InterruptedException
	 */
	public void testUpdateStatusLength() throws InterruptedException {
		Twitter tw = newTestTwitter();
		Random rnd = new Random();
		{	// WTF?!
			Status s2a = tw.updateStatus("Test tweet aaaa "+rnd.nextInt(1000));
		}
		String salt = new Random().nextInt(1000)+" ";
		Thread.sleep(1000);
		{	// well under
			String s = salt+"help help ";
			for(int i=0; i<2; i++) {
				s += rnd.nextInt(1000);
				s += " ";
			}
			System.out.println(s.length());
			Status s2a = tw.updateStatus(s);
			Status s2b = tw.getStatus();
			assert s2b.text.equals(s.trim()) : s2b.text;
			assert s2a.id == s2b.id;
		}
		{	// 130
			String s = salt;
			for(int i=0; i<12; i++) {
				s += repeat((char) ('a'+i), 9);
				s += " ";
			}
			s = s.trim();
			System.out.println(s.length());
			Status s2a = tw.updateStatus(s);
			Status s2b = tw.getStatus();
			assert s2b.text.equals(s) : s2b.text;
			assert s2a.id == s2b.id;
		}
		{	// 140
			String s = salt;
			for(int i=0; i<13; i++) {
				s += repeat((char) ('a'+i), 9);
				s += " ";
			}
			s = s.trim();
			System.out.println(s.length());
			Status s2a = tw.updateStatus(s);
			Status s2b = tw.getStatus();
			assert s2b.text.equals(s) : s2b.text;
			assert s2a.id == s2b.id;
		}
		// uncomment if you wish to test longer statuses
		if (true) return;
		{	// 150
			String s = salt;
			for(int i=0; i<14; i++) {
				s += repeat((char) ('a'+i), 9);
				s += " ";
			}
			s = s.trim();
			System.out.println(s.length());
			Status s2a = tw.updateStatus(s);
			Status s2b = tw.getStatus();
			assert s2b.text.equals(s) : s2b.text;
			assert s2a.id == s2b.id;
		}
		{	// 160
			String s = salt;
			for(int i=0; i<15; i++) {
				s += repeat((char) ('a'+i), 9);
				s += " ";
			}
			s = s.trim();
			System.out.println(s.length());
			Status s2a = tw.updateStatus(s);
			Status s2b = tw.getStatus();
			assert s2b.text.equals(s) : s2b.text;
			assert s2a.id == s2b.id;
		}
		{	// 170
			String s = salt;
			for(int i=0; i<16; i++) {
				s += repeat((char) ('a'+i), 9);
				s += " ";
			}
			s = s.trim();
			System.out.println(s.length());
			Status s2a = tw.updateStatus(s);
			Status s2b = tw.getStatus();
			assert s2b.text.equals(s) : s2b.text;
			assert s2a.id == s2b.id;
		}

	}


	private String repeat(char c, int i) {
		String s = "";
		for(int j=0; j<i; j++) {
			s += c;
		}
		return s;
	}

	public void testUpdateStatusUnicode() {
		Twitter tw = newTestTwitter();
		{
			String s = "Katten är hemma. Hur mår du? お元気ですか " +new Random().nextInt(1000) ;
			Status s2a = tw.updateStatus(s);
			Status s2b = tw.getStatus();
			assert s2b.text.equals(s) : s2b.text;
			assert s2a.id == s2b.id;
		}
		{
			String s = new Random().nextInt(1000)+" Гладыш Владимир";
			Status s2a = tw.updateStatus(s);
			Status s2b = tw.getStatus();
			assert s2a.text.equals(s) : s2a.text;
			assert s2b.text.equals(s) : s2b.text;
			assert s2a.id == s2b.id;
		}
		{
			Status s2a = tw.updateStatus("123\u0416");
			assert s2a.text.equals("123\u0416") : s2a.getText();
		}
		{
			String s = new Random().nextInt(1000)+" Гладыш Владимир";
			Message s2a = tw.sendMessage("winterstein", s);
			assert s2a.text.equals(s) : s2a.getText();
		}
		{
			Message s2a = tw.sendMessage("winterstein","123\u0416");
			assert s2a.text.equals("123\u0416") : s2a.getText();
		}
	}



	public void testUserExists() {
		Twitter tw = newTestTwitter();
		assert tw.userExists("spoonmcguffin") : "There is a Spoon, honest";
		assert ! tw.userExists("chopstickmcguffin") : "However, there is no Chopstick";
		assert ! tw.userExists("Alysha6822") : "Suspended users show up as nonexistent";
	}


	/**
	 * Created on a day when Twitter's followers API was being particularly flaky,
	 * in order to find out just how bad the lag was.
	 * @author miles
	 * @throws IOException if the output file can't be opened for writing
	 * @throws InterruptedException
	 *
	 */
	public void dontTestFollowLag() throws IOException, InterruptedException {
		Twitter jt = new Twitter(TEST_USER, TEST_PASSWORD);
		String spoon = "spoonmcguffin";
		long timestamp = (new Date()).getTime();
		FileWriter outfile = new FileWriter("twitlag" + timestamp + ".txt");
		for (int i = 0; i < 1000; i++) {
			System.out.println("Starting iteration " + i);
			try {
			if (jt.isFollowing(spoon)) {
				System.out.println("jtwit was following Spoon");
				jt.stopFollowing(spoon);
				int counter = 0;
				while (jt.isFollowing(spoon)) {
					Thread.sleep(1000);
					// jt.stopFollowing(spoon);
					counter++;
				}
				try {
					outfile.write("Stopped following: " + counter + "00ms\n");
				} catch (IOException e) {
					System.out.println("Couldn't write to file: " + e);
				}
			} else {
				System.out.println("jtwit was not following Spoon");
				jt.follow(spoon);
				int counter = 0;
				while (!jt.isFollowing(spoon)) {
					Thread.sleep(1000);
					// jt.follow(spoon);
					counter++;
				}
				try {
					outfile.write("Started following: " + counter + "00ms\n");
				} catch (IOException e) {
					System.out.println("Couldn't write to file: " + e);
				}
			}
			} catch (E403 e) {
				System.out.println("isFollower() was mistaken: " + e);
			}
			outfile.flush();
		}
		outfile.close();
	}

	/**
	 *
	 */
	public void testIsValidLogin() {
		{
			Twitter tw = newTestTwitter();
			assert tw.isValidLogin();
		}
		{
			Twitter tw = newTestTwitter();
			assertTrue(tw.isValidLogin());
		}
		{
			Twitter twitter = new Twitter("rumpelstiltskin", "thisisnotarealpassword");
			assertFalse(twitter.isValidLogin());
		}
	}

	/**
	 * This works fine
	 */
	public void testIdentica() {
		Twitter twitter = new Twitter(TEST_USER, TEST_PASSWORD);
		twitter.setAPIRootUrl("http://identi.ca/api");
		String salt = Utils.getRandomString(4);
		twitter.setStatus("Testing jTwitter http://winterwell.com/software/jtwitter.php "+salt);
		List<Status> timeline = twitter.getFriendsTimeline();
	}

	/**
	 * But this fails with in Date.parse
	 */
	public void testMarakana() {
		Twitter twitter = new Twitter("student", "password");
		twitter.setAPIRootUrl("http://yamba.marakana.com/api");
		String salt = Utils.getRandomString(4);
		twitter.setStatus("Testing jTwitter http://winterwell.com/software/jtwitter.php"+salt);
		List<Status> timeline = twitter.getFriendsTimeline();
	}

}