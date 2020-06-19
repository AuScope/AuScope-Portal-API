CREATE TABLE `job_annotations` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `job_id` int(11) NOT NULL,
    `value` varchar(255) NOT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`job_id`)
        REFERENCES jobs(`id`)
        ON DELETE CASCADE
);
