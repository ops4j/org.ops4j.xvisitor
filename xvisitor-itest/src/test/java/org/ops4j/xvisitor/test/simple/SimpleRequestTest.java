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
package org.ops4j.xvisitor.test.simple;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.example.simple.TSimpleRequest;
import org.junit.Test;
import org.ops4j.xvisitor.simple.jaxb.BaseVisitor;
import org.ops4j.xvisitor.simple.jaxb.VisitorAction;

public class SimpleRequestTest {


    private class SimpleVisitor extends BaseVisitor {
        boolean enterCalled;
        boolean leaveCalled;

        @Override
        public VisitorAction enter(TSimpleRequest aBean) {
            enterCalled = true;
            return VisitorAction.CONTINUE;
        }

        @Override
        public VisitorAction leave(TSimpleRequest aBean) {
            leaveCalled = true;
            return VisitorAction.CONTINUE;
        }
    }

    @Test
    public void simpleRequest1() throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(TSimpleRequest.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        InputStream is = getClass().getResourceAsStream("/test-data/SimpleRequest1.xml");
        Source source = new StreamSource(is);
        TSimpleRequest simpleRequest = unmarshaller.unmarshal(source, TSimpleRequest.class).getValue();
        
        SimpleVisitor visitor = new SimpleVisitor();        
        simpleRequest.accept(visitor);

        assertThat(visitor.enterCalled, is(true));
        assertThat(visitor.leaveCalled, is(true));
    }
}
