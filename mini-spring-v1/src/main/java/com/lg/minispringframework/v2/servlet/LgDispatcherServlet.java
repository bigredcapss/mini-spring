package com.lg.minispringframework.v2.servlet;

import com.lg.minispringframework.annotation.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * 在v1版本的基础上，采用常用的设计模式（工厂模式，单例模式，委派模式，策略模式）将init方法中的代码进行封装，对代码进行了优化。
 * 但代码的优雅程度还不太高，例如HandlerMapping还不能像SpringMVC一样支持正则，url参数还不支持强制类型转换，在发射调用前还需要
 * 获取beanName，在3.0中继续优化。
 * @author BigRedCaps
 * @date 2021/1/23 14:33
 */
public class LgDispatcherServlet extends HttpServlet
{

    //保存application.properties配置文件中的内容
    private Properties contextConfig = new Properties();

    //保存扫描的所有的类名
    private List<String> classNames = new ArrayList<String>();

    //传说中的IOC容器，我们来揭开它的神秘面纱
    //为了简化程序，暂时不考虑ConcurrentHashMap
    // 主要还是关注设计思想和原理
    //其中Ioc容器就是注册时单利的具体案例
    private Map<String,Object> ioc = new HashMap<String,Object>();

    //保存url和Method的对应关系
    private Map<String,Method> handlerMapping = new HashMap<String,Method>();

    @Override
    protected void doGet (HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        this.doPost(req,resp);
    }

    private void doDispatch (HttpServletRequest req, HttpServletResponse resp) throws Exception
    {
        //绝对路径
        String url = req.getRequestURI();
        //处理成相对路径
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath,"").replaceAll("/+","/");

        if(!this.handlerMapping.containsKey(url)){
            resp.getWriter().write("404 Not Found!!!");
            return;
        }

        Method method = this.handlerMapping.get(url);

        //从request中拿到url传过来的参数
        Map<String,String[]> params = req.getParameterMap();

        //获取方法的形参列表
        Class<?> [] parameterTypes = method.getParameterTypes();

