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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.junit.Before;
import org.junit.Test;
import org.ops4j.xvisitor.docbook.jaxb.ArticleClass;
import org.ops4j.xvisitor.docbook.jaxb.Emphasis;
import org.ops4j.xvisitor.docbook.jaxb.visitor.BaseVisitor;
import org.ops4j.xvisitor.docbook.jaxb.visitor.VisitorAction;

public class SkipMixedContentTest {

    private ArticleClass article;

    public class SkipEmphasisVisitor extends BaseVisitor {
        
        private StringBuilder buffer = new StringBuilder();
        
        @Override
        public VisitorAction enter(Emphasis emphasis) {
            return VisitorAction.SKIP;
        }

        @Override
        public VisitorAction visit(String text) {
            buffer.append(text);
            buffer.append(" ");
            return VisitorAction.CONTINUE;
        }
        
        public String getText() {
            return buffer.toString();
        }
    }

    @Before
    public void buildModel() throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(ArticleClass.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        InputStream is = getClass()
                .getResourceAsStream("/test-data/para.xml");
        Source source = new StreamSource(is);
        article = unmarshaller.unmarshal(source, ArticleClass.class).getValue();
        assertThat(article, is(notNullValue()));
    }

    @Test
    public void sectionTitleVisitor() {
        SkipEmphasisVisitor visitor = new SkipEmphasisVisitor();
        article.accept(visitor);       
        assertThat(visitor.getText(), not(containsString("Physical Storage")));
    }
}
