package org.sagebionetworks.repo.model.gaejdo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;

import org.sagebionetworks.repo.model.AnnotationDAO;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.Dataset;
import org.sagebionetworks.repo.model.DatasetDAO;
import org.sagebionetworks.repo.model.DatastoreException;
import org.sagebionetworks.repo.model.InvalidModelException;
import org.sagebionetworks.repo.model.LayerMetadata;
import org.sagebionetworks.repo.web.NotFoundException;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

/**
 * This is the DAO for the GAEJDO implementation of Dataset.  As such it implements 
 * BaseDAO, RevisableDAO and AnnotatableDAO.  It wraps BaseDAOHelper, RevisableDAOHelper
 * and GAEJDORevisableAnnotationDAOImpl, which provide much of the functionality.
 * 
 * @author bhoff
 *
 */
public class GAEJDODatasetDAOImpl implements DatasetDAO {

	// Question: is this the right spot for this sort of constant? Seems
	// like all revisable DAOs might want to share this contant
	private static final String DEFAULT_VERSION = "0.0.1";

	private GAEJDOBaseDAOHelper<Dataset, GAEJDODataset> baseDAO = null;
	private GAEJDORevisableDAOHelper<Dataset, GAEJDODataset> revisableDAO = null;

	public GAEJDODatasetDAOImpl() {
		final GAEJDODatasetDAOImpl parent = this;
		revisableDAO = new GAEJDORevisableDAOHelper<Dataset, GAEJDODataset>() {
			public Class<GAEJDODataset> getJdoClass() {
				return GAEJDODataset.class;
			}

			public GAEJDODataset newJDO() {
				return parent.newJDO();
			}

			public Dataset newDTO() {
				return parent.newDTO();
			}

			public void copyToDto(GAEJDODataset gae, Dataset dto) {
				parent.copyToDto(gae, dto);
			}

			public void copyFromDto(Dataset dto, GAEJDODataset gae)
					throws InvalidModelException {
				parent.copyFromDto(dto, gae);
			}
		};
		baseDAO = new GAEJDOBaseDAOHelper<Dataset, GAEJDODataset>() {
			public Class<GAEJDODataset> getJdoClass() {
				return GAEJDODataset.class;
			}

			public GAEJDODataset newJDO() {
				return parent.newJDO();
			}

			public Dataset newDTO() {
				return parent.newDTO();
			}

			public void copyToDto(GAEJDODataset gae, Dataset dto) {
				parent.copyToDto(gae, dto);
			}

			public void copyFromDto(Dataset dto, GAEJDODataset gae)
					throws InvalidModelException {
				parent.copyFromDto(dto, gae);
			}
		};
	}

	public Dataset newDTO() {
		Dataset dto = new Dataset();
		return dto;
	}

	public GAEJDODataset newJDO() {
		GAEJDODataset jdo = new GAEJDODataset();
		GAEJDOAnnotations a = GAEJDOAnnotations.newGAEJDOAnnotations();
		jdo.setAnnotations(a);
		GAEJDORevision<GAEJDODataset> r = new GAEJDORevision<GAEJDODataset>();
		jdo.setRevision(r);
		return jdo;
	}

	public void copyToDto(GAEJDODataset gae, Dataset dto) {
		dto.setId(KeyFactory.keyToString(gae.getId()));
		dto.setName(gae.getName());
		dto.setDescription(gae.getDescription());
		dto.setCreator(gae.getCreator());
		dto.setCreationDate(gae.getCreationDate());
		dto.setStatus(gae.getStatus());
		dto.setReleaseDate(gae.getReleaseDate());
		dto.setVersion(gae.getRevision().getVersion().toString());
		Collection<LayerMetadata> layers = new ArrayList<LayerMetadata>();
		Collection<Key> layerKeys = gae.getLayers();
		if (null != layerKeys) {
			for (Key l : layerKeys) {
				layers.add(new LayerMetadata(KeyFactory.keyToString(l)));
			}
		}
		dto.setLayers(layers);
	}

	/**
	 * 
	 * Note: This method does NOT copy layers or revision info to the GAEJDO
	 * object,
	 * those being done by the 'revise' method

	 * @param dto
	 * @param gae
	 * @throws InvalidModelException
	 */
	public void copyFromDto(Dataset dto, GAEJDODataset gae)
			throws InvalidModelException {

		//
		// Confirm that the DTO is valid by checking that all required fields
		// are set
		//
		// Question: is this where we want this sort of logic?
		// Dev Note: right now the only required field is name but I can imagine
		// that the
		// validation logic will become more complex over time
		if (null == dto.getName()) {
			throw new InvalidModelException(
					"'name' is a required property for Dataset");
		}
		gae.setName(dto.getName());
		gae.setDescription(dto.getDescription());
		gae.setCreator(dto.getCreator());
		gae.setCreationDate(dto.getCreationDate());
		gae.setStatus(dto.getStatus());
		gae.setReleaseDate(dto.getReleaseDate());
	}

