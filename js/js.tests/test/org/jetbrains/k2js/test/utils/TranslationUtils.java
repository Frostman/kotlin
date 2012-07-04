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

package org.jetbrains.k2js.test.utils;

import closurecompiler.internal.com.google.common.collect.Maps;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.dart.compiler.backend.js.ast.JsProgram;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.analyzer.AnalyzeExhaust;
import org.jetbrains.jet.lang.psi.JetFile;
import org.jetbrains.jet.lang.resolve.BindingContext;
import org.jetbrains.k2js.analyze.AnalyzerFacadeForJS;
import org.jetbrains.k2js.config.Config;
import org.jetbrains.k2js.config.EcmaVersion;
import org.jetbrains.k2js.facade.K2JSTranslator;
import org.jetbrains.k2js.facade.MainCallParameters;
import org.jetbrains.k2js.generate.CodeGenerator;
import org.jetbrains.k2js.test.config.TestConfig;
import org.jetbrains.k2js.utils.JetFileUtils;

import java.io.*;
import java.util.List;
import java.util.Map;

import static org.jetbrains.k2js.utils.JetFileUtils.createPsiFileList;

/**
 * @author Pavel Talanov
 */
public final class TranslationUtils {

    private TranslationUtils() {
    }

    @NotNull
    private static final Map<EcmaVersion, Config> testConfigs = Maps.newHashMap();

    @Nullable
    private static BindingContext libraryContext = null;

    @Nullable
    private static List<JetFile> libFiles = null;

    @NotNull
    private static List<JetFile> getAllLibFiles(@NotNull Project project) {
        if (libFiles == null) {
            libFiles = initLibFiles(project);
        }
        return libFiles;
    }

    @NotNull
    public static BindingContext getLibraryContext(@NotNull Project project) {
        if (libraryContext == null) {
            List<JetFile> allLibFiles = getAllLibFiles(project);
            Predicate<PsiFile> filesWithCode = new Predicate<PsiFile>() {
                @Override
                public boolean apply(@javax.annotation.Nullable PsiFile file) {
                    return isFileWithCode((JetFile) file);
                }
            };
            AnalyzeExhaust exhaust = AnalyzerFacadeForJS
                    .analyzeFiles(allLibFiles, filesWithCode, Config.getEmptyConfig(project));
            libraryContext = exhaust.getBindingContext();
            AnalyzerFacadeForJS.checkForErrors(allLibFiles, libraryContext);
        }
        return libraryContext;
    }

    private static boolean isFileWithCode(@NotNull JetFile file) {
        for (String filename : Config.LIB_FILES_WITH_CODE) {
            if (file.getName().contains(filename)) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    public static Config getConfig(@NotNull Project project, @NotNull EcmaVersion version) {
        Config config = testConfigs.get(version);
        if (config == null) {
            BindingContext preanalyzedContext = getLibraryContext(project);
            config = new TestConfig(project, version, getLibFilesWithCode(getAllLibFiles(project)), preanalyzedContext);
            testConfigs.put(version, config);
        }
        return config;
    }

    public static void translateFiles(@NotNull Project project, @NotNull List<String> inputFiles,
            @NotNull String outputFile,
            @NotNull MainCallParameters mainCallParameters,
            @NotNull EcmaVersion version) throws Exception {
        List<JetFile> psiFiles = createPsiFileList(inputFiles, project);
        JsProgram program = getTranslator(project, version).generateProgram(psiFiles, mainCallParameters);
        FileWriter writer = new FileWriter(new File(outputFile));
        try {
            writer.write(CodeGenerator.generateProgramToString(program));
        }
        finally {
            writer.close();
        }
    }

    @NotNull
    private static K2JSTranslator getTranslator(@NotNull Project project, @NotNull EcmaVersion version) {
        return new K2JSTranslator(getConfig(project, version));
    }

    @NotNull
    private static List<JetFile> initLibFiles(@NotNull Project project) {
        return getLibFiles(project, Config.LIB_FILE_NAMES);
    }

    @NotNull
    private static List<JetFile> getLibFilesWithCode(@NotNull List<JetFile> allFiles) {
        List<JetFile> result = Lists.newArrayList();
        for (JetFile file : allFiles) {
            if (isFileWithCode(file)) {
                result.add(file);
            }
        }
        return result;
    }

    @NotNull
    private static List<JetFile> getLibFiles(@NotNull Project project, @NotNull List<String> list) {
        List<JetFile> libFiles = Lists.newArrayList();
        for (String libFileName : list) {
            JetFile file = null;
            try {
                @SuppressWarnings("IOResourceOpenedButNotSafelyClosed")
                InputStream stream = new FileInputStream(Config.LIBRARIES_LOCATION + libFileName);
                try {
                    String text = FileUtil.loadTextAndClose(stream);
                    file = JetFileUtils.createPsiFile(libFileName, text, project);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                libFiles.add(file);
            }
            catch (Exception e) {
                //TODO: throw generic exception
                throw new IllegalStateException(e);
            }
        }
        return libFiles;
    }
}
