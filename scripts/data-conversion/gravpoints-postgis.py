# -*- coding: utf-8 -*-
"""
Converts Airbone Electro Magnetci Survey ASCII TSV files into a series of POSTGIS INSERT statements

@author: jxv599
"""
import glob, os, fnmatch, uuid, netCDF4, logging, csv, re, traceback, itertools

sqlCreateTable = """
CREATE TABLE gravitypoints
(
  id serial NOT NULL,
  survey_number integer,
  observation_number integer,
  station_number bigint,
  location geometry,
  geoid_groud_elevation double precision,
  ellipsoid_ground_height double precision,
  observed_gravity double precision,
  geoid_gravity_meter_height double precision,
  ellipsoid_hgt_of_gravity_meter double precision,
  geoid_ellipsoid_separation double precision,
  terrain_correction double precision,
  terr_corr_density_used double precision,
  observation_date date,
  processing_date date,
  ellipsoid_freeair_anomaly double precision,
  spherical_cap_bouguer_anomaly double precision,
  geoidal_freeair_anomaly double precision,
  infinite_slab_bouguer_anomaly double precision,
  CONSTRAINT pk_id PRIMARY KEY (id)
)

CREATE UNIQUE INDEX id_idx ON aemsurveys USING btree (id);
CREATE INDEX location_gist_idx ON aemsurveys USING GIST (location);

"""

sqlInsertFormat = "INSERT INTO gravitypoints (survey_number, observation_number, station_number, location, geoid_groud_elevation, ellipsoid_ground_height, observed_gravity, geoid_gravity_meter_height, ellipsoid_hgt_of_gravity_meter, geoid_ellipsoid_separation, terrain_correction, terr_corr_density_used, observation_date, processing_date, ellipsoid_freeair_anomaly, spherical_cap_bouguer_anomaly, geoidal_freeair_anomaly, infinite_slab_bouguer_anomaly) VALUES ( {0}, {1}, {2}, ST_GeomFromText('POINT({3} {4})', 4283), {5}, {6}, {7}, {8}, {9}, {10}, {11}, {12}, to_date('{13}', 'YYYY-MM-DD'), to_date('{14}', 'YYYY-MM-DD'), {15}, {16}, {17}, {18});\n"

print 'Creating create table statements'
with open('/projects/r17/shared/gravity/create-table.sql', 'w') as fout:
	fout.write(sqlCreateTable)


print 'Creating insert statements'
with open('/projects/r17/Point_Data/gravity/V_GRAV_JS_AAGD07_export.dat', 'rb') as csvin, open('/projects/r17/shared/gravity/insert.sql', 'w') as fout:
	csvin = csv.reader(csvin, delimiter=' ')

	fout.write('BEGIN;\n')

	# Write out insert statements in smaller transaction chunks
	counter = 1
	txnSize = 10000
	for row in itertools.chain(csvin):
		row = filter(None, row)
                fout.write(sqlInsertFormat.format(*row))
		if counter % txnSize == 0:
			fout.write('COMMIT;\n')
			fout.write('BEGIN;\n')
		counter = counter + 1
		

	fout.write('COMMIT;\n')

print 'done'



