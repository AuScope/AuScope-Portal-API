# -*- coding: utf-8 -*-
"""
Converts Airbone Electro Magnetci Survey ASCII TSV files into a series of POSTGIS INSERT statements

@author: jxv599
"""
import glob, os, fnmatch, uuid, netCDF4, logging, csv, re, traceback, itertools

sqlCreateTable = """
CREATE TABLE aemsurveys
(
    id serial,
    line integer, 
    flight integer,
    fid double precision,
    project_fas integer,
    project_ga integer,
    aircraft integer,
    timestamp timestamp,
    bearing integer,
    location geometry,
    lidar double precision,
    radalt double precision,
    tx_elevation double precision,
    dtm double precision,
    mag double precision,
    tx_pitch double precision,
    tx_roll double precision,
    tx_height double precision,
    hsep_raw double precision,
    vsep_raw double precision,
    tx_height_std double precision,
    hsep_std double precision,
    vsep_std double precision,
    emx_nonhprg1 double precision,
    emx_nonhprg2 double precision,
    emx_nonhprg3 double precision,
    emx_nonhprg4 double precision,
    emx_nonhprg5 double precision,
    emx_nonhprg6 double precision,
    emx_nonhprg7 double precision,
    emx_nonhprg8 double precision,
    emx_nonhprg9 double precision,
    emx_nonhprg10 double precision,
    emx_nonhprg11 double precision,
    emx_nonhprg12 double precision,
    emx_nonhprg13 double precision,
    emx_nonhprg14 double precision,
    emx_nonhprg15 double precision,
    emx_hprg1 double precision,
    emx_hprg2 double precision,
    emx_hprg3 double precision,
    emx_hprg4 double precision,
    emx_hprg5 double precision,
    emx_hprg6 double precision,
    emx_hprg7 double precision,
    emx_hprg8 double precision,
    emx_hprg9 double precision,
    emx_hprg10 double precision,
    emx_hprg11 double precision,
    emx_hprg12 double precision,
    emx_hprg13 double precision,
    emx_hprg14 double precision,
    emx_hprg15 double precision,
    x_sferics double precision,
    x_lowfreq double precision,
    x_powerline double precision,
    x_vlf1 double precision,
    x_vlf2 double precision,
    x_vlf3 double precision,
    x_vlf4 double precision,
    x_geofact double precision,
    emz_nonhprg1 double precision,
    emz_nonhprg2 double precision,
    emz_nonhprg3 double precision,
    emz_nonhprg4 double precision,
    emz_nonhprg5 double precision,
    emz_nonhprg6 double precision,
    emz_nonhprg7 double precision,
    emz_nonhprg8 double precision,
    emz_nonhprg9 double precision,
    emz_nonhprg10 double precision,
    emz_nonhprg11 double precision,
    emz_nonhprg12 double precision,
    emz_nonhprg13 double precision,
    emz_nonhprg14 double precision,
    emz_nonhprg15 double precision,
    emz_hprg1 double precision,
    emz_hprg2 double precision,
    emz_hprg3 double precision,
    emz_hprg4 double precision,
    emz_hprg5 double precision,
    emz_hprg6 double precision,
    emz_hprg7 double precision,
    emz_hprg8 double precision,
    emz_hprg9 double precision,
    emz_hprg10 double precision,
    emz_hprg11 double precision,
    emz_hprg12 double precision,
    emz_hprg13 double precision,
    emz_hprg14 double precision,
    emz_hprg15 double precision,
    z_sferics double precision,
    z_lowfreq double precision,
    z_powerline double precision,
    z_vlf1 double precision,
    z_vlf2 double precision,
    z_vlf3 double precision,
    z_vlf4 double precision,
    z_geofact double precision,
        PRIMARY KEY (id)
);

CREATE UNIQUE INDEX id_idx ON aemsurveys USING btree (id);
CREATE INDEX location_gist_idx ON aemsurveys USING GIST (location);

"""

