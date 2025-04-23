package com.lg.minispringframework.v1.servlet;

import com.lg.minispringframework.annotation.LGAutowired;
import com.lg.minispringframework.annotation.LGController;
import com.lg.minispringframework.annotation.LGRequestMapping;
import com.lg.minispringframework.annotation.LGService;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 仿照DispatcherServlet，自定义Servlet
 *
 * 以Mvc作为入口，启动Ioc容器，完成DI
 * @author BigRedCaps
 * @date 2021/1/23 14:33
 */
public class LgDispatcherServlet extends HttpServlet
{
    // 保存扫描的类
    private Map<String,Object> classMapping = new HashMap<String, Object>();

    // 将url和方法映射
    private Map<String,Object> handlerMapping = new HashMap<String, Object>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {this.doPost(req,resp);}
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req,resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("500 Exception " + Arrays.toString(e.getStackTrace()));
        }
    }
    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "/");
        if(!this.handlerMapping.containsKey(url)){resp.getWriter().write("404 Not Found!!");return;}
        Method method = (Method) this.handlerMapping.get(url);
        Map<String,String[]> params = req.getParameterMap();
        method.invoke(this.handlerMapping.get(method.getDeclaringClass().getName()),new Object[]{req,resp,params.get("name")[0]});
    }

    //当我晕车的时候，我就不去看源码了

    //init方法肯定干得的初始化的工作
    //init首先我得初始化所有的相关的类，IOC容器、servletBean
    @Override
    public void init(ServletConfig config) throws ServletException {
        InputStream is = null;
        try{
            // 1.加载配置文件
            Properties configContext = new Properties();
            is = this.getClass().getClassLoader().getResourceAsStream(config.getInitParameter("contextConfigLocation"));
            configContext.load(is);
            // 2.扫描相关的类
            String scanPackage = configContext.getProperty("scanPackage");
            doScanner(scanPackage);
            // 3.初始化Ioc容器，进行DI，初始化HandlerMapping；这里Ioc容器和handlerMapping并未拆开
            for (String className : classMapping.keySet()) {
                if(!className.contains(".")){continue;}
                Class<?> clazz = Class.forName(className);
                if(clazz.isAnnotationPresent(LGController.class)){
                    handlerMapping.put(className,clazz.newInstance());
                    String baseUrl = "";
                    if (clazz.isAnnotationPresent(LGRequestMapping.class)) {
                        LGRequestMapping requestMapping = clazz.getAnnotation(LGRequestMapping.class);
                        baseUrl = requestMapping.value();
                    }
                    Method[] methods = clazz.getMethods();
                    for (Method method : methods) {
                        if (!method.isAnnotationPresent(LGRequestMapping.class)) {  continue; }
                        LGRequestMapping requestMapping = method.getAnnotation(LGRequestMapping.class);
                        String url = (baseUrl + "/" + requestMapping.value()).replaceAll("/+", "/");
                        handlerMapping.put(url, method);
                        System.out.println("Mapped " + url + "," + method);
                    }
                }
                else if(clazz.isAnnotationPresent(LGService.class)){
                    LGService service = clazz.getAnnotation(LGService.class);
                    String beanName = service.value();
                    if("".equals(beanName)){beanName = clazz.getName();}
                    Object instance = clazz.newInstance();
                    handlerMapping.put(beanName,instance);
                    for (Class<?> i : clazz.getInterfaces()) {
                        handlerMapping.put(i.getName(),instance);
                    }
                }
                else {continue;}
            }
            for (Object object : handlerMapping.values()) {
                if(object == null){continue;}
                Class clazz = object.getClass();
                if(clazz.isAnnotationPresent(LGController.class)){
                    Field[] fields = clazz.getDeclaredFields();
                    for (Field field : fields) {
                        if(!field.isAnnotationPresent(LGAutowired.class)){continue; }
                        LGAutowired autowired = field.getAnnotation(LGAutowired.class);
                        String beanName = autowired.value();
                        if("".equals(beanName)){beanName = field.getType().getName();}
                        field.setAccessible(true);
                        try {
                            field.set(handlerMapping.get(clazz.getName()),handlerMapping.get(beanName));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if(is != null){
                try {
                    is.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.print("LG MVC Framework is init");
    }
    private void doScanner(String scanPackage) {
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.","/"));
        File classDir = new File(url.getFile());
        for (File file : classDir.listFiles()) {
            if(file.isDirectory()){ doScanner(scanPackage + "." +  file.getName());}else {
                if(!file.getName().endsWith(".class")){continue;}
                String clazzName = (scanPackage + "." + file.getName().replace(".class",""));
                // 扫描相关的类，放入Map中
                classMapping.put(clazzName,null);
            }
        }
    }
}
