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

package org.jetbrains.jet.plugin.references;

import com.google.common.collect.Lists;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.lang.descriptors.*;
import org.jetbrains.jet.lang.descriptors.annotations.AnnotationDescriptor;
import org.jetbrains.jet.lang.psi.JetFile;
import org.jetbrains.jet.lang.psi.JetReferenceExpression;
import org.jetbrains.jet.lang.resolve.*;
import org.jetbrains.jet.lang.resolve.name.FqName;
import org.jetbrains.jet.lang.resolve.name.Name;
import org.jetbrains.jet.lang.resolve.scopes.JetScope;
import org.jetbrains.jet.lang.resolve.scopes.RedeclarationHandler;
import org.jetbrains.jet.lang.resolve.scopes.WritableScope;
import org.jetbrains.jet.lang.resolve.scopes.WritableScopeImpl;
import org.jetbrains.jet.lang.types.lang.JetStandardClasses;
import org.jetbrains.jet.resolve.DescriptorRenderer;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class StandardLibraryReferenceResolver extends AbstractProjectComponent {
    @SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized")
    private BindingContext bindingContext = null;

    private final Object lock = new Object();
    private static final FqName TUPLE0_FQ_NAME = DescriptorUtils.getFQName(JetStandardClasses.getTuple(0)).toSafe();

    public StandardLibraryReferenceResolver(Project project) {
        super(project);
    }

    @Override
    public void initComponent() {
        StartupManager.getInstance(myProject).registerPostStartupActivity(new Runnable() {
            @Override
            public void run() {
                ensureInitialized();
            }
        });
    }

    private void ensureInitialized() {
        synchronized (lock) {
            if (bindingContext != null) {
                return;
            }

            BindingTraceContext context = new BindingTraceContext();
            FakeJetNamespaceDescriptor jetNamespace = new FakeJetNamespaceDescriptor();
            context.record(BindingContext.FQNAME_TO_NAMESPACE_DESCRIPTOR, JetStandardClasses.STANDARD_CLASSES_FQNAME, jetNamespace);

            WritableScopeImpl scope = new WritableScopeImpl(JetScope.EMPTY, jetNamespace, RedeclarationHandler.THROW_EXCEPTION,
                                                            "Builtin classes scope");
            scope.changeLockLevel(WritableScope.LockLevel.BOTH);
            jetNamespace.setMemberScope(scope);

            TopDownAnalyzer.processStandardLibraryNamespace(myProject, context, scope, jetNamespace, getJetFiles("jet.src"));

            ClassDescriptor tuple0 = context.get(BindingContext.FQNAME_TO_CLASS_DESCRIPTOR, TUPLE0_FQ_NAME);
            assert tuple0 != null;
            scope = new WritableScopeImpl(scope, jetNamespace, RedeclarationHandler.THROW_EXCEPTION,
                                          "Builtin classes scope: needed to analyze builtins which depend on Unit type alias");
            scope.changeLockLevel(WritableScope.LockLevel.BOTH);
            scope.addClassifierAlias(JetStandardClasses.UNIT_ALIAS, tuple0);
            jetNamespace.setMemberScope(scope);

            TopDownAnalyzer.processStandardLibraryNamespace(myProject, context, scope, jetNamespace, getJetFiles("jet"));
            AnalyzingUtils.throwExceptionOnErrors(context.getBindingContext());

            bindingContext = context.getBindingContext();
        }
    }

    private List<JetFile> getJetFiles(String dir) {
        URL url = StandardLibraryReferenceResolver.class.getResource("/" + dir + "/");
        VirtualFile vf = VfsUtil.findFileByURL(url);
        assert vf != null;

        PsiDirectory psiDirectory = PsiManager.getInstance(myProject).findDirectory(vf);
        assert psiDirectory != null;
        return ContainerUtil.mapNotNull(psiDirectory.getFiles(), new Function<PsiFile, JetFile>() {
            @Override
            public JetFile fun(PsiFile file) {
                return file instanceof JetFile ? (JetFile) file : null;
            }
        });
    }

    @Nullable
    private DeclarationDescriptor findCurrentDescriptorForClass(@NotNull ClassDescriptor originalDescriptor) {
        if (originalDescriptor.getKind().isObject()) {
            DeclarationDescriptor currentParent = findCurrentDescriptor(originalDescriptor.getContainingDeclaration());
            if (currentParent == null) return null;
            return ((ClassDescriptor) currentParent).getClassObjectDescriptor();
        }
        else {
            return bindingContext.get(BindingContext.FQNAME_TO_CLASS_DESCRIPTOR, DescriptorUtils.getFQName(originalDescriptor).toSafe());
        }
    }

    @Nullable
    private DeclarationDescriptor findCurrentDescriptorForMember(@NotNull MemberDescriptor originalDescriptor) {
        DeclarationDescriptor containingDeclaration = findCurrentDescriptor(originalDescriptor.getContainingDeclaration());
        JetScope memberScope = getMemberScope(containingDeclaration);
        if (memberScope == null) return null;

        String renderedOriginal = DescriptorRenderer.TEXT.render(originalDescriptor);
        Collection<? extends DeclarationDescriptor> descriptors;
        if (originalDescriptor instanceof ConstructorDescriptor && containingDeclaration instanceof ClassDescriptor) {
            descriptors = ((ClassDescriptor) containingDeclaration).getConstructors();
        }
        else {
            descriptors = memberScope.getAllDescriptors();
        }
        for (DeclarationDescriptor member : descriptors) {
            if (renderedOriginal.equals(DescriptorRenderer.TEXT.render(member).replace(TUPLE0_FQ_NAME.getFqName(),
                                                                                       JetStandardClasses.UNIT_ALIAS.getName()))) {
                return member;
            }
        }
        return null;
    }

    @Nullable
    private DeclarationDescriptor findCurrentDescriptor(@NotNull DeclarationDescriptor originalDescriptor) {
        if (originalDescriptor instanceof ClassDescriptor) {
            return findCurrentDescriptorForClass((ClassDescriptor) originalDescriptor);
        }
        else if (originalDescriptor instanceof NamespaceDescriptor) {
            return bindingContext.get(BindingContext.FQNAME_TO_NAMESPACE_DESCRIPTOR,
                                      DescriptorUtils.getFQName(originalDescriptor).toSafe());
        }
        else if (originalDescriptor instanceof MemberDescriptor) {
            return findCurrentDescriptorForMember((MemberDescriptor) originalDescriptor);
        }
        else {
            return null;
        }
    }

    @NotNull
    public Collection<PsiElement> resolveStandardLibrarySymbol(@NotNull BindingContext originalContext,
            @Nullable JetReferenceExpression referenceExpression) {
        ensureInitialized();
        DeclarationDescriptor declarationDescriptor = originalContext.get(BindingContext.REFERENCE_TARGET, referenceExpression);

        return declarationDescriptor != null ? resolveStandardLibrarySymbol(declarationDescriptor) : Collections.<PsiElement>emptyList();
    }

    @NotNull
    public Collection<PsiElement> resolveStandardLibrarySymbol(@NotNull DeclarationDescriptor declarationDescriptor) {
        ensureInitialized();
        DeclarationDescriptor descriptor = declarationDescriptor;

        descriptor = descriptor.getOriginal();
        descriptor = findCurrentDescriptor(descriptor);
        if (descriptor != null) {
            return BindingContextUtils.descriptorToDeclarations(bindingContext, descriptor);
        }
        return Collections.emptyList();
    }

    @Nullable
    private static JetScope getMemberScope(@Nullable DeclarationDescriptor parent) {
        if (parent instanceof ClassDescriptor) {
            return ((ClassDescriptor) parent).getDefaultType().getMemberScope();
        }
        else if (parent instanceof NamespaceDescriptor) {
            return ((NamespaceDescriptor)parent).getMemberScope();
        }
        else {
            return null;
        }
    }

    private static class FakeJetNamespaceDescriptor extends NamespaceDescriptorImpl {
        private WritableScope memberScope;

        private FakeJetNamespaceDescriptor() {
            super(new NamespaceDescriptorImpl(new ModuleDescriptor(Name.special("<fake_module>")),
                                              Collections.<AnnotationDescriptor>emptyList(), Name.special("<root>")),
                  Collections.<AnnotationDescriptor>emptyList(),
                  JetStandardClasses.STANDARD_CLASSES_NAMESPACE.getName());
        }

        void setMemberScope(WritableScope memberScope) {
            this.memberScope = memberScope;
        }

        @NotNull
        @Override
        public WritableScope getMemberScope() {
            return memberScope;
        }
    }
}