sqlInsertFormat = "INSERT INTO \"aemsurveys\" (line, flight, fid, project_fas, project_ga, aircraft, timestamp, bearing, location, lidar, radalt, tx_elevation, dtm, mag, tx_pitch, tx_roll, tx_height, hsep_raw, vsep_raw, tx_height_std, hsep_std, vsep_std, emx_nonhprg1, emx_nonhprg2, emx_nonhprg3, emx_nonhprg4, emx_nonhprg5, emx_nonhprg6, emx_nonhprg7, emx_nonhprg8, emx_nonhprg9, emx_nonhprg10, emx_nonhprg11, emx_nonhprg12, emx_nonhprg13, emx_nonhprg14, emx_nonhprg15, emx_hprg1, emx_hprg2, emx_hprg3, emx_hprg4, emx_hprg5, emx_hprg6, emx_hprg7, emx_hprg8, emx_hprg9, emx_hprg10, emx_hprg11, emx_hprg12, emx_hprg13, emx_hprg14, emx_hprg15, x_sferics, x_lowfreq, x_powerline, x_vlf1, x_vlf2, x_vlf3, x_vlf4, x_geofact, emz_nonhprg1, emz_nonhprg2, emz_nonhprg3, emz_nonhprg4, emz_nonhprg5, emz_nonhprg6, emz_nonhprg7, emz_nonhprg8, emz_nonhprg9, emz_nonhprg10, emz_nonhprg11, emz_nonhprg12, emz_nonhprg13, emz_nonhprg14, emz_nonhprg15, emz_hprg1, emz_hprg2, emz_hprg3, emz_hprg4, emz_hprg5, emz_hprg6, emz_hprg7, emz_hprg8, emz_hprg9, emz_hprg10, emz_hprg11, emz_hprg12, emz_hprg13, emz_hprg14, emz_hprg15, z_sferics, z_lowfreq, z_powerline, z_vlf1, z_vlf2, z_vlf3, z_vlf4, z_geofact) VALUES ({0}, {1}, {2}, {3}, {4}, {5}, to_timestamp('{6} {7}', 'YYYYMMDD SSSS'), {8}, ST_GeomFromText('POINT({10} {9})', 4283), {13}, {14}, {15}, {16}, {17}, {18}, {19}, {20}, {21}, {22}, {23}, {24}, {25}, {26}, {27}, {28}, {29}, {30}, {31}, {32}, {33}, {34}, {35}, {36}, {37}, {38}, {39}, {40}, {41}, {42}, {43}, {44}, {45}, {46}, {47}, {48}, {49}, {50}, {51}, {52}, {53}, {54}, {55}, {56}, {57}, {58}, {59}, {60}, {61}, {62}, {63}, {64}, {65}, {66}, {67}, {68}, {69}, {70}, {71}, {72}, {73}, {74}, {75}, {76}, {77}, {78}, {79}, {80}, {81}, {82}, {83}, {84}, {85}, {86}, {87}, {88}, {89}, {90}, {91}, {92}, {93}, {94}, {95}, {96}, {97}, {98}, {99}, {100}, {101});\n"

print 'Creating create table statements'
with open('/projects/r17/shared/aemsql/create-table.sql', 'w') as fout:
	fout.write(sqlCreateTable)


print 'Creating insert statements'
with open('/projects/r17/Point_Data/aem/Paterson_North_Final_EM.asc', 'rb') as northin, open('/projects/r17/Point_Data/aem/Paterson_South_Final_EM.asc', 'rb') as southin, open('/projects/r17/shared/aemsql/insert.sql', 'w') as fout:
	northin = csv.reader(northin, delimiter=' ')
        southin = csv.reader(southin, delimiter=' ')

	fout.write('BEGIN;\n')

	# Write out insert statements in smaller transaction chunks
	counter = 1
	txnSize = 10000
	for row in itertools.chain(northin, southin):
		row = filter(None, row)
                fout.write(sqlInsertFormat.format(*row))
		if counter % txnSize == 0:
			fout.write('COMMIT;\n')
			fout.write('BEGIN;\n')
		counter = counter + 1
		

	fout.write('COMMIT;\n')

print 'done'



