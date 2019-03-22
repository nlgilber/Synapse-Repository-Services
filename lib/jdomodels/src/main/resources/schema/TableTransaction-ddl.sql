CREATE TABLE IF NOT EXISTS `TABLE_TRANSACTION` (
  `TRX_ID` bigint(20) NOT NULL,
  `TABLE_ID` bigint(20) NOT NULL,
  `STARTED_BY` bigint(20) NOT NULL,
  `STARTED_ON` bigint(20) NOT NULL,
  PRIMARY KEY (`TRX_ID`),
  INDEX (`TABLE_ID`),
  CONSTRAINT `TRX_USER_ID_FK` FOREIGN KEY (`STARTED_BY`) REFERENCES `JDOUSERGROUP` (`ID`) ON DELETE CASCADE
)