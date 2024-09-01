CREATE TABLE users (
  id varchar(128) NOT NULL,
  fullName varchar(256) DEFAULT NULL,
  email varchar(256) DEFAULT NULL,
  acceptedTermsConditions int DEFAULT NULL,
  PRIMARY KEY (id),
  CONSTRAINT EMAIL UNIQUE (email)
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

CREATE TABLE states (
  id varchar(128) NOT NULL,
  "userid" varchar(128) DEFAULT NULL,
  "name" varchar(255) DEFAULT NULL,
  description varchar(255) DEFAULT NULL,
  jsonState varchar NOT NULL,
  creationDate timestamp(0) NOT NULL,
  isPublic boolean NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY ("userid")
     REFERENCES users(id)
     ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE hashmap_params (
  "key" varchar(128) NOT NULL,
  "value" text NOT NULL,
  PRIMARY KEY ("key")
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
