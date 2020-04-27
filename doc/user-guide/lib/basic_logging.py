#!/usr/bin/python

import os
import logging

class basic_logging:

    @staticmethod
    def init_basic_logger(logger):
        """Simple/basic initialisation of logging.

        Initialise the supplied logger to a simple ``stderr`` logging. The
        log level defaults to "INFO" but can be overridden by setting
        the environment variable ``LOGLEVEL`` :

            export LOGLEVEL=DEBUG
            ./program.py


        Args:
            #. logger; The logger object to initialise
        """
        # manual logging config

        # create console handler and set level
        ch = logging.StreamHandler()
        ch.setLevel(logging.DEBUG)

        # create formatter
        formatter = logging.Formatter("%(asctime)s %(levelname)s - %(message)s")

        # add formatter to handler
        ch.setFormatter(formatter)

        # add handler to logger
        logger.addHandler(ch)

        # set the logging level (may be overidden by the env variable)
        level = os.getenv('LOGLEVEL', 'INFO')
        numeric_level = getattr(logging, level.upper(), None)
        if not isinstance(numeric_level, int):
            numeric_level = logging.INFO

        logger.setLevel(numeric_level)

