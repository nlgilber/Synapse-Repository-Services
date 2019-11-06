CREATE TABLE IF NOT EXISTS `DOWNLOAD_LIST` (
  `PRINCIPAL_ID` bigint(20) NOT NULL,
  `UPDATED_ON` bigint(20) NOT NULL,
  `ETAG` char(36) NOT NULL,
  PRIMARY KEY (`PRINCIPAL_ID`),
  CONSTRAINT `DOWNLOAD_PRI_FK` FOREIGN KEY (`PRINCIPAL_ID`) REFERENCES `JDOUSERGROUP` (`ID`) ON DELETE CASCADE
)
