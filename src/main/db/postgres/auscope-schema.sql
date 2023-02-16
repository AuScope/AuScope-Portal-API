DROP TABLE IF EXISTS authorities;
DROP TABLE IF EXISTS job_solutions;
DROP TABLE IF EXISTS downloads;
DROP TABLE IF EXISTS jobs_audit_log;
DROP TABLE IF EXISTS parameters;
DROP TABLE IF EXISTS job_purchases;
DROP TABLE IF EXISTS job_annotations;
DROP TABLE IF EXISTS jobs;
DROP TABLE IF EXISTS series;
DROP TABLE IF EXISTS bookmark_download_options;
DROP TABLE IF EXISTS bookmarks;
DROP TABLE IF EXISTS data_purchases;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS nci_details;

CREATE TABLE users (
  id varchar(128) NOT NULL,
  fullName varchar(256) DEFAULT NULL,
  email varchar(256) DEFAULT NULL,
  arnExecution varchar(128) DEFAULT NULL,
  arnStorage varchar(128) DEFAULT NULL,
  awsSecret varchar(128) DEFAULT NULL,
  acceptedTermsConditions int DEFAULT NULL,
  awsAccount varchar(128) DEFAULT NULL,
  awsKeyName varchar(45) DEFAULT NULL,
  s3Bucket varchar(64) DEFAULT NULL,
  PRIMARY KEY (id),
  CONSTRAINT EMAIL UNIQUE (email)
);

CREATE SEQUENCE series_seq;

