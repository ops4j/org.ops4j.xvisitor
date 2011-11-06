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

public class VisitableCreator extends CodeCreator {

    private JDefinedClass visitorAction;

    private JDefinedClass visitor;

    public VisitableCreator(JDefinedClass visitorAction, JDefinedClass visitor,
            Outline outline, JPackage pkg) {
        super(outline, pkg);
        this.visitorAction = visitorAction;
        this.visitor = visitor;
    }

    @Override
    protected void run(Set<ClassOutline> classes) {
        JDefinedClass visitable = outline.getClassFactory().createInterface(pakkage, "Visitable", null);
        setOutput(visitable);
        visitable.method(JMod.PUBLIC, visitorAction, "accept").param(visitor, "visitor");

        for (ClassOutline classOutline : classes) {
            classOutline.implClass._implements(visitable);
        }
    }
}
