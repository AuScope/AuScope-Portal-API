The `images` directory contains the images used for all output documents.

The generation of a PDF requires the use of LaTeX which had problems with the
image resolutions - generating huge images in the PDf file.

The solution is to keep the original images and convert/copy them to a second
group all with the same resolution.  The most suitable one I found was 54
pixels per CM.

The `resolution.sh` script will convert all of the original images, or the
ones specified on the command line.


- Peter Thew

FYI the `../icons` directory holds copies of the images/icons used by the web app and are unchanged.
