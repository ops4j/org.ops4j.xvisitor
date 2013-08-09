/*
 * Copyright 2013 Harald Wellmann
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
package org.ops4j.xvisitor.test.simple;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import org.omg.simf.conceptspace.Document;
import org.omg.simf.conceptspace.Element;
import org.ops4j.xvisitor.concept.jaxb.BaseVisitor;
import org.ops4j.xvisitor.concept.jaxb.VisitorAction;

/**
 * Regression test for XVISITOR-8.
 * 
 * @author Harald Wellmann
 *
 */
public class ConceptSpaceTest {


    private class SkippingVisitor extends BaseVisitor {
        boolean enterCalled;
        boolean leaveCalled;

        @Override
        public VisitorAction enter(Document aBean) {
            return VisitorAction.SKIP;
        }

        @Override
        public VisitorAction enter(Element aBean) {
            enterCalled = true;
            return VisitorAction.CONTINUE;
        }

        @Override
        public VisitorAction leave(Element aBean) {
            leaveCalled = true;
            return VisitorAction.CONTINUE;
        }
    }

    @Test
    public void conceptSpace() throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(Document.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        InputStream is = getClass().getResourceAsStream("/test-data/ConceptSpace1.xml");
        Source source = new StreamSource(is);
        Document document = unmarshaller.unmarshal(source, Document.class).getValue();
        
        SkippingVisitor visitor = new SkippingVisitor();        
        document.accept(visitor);

        assertThat(visitor.enterCalled, is(false));
        assertThat(visitor.leaveCalled, is(false));
    }
}
