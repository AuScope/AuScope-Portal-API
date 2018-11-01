DROP TABLE IF EXISTS `authorities`;
DROP TABLE IF EXISTS `job_solutions`;
DROP TABLE IF EXISTS `downloads`;
DROP TABLE IF EXISTS `jobs_audit_log`;
DROP TABLE IF EXISTS `parameters`;
DROP TABLE IF EXISTS `jobs`;
DROP TABLE IF EXISTS `series`;
DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `nci_details`;
DROP TABLE IF EXISTS `bookmarks`;
DROP TABLE IF EXISTS `bookmark_download_options`;

CREATE TABLE `users` (
  `id` varchar(128) NOT NULL,
  `fullName` varchar(256) DEFAULT NULL,
  `email` varchar(256) DEFAULT NULL,
  `arnExecution` varchar(128) DEFAULT NULL,
  `arnStorage` varchar(128) DEFAULT NULL,
  `awsSecret` varchar(128) DEFAULT NULL,
  `acceptedTermsConditions` int(11) DEFAULT NULL,
  `awsAccount` varchar(128) DEFAULT NULL,
  `awsKeyName` varchar(45) DEFAULT NULL,
  `s3Bucket` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `EMAIL` (`email`) USING BTREE
);

CREATE TABLE `series` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`user`)
     REFERENCES users(`email`)
     ON DELETE CASCADE ON UPDATE CASCADE
);

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
  `promsReportUrl` varchar(255) DEFAULT NULL,
  `computeVmRunCommand` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY `SERIES` (`seriesId`)
     REFERENCES series(`id`)
     ON DELETE CASCADE,
  KEY `JOB_LIST` (`emailAddress`,`status`,`folderId`),
  FOREIGN KEY (`emailAddress`)
     REFERENCES users(`email`)
     ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE `authorities` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `authority` varchar(32) DEFAULT NULL,
  `userId` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY `USER_ID` (`userId`) 
     REFERENCES users(`id`)
     ON DELETE CASCADE
);

CREATE TABLE job_solutions (
    job_id int(11) NOT NULL,
    solution_id varchar(255) NOT NULL,
    FOREIGN KEY (`job_id`)
        REFERENCES jobs(`id`)
        ON DELETE CASCADE
);

CREATE TABLE `downloads` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `jobId` int(11) NOT NULL,
  `url` varchar(4096) NOT NULL,
  `localPath` varchar(1024) NOT NULL,
  `name` varchar(128) DEFAULT NULL,
  `description` varchar(1024) DEFAULT NULL,
  `northBoundLatitude` double DEFAULT NULL,
  `southBoundLatitude` double DEFAULT NULL,
  `eastBoundLongitude` double DEFAULT NULL,
  `westBoundLongitude` double DEFAULT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY `jobId` (`jobId`)
        REFERENCES jobs(`id`)
        ON DELETE CASCADE
);

CREATE TABLE `jobs_audit_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `jobId` int(11) NOT NULL,
  `fromStatus` varchar(255) DEFAULT NULL,
  `toStatus` varchar(255) NOT NULL,
  `transitionDate` datetime NOT NULL,
  `message` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY `jobId_audit` (`jobId`)
        REFERENCES jobs(`id`)
        ON DELETE CASCADE
);

CREATE TABLE `parameters` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `jobId` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `value` varchar(4096) DEFAULT NULL,
  `type` varchar(45) NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY `jobId_parameters` (`jobId`)
        REFERENCES jobs(`id`)
        ON DELETE CASCADE,
  KEY `jobIdName` (`jobId`,`name`)
);

CREATE TABLE `nci_details` (
  `id` int(11) NOT NULL,
  `user` varchar(128) DEFAULT NULL,
  `nci_username` blob DEFAULT NULL,
  `nci_key` blob DEFAULT NULL,
  `nci_project` blob DEFAULT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`user`)
     REFERENCES users(`id`)
     ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE `bookmarks` (
  `fileIdentifier` varchar(50) NOT NULL,
  `serviceId` varchar(25) NOT NULL,
  `userId` varchar(128) NOT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`),
  KEY `USER_ID_BOOKMARKS` (`userId`),
  CONSTRAINT `USER_ID_BOOKMARKS` FOREIGN KEY (`userId`) REFERENCES `users` (`id`) ON DELETE CASCADE
); 

CREATE TABLE `bookmark_download_options` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `bookmarkId` int(11) NOT NULL,
  `bookmarkOptionName` varchar(128) NOT NULL,
  `url` varchar(4096) DEFAULT NULL,
  `localPath` varchar(1024) DEFAULT NULL,
  `name` varchar(128) DEFAULT NULL,
  `description` varchar(1024) DEFAULT NULL,
  `northBoundLatitude` double DEFAULT NULL,
  `southBoundLatitude` double DEFAULT NULL,
  `eastBoundLongitude` double DEFAULT NULL,
  `westBoundLongitude` double DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `ID_BOOKMARKS` (`bookmarkId`),
  CONSTRAINT `ID_BOOKMARKS` FOREIGN KEY (`bookmarkId`) REFERENCES `bookmarks` (`id`) ON DELETE CASCADE
);

