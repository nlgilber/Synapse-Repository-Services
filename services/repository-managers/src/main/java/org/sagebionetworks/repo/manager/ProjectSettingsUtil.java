package org.sagebionetworks.repo.manager;

import org.sagebionetworks.repo.model.file.UploadType;
import org.sagebionetworks.repo.model.project.ExternalUploadDestinationSetting;
import org.sagebionetworks.repo.model.project.ProjectSetting;
import org.sagebionetworks.repo.model.project.S3UploadDestinationSetting;
import org.sagebionetworks.repo.model.project.UploadDestinationListSetting;
import org.sagebionetworks.repo.model.project.UploadDestinationSetting;

import java.util.List;

public class ProjectSettingsUtil {
	
	public static void validateProjectSetting(ProjectSetting setting) {
		if (setting == null) {
			throw new IllegalArgumentException("Setting cannot be null");
		}
		if (setting instanceof UploadDestinationListSetting) {
			ProjectSettingsUtil.validateUploadDestinationListSetting((UploadDestinationListSetting)setting);
		}
	}
	
	public static void validateUploadDestinationListSetting(UploadDestinationListSetting setting) {
		if (setting == null) {
			throw new IllegalArgumentException("Setting cannot be null.");
		}
		if (setting.getDestinations() == null) {
			throw new IllegalArgumentException("setting.getDestinations() cannot be null.");
		}
		List<UploadDestinationSetting> destinations = setting.getDestinations();
		for (UploadDestinationSetting dest: destinations) {
			validateUploadDestinationSetting(dest);
		}
	}
	
	public static void validateUploadDestinationSetting(UploadDestinationSetting setting) {
		if (setting == null) {
			throw new IllegalArgumentException("Setting cannot be null.");
		}
		if ((setting.getUploadType() == null)) {
			throw new IllegalArgumentException("Setting.getUploadType() cannot be null.");
		}
		
		if (setting instanceof ExternalUploadDestinationSetting) {
			if (((ExternalUploadDestinationSetting) setting).getUrl() == null) {
				throw new IllegalArgumentException("setting.getUrl() cannot be null.");
			}
			if (setting.getUploadType() == UploadType.S3) {
				throw new IllegalArgumentException("UploadType.S3 invalid for ExternalUploadDestinationSetting.");
			}
			if ((setting.getUploadType() == UploadType.HTTPS) && 
					(!((ExternalUploadDestinationSetting) setting).getUrl().startsWith("https:"))) {
				throw new IllegalArgumentException("setting.getUrl() must start with 'http' if setting.getUploadType() is UploadType.HTTP.");
			}
			if ((setting.getUploadType() == UploadType.SFTP) && 
					(!((ExternalUploadDestinationSetting) setting).getUrl().startsWith("sftp"))) {
				throw new IllegalArgumentException("setting.getUrl() must start with 'sftp' if setting.getUploadType() is UploadType.SFTP.");
			}
		}
		if ((setting instanceof S3UploadDestinationSetting) &&
				(setting.getUploadType() != UploadType.S3)) {
			throw new IllegalArgumentException("setting.getUploadType() must be UploadType.S3 if type is S3UploadDestinationSetting.");
		}
	}

}
