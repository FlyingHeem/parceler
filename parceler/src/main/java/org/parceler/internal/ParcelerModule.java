/**
 * Copyright 2013 John Ericksen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.parceler.internal;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import org.androidtransfuse.adapter.ASTFactory;
import org.androidtransfuse.adapter.ASTType;
import org.androidtransfuse.analysis.module.ModuleRepository;
import org.androidtransfuse.analysis.repository.AnalysisRepository;
import org.androidtransfuse.annotations.*;
import org.androidtransfuse.bootstrap.BootstrapModule;
import org.androidtransfuse.gen.FilerResourceWriter;
import org.androidtransfuse.gen.FilerSourceCodeWriter;
import org.androidtransfuse.gen.InjectionBuilderContextFactory;
import org.androidtransfuse.gen.invocationBuilder.DefaultInvocationBuilderStrategy;
import org.androidtransfuse.gen.invocationBuilder.InvocationBuilderStrategy;
import org.androidtransfuse.gen.variableDecorator.ExpressionDecoratorFactory;
import org.androidtransfuse.gen.variableDecorator.VariableExpressionBuilder;
import org.androidtransfuse.gen.variableDecorator.VariableExpressionBuilderFactory;
import org.androidtransfuse.transaction.*;
import org.androidtransfuse.util.Logger;
import org.androidtransfuse.util.MessagerLogger;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.lang.model.util.Elements;
import java.util.Map;

@BootstrapModule
@DefineScope(annotation = CodeGenerationScope.class, scope = ThreadLocalScope.class)
@Install({
        ASTFactory.class,
        VariableExpressionBuilderFactory.class,
        InjectionBuilderContextFactory.class})
public class ParcelerModule {

    public static final String PARCELS_TRANSACTION_WORKER = "parcelsTransactionWorker";
    public static final String PARCEL_TRANSACTION_WORKER = "parcelTransactionWorker";
    public static final String FACTORY_TRANSACTION_WORKER = "factoryTransactionWorker";
    public static final String FACTORIES_TRANSACTION_WORKER = "factoriessTransactionWorker";
    public static final String PACKAGE_HELPER_TRANSACTION_WORKER = "packageHelperTransactionWorker";
    public static final String COMPONENTS_TRANSACTION_WORKER = "componentsTransactionWorker";
    public static final String VIRTUAL_PROXY_TRANSACTION_WORKER = "virtualProxyTransactionWorker";
    public static final String SCOPES_UTIL_TRANSACTION_WORKER = "scopesUtilTransactionWorker";
    public static final String ORIGINAL_MANIFEST = "originalManifest";
    public static final String MANIFEST_FILE = "manifestFile";

    @Provides
    @CodeGenerationScope
    public JCodeModel getJCodeModel(){
        return new JCodeModel();
    }

    @Provides
    @Singleton
    public Elements getElements(ProcessingEnvironment processingEnvironment){
        return processingEnvironment.getElementUtils();
    }

    @Provides
    @Singleton
    public Messager getMessenger(ProcessingEnvironment processingEnvironment){
        return processingEnvironment.getMessager();
    }

    @Provides
    @Singleton
    public Logger getLogger(ProcessingEnvironment processingEnvironment){
        return new MessagerLogger(processingEnvironment.getMessager());
    }

    @Provides
    @Singleton
    public Filer getFiler(ProcessingEnvironment processingEnvironment){
        return processingEnvironment.getFiler();
    }

    @Provides
    @Singleton
    public ProcessingEnvironment getProcessingEnvironment(){
        throw new OutOfScopeException("Expected seeded object, unable to construct directly.");
    }

    @Provides
    @Named(PARCEL_TRANSACTION_WORKER)
    public TransactionWorker<Provider<ASTType>, JDefinedClass> getParcelTransactionWorker(JCodeModel codeModel,
                                                                                          FilerSourceCodeWriter codeWriter,
                                                                                          FilerResourceWriter resourceWriter,
                                                                                          ParcelTransactionWorker worker) {
        return new CodeGenerationScopedTransactionWorker<Provider<ASTType>, JDefinedClass>(codeModel, codeWriter, resourceWriter, worker);
    }

    @Provides
    @Named(PARCELS_TRANSACTION_WORKER)
    public TransactionWorker<Map<Provider<ASTType>, JDefinedClass>, Void> getParcelsTransactionWorker(JCodeModel codeModel,
                                                                                                      FilerSourceCodeWriter codeWriter,
                                                                                                      FilerResourceWriter resourceWriter,
                                                                                                      ParcelsTransactionWorker worker) {
        return new CodeGenerationScopedTransactionWorker<Map<Provider<ASTType>, JDefinedClass>, Void>(codeModel, codeWriter, resourceWriter, worker);
    }

    @Provides
    public ParcelProcessor getParcelProcessor(ParcelTransactionFactory parcelTransactionFactory,
                                              ParcelsTransactionFactory parcelsTransactionFactory) {

        TransactionProcessorPool<Provider<ASTType>, JDefinedClass> parcelProcessor =
                new TransactionProcessorPool<Provider<ASTType>, JDefinedClass>();
        TransactionProcessorPool<Map<Provider<ASTType>, JDefinedClass>, Void> parcelsProcessor =
                new TransactionProcessorPool<Map<Provider<ASTType>, JDefinedClass>, Void>();

        TransactionProcessor processor =
                new TransactionProcessorChannel<Provider<ASTType>, JDefinedClass, Void>(parcelProcessor, parcelsProcessor, parcelsTransactionFactory);

        return new ParcelProcessor(processor, parcelProcessor, parcelTransactionFactory);
    }
}