/*
 * Copyright 2011 Harald Wellmann
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.xvisitor.test.docbook;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.junit.Before;
import org.junit.Test;
import org.ops4j.xvisitor.docbook.jaxb.ArticleClass;
import org.ops4j.xvisitor.docbook.jaxb.Para;
import org.ops4j.xvisitor.docbook.jaxb.SectionClass;
import org.ops4j.xvisitor.docbook.jaxb.Title;
import org.ops4j.xvisitor.docbook.jaxb.visitor.BaseVisitor;
import org.ops4j.xvisitor.docbook.jaxb.visitor.VisitorAction;

public class DocbookVisitorTest {

    private ArticleClass article;

    public class SectionTitleVisitor extends BaseVisitor {
        private boolean inTitle = false;
        private int numParagraphs;
        private int numSections;
        private List<String> sectionTitles = new ArrayList<String>();

        @Override
        public VisitorAction enter(Para para) {
            numParagraphs++;
            return VisitorAction.CONTINUE;
        }

        @Override
        public VisitorAction enter(SectionClass section) {
            numSections++;
            return VisitorAction.CONTINUE;
        }

        @Override
        public VisitorAction enter(Title title) {
            inTitle = true;
            return VisitorAction.CONTINUE;
        }

        @Override
        public VisitorAction leave(Title title) {
            inTitle = false;
            return VisitorAction.CONTINUE;
        }

        @Override
        public VisitorAction visit(String text) {
            if (inTitle) {
                sectionTitles.add(text);
            }
            return VisitorAction.CONTINUE;
        }

        public int getNumParagraphs() {
            return numParagraphs;
        }

        public int getNumSections() {
            return numSections;
        }
        
        public List<String> getSectionTitles() {
            return sectionTitles;
        }

    }

    @Before
    public void buildModel() throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(ArticleClass.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        InputStream is = getClass()
                .getResourceAsStream("/test-data/DataScriptLanguageOverview.xml");
        Source source = new StreamSource(is);
        article = unmarshaller.unmarshal(source, ArticleClass.class).getValue();
        assertThat(article, is(notNullValue()));
    }

    @Test
    public void sectionTitleVisitor() {
        SectionTitleVisitor visitor = new SectionTitleVisitor();
        article.accept(visitor);       
        assertThat(visitor.getNumSections(), is(62));
        assertThat(visitor.getNumParagraphs(), is(170));
        assertThat(visitor.getSectionTitles().get(10), is("String Types"));
    }
}
