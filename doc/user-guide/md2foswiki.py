#!/usr/bin/python

"""Very simple conversion from Markdown to Foswiki"""

import os
import os.path
import re
import sys
import errno

from datetime import datetime

# local libraries (basic_logging)
sys.path.append(os.path.abspath('./lib'))

import logging
from basic_logging import basic_logging
logger = logging.getLogger()


# Some global stuff

def Link(url, name, trailing_space = ''):
    return '[[%s][%s]]%s' % (url, name, trailing_space)

PREAMBLE = """%%TOC%%

==Generated at %s==

"""

class MarkdownException(Exception):
    def __init__(self, value):
        self.message = value

    def __str__(self):
        return repr(self.message)

# ------------------ // ------------------

class MarkdownFoswiki:
    """ *NAME*

        MarkdownFoswiki -- simple conversion from Markdown to Foswiki

        *SYNOPSIS* ::

            md2fw = MarkdownFoswiki()
            md2fw.process(input_path, output_path)

        *DESCRIPTION*

        This is a Markdown to Foswiki converter for the AuScope user guide. It
        only covers the Markdown used in the guide.

        There is a sequence of regex that are applied to each line to convert
        the Markdown markup into Foswiki. As it only works on a line by line
        basis some formatting such as tables and block quotes are a bit
        dodgy.  Once the formatting has been converted a second pass goes
        through the new markup to fix links to headings.

        Note: I tried the Perl module ``Markdown::Foswiki`` and gave up trying to
        get it to work.

        *COPYRIGHT*

            Copyright (c) 2014 by CSIRO, Australia.  All rights reserved.

        *METHODS*
    """

    def __init__(self):
        """Constructor.

        Set up the regular expressions to use to convert the markup.

        """

        # Heading map {markdown} -> Foswiki
        # e.g. headings{'chapter-one'} = 'Chapter_One'
        self.headings = {}

        # Array of [regex, edit function] -- applied in order
        rules = []
        rules.append( [re.compile('^[#]+\s.*'), self.heading] )

        # URL abbreviations, e.g. [ABC]: http://www.abc.net.au
        #   It is [My ABC][ABC]
        #   The [ABC] web site

        self.urlAbbrevs = {}

        rules.append( [re.compile('\[([ A-Z]+)\]:\s+(\S+)', re.IGNORECASE), self.defineAbbrev] )
        rules.append( [re.compile('\[(.*?)\]\[([ A-Z]+)\](\s)', re.IGNORECASE), self.replaceAbbrev] )
        rules.append( [re.compile('\[([ A-Z]+)\](\s)', re.IGNORECASE), self.replaceAbbrev] )

        # Images and links ![text](url) and [text](url)
        rules.append( [re.compile('!\[(.*?)\]\((.*?)\)'), self.embedImage] )
        rules.append( [re.compile('\[(.*?)\]\((.*?)\)'), self.linky] )

        # text formatting
        rules.append( [re.compile('(\s\*\*.*?\*\*\s)'), self.bold] )
        rules.append( [re.compile('^>\s*(.*)'), self.blockquote] )   # TODO fixme multiple lines

        # (un)ordered lists
        rules.append( [re.compile('^\s*\*\s'), self.list] )
        rules.append( [re.compile('^\s*[0-9]\.\s'), self.list] )

        # tables (dodgy)
        rules.append( [re.compile('^.*\s\|\s.*$'), self.table] )
        rules.append( [re.compile('^.*-\|-.*$'), self.Ignore] )

        # save them
        self.rules = rules

    def heading(self, match):
        """Convert a heading and save it for xref.

        Convert the heading markup and save the mangled name for
        internal links.

        Args:
            #. match: the whole line

        Returns:
            a modified line
        """

        line = match.group(0)
        self.cacheHeading(line)

        updated = '---' + re.sub('#', '+', line)
        return updated


    def cacheHeading(self, line):
        """Save the heading for later conversions.

        Convert the heading text and save the mangled name for
        fixing internal links.

        E.g. [[#top][Top of page]]

        Markdown: lowercase and spaces become minus
        Foswiki: spaces become underscore

        Args:
            #. line: the heading line
        """

        # remove "###" prefix
        logger.debug('Heading: "%s"', line)
        heading = re.sub('^#+\s+', '', line)

        # format the anchor in the markdown & foswiki styles
        md = re.sub('\s', '-', heading.lower())
        fw = re.sub('\s', '_', heading)

        # save it for later
        self.headings[md] = fw
        logger.debug('Got heading %s (%s == %s)', heading, md, fw)


    def defineAbbrev(self, match):
        """Define an abbreviation for a URL.

        [ABC]: http:://www.abc.net.au

        Save the key in a hash and when it's encountered later replace it with
        a wiki link with the URL.

        Args:
            #. match: the entire line from "[abbr]:..."

        Returns:
            Nothing - the line is ignored

        Raises:
            MarkdownException: the match was not what we expected
        """

        if match.lastindex != 2:
            raise MarkdownException('Abbreviation match error')

        name = match.group(1)
        logger.debug('Define abbrev %s', name)

        self.urlAbbrevs[name] = match.group(2)

        return self.Ignore(None)


    def replaceAbbrev(self, match):
        """Replace a URL abbreviation.

        Replace the abbreviation:

        [ABC] becomes [[http:://www.abc.net.au][ABC]]

        [My ABC][ABC] becomes [[http:://www.abc.net.au][My ABC]]

        Note: the trailing white-space is also kept as it may be an end of line.

        Args:
            #. match: either "[abbr]" or "[label][abbr]"

        Returns:
            a Foswiki link

        Raises:
            MarkdownException: the match was not what we expected
        """

        space = ''

        if match.lastindex == 2:
            # [ABC]\s
            abbr = match.group(1)
            label = abbr
            space = match.group(2)

        elif match.lastindex == 3:
            # [My ABC][ABC]\s
            label = match.group(1)
            abbr = match.group(2)
            space = match.group(3)

        else:
            raise MarkdownException('Abbrev replacement match error: ' + match.lastindex)

        # fetch the URL and create the wiki markup
        url = self.urlAbbrevs[abbr]
        logger.debug('Got abbrev %s', abbr)

        return Link(url, label, space)


    def embedImage(self, match):
        """Embed an image into the markup.

        Create an <img> tag for the embedded image, but only use the basename
        of the image's URL because the ATTACHURL should point to the correct
        path.

        TODO: check that this is OK!

        Args:
            #. match: label and image URL
        """

        if match.lastindex != 2:
            raise MarkdownException('Image match error')

        label = match.group(1)
        base = os.path.basename( match.group(2) )
        logger.debug('Got image %s = %s', label, base)

        return '<img alt="%s" src="%%ATTACHURL%%/%s" />' % (label, base)


    def linky(self, match):
        """Convert a URL link.

        Simple markup conversion for a link:

            "[label](url)" becomes "[[url][label]]"

        Args:
            #. match: label and URL
        """

        if match.lastindex != 2:
            raise MarkdownException('Image match error')

        label = match.group(1)
        url = match.group(2)
        logger.debug('Got link %s = %s', label, url)

        return Link(url, label)


    def list(self, match):
        """Convert (un)ordered lists.

        Just prefix the line with three spaces.

        Args:
            #. match: the entire line
        """

        line = '   ' + match.group(0)
        return line


    def table(self, match):
        """Dodgy table work.

        TODO more work, especially headings.

        Args:
            #. match: the entire line
        """

        line = '| ' + match.group(0) + ' |'
        return line


    def bold(self, match):
        """Convert bold markup.

        It's just '**' converted to '*'.

        Args:
            #. match: **something**
        """

        line = re.sub( '\*\*', '*', match.group(1))
        return line


    def blockquote(self, match):
        """Convert a single blockquote line.

        This needs more work as it only changes one line, not a complete block.

        Args:
            #. match: a single line after  "> "
        """

        if match.lastindex != 1:
            raise MarkdownException('Image match error')

        line = '<blockquote>\n' + match.group(1) + '\n</blockquote>\n'
        return line


    def replaceLink(self, match):
        """Replace markdown internal links with foswiki.

        An internal link (usually to a heading) was found:

            "[[#hi-there][Hi There]]"

        The anchor has to be converted into foswiki format

            "[[#Hi_There][Hi There]]"

        Note: This is done AFTER all other processing has been completed and
        we have a cache of heading names in Markdown and Foswiki formats.

        Args:
            #. match: the internal anchor word
        """

        word = match.group(1)   #  "[[#word]"

        if word in self.headings:
            word = self.headings[word]
        else:
            logger.warn('Heading anchor "%s" not found in cache', word)

        return '[[#%s]' % word


    def Ignore(self, match):
        """Special method to ignore this whole line.

        If self.keep is false, the current line is NOT appended to the output cache.
        """
        self.keep = False
        return ''


    def process(self, input_path, output_path):
        """Process the markdown file.

        Convert the file line by line.  The internal links need to be
        processed in a second step so the lines are cached for that step.

        All conversion rules are applied to the conveted line in order.

        Args:
            #. input_path: The Markdown file
            #. output_path: the path to the new foswiki file

        Raises:
            Exception: IO errors etc
        """

        # cache the output for fixing internal links to headings
        output = []

        with open(input_path, 'r') as markdown:
            logger.info('Reading %s', input_path)

            for line in markdown.readlines():
                self.keep = True    # See self.Ignore

                # each rule is applied to the line (in order)
                for r in self.rules:
                    regex = r[0]
                    func = r[1]
                    line = re.sub(regex, func, line)

                # save the new line
                if self.keep:
                    output.append(line)

        # Internal links to headings
        link_re = re.compile('\[\[#(.*?)\]')

        with open(output_path, 'w') as foswiki:
            logger.info('Writing %s', output_path)

            # use 'write' to prevent additional \n
            now = datetime.now().strftime('%Y-%m-%d %H:%M')
            head = PREAMBLE % now
            foswiki.write(head)

            for line in output:
                updated = re.sub(link_re, self.replaceLink, line)
                foswiki.write(updated)

        logger.info('Done')


# ------------------ // ------------------

def main():
    basic_logging.init_basic_logger(logger)

    if len(sys.argv) == 3:
        input_path = sys.argv[1]
        if not os.path.isfile(input_path):
            logger.fatal('File not found: %s', input_path)
            sys.exit(errno.ENOENT)

        output_path = sys.argv[2]

        md = MarkdownFoswiki()
        md.process(input_path, output_path)

    else:
        logger.fatal('Wrong number of command line args: %s <markdown file> <floswiki file>', sys.argv[0])
        sys.exit(errno.EINVAL)


if __name__ == '__main__':
    main()

