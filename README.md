# 注解处理器在架构、框架中实战应用：MVVM中数据源提供Repository类的自动生成
## 一、项目目录介绍
<img src="https://gitee.com/wgllss888/Kotlin_KAPT/raw/master/pic/intr.jpg" width="452" height="600"/> 

如上图所示,项目Demo 分为4个部分：
1. **networkApiData**:Java Library,项目网络接口部分和 网络数据返回实体bean，可以将网络部分解耦出来
2. **annotations**:Java Library, 注解模块
3. **annotation_compiler**:Java Library 注解处理模块
4. **app**:真实android app 工程

## 二、网络模块部分编写（networkApiData）



build.gradle里面添加 dependencies 里面 添加依赖

```
  api project(path: ':annotations')
```

主要示例了Retrofit中请求接口写法
1. 示例了Get请求
2. 示例Post 请求 参数在url上
3. 示例post 请求 post body
4. 示例post 请求 post body 在Repository中的 第2种写法
```

interface Api {

    //示例Get 请求
    @GET("search/acjson?tn=resultjson_com&logid=12307192414549550342&ipn=rj&ct=201326592&is=&fp=result&fr=&cg=star&rn=30")
    suspend fun get899(@Query("word") word: String, @Query("queryWord") queryWord: String, @Query("pn") pn: Int, @Query("gsm") gsm: String): BaseResponse<ArrayList<BaiduDataBean>>

    //示例Post 请求 参数在url上
    @FormUrlEncoded
    @POST("https://www.wanandroid.com/user/register")
    suspend fun register(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("repassword") repassword: String
    ): String


    //示例post 请求 post body
    // 此处示例写法，这个真实post body 地址是不通的
    @POST("https://www.wanandroid.com/user/register")
    suspend fun testPostBody(@Body body: RequestBody): String

    // 示例post 请求 post body
    // 此处示例写法，这个真实post body 地址是不通的
    @PostBody("{\"ID\":\"Long\",\"name\":\"String\"}")
    @POST("https://www.wanandroid.com/user/register")
    suspend fun testPostBody222(@Body body: RequestBody): String
}
```


## 三、注解模块（annotations）

主要写了2个注解：
1. CreateService 注解在网络接口类上面
2. PostBody： 注解在post 请求body上面


```
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class CreateService(val interfaceApi: String, val superClass: String) {
    /**
     * interfaceApi:  接口类名字，
     *  superClass :  自动生成的类的继承的父类
     **/
}


```


```
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class PostBody(val json: String) {

}
```

## 四、真实android app 工程 里面

build.gradle里面添加

```
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
    id 'kotlin-kapt'//添加 kapt
}
```

开启 kapt
```
 kapt {
        generateStubs = true
    }
```

dependencies 里面 添加依赖

```
    implementation project(path: ':networkApiData')
    kapt project(path: ':annotation_compiler')
```

然后随便代码工程目录里面写个类，类名可自定义：


```
/**
 * interfaceApi:  接口类名字，
 *  superClass :  自动生成的类的继承的父类
 **/
@CreateService(interfaceApi = "com.wx.test.api.Api", superClass = "com.wx.kotlin_kapt_demo.data_source.repository.BaseRepository")
class KaptComponet {
}
```








## 五、注解处理模块（annotation_compiler）
主要负责自动成Repository 类

1. build.gradle里面编写
```
plugins {
    id 'java'
    id 'java-library'
    id 'kotlin'
    id 'kotlin-kapt'
}


dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    implementation "org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version"
//    annotationProcessor 'com.google.auto.service:auto-service:1.0-rc4'  java 用法
//    implementation 'com.google.auto.service:auto-service:1.0-rc4'    java 用法

    implementation "com.google.auto.service:auto-service:1.0-rc4"  // kotlin 用法
    kapt "com.google.auto.service:auto-service:1.0"                // kotlin 用法
    implementation "com.squareup:kotlinpoet:1.8.0"
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'

    implementation project(path: ':annotations')
    implementation project(path: ':networkApiData')
}
java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
```

2.自动生成代码处理类 AptProcessor：


```

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
```




## 六、查看自动生成的 Repository 类，及调用
 1.build app工程 ，
 或者点击 app task的 assembleDebug 如下图： 

<img src="https://gitee.com/wgllss888/Kotlin_KAPT/raw/master/pic/task.jpeg" width="603" height="641"/>
 2.查看自动生成的类：