	public Collection<String> getPrimaryFields() {
		return Arrays.asList(new String[] { "name", "description", "creator",
				"status", "releaseDate", "version" });
	}

	public List<Dataset> getInRange(int start, int end) throws DatastoreException {
		return revisableDAO.getInRange(start, end);
	}

	public List<Dataset> getInRangeSortedByPrimaryField(int start, int end,
			String sortBy, boolean asc) throws DatastoreException {
		return revisableDAO.getInRangeSortedByPrimaryField(start, end, sortBy,
				asc);
	}

	public List<Dataset> getInRangeHavingPrimaryField(int start, int end,
			String attribute, Object value) throws DatastoreException {
		return revisableDAO.getInRangeHavingPrimaryField(start, end, attribute,
				value);
	}

	/**
	 * @param dataset
	 *            an original (not revised) dataset
	 * @return the id of the newly created dataset
	 * @throws DatastoreException
	 * @throws InvalidModelException
	 */
	public String create(Dataset dataset) throws DatastoreException,
			InvalidModelException {
		PersistenceManager pm = PMF.get();
		Transaction tx = null;
		try {
			tx = pm.currentTransaction();
			tx.begin();

			//
			// Set system-controlled immutable fields
			//
			// Question: is this where we want to be setting immutable
			// system-controlled fields for our
			// objects? This should only be set at creation time so its not
			// appropriate to put it in copyFromDTO.
			dataset.setCreationDate(new Date()); // now

			//
			// Set default values for optional fields that have defaults
			//
			// Question: is this where we want to specify reasonable default
			// values?
			if (null == dataset.getVersion()) {
				dataset.setVersion(DEFAULT_VERSION);
			}

			GAEJDODataset jdo = revisableDAO.create(pm, dataset);
			tx.commit();
			copyToDto(jdo, dataset);
			return KeyFactory.keyToString(jdo.getId());
		} catch (InvalidModelException e) {
			throw e;
		} catch (Exception e) {
			throw new DatastoreException(e);
		} finally {
			if (tx.isActive()) {
				tx.rollback();
			}
			pm.close();
		}
	}

	public Dataset get(String id) throws DatastoreException, NotFoundException {
		return baseDAO.get(id);
	}

	public void delete(String id) throws DatastoreException, NotFoundException {
		baseDAO.delete(id);

	}

	/**
	 * This updates the 'shallow' properties. Neither Version nor deep
	 * properties change.
	 * 
	 * @param dto
	 *            non-null id is required
	 * @throws DatastoreException
	 * @throws InvalidModelException 
	 * @throws NotFoundException 
	 */
	public void update(Dataset dto) throws DatastoreException, InvalidModelException, NotFoundException {
		PersistenceManager pm = PMF.get();
		Transaction tx = null;
		try {
			tx = pm.currentTransaction();
			tx.begin();
			revisableDAO.update(pm, dto);
			tx.commit();
		} catch (InvalidModelException e) {
			throw e;
		} catch (JDOObjectNotFoundException e) {
			throw new NotFoundException(e);
		} catch (Exception e) {
			throw new DatastoreException(e);
		} finally {
			if (tx.isActive()) {
				tx.rollback();
			}
			pm.close();
		}
	}

	/**
	 * Create a revision of the object specified by the 'id' and 'version'
	 * fields, having the shallow properties from the given 'revision', and the
	 * deep properties of the given 'version'. The new revision will have the
	 * version given by the 'newVersion' parameter.
	 * 
	 * @param revision
	 * @param newVersion
	 * @param revisionDate
	 */
	public String revise(Dataset revision, Date revisionDate)
			throws DatastoreException {
		PersistenceManager pm = PMF.get();
		Transaction tx = null;
		try {
			tx = pm.currentTransaction();
			tx.begin();
			GAEJDODataset newRevision = revisableDAO.revise(pm, revision,
					revisionDate);
			// now copy the 'deep' properties
			Key reviseeId = KeyFactory.stringToKey(revision.getId());
			GAEJDODataset revisee = (GAEJDODataset) pm.getObjectId(reviseeId);
			GAEJDOAnnotations a = GAEJDOAnnotations.clone(revisee
					.getAnnotations());
			newRevision.setAnnotations(a);
			newRevision.setLayers(new HashSet<Key>(revisee.getLayers()));
			pm.makePersistent(newRevision); // don't know if this is necessary
			tx.commit();
			return KeyFactory.keyToString(newRevision.getId());
		} catch (Exception e) {
			throw new DatastoreException(e);
		} finally {
			if (tx.isActive()) {
				tx.rollback();
			}
			pm.close();
		}
	}

