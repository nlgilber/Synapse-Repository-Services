package org.sagebionetworks.repo.web.service.dataaccess;

import org.sagebionetworks.repo.manager.AccessApprovalManager;
import org.sagebionetworks.repo.manager.AccessRequirementManager;
import org.sagebionetworks.repo.manager.UserManager;
import org.sagebionetworks.repo.manager.dataaccess.RequestManager;
import org.sagebionetworks.repo.manager.dataaccess.SubmissionManager;
import org.sagebionetworks.repo.manager.dataaccess.ResearchProjectManager;
import org.sagebionetworks.repo.model.RestrictionInformationRequest;
import org.sagebionetworks.repo.model.RestrictionInformationResponse;
import org.sagebionetworks.repo.model.UserInfo;
import org.sagebionetworks.repo.model.dataaccess.RequestInterface;
import org.sagebionetworks.repo.model.dataaccess.Submission;
import org.sagebionetworks.repo.model.dataaccess.SubmissionPage;
import org.sagebionetworks.repo.model.dataaccess.SubmissionPageRequest;
import org.sagebionetworks.repo.model.dataaccess.SubmissionStatus;
import org.sagebionetworks.repo.model.dataaccess.OpenSubmissionPage;
import org.sagebionetworks.repo.model.dataaccess.AccessRequirementStatus;
import org.sagebionetworks.repo.model.dataaccess.BatchAccessApprovalRequest;
import org.sagebionetworks.repo.model.dataaccess.BatchAccessApprovalResult;
import org.sagebionetworks.repo.model.dataaccess.ResearchProject;
import org.sagebionetworks.repo.model.dataaccess.SubmissionStateChangeRequest;
import org.springframework.beans.factory.annotation.Autowired;

public class DataAccessServiceImpl implements DataAccessService{

	@Autowired
	private UserManager userManager;
	@Autowired
	private ResearchProjectManager researchProjectManager;
	@Autowired
	private RequestManager dataAccessRequestManager;
	@Autowired
	private SubmissionManager dataAccessSubmissionManager;
	@Autowired
	private AccessRequirementManager accessRequirementManager;
	@Autowired
	private AccessApprovalManager accessApprovalManager;

	@Override
	public ResearchProject createOrUpdate(Long userId, ResearchProject toCreateOrUpdate) {
		UserInfo user = userManager.getUserInfo(userId);
		return researchProjectManager.createOrUpdate(user, toCreateOrUpdate);
	}

	@Override
	public ResearchProject getUserOwnResearchProjectForUpdate(Long userId, String accessRequirementId) {
		UserInfo user = userManager.getUserInfo(userId);
		return researchProjectManager.getUserOwnResearchProjectForUpdate(user, accessRequirementId);
	}

	@Override
	public RequestInterface createOrUpdate(Long userId, RequestInterface toCreateOrUpdate) {
		UserInfo user = userManager.getUserInfo(userId);
		return dataAccessRequestManager.createOrUpdate(user, toCreateOrUpdate);
	}

	@Override
	public RequestInterface getRequestForUpdate(Long userId, String requirementId) {
		UserInfo user = userManager.getUserInfo(userId);
		return dataAccessRequestManager.getRequestForUpdate(user, requirementId);
	}

	@Override
	public SubmissionStatus submit(Long userId, String requestId, String etag) {
		UserInfo user = userManager.getUserInfo(userId);
		return dataAccessSubmissionManager.create(user, requestId, etag);
	}

	@Override
	public SubmissionStatus cancel(Long userId, String submissionId) {
		UserInfo user = userManager.getUserInfo(userId);
		return dataAccessSubmissionManager.cancel(user, submissionId);
	}

	@Override
	public Submission updateState(Long userId, SubmissionStateChangeRequest request) {
		UserInfo user = userManager.getUserInfo(userId);
		return dataAccessSubmissionManager.updateStatus(user, request);
	}

	@Override
	public SubmissionPage listSubmissions(Long userId, SubmissionPageRequest request) {
		UserInfo user = userManager.getUserInfo(userId);
		return dataAccessSubmissionManager.listSubmission(user, request);
	}

	@Override
	public AccessRequirementStatus getAccessRequirementStatus(Long userId, String requirementId) {
		UserInfo user = userManager.getUserInfo(userId);
		return dataAccessSubmissionManager.getAccessRequirementStatus(user, requirementId);
	}

	@Override
	public RestrictionInformationResponse getRestrictionInformation(Long userId, RestrictionInformationRequest request) {
		UserInfo user = userManager.getUserInfo(userId);
		return accessRequirementManager.getRestrictionInformation(user, request);
	}

	@Override
	public OpenSubmissionPage getOpenSubmissions(Long userId, String nextPageToken) {
		UserInfo user = userManager.getUserInfo(userId);
		return dataAccessSubmissionManager.getOpenSubmissions(user, nextPageToken);
	}

	@Override
	public BatchAccessApprovalResult getAccessApprovalInfo(Long userId, BatchAccessApprovalRequest batchRequest) {
		UserInfo user = userManager.getUserInfo(userId);
		return accessApprovalManager.getApprovalInfo(user, batchRequest);
	}
}