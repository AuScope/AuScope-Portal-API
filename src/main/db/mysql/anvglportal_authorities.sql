CREATE TABLE `authorities` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `authority` varchar(32) DEFAULT NULL,
  `userId` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `USER_ID` (`userId`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
