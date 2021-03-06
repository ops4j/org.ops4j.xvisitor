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
import com.sun.codemodel.JPackage;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;

/**
 * Base class for code generating classes of the XVisitor plugin
 * @author hwellmann
 *
 */
public abstract class CodeCreator {

    protected JPackage pkg;
    protected Outline outline;
    private JDefinedClass output;

    /**
     * Creates a code creator working on a given outline and generating classes for the given 
     * package.
     * @param outline  code model outline
     * @param pkg      package for generated classes
     */
    public CodeCreator(Outline outline, JPackage pkg) {
        setOutline(outline);
        setPackage(pkg);
    }

    /**
     * Runs the generating action of this code create on each of the given class outlines.
     * @param aClasses
     */
    protected abstract void run(Set<ClassOutline> classes);

    /**
     * Returns the model of the class generated by this creator.
     * @return
     */
    protected JDefinedClass getOutput() {
        return output;
    }

    /**
     * Sets the model of the class generated by this creator.
     */
    protected void setOutput(JDefinedClass output) {
        this.output = output;
    }

    /**
     * Returns the package of generated classes.
     * @return
     */
    protected JPackage getPackage() {
        return pkg;
    }

    /**
     * Sets the package of generated classes.
     */
    protected void setPackage(JPackage pkg) {
        this.pkg = pkg;
    }

    /**
     * Returns the outline of the class model this creator is working on.
     * @return
     */
    protected Outline getOutline() {
        return outline;
    }

    /**
     * Sets the outline of the class model this creator is working on.
     * @return
     */
    protected void setOutline(Outline aOutline) {
        this.outline = aOutline;
    }
}
