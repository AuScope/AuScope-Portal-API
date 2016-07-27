DROP TABLE IF EXISTS `downloads`;

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
