CREATE TABLE IF NOT EXISTS `FORM_DATA` (
  `ID` bigint(20) NOT NULL,
  `ETAG` char(36) NOT NULL,
  `NAME` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `CREATED_BY` bigint(20) NOT NULL,
  `CREATED_ON` TIMESTAMP(3) NOT NULL,
  `MODIFIED_ON` TIMESTAMP(3) NOT NULL,
  `GROUP_ID` bigint(20) NOT NULL,
  `FILE_HANDLE_ID` bigint(20) NOT NULL,
  `SUBMITTED_ON` TIMESTAMP(3) DEFAULT NULL,
  `REVIEWED_ON` TIMESTAMP(3) DEFAULT NULL,
  `REVIEWED_BY` bigint(20) DEFAULT NULL,
  `STATE` ENUM('WAITING_FOR_SUBMISSION', 'SUBMITTED_WAITING_FOR_REVIEW', 'ACCEPTED', 'REJECTED') NOT NULL,
  `REJECTION_MESSAGE` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  PRIMARY KEY (`ID`),
  CONSTRAINT `FORM_GROUP_FK` FOREIGN KEY (`GROUP_ID`) REFERENCES `FORM_GROUP` (`GROUP_ID`) ON DELETE RESTRICT,
  CONSTRAINT `FORM_FILE_FK` FOREIGN KEY (`FILE_HANDLE_ID`) REFERENCES `FILES` (`ID`)  ON DELETE RESTRICT,
  CONSTRAINT `FORM_CREATOR_FK` FOREIGN KEY (`CREATED_BY`) REFERENCES `JDOUSERGROUP` (`ID`) ON DELETE RESTRICT,
  CONSTRAINT `FORM_REVIEWER_FK` FOREIGN KEY (`REVIEWED_BY`) REFERENCES `JDOUSERGROUP` (`ID`) ON DELETE RESTRICT
)
