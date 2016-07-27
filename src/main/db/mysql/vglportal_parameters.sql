DROP TABLE IF EXISTS `parameters`;

CREATE TABLE `parameters` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `jobId` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `value` varchar(4096) DEFAULT NULL,
  `type` varchar(45) NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY `jobId` (`jobId`)
        REFERENCES jobs(`id`)
        ON DELETE CASCADE,
  KEY `jobIdName` (`jobId`,`name`)
);
