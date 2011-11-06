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

import org.example.imported.ImportedData;
import org.example.simple.ComplexObject;
import org.example.simple.TSimpleResponse;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.xvisitor.simple.jaxb.BaseVisitor;
import org.ops4j.xvisitor.simple.jaxb.VisitorAction;

public class ComplexObjectTest {


    private ComplexObject complexObject;

    private class SimpleResponseVisitor extends BaseVisitor {
        List<TSimpleResponse> responses = new ArrayList<TSimpleResponse>();

        @Override
        public VisitorAction enter(TSimpleResponse response) {
            responses.add(response);
            return VisitorAction.CONTINUE;
        }
    }

    private class TerminatingImportedDataVisitor extends BaseVisitor {
        List<TSimpleResponse> responses = new ArrayList<TSimpleResponse>();
        
        @Override
        public VisitorAction enter(ImportedData aBean) {
            return VisitorAction.TERMINATE;
        }
        
        @Override
        public VisitorAction enter(TSimpleResponse response) {
            responses.add(response);
            return VisitorAction.CONTINUE;
        }
    }

    
    private class SkippingImportedDataVisitor extends BaseVisitor {
        List<TSimpleResponse> responses = new ArrayList<TSimpleResponse>();
        
        @Override
        public VisitorAction enter(ImportedData aBean) {
            return VisitorAction.SKIP;
        }
        
        @Override
        public VisitorAction enter(TSimpleResponse response) {
            responses.add(response);
            return VisitorAction.CONTINUE;
        }
    }

    
    @Before
    public void buildModel() throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(ComplexObject.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        InputStream is = getClass().getResourceAsStream("/test-data/ComplexObject1.xml");
        Source source = new StreamSource(is);
        complexObject = unmarshaller.unmarshal(source, ComplexObject.class).getValue();
        assertThat(complexObject, is(notNullValue()));
    }
    
    @Test
    public void complexObject1() throws JAXBException {
        SimpleResponseVisitor visitor = new SimpleResponseVisitor();
        complexObject.accept(visitor);
        
        assertThat(visitor.responses.size(), is(2));
        TSimpleResponse response0 = visitor.responses.get(0);
        TSimpleResponse response1 = visitor.responses.get(1);
        assertThat(response0.getResponse1(), is("Response to first request"));
        assertThat(response0.getResponse2(), is("Response to second request"));
        assertThat(response1.getResponse1(), is("Local response 1"));
        assertThat(response1.getResponse2(), is("Local response 2"));        
    }

    @Test
    public void complexObject2() throws JAXBException {
        TerminatingImportedDataVisitor visitor = new TerminatingImportedDataVisitor();
        complexObject.accept(visitor);
        
        assertThat(visitor.responses.size(), is(1));
    }

    @Test
    public void complexObject3() throws JAXBException {
        SkippingImportedDataVisitor visitor = new SkippingImportedDataVisitor();
        complexObject.accept(visitor);
        
        assertThat(visitor.responses.size(), is(2));
    }
}