	public int getCount() throws DatastoreException {
		PersistenceManager pm = PMF.get();
		try {
			int count = revisableDAO.getCount(pm);
			return count;
		} catch (Exception e) {
			throw new DatastoreException(e);
		} finally {
			pm.close();
		}
	}

	/**
	 * 
	 * @param id
	 *            the id of any revision of the object
	 * @return the latest version of the object
	 * @throws DatastoreException
	 *             if no result
	 */
	public Dataset getLatest(String id) throws DatastoreException {
		PersistenceManager pm = PMF.get();
		try {
			Dataset latest = revisableDAO.getLatest(pm, id);
			return latest;
		} catch (Exception e) {
			throw new DatastoreException(e);
		} finally {
			pm.close();
		}
	}

	/**
	 * Get all versions of an object
	 * 
	 * @param id
	 * @return all revisions of the given object
	 */
	public Collection<Dataset> getAllVersions(String id)
			throws DatastoreException {
		PersistenceManager pm = PMF.get();
		try {
			Collection<Dataset> allVersions = revisableDAO.getAllVersions(pm,
					id);
			return allVersions;
		} catch (Exception e) {
			throw new DatastoreException(e);
		} finally {
			pm.close();
		}
	}

	/**
	 * Deletes all revisions of a Dataset
	 * 
	 * @param id
	 *            the id of any version of a revision series
	 * @throws DatastoreException
	 */
	public void deleteAllVersions(String id) throws DatastoreException {
		PersistenceManager pm = PMF.get();
		Transaction tx = null;
		try {
			tx = pm.currentTransaction();
			tx.begin();
			Key key = KeyFactory.stringToKey(id);
			Collection<GAEJDODataset> allVersions = revisableDAO
					.getAllVersions(pm, key);
			for (GAEJDODataset jdo : allVersions) {
				for (Key layerKey : jdo.getLayers()) {
					// may have to check whether it's a InputDataLayer or
					// AnalysisResult
					GAEJDODatasetLayer layer = (GAEJDODatasetLayer) pm
							.getObjectById(GAEJDODatasetLayer.class, layerKey);
					pm.deletePersistent(layer);
				}
				pm.deletePersistent(jdo);
			}
			tx.commit();
		} catch (Exception e) {
			throw new DatastoreException(e);
		} finally {
			if (tx.isActive()) {
				tx.rollback();
			}
			pm.close();
		}
	}

	/**
	 * @param id
	 * @return annotations for the given object of the given type
	 * @throws NotFoundException 
	 */
	public Annotations getAnnotations(String id) throws DatastoreException, NotFoundException {
		Annotations ans = new Annotations();
		ans.setStringAnnotations(getStringAnnotationDAO().getAnnotations(id));
		ans.setFloatAnnotations(getFloatAnnotationDAO().getAnnotations(id));
		ans.setDateAnnotations(getDateAnnotationDAO().getAnnotations(id));
		return ans;
	}

	public AnnotationDAO<Dataset, String> getStringAnnotationDAO() {
		final GAEJDODatasetDAOImpl parent = this;
		return new GAEJDORevisableAnnotationDAOImpl<Dataset, GAEJDODataset, String>() {
			protected Class<? extends GAEJDOAnnotation<String>> getAnnotationClass() {
				return GAEJDOStringAnnotation.class;
			}

			protected Class<String> getValueClass() {
				return String.class;
			}

			protected String getCollectionName() {
				return "stringAnnotations";
			}

			public Dataset newDTO() {
				return parent.newDTO();
			}

			public GAEJDODataset newJDO() {
				return parent.newJDO();
			}

			public void copyToDto(GAEJDODataset jdo, Dataset dto) {
				parent.copyToDto(jdo, dto);
			}

			public void copyFromDto(Dataset dto, GAEJDODataset jdo)
					throws InvalidModelException {
				parent.copyFromDto(dto, jdo);
			}

			protected Class<GAEJDODataset> getOwnerClass() {
				return GAEJDODataset.class;
			}

			protected void addAnnotation(GAEJDOAnnotations annots,
					String attribute, String value) {
				annots.add(attribute, value);
			}

			protected void removeAnnotation(GAEJDOAnnotations annots,
					String attribute, String value) {
				annots.remove(attribute, value);
			}

			protected Iterable<GAEJDOAnnotation<String>> getIterable(
					GAEJDOAnnotations annots) {
				return annots.getStringIterable();
			}
		};
	}

