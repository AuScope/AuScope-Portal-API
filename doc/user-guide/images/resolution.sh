#!/bin/sh

# make sure all images have the same resolution for LaTeX
# use ImageMagick to set the image resolutions (copy from
# originals/ directory to images/)
#
# Args: either specific files to convert or everything

RES=54

if [ $# -gt 0 ]
then
	files="$@"
else
	files=`ls originals/*.png`
fi

for f in $files
do
	base=`basename $f`
	echo "`date` $base"

	if [ ! -f "$f" ]
	then
		echo "[warn] No file found: $f"

	elif [ "$base" = "$f" ]
	then
		echo "[warn] Can't convert the same file: $f (maybe use originals/$base ??)"

	else
		convert $f -density $RES -units PixelsPerCentimeter $base
	fi
done

