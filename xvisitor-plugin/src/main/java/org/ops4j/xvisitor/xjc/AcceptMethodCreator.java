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

import static com.sun.codemodel.JExpr._null;
import static com.sun.codemodel.JExpr._this;
import static com.sun.codemodel.JExpr.cast;
import static com.sun.codemodel.JExpr.invoke;
import static com.sun.codemodel.JExpr.ref;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBElement;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JForEach;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import com.sun.tools.xjc.outline.Outline;

public class AcceptMethodCreator {

    private static final JType[] NONE = new JType[0];

    private JDefinedClass visitorAction;
    private JDefinedClass visitable;
    private Outline outline;

    private Set<JType> traversableTypes = new HashSet<JType>();
    private JClass stringClass;

    public AcceptMethodCreator(JDefinedClass visitorAction, JDefinedClass visitable, Outline outline) {
        this.visitorAction = visitorAction;
        this.visitable = visitable;
        this.outline = outline;
        this.stringClass = visitable.owner().ref(String.class);
    }

    public void run(Set<ClassOutline> sorted, JDefinedClass visitor) {
        collectTraversableTypes();

        for (ClassOutline classOutline : sorted) {

            JDefinedClass beanImpl = classOutline.implClass;
            JMethod acceptMethod = beanImpl.method(JMod.PUBLIC, visitorAction, "accept");
            JVar vizParam = acceptMethod.param(visitor, "visitor");
            JBlock block = acceptMethod.body();
            JVar result = block.decl(visitorAction, "result",
                    vizParam.invoke("enter").arg(_this()));

            List<FieldOutline> fields = findAllDeclaredAndInheritedFields(classOutline);
            for (FieldOutline field : fields) {
                visitField(field, block, vizParam, result);
            }

            JVar leaveResult = block.decl(visitorAction, "leaveResult", invoke(vizParam, "leave").arg(_this()));
            JConditional conditional = block._if(leaveResult.eq(visitorAction.enumConstant("TERMINATE")));
            conditional._then().
                assign(result, leaveResult);
            conditional._elseif(result.eq(visitorAction.enumConstant("SKIP")))._then().
                assign(result, visitorAction.enumConstant("CONTINUE"));
            block._return(result);
        }
    }

    private void collectTraversableTypes() {
        for (ClassOutline classOutline : outline.getClasses()) {
            traversableTypes.add(classOutline.implClass);
        }
    }

    private void visitField(FieldOutline field, JBlock block, JVar vizParam, JVar result) {
        JMethod getter = getter(field);
        boolean isCollection = field.getPropertyInfo().isCollection();
        if (isCollection) {
            visitCollectionField(field, block, vizParam, result, getter);
        }
        else if (isTraversable(field.getRawType())) {
            visitSimpleField(field, block, vizParam, result, getter);
        }
    }

    private void visitCollectionField(FieldOutline field, JBlock block, JVar vizParam, JVar result,
            JMethod getter) {
        JType rawType = field.getRawType();
        JClass collClazz = (JClass) rawType;
        JClass collType = collClazz.getTypeParameters().get(0);
        if (traversableTypes.contains(collType)) {
            JForEach forEach = block.forEach((collClazz).getTypeParameters().get(0), "bean",
                    invoke(getter));
            JBlock body = forEach.body();
            body.assign(result, invoke(ref("bean"), "accept").arg(vizParam));
            body._if(result.ne(visitorAction.enumConstant("CONTINUE")))._then()._break();
        }
        else if (isAnyType(collType)) {
            JForEach forEach = block.forEach(collType, "obj", invoke(getter));
            JConditional conditional = forEach.body()._if(ref("obj")._instanceof(visitable));
            conditional._then().invoke(cast(visitable, ref("obj")), "accept")
                    .arg(vizParam);
            conditional._elseif(ref("obj")._instanceof(stringClass))._then()
                    .invoke(vizParam, "visit").arg(cast(stringClass, ref("obj")));
        }
    }

    private void visitSimpleField(FieldOutline field, JBlock block, JVar vizParam, JVar result,
            JMethod getter) {
        JInvocation visitable = null;
        if (isJAXBElement(field.getRawType())) {
            visitable = invoke(getter).invoke("getValue");
        }
        else {
            visitable = invoke(getter);
        }
        JInvocation resultExpr = invoke(visitable, "accept").arg(vizParam);
        block._if(result.eq(visitorAction.enumConstant("CONTINUE")))._then()
                ._if(visitable.ne(_null()))._then().assign(result, resultExpr);
    }

    private List<FieldOutline> findAllDeclaredAndInheritedFields(ClassOutline classOutline) {
        List<FieldOutline> fields = new LinkedList<FieldOutline>();
        ClassOutline currentClassOutline = classOutline;
        while (currentClassOutline != null) {
            fields.addAll(Arrays.asList(currentClassOutline.getDeclaredFields()));
            currentClassOutline = currentClassOutline.getSuperClass();
        }
        return fields;
    }

    /**
     * Returns true if the type is something that we should traverse. We want to traverse all of the
     * beans that were generated. We also include JAXBElement and collections of beans.
     * 
     * @param rawType
     */
    private boolean isTraversable(JType rawType) {
        if (traversableTypes.contains(rawType))
            return true;
        if (rawType.isPrimitive())
            return false;
        JClass clazz = (JClass) rawType;
        for (JClass arg : clazz.getTypeParameters()) {
            if (traversableTypes.contains(arg))
                return true;
        }

        return false;
    }

    /**
     * Returns true if the type is a JAXBElement. In the case of JAXBElements, we want to traverse
     * its underlying value as opposed to the JAXBElement.
     * 
     * @param type
     */
    private boolean isJAXBElement(JType type) {
        if (type.fullName().startsWith(JAXBElement.class.getName())) {
            return true;
        }
        return false;
    }

    /**
     * Checks if a collection contains an "any" type. Normally, this is {@code java.lang.Object},
     * but when the {@code serializable} customization is active, this will be
     * {@code java.io.Serializable}.
     * 
     * @param collType
     *            collection element type
     * @return true if type is Object or Serializable
     */
    private boolean isAnyType(JClass collType) {
        String fullName = collType.fullName();
        return fullName.equals("java.lang.Object") || fullName.equals("java.io.Serializable");
    }

    /**
     * Borrowed this code from jaxb-commons project
     * 
     * @param fieldOutline
     */
    private static JMethod getter(FieldOutline fieldOutline) {
        JDefinedClass theClass = fieldOutline.parent().implClass;
        String publicName = fieldOutline.getPropertyInfo().getName(true);
        JMethod getGetter = theClass.getMethod("get" + publicName, NONE);
        if (getGetter != null) {
            return getGetter;
        }
        else {
            JMethod isGetter = theClass.getMethod("is" + publicName, NONE);
            if (isGetter != null) {
                return isGetter;
            }
            else {
                return null;
            }
        }
    }
}
