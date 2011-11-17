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
package org.ops4j.xvisitor.xjc;

import java.util.Set;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;

/**
 * Creates the Visitable interface with its single {@code accept()} method which is implemented by
 * all complex model classes.
 * 
 * @author hwellmann
 * 
 */
public class VisitableCreator extends CodeCreator {

    /** Return type of {@code accept()} method. */
    private JDefinedClass visitorAction;

    /** Visitor argument type. */
    private JDefinedClass visitor;

    public VisitableCreator(JDefinedClass visitorAction, JDefinedClass visitor,
            Outline outline, JPackage pkg) {
        super(outline, pkg);
        this.visitorAction = visitorAction;
        this.visitor = visitor;
    }

    /**
     * Creates Visitable interface and lets each model class implement it.
     */
    @Override
    protected void run(Set<ClassOutline> classes) {
        // Create Visitable interface
        JDefinedClass visitable = outline.getClassFactory().createInterface(pkg, "Visitable", null);
        setOutput(visitable);
        
        // Add accept() method
        visitable.method(JMod.PUBLIC, visitorAction, "accept").param(visitor, "visitor");

        // Add "implements Visitable" to model classes
        for (ClassOutline classOutline : classes) {
            classOutline.implClass._implements(visitable);
        }
    }
}
