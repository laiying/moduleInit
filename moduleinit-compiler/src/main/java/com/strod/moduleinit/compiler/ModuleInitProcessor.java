package com.strod.moduleinit.compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import com.strod.moduleinit.annotation.ModuleInit;
import com.strod.moduleinit.compiler.utils.Logger;
import com.strod.moduleinit.compiler.utils.StringUtils;
import com.strod.moduleinit.compiler.utils.TypeUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static javax.lang.model.element.Modifier.PUBLIC;

@AutoService(Processor.class)
public class ModuleInitProcessor extends AbstractProcessor {

    private Filer mFiler;
    private Elements mElementUtils;
    private Types types;
    private TypeUtils typeUtils;

    private Logger logger;

//    private List<String> moduleInitLists;
    private Map<Integer, Element> moduleInits = new TreeMap<>();
    String moduleName = null;
    // Options of processor
    public static final String KEY_MODULE_NAME = "MODULE_NAME";

    Map<String, String> groupMap = new HashMap<>(); // ModuleName and ModuleInit.

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        mFiler = processingEnv.getFiler();
        mElementUtils = processingEnv.getElementUtils();
        types = processingEnv.getTypeUtils();
        logger = new Logger(processingEnv.getMessager());
        typeUtils = new TypeUtils(types, mElementUtils, logger);

        // Attempt to get user configuration [moduleName]
        Map<String, String> options = processingEnv.getOptions();
        if (options != null && !options.isEmpty()) {
            moduleName = options.get(KEY_MODULE_NAME);
        }

        if (StringUtils.isNotEmpty(moduleName)) {
            moduleName = moduleName.replaceAll("[^0-9a-zA-Z_]+", "");
            logger.info("The user has configuration the module name, it was [" + moduleName + "]");
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(ModuleInit.class.getCanonicalName());
        return annotations;
    }

    @Override
    public Set<String> getSupportedOptions() {
        return new HashSet<String>() {{
            this.add(KEY_MODULE_NAME);
        }};
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        moduleInits.clear();
        if (annotations != null && !annotations.isEmpty()) {
            Set<? extends Element> routeElements = roundEnv.getElementsAnnotatedWith(ModuleInit.class);
            try {
                logger.info(">>> Found ModuleInit, start... <<<");
                this.parseModuleInit(routeElements);

            } catch (Exception e) {
                logger.error(e);
            }
            return true;
        }

        return false;

    }

