/*
 * Copyright 2010-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.jet.codegen.binding;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.lang.descriptors.ClassDescriptor;
import org.jetbrains.jet.lang.descriptors.DeclarationDescriptor;
import org.jetbrains.jet.lang.psi.*;
import org.jetbrains.jet.lang.resolve.BindingContext;
import org.jetbrains.jet.lang.resolve.BindingTrace;
import org.jetbrains.jet.lang.resolve.java.JvmAbi;
import org.jetbrains.jet.lang.resolve.java.JvmClassName;
import org.jetbrains.jet.lang.resolve.name.FqName;
import org.jetbrains.jet.lang.resolve.name.Name;

import static org.jetbrains.jet.lang.resolve.BindingContextUtils.descriptorToDeclaration;

public final class PsiCodegenPredictor {
    private PsiCodegenPredictor() {
    }

    public static boolean checkPredictedNameFromPsi(
            @NotNull BindingTrace bindingTrace, @NotNull DeclarationDescriptor descriptor, JvmClassName nameFromDescriptors
    ) {
        PsiElement element = descriptorToDeclaration(bindingTrace.getBindingContext(), descriptor);
        if (element instanceof JetDeclaration) {
            JvmClassName classNameFromPsi = getPredefinedJvmClassName((JetDeclaration) element);
            assert classNameFromPsi == null || classNameFromPsi.equals(nameFromDescriptors) : "Invalid algorithm for getting qualified name from psi!";
        }

        return true;
    }

    @Nullable
    public static JvmClassName getPredefinedJvmClassName(@NotNull JetFile jetFile, boolean withNamespace) {
        String packageName = jetFile.getPackageName();
        if (packageName == null) {
            return null;
        }

        JvmClassName packageJvmName = JvmClassName.byFqNameWithoutInnerClasses(packageName);
        return !withNamespace ? packageJvmName : addPackageClass(packageJvmName);
    }

    /**
     * TODO: Finish this method for all cases. Now it's only used and tested in JetLightClass.
     *
     * @return null if no prediction can be done.
     */
    @Nullable
    public static JvmClassName getPredefinedJvmClassName(@NotNull JetDeclaration declaration) {
        // TODO: Method won't work for declarations inside class objects
        // TODO: Method won't give correct class name for traits implementations

        JetDeclaration parentDeclaration = PsiTreeUtil.getParentOfType(declaration, JetDeclaration.class);
        JvmClassName parentClassName = parentDeclaration != null ?
                                       getPredefinedJvmClassName(parentDeclaration) :
                                       getPredefinedJvmClassName((JetFile) declaration.getContainingFile(), false);
        if (parentClassName == null) {
            return null;
        }

        if (declaration instanceof JetClassObject) {
            // Get parent and assign Class object prefix
            return JvmClassName.byInternalName(parentClassName.getInternalName() + JvmAbi.CLASS_OBJECT_SUFFIX);
        }

        if (declaration instanceof JetNamedDeclaration) {
            if (!PsiTreeUtil.instanceOf(declaration, JetClass.class, JetObjectDeclaration.class, JetNamedFunction.class, JetProperty.class) ||
                    declaration instanceof JetEnumEntry) {
                // Other subclasses are not valid for class name prediction.
                // For example EnumEntry, JetFunctionLiteral
                return null;
            }

            JetNamedDeclaration namedDeclaration = (JetNamedDeclaration) declaration;
            Name name = namedDeclaration.getNameAsName();
            if (name == null) {
                return null;
            }

            FqName fqName = parentClassName.getFqName();

            if (declaration instanceof JetNamedFunction) {
                if (parentDeclaration == null) {
                    JvmClassName packageClass = addPackageClass(parentClassName);
                    return JvmClassName.byInternalName(packageClass.getInternalName() + "$" + name.getName());
                }

                if (!(parentDeclaration instanceof JetClass || parentDeclaration instanceof JetObjectDeclaration)) {
                    // Can't generate predefined name for internal functions
                    return null;
                }
            }

            // NOTE: looks like a bug - for class in getter of top level property class name will be $propertyName$ClassName but not
            // namespace$propertyName$ClassName
            if (declaration instanceof JetProperty) {
                return JvmClassName.byInternalName(parentClassName.getInternalName() + "$" + name.getName());
            }

            if (fqName.isRoot()) {
                return JvmClassName.byInternalName(name.getName());
            }

            return JvmClassName.byInternalName(parentDeclaration == null ?
                                               parentClassName.getInternalName() + "/" + name.getName() :
                                               parentClassName.getInternalName() + "$" + name.getName());
        }

        return null;
    }

    private static JvmClassName addPackageClass(JvmClassName packageName) {
        FqName name = packageName.getFqName();
        return name.isRoot() ?
               JvmClassName.byFqNameWithoutInnerClasses(JvmAbi.PACKAGE_CLASS) :
               JvmClassName.byInternalName(packageName.getInternalName() + "/" + JvmAbi.PACKAGE_CLASS);
    }

    public static boolean checkPredictedClassNameForFun(
            BindingContext bindingContext, @NotNull DeclarationDescriptor descriptor,
            ClassDescriptor classDescriptor
    ) {
        PsiElement element = descriptorToDeclaration(bindingContext, descriptor);
        PsiElement classDeclaration = descriptorToDeclaration(bindingContext, classDescriptor);
        if (element instanceof JetNamedFunction && classDeclaration instanceof JetDeclaration) {
            JvmClassName classNameFromPsi = getPredefinedJvmClassName((JetDeclaration) classDeclaration);
            JvmClassName classNameForFun = getPredefinedJvmClassNameForFun((JetNamedFunction) element);
            assert classNameForFun == null || classNameForFun.equals(classNameFromPsi) : "Invalid algorithm for getting enclosing method name!";
        }

        return true;
    }

    @Nullable
    public static JvmClassName getPredefinedJvmClassNameForFun(@NotNull JetNamedFunction function) {
        PsiElement parent = function.getParent();
        if (parent instanceof JetFile) {
            return getPredefinedJvmClassName((JetFile) parent, true);
        }

        @SuppressWarnings("unchecked")
        JetClass containingClass = PsiTreeUtil.getParentOfType(function, JetClass.class, true, JetDeclaration.class);
        if (containingClass != null) {
            return getPredefinedJvmClassName(containingClass);
        }

        @SuppressWarnings("unchecked")
        JetObjectDeclaration objectDeclaration = PsiTreeUtil.getParentOfType(function, JetObjectDeclaration.class, true, JetDeclaration.class);
        if (objectDeclaration != null) {
            if (objectDeclaration.getParent() instanceof JetClassObject) {
                return getPredefinedJvmClassName((JetClassObject) objectDeclaration.getParent());
            }

            return getPredefinedJvmClassName(objectDeclaration);
        }

        return null;
    }
}
