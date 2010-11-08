/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package paper2ebook;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.util.PageExtractor;

/**
 * A special implementation of PDFBox's PageExtractor that multiplies the number
 * of output pages to focus on portions of the input pages to make them readable
 * on a small screens while trying to preserve the intended reading order.
 *
 * @author Olivier Grisel <olivier.grisel@ensta.org>
 */
public class Transformer extends PageExtractor {

    public Transformer(PDDocument sourceDocument) {
        this(sourceDocument, 0, Integer.MAX_VALUE);
    }

    public Transformer(PDDocument sourceDocument, int startPage, int endPage) {
        super(sourceDocument, startPage, endPage);
    }

    /**
     * Output a PDF with as many pages as there are interesting areas in the
     * input document
     */
    @Override
    public PDDocument extract() throws IOException {
        PDDocument extractedDocument = new PDDocument();
        extractedDocument.setDocumentInformation(sourceDocument.getDocumentInformation());
        extractedDocument.getDocumentCatalog().setViewerPreferences(
                sourceDocument.getDocumentCatalog().getViewerPreferences());

        @SuppressWarnings("unchecked")
        List<PDPage> pages = sourceDocument.getDocumentCatalog().getAllPages();
        int pageCounter = 1;
        for (PDPage page : pages) {
            if (pageCounter >= startPage && pageCounter <= endPage) {

                List<PDRectangle> zoomedFragments = getFragments(page);
                for (PDRectangle fragment : zoomedFragments) {
                    PDPage outputPage = extractedDocument.importPage(page);
                    outputPage.setCropBox(fragment);
                    outputPage.setMediaBox(page.getMediaBox());
                    outputPage.setResources(page.findResources());
                    outputPage.setRotation(page.findRotation());

                    // TODO: rotate the page in landscape mode is width > height
                }
            }
            pageCounter++;
        }
        return extractedDocument;
    }

    /**
     * Heuristic search of the list of interesting areas in page, returned by
     * natural read order.
     */
    public List<PDRectangle> getFragments(PDPage page) {
        List<PDRectangle> fragments = new ArrayList<PDRectangle>();

        // TODO: naive 2 columns hack: rewrite me to introspect the document
        // structure instead

        PDRectangle origBox = page.findCropBox();
        float width = origBox.getWidth();
        float height = origBox.getHeight();

        // top left
        PDRectangle box = new PDRectangle();
        box.setLowerLeftX(origBox.getLowerLeftX());
        box.setLowerLeftY(origBox.getLowerLeftY() + height / 2);
        box.setUpperRightX(origBox.getUpperRightX() / 2);
        box.setUpperRightY(origBox.getUpperRightY());
        fragments.add(box);

        // bottom left
        box = new PDRectangle();
        box.setLowerLeftX(origBox.getLowerLeftX());
        box.setLowerLeftY(origBox.getLowerLeftY());
        box.setUpperRightX(origBox.getUpperRightX() / 2);
        box.setUpperRightY(origBox.getUpperRightY() / 2);
        fragments.add(box);

        // top right
        box = new PDRectangle();
        box.setLowerLeftX(origBox.getLowerLeftX() + width / 2);
        box.setLowerLeftY(origBox.getLowerLeftY() + height / 2);
        box.setUpperRightX(origBox.getUpperRightX());
        box.setUpperRightY(origBox.getUpperRightY());
        fragments.add(box);

        // bottom right
        box = new PDRectangle();
        box.setLowerLeftX(origBox.getLowerLeftX() + width / 2);
        box.setLowerLeftY(origBox.getLowerLeftY());
        box.setUpperRightX(origBox.getUpperRightX());
        box.setUpperRightY(origBox.getUpperRightY() / 2);
        fragments.add(box);

        return fragments;
    }

    public static void main(String[] args) throws IOException,
            COSVisitorException {
        String original_pdf;
        if (args.length < 1 || args.length > 2) {
            System.err.println("Usage: java -jar paper2ebook-*.jar input.pdf [output.pdf]");
            return;
        } else {
            original_pdf = args[0];
        }
        Transformer transformer = new Transformer(PDDocument.load(original_pdf));
        PDDocument output = transformer.extract();
        if (args.length == 1) {
            String orig_no_pdf = original_pdf.substring(0, original_pdf.length() - 4);
            output.save(orig_no_pdf + "_ebook.pdf");
        } else {
            output.save(args[1]);
        }
    }

}