CREATE TABLE series (
  id int NOT NULL DEFAULT NEXTVAL ('series_seq'),
  "user" varchar(255) NOT NULL,
  "name" varchar(255) NOT NULL,
  description varchar(255) DEFAULT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY ("user")
     REFERENCES users(email)
     ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE SEQUENCE jobs_seq;

CREATE TABLE jobs (
  id int NOT NULL DEFAULT NEXTVAL ('jobs_seq'),
  "name" varchar(255) DEFAULT NULL,
  description varchar(255) DEFAULT NULL,
  emailAddress varchar(255) DEFAULT NULL,
  "user" varchar(255) DEFAULT NULL,
  submitDate timestamp(0) DEFAULT NULL,
  status varchar(255) DEFAULT NULL,
  computeVmId varchar(255) DEFAULT NULL,
  computeInstanceId varchar(255) DEFAULT NULL,
  computeInstanceType varchar(255) DEFAULT NULL,
  computeInstanceKey varchar(255) DEFAULT NULL,
  registeredUrl varchar(255) DEFAULT NULL,
  seriesId int DEFAULT NULL,
  storageBaseKey varchar(255) DEFAULT NULL,
  computeServiceId varchar(45) DEFAULT NULL,
  storageServiceId varchar(45) DEFAULT NULL,
  processDate timestamp(0) DEFAULT NULL,
  emailNotification char(1) DEFAULT 'N',
  processTimeLog varchar(255) DEFAULT '',
  storageBucket varchar(64) DEFAULT NULL,
  walltime int DEFAULT NULL,
  executeDate timestamp(0) DEFAULT NULL,
  folderId int DEFAULT NULL,
  containsPersistentVolumes char(1) DEFAULT 'N',
  promsReportUrl varchar(255) DEFAULT NULL,
  computeVmRunCommand varchar(64) DEFAULT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (seriesId)
     REFERENCES series(id)
     ON DELETE CASCADE,
  --KEY JOB_LIST (emailAddress,status,folderId),
  FOREIGN KEY (emailAddress)
     REFERENCES users(email)
     ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE SEQUENCE authorities_seq;

CREATE TABLE authorities (
  id int NOT NULL DEFAULT NEXTVAL ('authorities_seq'),
  authority varchar(32) DEFAULT NULL,
  userId varchar(128) DEFAULT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (userId) 
     REFERENCES users(id)
     ON DELETE CASCADE
);

CREATE TABLE job_solutions (
    job_id int NOT NULL,
    solution_id varchar(255) NOT NULL,
    FOREIGN KEY (job_id)
        REFERENCES jobs(id)
        ON DELETE CASCADE
);

CREATE SEQUENCE downloads_seq;

CREATE TABLE downloads (
  id int NOT NULL DEFAULT NEXTVAL ('downloads_seq'),
  jobId int NOT NULL,
  url varchar(4096) NOT NULL,
  localPath varchar(1024) NOT NULL,
  "name" varchar(128) DEFAULT NULL,
  description varchar(1024) DEFAULT NULL,
  northBoundLatitude double precision DEFAULT NULL,
  southBoundLatitude double precision DEFAULT NULL,
  eastBoundLongitude double precision DEFAULT NULL,
  westBoundLongitude double precision DEFAULT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (jobId)
        REFERENCES jobs(id)
        ON DELETE CASCADE
);

CREATE SEQUENCE jobs_audit_log_seq;

CREATE TABLE jobs_audit_log (
  id int NOT NULL DEFAULT NEXTVAL ('jobs_audit_log_seq'),
  jobId int NOT NULL,
  fromStatus varchar(255) DEFAULT NULL,
  toStatus varchar(255) NOT NULL,
  transitionDate timestamp(0) NOT NULL,
  message varchar(1000) DEFAULT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (jobId)
        REFERENCES jobs(id)
        ON DELETE CASCADE
);

CREATE SEQUENCE parameters_seq;

CREATE TABLE parameters (
  id int NOT NULL DEFAULT NEXTVAL ('parameters_seq'),
  jobId int NOT NULL,
  "name" varchar(255) NOT NULL,
  value varchar(4096) DEFAULT NULL,
  type varchar(45) NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (jobId)
        REFERENCES jobs(id)
        ON DELETE CASCADE--,
  --KEY jobIdName (jobId,"name")
);

CREATE SEQUENCE nci_details_seq;

CREATE TABLE nci_details (
  id int NOT NULL DEFAULT NEXTVAL ('nci_details_seq'),
  "user" varchar(128) DEFAULT NULL,
  nci_username bytea DEFAULT NULL,
  nci_key bytea DEFAULT NULL,
  nci_project bytea DEFAULT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY ("user")
     REFERENCES users(id)
     ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE SEQUENCE bookmarks_seq;

CREATE TABLE bookmarks (
  fileIdentifier varchar(128) NOT NULL,
  serviceId varchar(25) NOT NULL,
  userId varchar(128) NOT NULL,
  id int NOT NULL DEFAULT NEXTVAL ('bookmarks_seq'),
  PRIMARY KEY (id),
  CONSTRAINT USER_ID_BOOKMARKS FOREIGN KEY (userId) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX USER_ID_BOOKMARKS ON bookmarks (userId); 

CREATE SEQUENCE bookmark_download_options_seq;

CREATE TABLE bookmark_download_options (
  id int NOT NULL DEFAULT NEXTVAL ('bookmark_download_options_seq'),
  bookmarkId int NOT NULL,
  bookmarkOptionName varchar(128) NOT NULL,
  url varchar(4096) DEFAULT NULL,
  localPath varchar(1024) DEFAULT NULL,
  "name" varchar(128) DEFAULT NULL,
  description varchar(1024) DEFAULT NULL,
  northBoundLatitude double precision DEFAULT NULL,
  southBoundLatitude double precision DEFAULT NULL,
  eastBoundLongitude double precision DEFAULT NULL,
  westBoundLongitude double precision DEFAULT NULL,
  PRIMARY KEY (id),
  CONSTRAINT ID_BOOKMARKS FOREIGN KEY (bookmarkId) REFERENCES bookmarks (id) ON DELETE CASCADE
);

CREATE INDEX ID_BOOKMARKS ON bookmark_download_options (bookmarkId);

CREATE SEQUENCE data_purchases_seq;

CREATE TABLE data_purchases (
  id int NOT NULL DEFAULT NEXTVAL ('data_purchases_seq'),
  date timestamp(0) NOT NULL,
  amount double precision NOT NULL,
  downloadUrl varchar(4096) NOT NULL,
  cswRecord text NOT NULL,
  onlineResourceType varchar(25) NOT NULL,
  url varchar(4096) NOT NULL,
  localPath varchar(1024) DEFAULT NULL,
  "name" varchar(128) DEFAULT NULL,
  description varchar(1024) DEFAULT NULL,
  northBoundLatitude double precision DEFAULT NULL,
  southBoundLatitude double precision DEFAULT NULL,
  eastBoundLongitude double precision DEFAULT NULL,
  westBoundLongitude double precision DEFAULT NULL,
  paymentRecord varchar(4096) NOT NULL,
  userId varchar(128) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT USER_ID_DATA_PURCHASES FOREIGN KEY (userId) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX USER_ID_DATA_PURCHASES ON data_purchases (userId); 

CREATE SEQUENCE job_purchases_seq;

CREATE TABLE job_purchases (
  id int NOT NULL DEFAULT NEXTVAL ('job_purchases_seq'),
  date timestamp(0) NOT NULL,
  amount double precision NOT NULL,
  jobId int NOT NULL,
  jobName varchar(128) DEFAULT NULL,
  paymentRecord varchar(4096) NOT NULL,
  userId varchar(128) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT USER_ID_JOB_PURCHASES FOREIGN KEY (userId) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX USER_ID_JOB_PURCHASES ON job_purchases (userId); 

CREATE SEQUENCE job_annotations_seq;

CREATE TABLE job_annotations (
    id int NOT NULL DEFAULT NEXTVAL ('job_annotations_seq'),
    job_id int NOT NULL,
    value varchar(255) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (job_id)
        REFERENCES jobs(id)
        ON DELETE CASCADE
);
