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

import java.io.IOException;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;

/**
 * Entry point for the XVisitor xjc plugin.
 * 
 * @author hwellmann
 * 
 */
public class VisitorPlugin extends Plugin {

    private String packageName;

    @Override
    public String getOptionName() {
        return "Xvisitor";
    }

    @Override
    public String getUsage() {
        return "  -Xvisitor          :  add Visitor pattern support to the generated code";
    }

    @Override
    public int parseArgument(Options opt, String[] args, int index) throws BadCommandLineException,
            IOException {

        String arg = args[index];
        if (arg.startsWith("-Xvisitor-package:")) {
            String[] words = arg.split(":");
            if (words.length == 2) {
                packageName = words[1];
                return 1;
            }
        }
        return 0;
    }

    @Override
    public boolean run(Outline outline, Options options, ErrorHandler errorHandler)
            throws SAXException {
        try {

            JPackage visitorPackage = getOrCreatePackageForVisitors(outline);

            Set<ClassOutline> sorted = sortClasses(outline);

            VisitorActionCreator createVisitorActionEnum = new VisitorActionCreator(outline,
                    visitorPackage);
            createVisitorActionEnum.run(sorted);
            JDefinedClass visitorAction = createVisitorActionEnum.getOutput();

            // create visitor interface
            VisitorCreator createVisitorInterface = new VisitorCreator(visitorAction, outline,
                    visitorPackage);
            createVisitorInterface.run(sorted);
            JDefinedClass visitor = createVisitorInterface.getOutput();

            // create visitable interface and have all the beans implement it
            VisitableCreator createVisitableInterface = new VisitableCreator(visitorAction,
                    visitor, outline, visitorPackage);
            createVisitableInterface.run(sorted);
            JDefinedClass visitable = createVisitableInterface.getOutput();

            // add accept method to beans
            AcceptMethodCreator addAcceptMethod = new AcceptMethodCreator(visitorAction, visitable,
                    outline);
            addAcceptMethod.run(sorted, visitor);

            // create base visitor class
            BaseVisitorCreator createBaseVisitorClass = new BaseVisitorCreator(visitorAction,
                    visitor, outline, visitorPackage);
            createBaseVisitorClass.run(sorted);

        }
        catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
        return true;
    }

    private Set<ClassOutline> sortClasses(Outline outline) {
        Set<ClassOutline> sorted = new TreeSet<ClassOutline>(new Comparator<ClassOutline>() {

            public int compare(ClassOutline outlineOne, ClassOutline outlineTwo) {
                String one = outlineOne.implClass.fullName();
                String two = outlineTwo.implClass.fullName();
                return one.compareTo(two);
            }
        });
        for (ClassOutline classOutline : outline.getClasses()) {
            sorted.add(classOutline);
        }
        return sorted;
    }

    private JPackage getOrCreatePackageForVisitors(Outline outline) {
        JPackage visitorPackage = null;
        if (getPackageName() == null) {
            visitorPackage = outline.getAllPackageContexts().iterator().next()._package();
        }
        else {
            visitorPackage = outline.getCodeModel()._package(getPackageName());
        }
        return visitorPackage;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
