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

package org.jetbrains.jet.resolve;

import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.testFramework.LightCodeInsightTestCase;
import junit.framework.Assert;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.plugin.PluginTestCaseBase;
import org.jetbrains.jet.testing.ReferenceUtils;

import java.io.File;

/**
 * @author Nikolay Krasko
 */
public class ResolveBaseTest extends LightCodeInsightTestCase {
    public void testMultiResolve() throws Exception {
        doMultiResolveTest();
    }

    public void testResolveClass() throws Exception {
        doSingleResolveTest("(<root>).Test");
    }

    public void testResolvePackageInProperty() throws Exception {
        doSingleResolveTest("test1");
    }

    public void testSeveralOverrides() throws Exception {
        doMultiResolveTest();
    }

    protected void doSingleResolveTest(@Nullable String referenceToString) throws Exception {
        final String testName = getTestName(false);
        configureByFile(testName + ".kt");

        int offset = getEditor().getCaretModel().getOffset();
        final PsiReference psiReference = getFile().findReferenceAt(offset);
        if (psiReference != null) {
            PsiElement resolvedTo = psiReference.resolve();
            if (resolvedTo != null) {
                String resolvedToElementStr = ReferenceUtils.renderAsGotoImplementation(resolvedTo);
                String notEqualMessage = String.format("Found reference to '%s', but '%s' was expected", resolvedToElementStr,
                                              referenceToString != null ? referenceToString : "<null>");
                assertEquals(notEqualMessage, referenceToString, resolvedToElementStr);
            }
            else {
                Assert.assertNull(
                        String.format("Element %s wasn't resolved to anything, but %s was expected", psiReference, referenceToString),
                        referenceToString);
            }
        }
        else {
            Assert.assertNull(String.format("No reference found at offset: %s, but one resolved to %s was expected", offset, referenceToString),
                              referenceToString);
        }
    }

    protected void doMultiResolveTest() throws Exception {
        final String testName = getTestName(false);
        configureByFile(testName + ".kt");

        final PsiReference psiReference =
                getFile().findReferenceAt(getEditor().getCaretModel().getOffset());

        assertTrue(psiReference instanceof PsiPolyVariantReference);

        final PsiPolyVariantReference variantReference = (PsiPolyVariantReference) psiReference;

        PsiElement element = variantReference.resolve();
        final ResolveResult[] results = variantReference.multiResolve(true);
        for (ResolveResult result : results) {
            assertNotNull(result);
        }
        assertTrue("Nothing resolved by reference '" + psiReference.getElement().getText() + "'.", element != null || results.length > 0);
    }

    @Override
    protected Sdk getProjectJDK() {
        return PluginTestCaseBase.jdkFromIdeaHome();
    }

    @Override
    protected String getTestDataPath() {
        return new File(PluginTestCaseBase.getTestDataPathBase(), "/resolve/").getPath() + File.separator;
    }
}
