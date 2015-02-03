package org.sagebionetworks.repo.manager;

import org.sagebionetworks.repo.model.Challenge;
import org.sagebionetworks.repo.model.ChallengePagedResults;
import org.sagebionetworks.repo.model.ChallengeTeam;
import org.sagebionetworks.repo.model.ChallengeTeamPagedResults;
import org.sagebionetworks.repo.model.DatastoreException;
import org.sagebionetworks.repo.model.PaginatedIds;
import org.sagebionetworks.repo.model.SubmissionTeamPagedResults;
import org.sagebionetworks.repo.model.UserInfo;
import org.sagebionetworks.repo.web.NotFoundException;

public interface ChallengeManager {
	
	/**
	 * 
	 * @param userInfo
	 * @param challenge
	 * @return
	 * @throws DatastoreException
	 * @throws NotFoundException
	 */
	public Challenge createChallenge(UserInfo userInfo, Challenge challenge) throws DatastoreException, NotFoundException;

	/**
	 * 
	 * @param userInfo
	 * @param projectId
	 * @return
	 * @throws DatastoreException
	 * @throws NotFoundException
	 */
	public Challenge getChallengeByProjectId(UserInfo userInfo, String projectId) throws DatastoreException, NotFoundException;
	
	/**
	 * 
	 * @param userInfo
	 * @param participantId
	 * @return
	 * @throws DatastoreException
	 */
	public ChallengePagedResults listChallengesForParticipant(UserInfo userInfo, String participantId) throws DatastoreException;

	/**
	 * 
	 * @param userInfo
	 * @param challenge
	 * @return
	 * @throws DatastoreException
	 * @throws NotFoundException
	 */
	public Challenge updateChallenge(UserInfo userInfo, Challenge challenge) throws DatastoreException, NotFoundException;
	
	/**
	 * 
	 * @param userInfo
	 * @param challengeId
	 * @throws DatastoreException
	 */
	public void deleteChallenge(UserInfo userInfo, long challengeId) throws DatastoreException;
	
	/**
	 * 
	 * @param userInfo
	 * @param challengeId
	 * @param affiliated
	 * @param limit
	 * @param offset
	 * @return
	 * @throws DatastoreException
	 * @throws NotFoundException
	 */
	public PaginatedIds listParticipantsInChallenge(UserInfo userInfo, long challengeId, Boolean affiliated, long limit, long offset) throws DatastoreException, NotFoundException;

	/**
	 * 
	 * @param userInfo
	 * @param challengeTeam
	 * @return
	 * @throws DatastoreException
	 */
	public ChallengeTeam createChallengeTeam(UserInfo userInfo, ChallengeTeam challengeTeam) throws DatastoreException;
	
	/**
	 * 
	 * @param userInfo
	 * @param challengeId
	 * @param limit
	 * @param offset
	 * @return
	 * @throws DatastoreException
	 * @throws NotFoundException
	 */
	public ChallengeTeamPagedResults listChallengeTeams(UserInfo userInfo, long challengeId, long limit, long offset) throws DatastoreException, NotFoundException;

	/**
	 * 
	 * @param userInfo
	 * @param challengeId
	 * @param limit
	 * @param offset
	 * @return
	 * @throws DatastoreException
	 * @throws NotFoundException
	 */
	public PaginatedIds listRegistratableTeams(UserInfo userInfo, long challengeId, long limit, long offset) throws DatastoreException, NotFoundException;

	/**
	 * 
	 * @param userInfo
	 * @param challengeTeam
	 * @return
	 * @throws DatastoreException
	 * @throws NotFoundException
	 */
	public ChallengeTeam updateChallengeTeam(UserInfo userInfo, ChallengeTeam challengeTeam) throws DatastoreException, NotFoundException;
	
	/**
	 * 
	 * @param userInfo
	 * @param challengeTeamId
	 * @throws DatastoreException
	 */
	public void deleteChallengeTeam(UserInfo userInfo, long challengeTeamId) throws DatastoreException;
	
	/**
	 * Returns a list of Teams either (1) on whose behalf the user is eligible to submit or (2) on 
	 * whose behalf the user WOULD be eligible to submit if the Team has been registered for the 
	 * Challenge, and which the User CAN register for the Challenge.
	 * @param userInfo
	 * @param challengeId
	 * @param submitterPrincipalId
	 * @param limit
	 * @param offset
	 * @return
	 * @throws DatastoreException
	 * @throws NotFoundException
	 */
	SubmissionTeamPagedResults listSubmissionTeams(UserInfo userInfo, long challengeId, long submitterPrincipalId, long limit, long offset) throws DatastoreException, NotFoundException;
}
