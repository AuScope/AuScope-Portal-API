DROP TABLE IF EXISTS `signatures`;

CREATE TABLE `signatures` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user` varchar(255) NOT NULL,
  `individualName` varchar(255) DEFAULT NULL,
  `organisationName` varchar(255) DEFAULT NULL,
  `positionName` varchar(128) DEFAULT NULL,
  `telephone` varchar(50) DEFAULT NULL,
  `facsimile` varchar(50) DEFAULT NULL,
  `deliveryPoint` varchar(255) DEFAULT NULL,
  `city` varchar(50) DEFAULT NULL,
  `administrativeArea` varchar(255) DEFAULT NULL,
  `postalCode` varchar(10) DEFAULT NULL,
  `country` varchar(128) DEFAULT NULL,
  `onlineContactName` varchar(255) DEFAULT NULL,
  `onlineContactDescription` varchar(255) DEFAULT NULL,
  `onlineContactURL` varchar(255) DEFAULT NULL,
  `keywords` varchar(255) DEFAULT NULL,
  `constraints` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`user`)
     REFERENCES users(`email`)
     ON DELETE CASCADE ON UPDATE CASCADE
);