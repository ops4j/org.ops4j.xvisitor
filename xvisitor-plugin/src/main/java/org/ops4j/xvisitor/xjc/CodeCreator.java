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

public abstract class CodeCreator {

    protected JPackage pakkage;
    protected Outline outline;
    private JDefinedClass output;

    public CodeCreator(Outline outline, JPackage pkg) {
        setOutline(outline);
        setPackage(pkg);
    }

    protected abstract void run(Set<ClassOutline> aClasses);

    protected JDefinedClass getOutput() {
        return output;
    }

    protected void setOutput(JDefinedClass aOutput) {
        output = aOutput;
    }

    protected JPackage getPackage() {
        return pakkage;
    }

    protected void setPackage(JPackage aPackage) {
        pakkage = aPackage;
    }

    protected Outline getOutline() {
        return outline;
    }

    protected void setOutline(Outline aOutline) {
        outline = aOutline;
    }

}
