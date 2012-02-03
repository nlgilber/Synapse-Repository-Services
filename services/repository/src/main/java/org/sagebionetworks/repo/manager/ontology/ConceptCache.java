package org.sagebionetworks.repo.manager.ontology;

import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.ontology.ConceptSummary;

public interface ConceptCache {
		
	/**
	 * Get an ordered list of concept summaries for a given key.
	 * @param key
	 * @return
	 */
	public List<ConceptSummary> get(String key);
	
	/**
	 * Set the concept summary list for a given key.
	 * @param key
	 * @param value
	 */
	public void put(String key, List<ConceptSummary> value);
	
	/**
	 * Add all values from the passed map.
	 * @param map
	 */
	public void putAll(Map<String, List<ConceptSummary>> map);
	
	/**
	 * Does the cache contain the given key.
	 * @param key
	 * @return
	 */
	public boolean containsKey(String key);
}
