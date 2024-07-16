package com.annotation_compiler

import com.google.auto.service.AutoService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.wx.annotations.CreateService
import com.wx.annotations.PostBody
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.tools.Diagnostic


@AutoService(Processor::class)
class AptProcessor : AbstractProcessor() {

    private var mFiler: Filer? = null

    private var mElementUtils: Elements? = null
    private val gson by lazy { Gson() }

    override fun init(processingEnv: ProcessingEnvironment?) {
        super.init(processingEnv)
        mFiler = processingEnv?.filer
        mElementUtils = processingEnv?.elementUtils
    }

    //指定处理的版本
    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    //给到需要处理的注解
    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        val types: LinkedHashSet<String> = LinkedHashSet()
        getSupportedAnnotations().forEach { clazz: Class<out Annotation> ->
            types.add(clazz.canonicalName)
        }
        return types
    }

    private fun getSupportedAnnotations(): Set<Class<out Annotation>> {
        val annotations: LinkedHashSet<Class<out Annotation>> = LinkedHashSet()
        // 需要解析的自定义注解
        annotations.add(CreateService::class.java)
        annotations.add(PostBody::class.java)
        return annotations
    }

    /**
    KotlinPoet 官方helloWorld示例：
    val greeterClass = ClassName("", "Greeter")
    val file = FileSpec.builder("", "HelloWorld")
    .addType(TypeSpec.classBuilder("Greeter")
    .primaryConstructor(FunSpec.constructorBuilder()
    .addParameter("name", String::class).build())
    .addProperty(PropertySpec.builder("name", String::class)
    .initializer("name").build())
    .addFunction(FunSpec.builder("greet")
    .addStatement("println(%P)", "Hello, \$name").build())
    .build())
    .addFunction(FunSpec.builder("main")
    .addParameter("args", String::class, VARARG)
    .addStatement("%T(args[0]).greet()", greeterClass).build())
    .build()
    file.writeTo(System.out)
    ——————————————————————————————————
    class Greeter(val name: String) {
    fun greet() {println("""Hello, $name""")}}
    fun main(vararg args: String) {Greeter(args[0]).greet()}
     */
    override fun process(annotations: MutableSet<out TypeElement>, roundEnvironment: RoundEnvironment): Boolean {
        val elementsAnnotatedWith: Set<out Element> = roundEnvironment.getElementsAnnotatedWith(CreateService::class.java);
        elementsAnnotatedWith.forEach { element ->
            //得到包名
            var e = element
            while (e.kind != ElementKind.PACKAGE) {
                e = e.enclosingElement
            }
            val packageName = (e as PackageElement).toString()
            val service = element.getAnnotation(CreateService::class.java)
            val funspecs = mutableListOf<FunSpec>()
            try {
                val apiClass = Class.forName(service.interfaceApi)
                val mapMethod = mutableMapOf<String, String>()
                apiClass.methods.forEach { m ->
                    m.annotations.forEach { an ->
                        if (an.annotationClass.simpleName == PostBody::class.java.simpleName) {
                            mapMethod[m.name] = (an as PostBody).json
                        }
                    }
                }

                apiClass.kotlin.members.forEach { m ->
                    when (m.name) {
                        "equals" -> ""
                        "hashCode" -> ""
                        "toString" -> ""
                        else -> {
                            if (mapMethod.containsKey(m.name)) {
                                val builder: FunSpec.Builder = FunSpec.builder(m.name)
                                val mapParams = gson.fromJson<Map<String, String>>(mapMethod[m.name], object : TypeToken<Map<String, String>>() {}.type)
                                val sb = StringBuilder()
                                sb.append("val map = mutableMapOf<String, Any>()\n")
                                mapParams?.forEach {
                                    sb.append("map[\"${it.key}\"]=${it.key}\n")
                                    when (it.value) {
                                        "String" -> {
                                            builder.addParameter(it.key, String::class.java)//参数名，参数类型
                                        }
                                        "Int" -> {
                                            builder.addParameter(it.key, Int::class.java)//参数名，参数类型
                                        }
                                        "Long" -> {
                                            builder.addParameter(it.key, Long::class.java)//参数名，参数类型
                                        }
                                        "Double" -> {
                                            builder.addParameter(it.key, Double::class.java)//参数名，参数类型
                                        }
                                        "Float" -> {
                                            builder.addParameter(it.key, Float::class.java)//参数名，参数类型
                                        }
                                        "Boolean" -> {
                                            builder.addParameter(it.key, Boolean::class.java)//参数名，参数类型
                                        }
                                        "Short" -> {
                                            builder.addParameter(it.key, Short::class.java)//参数名，参数类型
                                        }
//                                        "String" -> {
//                                            builder.addParameter(it.key, String::class.java)//参数名，参数类型
//                                        }
                                    }
                                }

                                sb.append("val result = service.${m.name}(com.wx.test.api.RequestBodyCreate.toBody(com.google.gson.Gson().toJson(map)))\n")
                                sb.append("return result")
                                builder.addModifiers(KModifier.SUSPEND)
                                    .returns(m.returnType.asTypeName())//获取返回类型
                                    .addStatement(sb.toString())
//                                    .addModifiers(KModifier.OVERRIDE)
                                funspecs.add(builder.build())
                            } else {
                                val builder: FunSpec.Builder = FunSpec.builder(m.name)
                                val sb = StringBuilder()
                                sb.append("return service.${m.name}(")
                                for ((index, p) in m.parameters.withIndex()) {
                                    p.name?.let {
                                        builder.addParameter(it, p.type.asTypeName())//参数名，参数类型
                                        sb.append("${p.name}")
                                        if (index < m.parameters.size - 1)
                                            sb.append(",")
                                    }
                                }
                                sb.append(")")
                                builder.addModifiers(KModifier.SUSPEND)
                                    .returns(m.returnType.asTypeName())//获取返回类型
                                    .addStatement(sb.toString())
//                                    .addModifiers(KModifier.OVERRIDE)
                                funspecs.add(builder.build())
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val classNameOrigin = service.interfaceApi
            val superClassNameOrigin = service.superClass
            val index = classNameOrigin.lastIndexOf('.')
            val indexS = superClassNameOrigin.lastIndexOf('.')
            val className = classNameOrigin.substring(index + 1 until classNameOrigin.length)
            var greeterClass = "${className}Repository";
            val superClassName = ClassName(superClassNameOrigin.substring(0 until indexS), superClassNameOrigin.substring(indexS + 1 until superClassNameOrigin.length))
            val superInterfaceClassName = ClassName(classNameOrigin.substring(0 until index), className)
            val newSuperClassName = superClassName.parameterizedBy(superInterfaceClassName)
            val typeSpecClassBuilder = TypeSpec.classBuilder(greeterClass)//类名
                .primaryConstructor(//本类默认构造函数
                    FunSpec.constructorBuilder()
//                        .addParameter("retrofit", Retrofit::class)//构造函数里面参数
//                        .addAnnotation(Inject::class.java)//构造函数加注解
                        .build()
                ).superclass(newSuperClassName)//继承的父类
//                .addSuperclassConstructorParameter("retrofit", Retrofit::class)//父类构造函数参数
//                .addSuperinterface(superInterfaceClassName)//父类实现接口
            funspecs.forEach {
                typeSpecClassBuilder.addFunction(it)
            }
            val file = FileSpec.builder(packageName, greeterClass)
                .addType(
                    typeSpecClassBuilder.build()
                ).build()
            mFiler?.let { filer -> file.writeTo(filer) }
        }
        return true
    }

    private fun log(message: String) {
        processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, message)
    }
}