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
  PRIMARY KEY (`id`),
  KEY `EMAIL` (`email`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
