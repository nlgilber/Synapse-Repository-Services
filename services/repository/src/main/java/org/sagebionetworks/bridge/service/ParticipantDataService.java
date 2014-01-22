package org.sagebionetworks.bridge.service;

import java.io.IOException;
import java.util.List;

import org.sagebionetworks.bridge.model.data.ParticipantDataCurrentRow;
import org.sagebionetworks.bridge.model.data.ParticipantDataColumnDescriptor;
import org.sagebionetworks.bridge.model.data.ParticipantDataDescriptor;
import org.sagebionetworks.bridge.model.data.ParticipantDataRow;
import org.sagebionetworks.bridge.model.data.ParticipantDataStatus;
import org.sagebionetworks.repo.model.DatastoreException;
import org.sagebionetworks.repo.model.PaginatedResults;
import org.sagebionetworks.repo.web.NotFoundException;

public interface ParticipantDataService {
	public PaginatedResults<ParticipantDataRow> get(Long userId, String participantDataId, Integer limit, Integer offset)
			throws DatastoreException, NotFoundException, IOException;

	public ParticipantDataRow getRow(Long userId, String participantDataId, Long rowId) throws DatastoreException,
			NotFoundException, IOException;

	public ParticipantDataCurrentRow getCurrent(Long userId, String participantDataId)
			throws DatastoreException, NotFoundException, IOException;

	public List<ParticipantDataRow> append(Long userId, String participantDataId, List<ParticipantDataRow> data) throws DatastoreException,
			NotFoundException, IOException;

	public List<ParticipantDataRow> append(Long userId, String participantId, String participantDataId, List<ParticipantDataRow> data)
			throws DatastoreException, NotFoundException, IOException;

	public List<ParticipantDataRow> update(Long userId, String participantDataId, List<ParticipantDataRow> data) throws DatastoreException,
			NotFoundException, IOException;

	public void updateParticipantStatuses(Long userId, List<ParticipantDataStatus> statuses) throws NotFoundException;

	public ParticipantDataDescriptor createParticipantDataDescriptor(Long userId, ParticipantDataDescriptor participantDataDescriptor)
			throws DatastoreException, NotFoundException;

	public ParticipantDataDescriptor getParticipantDataDescriptor(Long userId, String participantDataId) throws DatastoreException,
			NotFoundException;

	public PaginatedResults<ParticipantDataDescriptor> getAllParticipantDataDescriptors(Long userId, Integer limit, Integer offset)
			throws DatastoreException, NotFoundException;

	public PaginatedResults<ParticipantDataDescriptor> getUserParticipantDataDescriptors(Long userId, Integer limit, Integer offset)
			throws DatastoreException, NotFoundException;

	public ParticipantDataColumnDescriptor createParticipantDataColumnDescriptor(Long userId,
			ParticipantDataColumnDescriptor participantDataColumnDescriptor) throws DatastoreException, NotFoundException;

	public PaginatedResults<ParticipantDataColumnDescriptor> getParticipantDataColumnDescriptors(Long userId, String participantDataId,
			Integer limit, Integer offset) throws DatastoreException, NotFoundException;
}
