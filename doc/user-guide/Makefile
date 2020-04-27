# Use pandoc to convert the markdown source file into other
# document formats.

.PHONY: all help clean zip html tex pdf foswiki

BASE = ug
FILES = $(BASE).md
TITLE = "AuScope User Guide"

define HELP_TEXT
Targets...

	all:     Create all document types (but not zip)

	tex:     Create LaTeX file
	pdf:     Create a PDF file, using the TeX file and LaTeX
	htm:     A portal version (to be included in the WAR)
	html:    A standalone HTML file
	foswiki: Foswiki markup

	zip:     A zip file with all files and images
	clean:   Remove all output files (pdf, tex, etc)
endef

export HELP_TEXT

# Print help text and check for programs (TODO is there a better way?)
help:
	@echo "$$HELP_TEXT"
	@echo "\n\nChecking for toolchain... (Linux)"
	which pandoc
	which pdflatex

# Basic conversion command.  Additional options may be appended
# depending on the target.

CMD = pandoc --standalone --toc --number-sections \
	 --from=markdown_github+implicit_figures --output=$@ \
	  --metadata title-meta=$(TITLE) --metadata author-meta=CSIRO \
	 $(BASE).md

LATEX_TEMP = latex.template
LATEX_OPTS = --template=$(LATEX_TEMP) --metadata documentclass=book

all: htm html tex pdf foswiki

# Portal version
htm: $(BASE).htm

# standalone version
html: $(BASE).html

tex: $(BASE).tex

pdf: $(BASE).pdf

foswiki: $(BASE).foswiki

# --- real rules

# HTML with a link to the PDF file
%.htm: $(FILES)
	$(CMD) --include-before-body=html-preface.txt

%.html: $(FILES)
	$(CMD)

%.pdf: $(FILES) $(LATEX_TEMP)
	$(CMD) $(LATEX_OPTS)

%.tex: $(FILES) $(LATEX_TEMP)
	$(CMD) $(LATEX_OPTS)

%.foswiki: $(FILES)
	./md2foswiki.py $(BASE).md $@

# re-create all files before creating the zip file
zip: clean all
	-rm -f $(BASE).zip
	zip -r $(BASE).zip $(BASE).* images/ icons/

# Delete all output files
clean:
	for x in htm html pdf tex zip foswiki; do \
		rm -f $(BASE).$$x ; \
	done
