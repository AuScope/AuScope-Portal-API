/*
Navicat PGSQL Data Transfer

Source Server         : geophys3.nectar
Source Server Version : 90104
Source Host           : localhost:5432
Source Database       : vgl
Source Schema         : public

Target Server Type    : PGSQL
Target Server Version : 90104
File Encoding         : 65001

Date: 2012-07-26 10:37:36
*/


-- ----------------------------
-- Sequence structure for "jobs_id_seq"
-- ----------------------------
DROP SEQUENCE "jobs_id_seq";
CREATE SEQUENCE "jobs_id_seq"
 INCREMENT 1
 MINVALUE 1
 MAXVALUE 9223372036854775807
 START 1
 CACHE 1;

-- ----------------------------
-- Sequence structure for "series_id_seq"
-- ----------------------------
DROP SEQUENCE "series_id_seq";
CREATE SEQUENCE "series_id_seq"
 INCREMENT 1
 MINVALUE 1
 MAXVALUE 9223372036854775807
 START 1
 CACHE 1;

-- ----------------------------
-- Table structure for "jobs"
-- ----------------------------
DROP TABLE "jobs";
CREATE TABLE "jobs" (
"id" int4 DEFAULT nextval('jobs_id_seq'::regclass) NOT NULL,
"name" varchar(255),
"description" varchar(255),
"emailAddress" varchar(255),
"user" varchar(255),
"submitDate" timestamp(6),
"status" varchar(255),
"computeVmId" varchar(255),
"computeInstanceId" varchar(255),
"computeInstanceType" varchar(255),
"computeInstanceKey" varchar(255),
"storageProvider" varchar(255),
"storageEndpoint" varchar(255),
"storageBucket" varchar(255),
"storageBaseKey" varchar(255),
"storageAccessKey" varchar(255),
"storageSecretKey" varchar(255),
"registeredUrl" varchar(255),
"seriesId" int4,
"vmSubsetFilePath" varchar(255),
"vmSubsetUrl" varchar(255),
"paddingMinEasting" float8,
"paddingMaxEasting" float8,
"paddingMinNorthing" float8,
"paddingMaxNorthing" float8,
"selectionMinEasting" float8,
"selectionMaxEasting" float8,
"selectionMinNorthing" float8,
"selectionMaxNorthing" float8,
"mgaZone" varchar(255),
"cellX" int4,
"cellY" int4,
"cellZ" int4,
"inversionDepth" int4
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Records of jobs
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for "series"
-- ----------------------------
DROP TABLE "series";
CREATE TABLE "series" (
"id" int4 DEFAULT nextval('series_id_seq'::regclass) NOT NULL,
"user" varchar(255) NOT NULL,
"name" varchar(255) NOT NULL,
"description" varchar(255)
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Records of series
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Alter Sequences Owned By 
-- ----------------------------
ALTER SEQUENCE "jobs_id_seq" OWNED BY "jobs"."id";
ALTER SEQUENCE "series_id_seq" OWNED BY "series"."id";

-- ----------------------------
-- Indexes structure for table jobs
-- ----------------------------
CREATE INDEX "jobs_emailaddr_index" ON "jobs" USING btree ("emailAddress");
CREATE UNIQUE INDEX "jobs_id_index" ON "jobs" USING btree ("id");
CREATE INDEX "jobs_seriesID_index" ON "jobs" USING btree ("seriesId");
CREATE INDEX "jobs_user_index" ON "jobs" USING btree ("user");

-- ----------------------------
-- Primary Key structure for table "jobs"
-- ----------------------------
ALTER TABLE "jobs" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table series
-- ----------------------------
CREATE UNIQUE INDEX "id_index" ON "series" USING btree ("id");
CREATE INDEX "uname_index" ON "series" USING btree ("user");

-- ----------------------------
-- Primary Key structure for table "series"
-- ----------------------------
ALTER TABLE "series" ADD PRIMARY KEY ("id");
