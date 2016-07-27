DROP TABLE IF EXISTS `jobs`;

CREATE TABLE `jobs` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `emailAddress` varchar(255) DEFAULT NULL,
  `user` varchar(255) DEFAULT NULL,
  `submitDate` datetime DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `computeVmId` varchar(255) DEFAULT NULL,
  `computeInstanceId` varchar(255) DEFAULT NULL,
  `computeInstanceType` varchar(255) DEFAULT NULL,
  `computeInstanceKey` varchar(255) DEFAULT NULL,
  `registeredUrl` varchar(255) DEFAULT NULL,
  `seriesId` int(11) DEFAULT NULL,
  `storageBaseKey` varchar(255) DEFAULT NULL,
  `computeServiceId` varchar(45) DEFAULT NULL,
  `storageServiceId` varchar(45) DEFAULT NULL,
  `processDate` datetime DEFAULT NULL,
  `emailNotification` char(1) DEFAULT 'N',
  `processTimeLog` varchar(255) DEFAULT '',
  `storageBucket` varchar(64) DEFAULT NULL,
  `walltime` int(11) DEFAULT NULL,
  `executeDate` datetime DEFAULT NULL,
  `folderId` int(11) DEFAULT NULL,
  `containsPersistentVolumes` char(1) DEFAULT 'N',
  PRIMARY KEY (`id`),
  FOREIGN KEY `SERIES` (`seriesId`)
     REFERENCES series(`id`)
     ON DELETE CASCADE,
  KEY `JOB_LIST` (`emailAddress`,`status`,`folderId`),
  FOREIGN KEY (`emailAddress`)
     REFERENCES users(`email`)
     ON DELETE CASCADE ON UPDATE CASCADE
);
