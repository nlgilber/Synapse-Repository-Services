package org.sagebionetworks.repo.model.dbo.persistence;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;


public class DBOMessageToUserTest {
	
	@Test
	public void testMigrateOverrideNotificationSettings() {
		DBOMessageToUser messageToUser = new DBOMessageToUser();
		
		messageToUser.setOverrideNotificationSettings(null);
		
		DBOMessageToUser result = messageToUser.getTranslator().createDatabaseObjectFromBackup(messageToUser);
		
		assertFalse(result.getOverrideNotificationSettings());
		
	}
	
}