<img src="https://gitee.com/wgllss888/Kotlin_KAPT/raw/master/pic/cre.jpg" width="561" height="524"/>

 3.查看生成的代码：
```
package com.wx.kotlin_kapt_demo.data_source.kapt

import com.wx.kotlin_kapt_demo.data_source.repository.BaseRepository
import com.wx.test.api.Api
import com.wx.test.api.`data`.BaiduDataBean
import com.wx.test.api.`data`.BaseResponse
import java.util.ArrayList
import kotlin.Int
import kotlin.Long
import kotlin.String
import okhttp3.RequestBody

public class ApiRepository : BaseRepository<Api>() {
    public suspend fun get899(
        word: String,
        queryWord: String,
        pn: Int,
        gsm: String
    ): BaseResponse<ArrayList<BaiduDataBean>> = service.get899(word, queryWord, pn, gsm)

    public suspend fun register(
        username: String,
        password: String,
        repassword: String
    ): String = service.register(username, password, repassword)

    public suspend fun testPostBody(body: RequestBody): String = service.testPostBody(body)

    public suspend fun testPostBody222(ID: Long, name: java.lang.String): String {
        val map = mutableMapOf<String, Any>()
        map["ID"] = ID
        map["name"] = name
        val result = service.testPostBody222(com.wx.test.api.RequestBodyCreate.toBody(com.google.gson.Gson().toJson(map)))
        return result
    }
}
```
 4.app 工程中调用:
```
class MainVIewModel : ViewModel() {

    private val repository by lazy { ApiRepository() }
    val liveDataImg by lazy { MutableLiveData<String>() }

    fun requestTest() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.get899("西游记", "西游记", 1, "")
            result?.data?.takeIf {
                it.size > 0
            }?.let {
                liveDataImg.postValue(it[0].middleURL)
            }
        }
    }
}
```



## 七、我的其他开源
#### [Kotlin+协程+Flow+Retrofit+OkHttp这么好用，不运行安装到手机可以调试接口吗?可以自己搭建一套网络请求工具](https://juejin.cn/post/7406675078810910761)
#### [花式封装：Kotlin+协程+Flow+Retrofit+OkHttp +Repository，倾囊相授,彻底减少模版代码进阶之路](https://juejin.cn/post/7417847546323042345)
#### [注解处理器在架构，框架中实战应用：MVVM中数据源提供Repository类的自动生成](https://juejin.cn/post/7392258195089162290)

## 八、我的全动态插件化框架WXDynamicPlugin介绍文章：
#### [(一) 插件化框架开发背景：零反射，零HooK,全动态化，插件化框架，全网唯一结合启动优化的插件化架构](https://juejin.cn/post/7347994218235363382)
#### [(二）插件化框架主要介绍：零反射，零HooK,全动态化，插件化框架，全网唯一结合启动优化的插件化架构](https://juejin.cn/post/7367676494976532490)
#### [(三）插件化框架内部详细介绍: 零反射，零HooK,全动态化，插件化框架，全网唯一结合启动优化的插件化架构](https://juejin.cn/post/7368397264026370083)
#### [(四）插件化框架接入详细指南：零反射，零HooK,全动态化，插件化框架，全网唯一结合启动优化的插件化架构](https://juejin.cn/post/7372393698230550565)
#### [(五) 大型项目架构：全动态插件化+模块化+Kotlin+协程+Flow+Retrofit+JetPack+MVVM+极限瘦身+极限启动优化+架构示例+全网唯一](https://juejin.cn/post/7381787510071934985)
#### [(六) 大型项目架构：解析全动态插件化框架WXDynamicPlugin是如何做到全动态化的？](https://juejin.cn/post/7388891131037777929)
#### [(七) 还在不断升级发版吗？从0到1带你看懂WXDynamicPlugin全动态插件化框架？](https://juejin.cn/post/7412124636239904819)
#### [(八) Compose插件化：一个Demo带你入门Compose，同时带你入门插件化开发](https://juejin.cn/post/7425434773026537483)

#### 感谢阅读，欢迎给给个星，你们的支持是我开源的动力
## 欢迎光临：
#### **[我的掘金地址](https://juejin.cn/user/356661835082573)**

#### 关于我
**VX号：wgllss**  ,如果想更多交流请加我VX
