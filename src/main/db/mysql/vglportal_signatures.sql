CREATE DATABASE  IF NOT EXISTS `veglportal` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `veglportaltest`;
-- MySQL dump 10.13  Distrib 5.1.40, for Win32 (ia32)
--
-- Host: cgsrv4.arrc.csiro.au    Database: veglportaltest
-- ------------------------------------------------------
-- Server version	5.1.63-0+squeeze1-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `signatures`
--

DROP TABLE IF EXISTS `signatures`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;