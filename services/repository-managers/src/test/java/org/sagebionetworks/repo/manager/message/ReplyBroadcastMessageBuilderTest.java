package org.sagebionetworks.repo.manager.message;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.message.ChangeType;
import org.sagebionetworks.repo.model.subscription.Subscriber;

import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;

public class ReplyBroadcastMessageBuilderTest {
	
	
	DiscussionReplyBundle replyBundle;
	DiscussionThreadBundle threadBundle;
	EntityHeader projectHeader;
	ChangeType changeType;
	String replyUsername;
	Subscriber subscriber;
	
	ReplyBroadcastMessageBuilder builder;
	
	
	@Before
	public void before(){
		replyBundle = new DiscussionReplyBundle();
		threadBundle = new DiscussionThreadBundle();
		threadBundle.setId("333");
		threadBundle.setTitle("thread-title");
		projectHeader = new EntityHeader();
		projectHeader.setId("syn8888");
		projectHeader.setName("projectName");
		changeType = ChangeType.CREATE;
		replyUsername = "reply-username";
		
		subscriber = new Subscriber();
		subscriber.setFirstName("subscriberFirstName");
		subscriber.setLastName("subscriberLastName");
		subscriber.setNotificationEmail("subsciber@domain.org");
		subscriber.setSubscriberId("123");
		subscriber.setUsername("subscriberUsername");
		subscriber.setSubscriptionId("999");
	
		builder = new ReplyBroadcastMessageBuilder(replyBundle, threadBundle, projectHeader, changeType, replyUsername);
	}
	
	@Test
	public void testTruncateStringOver(){
		String input = "123456789";
		String truncate = ReplyBroadcastMessageBuilder.truncateString(input, 4);
		assertEquals("1234...", truncate);
	}

	@Test
	public void testTruncateStringUnder(){
		String input = "123456789";
		String truncate = ReplyBroadcastMessageBuilder.truncateString(input, input.length());
		assertEquals(input, truncate);
	}
	
	@Test
	public void testSubjectCreate(){
		String title = "A-title-that-is-too-long-to-show-so-we-truncate-it-to-a-much-smaller-string";
		String subject = ReplyBroadcastMessageBuilder.buildSubject(title, ChangeType.CREATE);
		assertEquals("Synapse Notification: New reply in thread 'A-title-that-is-too-long-to-show-so-we-truncate-it...'", subject);
	}
	
	@Test
	public void testSubjectUpdate(){
		String title = "A-title";
		String subject = ReplyBroadcastMessageBuilder.buildSubject(title, ChangeType.UPDATE);
		assertEquals("Synapse Notification: Reply updated in thread 'A-title'", subject);
	}
	
	@Test
	public void testSubjectDelete(){
		String title = "A-title";
		String subject = ReplyBroadcastMessageBuilder.buildSubject(title, ChangeType.DELETE);
		assertEquals("Synapse Notification: Reply removed in thread 'A-title'", subject);
	}
	
	@Test
	public void testBuildBody(){
		String body = builder.buildBody(subscriber);
		assertNotNull(body);
		assertTrue(body.contains("subscriberFirstName subscriberLastName (subscriberUsername)"));
		assertTrue(body.contains("reply-username"));
		assertTrue(body.contains(threadBundle.getTitle()));
		assertTrue(body.contains(projectHeader.getName()));
		assertTrue(body.contains("https://www.synapse.org/#!Subscription:subscriptionID=999"));
		assertTrue(body.contains("https://www.synapse.org/#!Synapse:syn8888/discussion/threadId=333"));
		assertTrue(body.contains("https://www.synapse.org/#!Synapse:syn8888"));
		assertTrue(body.contains("replied"));
	}
	
	@Test
	public void testBuildEmailForSubscriber(){
		SendRawEmailRequest request = builder.buildEmailForSubscriber(subscriber);
		assertNotNull(request);
	}

	@Test
	public void testGetAction() {
		assertEquals("replied", ReplyBroadcastMessageBuilder.getAction(ChangeType.CREATE));
		assertEquals("updated a reply", ReplyBroadcastMessageBuilder.getAction(ChangeType.UPDATE));
		assertEquals("removed a reply", ReplyBroadcastMessageBuilder.getAction(ChangeType.DELETE));
	}
}