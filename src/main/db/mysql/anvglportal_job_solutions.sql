CREATE TABLE job_solutions (
    job_id int(11) NOT NULL,
    solution_id varchar(255) NOT NULL,
    FOREIGN KEY (job_id)
        REFERENCES jobs(id)
        ON DELETE CASCADE
) ENGINE=MyISAM ;
