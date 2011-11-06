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
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;

public class VisitorCreator extends CodeCreator {

    private JDefinedClass visitorAction;

    public VisitorCreator(JDefinedClass visitorAction, Outline outline, JPackage pkg) {
        super(outline, pkg);
        this.visitorAction = visitorAction;
    }

    @Override
    protected void run(Set<ClassOutline> classes) {

        setOutput(outline.getClassFactory().createInterface(pakkage, "Visitor", null));

        for (ClassOutline classOutline : classes) {
            JMethod enterMethod = getOutput().method(JMod.PUBLIC, visitorAction, "enter");
            enterMethod.param(classOutline.implClass, "aBean");

            JMethod leaveMethod = getOutput().method(JMod.PUBLIC, visitorAction, "leave");
            leaveMethod.param(classOutline.implClass, "aBean");
        }
        JMethod vizMethod = getOutput().method(JMod.PUBLIC, visitorAction, "visit");
        vizMethod.param(getOutput().owner().ref(String.class), "text");
    }
}
