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

/**
 * Creates the Visitor interface with {@code enter()} and {@code leave()} methods for every
 * complex model class and a {@code visit(String)} method for mixed content.
 * @author hwellmann
 *
 */
public class VisitorCreator extends CodeCreator {

    /** Return type for Visitor methods. */
    private JDefinedClass visitorAction;

    /**
     * Creates a VisitorCreator.
     * @param visitorAction  return type for visitor methods
     * @param outline        outline of code model
     * @param pkg            package for Visitor class
     */
    public VisitorCreator(JDefinedClass visitorAction, Outline outline, JPackage pkg) {
        super(outline, pkg);
        this.visitorAction = visitorAction;
    }

    @Override
    protected void run(Set<ClassOutline> classes) {
        
        // Create Visitor interface
        JDefinedClass visitor = outline.getClassFactory().createInterface(pkg, "Visitor", null);
        setOutput(visitor);

        // Add enter() and leave() methods for every model class
        for (ClassOutline classOutline : classes) {
            JMethod enterMethod = getOutput().method(JMod.PUBLIC, visitorAction, "enter");
            enterMethod.param(classOutline.implClass, "bean");

            JMethod leaveMethod = getOutput().method(JMod.PUBLIC, visitorAction, "leave");
            leaveMethod.param(classOutline.implClass, "bean");
        }
        
        // Add visit() method for String content
        JMethod vizMethod = getOutput().method(JMod.PUBLIC, visitorAction, "visit");
        vizMethod.param(getOutput().owner().ref(String.class), "text");
    }
}
