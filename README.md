# paper2ebook

Utility to re-structure research papers published in US Letter or A4
format PDF files potentially with 2 columns layout.

The goal is to try to make them more readable on ebook reader devices
with small screen really estate and without breaking the formating,
fonts, math formulae, ...

It is reported to work quite well for 2 columns papers on the Amazon
Kindle 3. Other ebook readers that natively support PDF rendering should
benefit from this transformer as well.


## License

Apache Software License 2.0 (as the main dependency: Apache PDFBox).


## Project status

This project is alpha / experimental code. Features are implemented
when needed. Expects bugs and not implemented exceptions.

Right now the program assumes an hardcoded 2 columns layout. It is
planned to make the program instrospect the structure of the document
to decompose parts in somewhat smart way.


## Building from source

You need a Java 6 SDK and Apache Maven, then:

   $ mvn assembly:assembly


## Usage

    $ java -jar target/paper2ebook-VERSION.jar original.pdf transformed.pdf

TODO: make it possible to omit the output file and name it
original-ebook.pdf automatically.


## How it works

To avoid breaking anything paper2ebook simply copy the original stream of PDF
objects to the output PDF file but duplicates the number of pages so that each
page in the ouput is a focus (using the CropBox property of PDF page objects)
on a meaningful portion of the input page (e.g. the half left column, or a
central figure, ...).

Heuristics to find the interesting portions to focus on is a work in progress.

