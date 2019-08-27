package org.sagebionetworks.repo.model.annotation.v2;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.EntityFactory;
import org.sagebionetworks.util.ValidateArgument;

public class AnnotationsV2Utils {

	/**
	 *
	 * @return first value in the AnnotationV2 for the given key if it exists. null otherwise
	 */
	public static String getSingleValue(AnnotationsV2 annotationsV2, String key){
		//TODO: test
		ValidateArgument.required(annotationsV2, "annotationsV2");
		ValidateArgument.required(key, "key");
		Map<String, AnnotationsV2Value> map = annotationsV2.getAnnotations();
		if(map == null){
			return null;
		}

		return getSingleValue(map.get(key));
	}

	public static String getSingleValue(AnnotationsV2Value value){
		//TODO: test
		if(value == null){
			return null;
		}

		List<String> stringValues = value.getValue();
		if(stringValues == null || stringValues.isEmpty()){
			return null;
		}
		return stringValues.get(0);
	}

	/**
	 * Serializes ONLY the annotations, ignoring ID and Etag.
	 * @param annotationsV2
	 * @return JSON String of annotationsV2 if any annotations exist. Null otherwise
	 * @throws JSONObjectAdapterException
	 */
	public static String toJSONStringForStorage(AnnotationsV2 annotationsV2) throws JSONObjectAdapterException {
		if(annotationsV2 == null || annotationsV2.getAnnotations() == null || annotationsV2.getAnnotations().isEmpty()){
			return null;
		}

		//use a shallow copy which does not contain the id and etag info
		AnnotationsV2 annotationsV2ShallowCopy = new AnnotationsV2();
		annotationsV2ShallowCopy.setAnnotations(annotationsV2.getAnnotations());

		return EntityFactory.createJSONStringForEntity(annotationsV2ShallowCopy);
	}

}
