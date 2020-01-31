CREATE TABLE IF NOT EXISTS `JDOUSERPROFILE` (
  `OWNER_ID` BIGINT NOT NULL,
  `ETAG` char(36) NOT NULL,
  `PROPERTIES` mediumblob,
  `PICTURE_ID` BIGINT,
  `SEND_EMAIL_NOTIFICATION` Boolean,
  `FIRST_NAME` tinyblob,
  `LAST_NAME` tinyblob,
  PRIMARY KEY (`OWNER_ID`),
  CONSTRAINT `PRINCIPAL_OWNER_FK` FOREIGN KEY (`OWNER_ID`) REFERENCES `JDOUSERGROUP` (`ID`) ON DELETE CASCADE,
  CONSTRAINT `PROFILE_PIC_FH_FK` FOREIGN KEY (`PICTURE_ID`) REFERENCES `FILES` (`ID`) ON DELETE SET NULL
)