        Object [] paramValues = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i ++) {
            Class parameterType = parameterTypes[i];
            //不能用instanceof，parameterType它不是实参，而是形参
            if(parameterType == HttpServletRequest.class){
                paramValues[i] = req;
                continue;
            }else if(parameterType == HttpServletResponse.class){
                paramValues[i] = resp;
                continue;
            }else if(parameterType == String.class){
                //提取方法中加了注解的参数
                //把方法上的注解拿到，得到的是一个二维数组
                //因为一个参数可以有多个注解，而一个方法又有多个参数
                Annotation[] [] pa = method.getParameterAnnotations();
                for (int j = 0; j < pa.length ; j ++) {
                    for(Annotation a : pa[i]){
                        if(a instanceof LGRequestParam){
                            String paramName = ((LGRequestParam) a).value();
                            if(!"".equals(paramName.trim())){
                                String value = Arrays.toString(params.get(paramName))
                                        .replaceAll("\\[|\\]","")
                                        .replaceAll("\\s+",",");
                                paramValues[i] = value;
                            }
                        }
                    }
                }
            }
        }

        //投机取巧的方式
        //通过反射拿到method所在class，拿到class之后还是拿到class的名称
        //再调用toLowerFirstCase获得beanName
        String beanName  = toLowerFirstCase(method.getDeclaringClass().getSimpleName());
        method.invoke(ioc.get(beanName),paramValues);
    }

    @Override
    protected void doPost (HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        //6、调用，运行阶段，通过URL去找到一个对应的Method，并通过response返回
        try {
            doDispatch(req,resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("500 Exection,Detail : " + Arrays.toString(e.getStackTrace()));
        }
    }

    /**
     * 初始化阶段
     * @param config
     * @throws ServletException
     */
    @Override
    public void init (ServletConfig config) throws ServletException
    {
        // 1.调用init方法，加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));

        // 2.扫描相关的类
        doScanner(contextConfig.getProperty("scanPackage"));

        // 3.初始化IoC容器，并进行实例化，保存到IoC容器中，工厂模式的体现
        doInstance();

        // 4.完成依赖注入（DI操作）
        doAutowired();

        // 5.初始化HandlerMapping，策略模式的体现
        doInitHandlerMapping();

        System.out.println("LG Spring framework is init.");


    }

    private void doInitHandlerMapping ()
    {
        if(ioc.isEmpty()){ return; }

        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();

            if(!clazz.isAnnotationPresent(LGController.class)){continue;}

            //保存写在类上面的@GPRequestMapping("/demo")
            String baseUrl = "";
            if(clazz.isAnnotationPresent(LGRequestMapping.class)){
                LGRequestMapping requestMapping = clazz.getAnnotation(LGRequestMapping.class);
                baseUrl = requestMapping.value();
            }

            //默认获取所有的public方法
            for (Method method : clazz.getMethods()) {
                if(!method.isAnnotationPresent(LGRequestMapping.class)){continue;}

                LGRequestMapping requestMapping = method.getAnnotation(LGRequestMapping.class);
                //优化
                // //demo///query
                String url = ("/" + baseUrl + "/" + requestMapping.value())
                        .replaceAll("/+","/");
                handlerMapping.put(url,method);
                System.out.println("Mapped :" + url + "," + method);

            }

        }
    }

    private void doAutowired ()
    {
        if(ioc.isEmpty()){return;}

        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            //Declared 所有的，特定的 字段，包括private/protected/default
            //正常来说，普通的OOP编程只能拿到public的属性
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields) {
                if(!field.isAnnotationPresent(LGAutowired.class)){continue;}
                LGAutowired autowired = field.getAnnotation(LGAutowired.class);

                //如果用户没有自定义beanName，默认就根据类型注入
                //这个地方省去了对类名首字母小写的情况的判断
                //小伙伴们自己去完善
                String beanName = autowired.value().trim();
                if("".equals(beanName)){
                    //获得接口的类型，作为key待会拿这个key到ioc容器中去取值
                    beanName = field.getType().getName();
                }

                //如果是public以外的修饰符，只要加了@Autowired注解，都要强制赋值
                //反射中叫做暴力访问， 强吻
                field.setAccessible(true);

                try {
                    //用反射机制，动态给字段赋值
                    field.set(entry.getValue(),ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }

        }
    }

    private void doInstance ()
    {
        //初始化，为DI做准备
        if(classNames.isEmpty()){return;}
        try {
            for (String className : classNames) {
                Class<?> clazz = Class.forName(className);

                //什么样的类才需要初始化呢？
                //加了注解的类，才初始化，怎么判断？
                //为了简化代码逻辑，主要体会设计思想，只举例 @Controller和@Service,
                // @Componment...就不一一举例了
                if(clazz.isAnnotationPresent(LGController.class)){
                    Object instance = clazz.newInstance();
                    //Spring默认类名首字母小写
                    String beanName = toLowerFirstCase(clazz.getSimpleName());
                    ioc.put(beanName,instance);
                }else if(clazz.isAnnotationPresent(LGService.class)){
                    //1、自定义的beanName
                    LGService service = clazz.getAnnotation(LGService.class);
                    String beanName = service.value();
                    //2、默认类名首字母小写
                    if("".equals(beanName.trim())){
                        beanName = toLowerFirstCase(clazz.getSimpleName());
                    }

                    Object instance = clazz.newInstance();
                    ioc.put(beanName,instance);
                    //3、根据类型自动赋值,投机取巧的方式
                    for (Class<?> i : clazz.getInterfaces()) {
                        if(ioc.containsKey(i.getName())){
                            throw new Exception("The “" + i.getName() + "” is exists!!");
                        }
                        //把接口的类型直接当成key了
                        ioc.put(i.getName(),instance);
                    }
                }else {
                    continue;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //如果类名本身是小写字母，确实会出问题
    //但是我要说明的是：这个方法是我自己用，private的
    //传值也是自己传，类也都遵循了驼峰命名法
    //默认传入的值，存在首字母小写的情况，也可能出现非字母的情况

    //为了简化程序逻辑，就不做其他判断了，了解就OK
    private String toLowerFirstCase(String simpleName) {
        char [] chars = simpleName.toCharArray();
        // 之所以加，是因为大小写字母的ASCII码相差32，
        // 而且大写字母的ASCII码要小于小写字母的ASCII码
        //在Java中，对char做算学运算，实际上就是对ASCII码做算学运算
        chars[0] += 32;
        return String.valueOf(chars);
    }

    private void doScanner (String scanPackage)
    {
        //scanPackage = com.gupaoedu.demo ，存储的是包路径
        //转换为文件路径，实际上就是把.替换为/就OK了
        //classpath
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.","/"));
        File classPath = new File(url.getFile());
        for (File file : classPath.listFiles()) {
            if(file.isDirectory()){
                doScanner(scanPackage + "." + file.getName());
            }else{
                if(!file.getName().endsWith(".class")){ continue;}
                String className = (scanPackage + "." + file.getName().replace(".class",""));
                classNames.add(className);
            }
        }
    }

    private void doLoadConfig (String contextConfigLocation)
    {
        // 直接从类路径下找到Spring主配置文件所在的路径
        // 并且将其读取出来放到Properties对象中
        // 相当于scanPackage=com.lg.demo 从文件中保存到了内存中
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try
        {
            contextConfig.load(inputStream);
        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            if(null != inputStream){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
