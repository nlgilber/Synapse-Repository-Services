package org.sagebionetworks.repo.model.dbo.file;

import static org.sagebionetworks.repo.model.query.jdo.SqlConstants.*;

import java.io.UnsupportedEncodingException;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.sagebionetworks.ids.IdGenerator;
import org.sagebionetworks.ids.IdGenerator.TYPE;
import org.sagebionetworks.repo.model.dbo.DBOBasicDao;
import org.sagebionetworks.repo.model.file.MultipartUploadStatus;
import org.sagebionetworks.repo.model.file.Multipart.State;
import org.sagebionetworks.repo.transactions.WriteTransactionReadCommitted;
import org.sagebionetworks.repo.web.NotFoundException;
import org.sagebionetworks.util.ValidateArgument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class MultipartUploadDAOImpl implements MultipartUploadDAO {

	private static final String SQL_UPDATE_ETAG = "UPDATE "
			+ TABLE_MULTIPART_UPLOAD + " SET " + COL_MULTIPART_UPLOAD_ETAG
			+ " = ? WHERE " + COL_MULTIPART_UPLOAD_ID + " = ?";

	private static final String SQL_SELECT_ADDED_PART_NUMBERS = "SELECT "
			+ COL_MULTIPART_PART_NUMBER + " FROM "
			+ TABLE_MULTIPART_UPLOAD_PART_STATE + " WHERE "
			+ COL_MULTIPART_PART_UPLOAD_ID + " = ? AND "
			+ COL_MULTIPART_PART_MD5_HEX + " IS NOT NULL";

	private static final String SQL_SELECT_PART_MD5S = "SELECT "
			+ COL_MULTIPART_PART_NUMBER + ", " + COL_MULTIPART_PART_MD5_HEX
			+ " FROM " + TABLE_MULTIPART_UPLOAD_PART_STATE + " WHERE "
			+ COL_MULTIPART_PART_UPLOAD_ID + " = ? AND "
			+ COL_MULTIPART_PART_MD5_HEX + " IS NOT NULL ORDER BY "
			+ COL_MULTIPART_PART_NUMBER + " ASC";

	private static final String SQL_SELECT_PART_ERROR = "SELECT "
			+ COL_MULTIPART_PART_NUMBER + ", "
			+ COL_MULTIPART_PART_ERROR_DETAILS + " FROM "
			+ TABLE_MULTIPART_UPLOAD_PART_STATE + " WHERE "
			+ COL_MULTIPART_PART_UPLOAD_ID + " = ? AND "
			+ COL_MULTIPART_PART_ERROR_DETAILS + " IS NOT NULL ORDER BY "
			+ COL_MULTIPART_PART_NUMBER + " ASC";

	private static final String SQL_TRUNCATE_ALL = "DELETE FROM "
			+ TABLE_MULTIPART_UPLOAD + " WHERE " + COL_MULTIPART_UPLOAD_ID
			+ " > -1";

	private static final String STATUS_SELECT = COL_MULTIPART_UPLOAD_ID + ","
			+ COL_MULTIPART_STARTED_BY + "," + COL_MULTIPART_STARTED_ON + ","
			+ COL_MULTIPART_UPDATED_ON + "," + COL_MULTIPART_FILE_HANDLE_ID
			+ "," + COL_MULTIPART_STATE + "," + COL_MULTIPART_UPLOAD_TOKEN
			+ "," + COL_MULTIPART_BUCKET + "," + COL_MULTIPART_KEY + ","
			+ COL_MULTIPART_NUMBER_OF_PARTS+"," +COL_MULTIPART_UPLOAD_ETAG;

	private static final String SELECT_BY_ID = "SELECT " + STATUS_SELECT
			+ " FROM " + TABLE_MULTIPART_UPLOAD + " WHERE "
			+ COL_MULTIPART_UPLOAD_ID + " = ?";

	private static final String SELECT_BY_USER_AND_HASH = "SELECT "
			+ STATUS_SELECT + " FROM " + TABLE_MULTIPART_UPLOAD + " WHERE "
			+ COL_MULTIPART_STARTED_BY + " = ? AND "
			+ COL_MULTIPART_REQUEST_HASH + " = ?";

	@Autowired
	private IdGenerator idGenerator;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private DBOBasicDao basicDao;

	RowMapper<CompositeMultipartUploadStatus> statusMapper = new RowMapper<CompositeMultipartUploadStatus>() {
		@Override
		public CompositeMultipartUploadStatus mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			CompositeMultipartUploadStatus dto = new CompositeMultipartUploadStatus();
			MultipartUploadStatus mus = new MultipartUploadStatus();
			mus.setUploadId(rs.getString(COL_MULTIPART_UPLOAD_ID));
			mus.setStartedBy(rs.getString(COL_MULTIPART_STARTED_BY));
			mus.setStartedOn(rs.getDate(COL_MULTIPART_STARTED_ON));
			mus.setUpdatedOn(rs.getDate(COL_MULTIPART_UPDATED_ON));
			mus.setResultFileHandleId(rs
					.getString(COL_MULTIPART_FILE_HANDLE_ID));
			mus.setState(State.valueOf(rs.getString(COL_MULTIPART_STATE)));
			dto.setMultipartUploadStatus(mus);
			dto.setUploadToken(rs.getString(COL_MULTIPART_UPLOAD_TOKEN));
			dto.setBucket(rs.getString(COL_MULTIPART_BUCKET));
			dto.setKey(rs.getString(COL_MULTIPART_KEY));
			dto.setNumberOfParts((int) rs
					.getLong(COL_MULTIPART_NUMBER_OF_PARTS));
			dto.setEtag(rs.getString(COL_MULTIPART_UPLOAD_ETAG));
			return dto;
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sagebionetworks.repo.model.dbo.file.MultipartUploadDAO#getUploadStatus
	 * (long, java.lang.String)
	 */
	@Override
	public CompositeMultipartUploadStatus getUploadStatus(Long userId,
			String hash) {
		ValidateArgument.required(userId, "UserId");
		ValidateArgument.required(hash, "RequestHash");
		try {
			return this.jdbcTemplate.queryForObject(SELECT_BY_USER_AND_HASH,
					statusMapper, userId, hash);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sagebionetworks.repo.model.dbo.file.MultipartUploadDAO#getUploadStatus
	 * (java.lang.String)
	 */
	@Override
	public CompositeMultipartUploadStatus getUploadStatus(String idString) {
		ValidateArgument.required(idString, "UploadId");
		try {
			long id = Long.parseLong(idString);
			return getUploadStatus(id);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid UploadId.");
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sagebionetworks.repo.model.dbo.file.MultipartUploadDAO#deleteUploadStatus
	 * (long, java.lang.String)
	 */
	@WriteTransactionReadCommitted
	@Override
	public void deleteUploadStatus(long userId, String hash) {
		ValidateArgument.required(userId, "UserId");
		ValidateArgument.required(hash, "RequestHash");
		this.jdbcTemplate.update("DELETE FROM " + TABLE_MULTIPART_UPLOAD
				+ " WHERE " + COL_MULTIPART_STARTED_BY + " = ? AND "
				+ COL_MULTIPART_REQUEST_HASH + " = ?", userId, hash);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sagebionetworks.repo.model.dbo.file.MultipartUploadDAO#createUploadStatus
	 * (long, java.lang.String,
	 * org.sagebionetworks.repo.model.file.MultipartUploadRequest)
	 */
	@WriteTransactionReadCommitted
	@Override
	public CompositeMultipartUploadStatus createUploadStatus(
			CreateMultipartRequest createRequest) {
		ValidateArgument.required(createRequest, "CreateMultipartRequest");
		ValidateArgument.required(createRequest.getUserId(), "UserId");
		ValidateArgument.required(createRequest.getHash(), "RequestHash");
		ValidateArgument
				.required(createRequest.getUploadToken(), "UploadToken");
		ValidateArgument.required(createRequest.getBucket(), "Bucket");
		ValidateArgument.required(createRequest.getKey(), "Key");
		ValidateArgument.required(createRequest.getNumberOfParts(),
				"NumberOfParts");

		DBOMultipartUpload dbo = new DBOMultipartUpload();
		dbo.setId(idGenerator.generateNewId(TYPE.MULTIPART_UPLOAD_ID));
		dbo.setEtag(UUID.randomUUID().toString());
		dbo.setRequestHash(createRequest.getHash());
		dbo.setStartedBy(createRequest.getUserId());
		dbo.setState(State.UPLOADING.name());
		dbo.setRequestBlob(extractBytes(createRequest.getRequestBody()));
		dbo.setStartedOn(new Date(System.currentTimeMillis()));
		dbo.setUpdatedOn(dbo.getStartedOn());
		dbo.setUploadToken(createRequest.getUploadToken());
		dbo.setBucket(createRequest.getBucket());
		dbo.setKey(createRequest.getKey());
		dbo.setNumberOfParts(createRequest.getNumberOfParts());
		basicDao.createNew(dbo);
		return getUploadStatus(dbo.getId());
	}

	private byte[] extractBytes(String requestBody) {
		ValidateArgument.required(requestBody, "RequestBody");
		try {
			return requestBody.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Get the upload status given an upload id.
	 * 
	 * @param id
	 * @return
	 */
	private CompositeMultipartUploadStatus getUploadStatus(long id) {
		try {
			return this.jdbcTemplate.queryForObject(SELECT_BY_ID, statusMapper,
					id);
		} catch (EmptyResultDataAccessException e) {
			throw new NotFoundException(
					"MultipartUploadStatus cannot be found for id: " + id);
		}
	}

	@Override
	public void truncateAll() {
		jdbcTemplate.update(SQL_TRUNCATE_ALL);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sagebionetworks.repo.model.dbo.file.MultipartUploadDAO#getPartsState
	 * (java.lang.String, int)
	 */
	@Override
	public String getPartsState(String uploadId, int numberOfParts) {
		ValidateArgument.required(uploadId, "UploadId");
		validatePartNumber(numberOfParts);
		char[] chars = new char[numberOfParts];
		// start with each part missing
		Arrays.fill(chars, '0');
		List<Integer> addedParts = jdbcTemplate.queryForList(
				SQL_SELECT_ADDED_PART_NUMBERS, Integer.class, uploadId);
		for (Integer partNumber : addedParts) {
			chars[partNumber - 1] = '1';
		}
		return new String(chars);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sagebionetworks.repo.model.dbo.file.MultipartUploadDAO#addPartToUpload
	 * (java.lang.String, int, java.lang.String)
	 */
	@WriteTransactionReadCommitted
	@Override
	public void addPartToUpload(String uploadId, int partNumber,
			String partMD5Hex) {
		ValidateArgument.required(uploadId, "UploadId");
		ValidateArgument.required(partMD5Hex, "PartMD5Hex");
		validatePartNumber(partNumber);
		// update the etag of the master row.
		updateEtag(uploadId);
		// update the part state
		DBOMultipartUploadPartState partState = new DBOMultipartUploadPartState();
		partState.setUploadId(Long.parseLong(uploadId));
		partState.setPartNumber(partNumber);
		partState.setPartMD5Hex(partMD5Hex);
		partState.setErrorDetails(null);
		basicDao.createOrUpdate(partState);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sagebionetworks.repo.model.dbo.file.MultipartUploadDAO#setPartToFailed
	 * (java.lang.String, int, java.lang.String)
	 */
	@WriteTransactionReadCommitted
	@Override
	public void setPartToFailed(String uploadId, int partNumber,
			String errorDetails) {
		ValidateArgument.required(uploadId, "UploadId");
		ValidateArgument.required(errorDetails, "ErrorDetails");
		validatePartNumber(partNumber);
		// update the etag of the master row.
		updateEtag(uploadId);
		// update the part state
		DBOMultipartUploadPartState partState = new DBOMultipartUploadPartState();
		partState.setUploadId(Long.parseLong(uploadId));
		partState.setPartNumber(partNumber);
		partState.setPartMD5Hex(null);
		try {
			partState.setErrorDetails(errorDetails.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		basicDao.createOrUpdate(partState);
	}

	/**
	 * Validate that a part number is within range.
	 * 
	 * @param partNumber
	 */
	private void validatePartNumber(int partNumber) {
		if (partNumber < 1 || partNumber > 10 * 1000) {
			throw new IllegalArgumentException(
					"Part number must be between 1 and 10,000 (inclusive).");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sagebionetworks.repo.model.dbo.file.MultipartUploadDAO#getAddedPartMD5s
	 * (java.lang.String)
	 */
	@Override
	public List<PartMD5> getAddedPartMD5s(final String uploadId) {
		ValidateArgument.required(uploadId, "UploadId");
		return jdbcTemplate.query(SQL_SELECT_PART_MD5S,
				new RowMapper<PartMD5>() {

					@Override
					public PartMD5 mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						int partNumber = rs.getInt(COL_MULTIPART_PART_NUMBER);
						String partMD5Hex = rs
								.getString(COL_MULTIPART_PART_MD5_HEX);
						return new PartMD5(partNumber, partMD5Hex);
					}
				}, uploadId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sagebionetworks.repo.model.dbo.file.MultipartUploadDAO#getPartErrors
	 * (java.lang.String)
	 */
	@Override
	public List<PartErrors> getPartErrors(String uploadId) {
		ValidateArgument.required(uploadId, "UploadId");
		return jdbcTemplate.query(SQL_SELECT_PART_ERROR,
				new RowMapper<PartErrors>() {

					@Override
					public PartErrors mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						int partNumber = rs.getInt(COL_MULTIPART_PART_NUMBER);
						Blob blob = rs
								.getBlob(COL_MULTIPART_PART_ERROR_DETAILS);
						String error;
						try {
							error = new String(blob.getBytes(1,
									(int) blob.length()), "UTF-8");
							return new PartErrors(partNumber, error);
						} catch (UnsupportedEncodingException e) {
							throw new RuntimeException(e);
						}

					}
				}, uploadId);
	}

	/**
	 * Update the etag for the given upload.
	 * @param uploadId
	 */
	private void updateEtag(String uploadId) {
		ValidateArgument.required(uploadId, "UploadId");
		String newEtag = UUID.randomUUID().toString();
		jdbcTemplate.update(SQL_UPDATE_ETAG, newEtag, uploadId);
	}

}