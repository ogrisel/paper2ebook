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
import java.net.URL;
import java.util.List;

import junit.framework.Assert;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for page2ebook.Transformer.
 *
 * Tests based on a test document: check the number of page and the dimensions
 * of the detected interesting areas.
 */
public class TransformerTest {

    PDDocument sourcePdf;

    PDDocument resultPdf;

    Transformer transformer;

    @Before
    public void readSourceDocument() throws IOException {
        URL url = getClass().getResource("/journal.pcbi.1000211.pdf");
        Assert.assertNotNull(url);
        sourcePdf = PDDocument.load(url);
        transformer = new Transformer(sourcePdf);
    }

    @After
    public void closeDocuments() throws IOException {
        if (sourcePdf != null) {
            sourcePdf.close();
        }
        if (resultPdf != null) {
            resultPdf.close();
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFragmentSizes() throws Exception {

        List<PDPage> pages = sourcePdf.getDocumentCatalog().getAllPages();
        for (PDPage page : pages) {
            List<PDRectangle> fragments = transformer.getFragments(page);
            Assert.assertNotNull(fragments);

            // naive 2 columns splitter will output the 4 quadrants of the page
            Assert.assertEquals(4, fragments.size());
            for (PDRectangle fragment : fragments) {
                Assert.assertEquals(306.1415f, fragment.getWidth());
                Assert.assertEquals(395.433f, fragment.getHeight());
            }
        }
    }

    @Test
    public void testTransform() throws Exception {
        resultPdf = transformer.extract();
        Assert.assertEquals(sourcePdf.getNumberOfPages() * 4,
                resultPdf.getNumberOfPages());
        // result.save(new FileOutputStream("/tmp/out.pdf"));
    }
}
