DROP TABLE IF EXISTS `authorities`;

CREATE TABLE `authorities` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `authority` varchar(32) DEFAULT NULL,
  `userId` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY `USER_ID` (`userId`) 
     REFERENCES users(`id`)
     ON DELETE CASCADE
);
