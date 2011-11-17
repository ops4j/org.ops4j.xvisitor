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
 * Creates BaseVisitor class implementing the Visitor interface with no-op methods.
 * @author hwellmann
 *
 */
public class BaseVisitorCreator extends CodeCreator {

    private JDefinedClass visitorAction;
    private JDefinedClass visitor;

    /**
     * Creates a BaseVisitorCreator
     * @param visitorAction  return type of Visitor methods
     * @param visitor        Visitor interface
     * @param outline        code model
     * @param pkg            package for generated class
     */
    public BaseVisitorCreator(JDefinedClass visitorAction, JDefinedClass visitor,
            Outline outline, JPackage pkg) {
        super(outline, pkg);
        this.visitorAction = visitorAction;
        this.visitor = visitor;
    }

    /**
     * Creates the BaseVisitor class, implementing all required methods by 
     * {@code return VisitorAction.CONTINUE}.
     */
    @Override
    protected void run(Set<ClassOutline> aClasses) {
        JDefinedClass baseVisitor = getOutline().getClassFactory().createClass(getPackage(), "BaseVisitor", null);
        setOutput(baseVisitor);
        baseVisitor._implements(visitor);
        for (ClassOutline classOutline : aClasses) {
            JMethod enterMethod = baseVisitor.method(JMod.PUBLIC, visitorAction, "enter");
            enterMethod.param(classOutline.implClass, "aBean");
            enterMethod.body()._return(visitorAction.enumConstant("CONTINUE"));

            JMethod leaveMethod = baseVisitor.method(JMod.PUBLIC, visitorAction, "leave");
            leaveMethod.param(classOutline.implClass, "aBean");
            leaveMethod.body()._return(visitorAction.enumConstant("CONTINUE"));
        }
        JMethod vizMethod = baseVisitor.method(JMod.PUBLIC, visitorAction, "visit");
        vizMethod.param(baseVisitor.owner().ref(String.class), "text");
        vizMethod.body()._return(visitorAction.enumConstant("CONTINUE"));
    }
}