	public AnnotationDAO<Dataset, Float> getFloatAnnotationDAO() {
		final GAEJDODatasetDAOImpl parent = this;
		return new GAEJDORevisableAnnotationDAOImpl<Dataset, GAEJDODataset, Float>() {

			protected Class<? extends GAEJDOAnnotation<Float>> getAnnotationClass() {
				return GAEJDOFloatAnnotation.class;
			}

			protected Class<Float> getValueClass() {
				return Float.class;
			}

			protected String getCollectionName() {
				return "floatAnnotations";
			}

			public Dataset newDTO() {
				return parent.newDTO();
			}

			public GAEJDODataset newJDO() {
				return parent.newJDO();
			}

			public void copyToDto(GAEJDODataset jdo, Dataset dto) {
				parent.copyToDto(jdo, dto);
			}

			public void copyFromDto(Dataset dto, GAEJDODataset jdo)
					throws InvalidModelException {
				parent.copyFromDto(dto, jdo);
			}

			protected Class<GAEJDODataset> getOwnerClass() {
				return GAEJDODataset.class;
			}

			protected void addAnnotation(GAEJDOAnnotations annots,
					String attribute, Float value) {
				annots.add(attribute, value);
			}

			protected void removeAnnotation(GAEJDOAnnotations annots,
					String attribute, Float value) {
				annots.remove(attribute, value);
			}

			protected Iterable<GAEJDOAnnotation<Float>> getIterable(
					GAEJDOAnnotations annots) {
				return annots.getFloatIterable();
			}
		};
	}

	public AnnotationDAO<Dataset, Date> getDateAnnotationDAO() {
		final GAEJDODatasetDAOImpl parent = this;
		return new GAEJDORevisableAnnotationDAOImpl<Dataset, GAEJDODataset, Date>() {
			protected Class<? extends GAEJDOAnnotation<Date>> getAnnotationClass() {
				return GAEJDODateAnnotation.class;
			}

			protected Class<Date> getValueClass() {
				return Date.class;
			}

			protected String getCollectionName() {
				return "dateAnnotations";
			}

			public Dataset newDTO() {
				return parent.newDTO();
			}

			public GAEJDODataset newJDO() {
				return parent.newJDO();
			}

			public void copyToDto(GAEJDODataset jdo, Dataset dto) {
				parent.copyToDto(jdo, dto);
			}

			public void copyFromDto(Dataset dto, GAEJDODataset jdo)
					throws InvalidModelException {
				parent.copyFromDto(dto, jdo);
			}

			protected Class<GAEJDODataset> getOwnerClass() {
				return GAEJDODataset.class;
			}

			protected void addAnnotation(GAEJDOAnnotations annots,
					String attribute, Date value) {
				annots.add(attribute, value);
			}

			protected void removeAnnotation(GAEJDOAnnotations annots,
					String attribute, Date value) {
				annots.remove(attribute, value);
			}

			protected Iterable<GAEJDOAnnotation<Date>> getIterable(
					GAEJDOAnnotations annots) {
				return annots.getDateIterable();
			}
		};
	}

	/**
	 * 
	 * @param datasetId
	 *            the ID of the dataset to which the layer is to be added
	 * @param layer the layer to add, including the 'shallow' values
	 * @return the id of the created layer
	 */
	public String createLayer(String datasetId, LayerMetadata layer) throws DatastoreException, InvalidModelException {
		throw new RuntimeException("Not yet implemented");
	}

	/**
	 * 
	 * @param datasetId
	 *            the ID of the dataset of interest
	 * @return IDs of all the layers in the dataset
	 */
	public Collection<String> getLayers(String datasetId) throws DatastoreException {
		throw new RuntimeException("Not yet implemented");
	}
	
	/**
	 * 
	 * @param id id for the layer of interest
	 * @return the DTO for the layer
	 */
	public LayerMetadata getLayer(String id) {
		throw new RuntimeException("Not yet implemented");
	} 
	
	/**
	 * 
	 * @param layer the layer to update (id field required)
	 */
	public void updateLayer(LayerMetadata layer) throws DatastoreException, InvalidModelException {
		throw new RuntimeException("Not yet implemented");
	}

	
	/**
	 * 
	 * @param layer the layer to remove (id field required)
	 */
	public void removeLayer(LayerMetadata layer) throws DatastoreException {
		throw new RuntimeException("Not yet implemented");
	}

}
