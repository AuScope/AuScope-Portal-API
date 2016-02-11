CREATE TABLE `users` (
  `id` varchar(128) NOT NULL,
  `fullName` varchar(256) DEFAULT NULL,
  `email` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `EMAIL` (`email`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
