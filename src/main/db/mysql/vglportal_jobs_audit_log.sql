DROP TABLE IF EXISTS `jobs_audit_log`;

CREATE TABLE `jobs_audit_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `jobId` int(11) NOT NULL,
  `fromStatus` varchar(255) DEFAULT NULL,
  `toStatus` varchar(255) NOT NULL,
  `transitionDate` datetime NOT NULL,
  `message` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY `jobId` (`jobId`)
        REFERENCES jobs(`id`)
        ON DELETE CASCADE
);