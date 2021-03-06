CREATE TABLE IF NOT EXISTS `ACCESS_REQUIREMENT` (
  `ID` BIGINT NOT NULL,
  `ETAG` char(36) NOT NULL,
  `CURRENT_REV_NUM` BIGINT DEFAULT 0,
  `CREATED_BY` BIGINT NOT NULL,
  `CREATED_ON` BIGINT NOT NULL,
  `ACCESS_TYPE` ENUM('DOWNLOAD', 'PARTICIPATE') NOT NULL,
  `CONCRETE_TYPE` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  PRIMARY KEY (`ID`),
  CONSTRAINT `ACCESS_REQUIREMENT_CREATED_BY_FK` FOREIGN KEY (`CREATED_BY`) REFERENCES `JDOUSERGROUP` (`ID`) ON DELETE CASCADE
)