    private void parseModuleInit(Set<? extends Element> elements) throws IOException {
        if (elements != null && !elements.isEmpty()) {
            logger.info(">>> Found ModuleInits, size is " + elements.size() + " <<<");
            for (Element element : elements) {
                //1.package name
                PackageElement packageElement = mElementUtils.getPackageOf(element);
                String pkgName = packageElement.getQualifiedName().toString();
                logger.info(String.format("package = %s", pkgName));


                //2.package type
//                TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
//                String clsName = enclosingElement.getQualifiedName().toString();
//                logger.info(String.format("clsName = %s", clsName));

//            Element enclosingElement = element.getEnclosingElement();
                if (element instanceof TypeElement) {//类型元素，注意枚举对应的时类，注解对应的接口
                    logger.info("Element is TypeElement");

                    TypeElement typeElement = (TypeElement) element;
                    logger.info(String.format("TypeElement = %s", typeElement));


//                for (Element e : typeElement.getEnclosedElements()){ // iterate over children
//                    Element parent = e.getEnclosingElement();  // parent == fooClass
//                }

                    String enclosingName = typeElement.getQualifiedName().toString();
                    logger.info(String.format("enclosingName = %s", enclosingName));

                    ElementKind elementKind = typeElement.getKind();
                    logger.info(String.format("elementKind = %s", elementKind.toString()));
                    logger.info(String.format("elementKind isField: %s", elementKind.isField()));
                    logger.info(String.format("elementKind isClass: %s", elementKind.isClass()));
                    logger.info(String.format("elementKind isInterface: %s", elementKind.isInterface()));

                    TypeMirror typeMirror = element.asType();
                    logger.info(String.format("typeMirror = %s", typeMirror.toString()));

                    TypeKind typeKind = typeMirror.getKind();
                    logger.info(String.format("TypeKind = %s", typeKind.toString()));

                    Set<Modifier> modifiers = typeElement.getModifiers();
                    for (Modifier modifier : modifiers) {
                        logger.info(String.format("modifier = %s", modifier.toString()));
                    }

                    ModuleInit moduleInit = typeElement.getAnnotation(ModuleInit.class);
                    int priority = moduleInit.priority();
                    String name = moduleInit.name();
                    logger.info(String.format("priority = %d", priority));
                    logger.info(String.format("name = %s", name));

                    boolean isPrimitive = typeKind.isPrimitive();
                    logger.info(String.format("isPrimitive = %s", isPrimitive));

                    Element lastModuleInit = moduleInits.get(moduleInit.priority());
                    if (null != lastModuleInit) { // Added, throw exceptions
                        throw new IllegalArgumentException(
                                String.format(Locale.getDefault(), "More than one moduleInits use same priority [%d], They are [%s] and [%s].",
                                        moduleInit.priority(),
                                        lastModuleInit.getSimpleName(),
                                        element.getSimpleName())
                        );
                    }

                    moduleInits.put(moduleInit.priority(), element);

                    logger.info("--------------------");

                }
            }

            groupMap.put(moduleName, ModuleInitConsts.PACKAGE_OF_GENERATE_FILE + "."+ ModuleInitConsts.PROJECT + ModuleInitConsts.SEPARATOR + ModuleInitConsts.ROOT  + ModuleInitConsts.SEPARATOR + moduleName);

            // Interface of ModuleInit.
            TypeElement type_ITollgate = mElementUtils.getTypeElement(ModuleInitConsts.MODULEINIT_ROOT);
            TypeElement type_IModuleInit = mElementUtils.getTypeElement(ModuleInitConsts.MODULE_INIT);

            /**
             *  Build input type, format as :
             *
             *  ```Map<Integer, Class<? extends ITollgate>>```
             */
            ParameterizedTypeName inputMapTypeOfTollgate = ParameterizedTypeName.get(
                    ClassName.get(Map.class),
                    ClassName.get(Integer.class),
                    ParameterizedTypeName.get(
                            ClassName.get(Class.class),
                            WildcardTypeName.subtypeOf(ClassName.get(type_IModuleInit))
                    )
            );

            // Build input param name.
            ParameterSpec routersParamSpec = ParameterSpec.builder(inputMapTypeOfTollgate, "module").build();

            // Build method : 'loadInto'
            MethodSpec.Builder loadRoutersMethodOfTollgateBuilder = MethodSpec.methodBuilder(ModuleInitConsts.METHOD_LOAD_INTO)
                    .addAnnotation(Override.class)
                    .addModifiers(PUBLIC)
                    .addParameter(routersParamSpec);
            // Generate
            if (null != moduleInits && moduleInits.size() > 0) {
                // Build method body
                for (Map.Entry<Integer, Element> entry : moduleInits.entrySet()) {
                    loadRoutersMethodOfTollgateBuilder.addStatement("module.put(" + entry.getKey() + ", $T.class)", ClassName.get((TypeElement) entry.getValue()));
                }
            }

            // Write to disk(Write file even mappings is empty.)
            JavaFile.builder(ModuleInitConsts.PACKAGE_OF_GENERATE_FILE,
                    TypeSpec.classBuilder(ModuleInitConsts.PROJECT + ModuleInitConsts.SEPARATOR + ModuleInitConsts.ROOT + ModuleInitConsts.SEPARATOR + moduleName)
                            .addModifiers(PUBLIC)
                            .addJavadoc(ModuleInitConsts.WARNING_TIPS)
                            .addMethod(loadRoutersMethodOfTollgateBuilder.build())
                            .addSuperinterface(ClassName.get(type_ITollgate))
                            .build()
            ).build().writeTo(mFiler);


            //Group
            /**
             *  Build input type, format as :
             *
             *  ```Map<String, String>```
             */
            /*ParameterizedTypeName inputMapTypeOfModules = ParameterizedTypeName.get(
                    ClassName.get(Map.class),
                    ClassName.get(String.class),
                    ClassName.get(String.class)
            );

            // Build input param name.
            ParameterSpec modulesParamSpec = ParameterSpec.builder(inputMapTypeOfModules, "modules").build();

            // Build method loadRouters
            MethodSpec.Builder loadModulesMethodOfTollgateBuilder = MethodSpec.methodBuilder(ModuleInitConsts.METHOD_LOAD_INTO)
                    .addAnnotation(Override.class)
                    .addModifiers(PUBLIC)
                    .addParameter(modulesParamSpec);
            // Generate
            if (null != groupMap && groupMap.size() > 0) {
                // Build method body
                for (Map.Entry<String, String> entry : groupMap.entrySet()) {
                    loadModulesMethodOfTollgateBuilder.addStatement("modules.put($S, $S)", entry.getKey(), entry.getValue());
                }
            }

            // Write to disk(Write file even mappings is empty.)
            JavaFile.builder(ModuleInitConsts.PACKAGE_OF_GENERATE_FILE,
                    TypeSpec.classBuilder(ModuleInitConsts.PROJECT + ModuleInitConsts.SEPARATOR + ModuleInitConsts.GROUP + ModuleInitConsts.SEPARATOR + moduleName)
                            .addModifiers(PUBLIC)
                            .addJavadoc(ModuleInitConsts.WARNING_TIPS)
                            .addMethod(loadModulesMethodOfTollgateBuilder.build())
                            .addSuperinterface(ClassName.get(type_ITollgateGroup))
                            .build()
            ).build().writeTo(mFiler);*/

        }
    }
}
