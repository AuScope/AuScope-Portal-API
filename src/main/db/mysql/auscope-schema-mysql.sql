CREATE TABLE `users` (
  `id` varchar(128) NOT NULL,
  `fullName` varchar(256) DEFAULT NULL,
  `email` varchar(256) DEFAULT NULL,
  `acceptedTermsConditions` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `EMAIL` (`email`) USING BTREE
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

CREATE TABLE `states` (
  `id` varchar(128) NOT NULL,
  `userid` varchar(128) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `jsonState` text NOT NULL,
  `creationDate` timestamp(0) NOT NULL,
  `isPublic` boolean NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`userid`)
     REFERENCES users(`id`)
     ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE `hashmap_params` (
  `key` varchar(128) NOT NULL,
  `value` varchar(1000000) NOT NULL,
  PRIMARY KEY (`key`)
); 

CREATE TABLE `bookmarks` (
  `fileIdentifier` varchar(128) NOT NULL,
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
