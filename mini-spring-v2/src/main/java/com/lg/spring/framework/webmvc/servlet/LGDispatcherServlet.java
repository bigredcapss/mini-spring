package com.lg.spring.framework.webmvc.servlet;

import com.lg.spring.framework.annotation.LGController;
import com.lg.spring.framework.annotation.LGRequestMapping;
import com.lg.spring.framework.context.LGApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 委派模式
 * 该类职责：负责任务调度，请求分发
 * @author BigRedCaps
 * @date 2021/2/18 22:03
 */
public class LGDispatcherServlet extends HttpServlet
{
    private LGApplicationContext applicationContext;

    private List<LGHandlerMapping> handlerMappings = new ArrayList<LGHandlerMapping>();

    private Map<LGHandlerMapping,LGHandlerAdapter> handlerAdapters = new HashMap<LGHandlerMapping, LGHandlerAdapter>();

    private List<LGViewResolver> viewResolvers = new ArrayList<LGViewResolver>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        //6、委派,根据URL去找到一个对应的Method并通过response返回
        try {
            doDispatch(req,resp);
        } catch (Exception e) {
            try {
                processDispatchResult(req,resp,new LGModelAndView("500"));
            } catch (Exception e1) {
                e1.printStackTrace();
                resp.getWriter().write("500 Exception,Detail : " + Arrays.toString(e.getStackTrace()));
            }
        }

    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        //完成了对HandlerMapping的封装
        //完成了对方法返回值的封装ModelAndView

        //1、通过URL获得一个HandlerMapping
        LGHandlerMapping handler = getHandler(req);
        if(handler == null){
            processDispatchResult(req,resp,new LGModelAndView("404"));
            return;
        }

        //2、根据一个HandlerMaping获得一个HandlerAdapter
        LGHandlerAdapter ha = getHandlerAdapter(handler);

        //3、解析某一个方法的形参和返回值之后，统一封装为ModelAndView对象
        LGModelAndView mv = ha.handler(req,resp,handler);

        // 就把ModelAndView变成一个ViewResolver
        processDispatchResult(req,resp,mv);

    }

    private LGHandlerAdapter getHandlerAdapter(LGHandlerMapping handler) {
        if(this.handlerAdapters.isEmpty()){return null;}
        return this.handlerAdapters.get(handler);
    }

    private void processDispatchResult(HttpServletRequest req, HttpServletResponse resp, LGModelAndView mv) throws Exception {
        if(null == mv){return;}
        if(this.viewResolvers.isEmpty()){return;}

        for (LGViewResolver viewResolver : this.viewResolvers) {
            LGView view = viewResolver.resolveViewName(mv.getViewName());
            //直接往浏览器输出
            view.render(mv.getModel(),req,resp);
            return;
        }
    }

    private LGHandlerMapping getHandler(HttpServletRequest req) {
        if(this.handlerMappings.isEmpty()){return  null;}
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath,"").replaceAll("/+","/");

        for (LGHandlerMapping mapping : handlerMappings) {
            Matcher matcher = mapping.getPattern().matcher(url);
            if(!matcher.matches()){continue;}
            return mapping;
        }
        return null;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {

        //初始化Spring核心IoC容器
        applicationContext = new LGApplicationContext(config.getInitParameter("contextConfigLocation"));

        //完成了IoC、DI和MVC部分对接

        //初始化九大组件
        initStrategies(applicationContext);

        System.out.println("LG Spring framework is init.");
    }

    private void initStrategies(LGApplicationContext context) {
//        //多文件上传的组件
//        initMultipartResolver(context);
//        //初始化本地语言环境
//        initLocaleResolver(context);
//        //初始化模板处理器
//        initThemeResolver(context);
        //handlerMapping
        initHandlerMappings(context);
        //初始化参数适配器
        initHandlerAdapters(context);
//        //初始化异常拦截器
//        initHandlerExceptionResolvers(context);
//        //初始化视图预处理器
//        initRequestToViewNameTranslator(context);
        //初始化视图转换器
        initViewResolvers(context);
//        //FlashMap管理器
//        initFlashMapManager(context);
    }

    private void initViewResolvers(LGApplicationContext context) {
        String templateRoot = context.getConfig().getProperty("templateRoot");
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();

        File templateRootDir = new File(templateRootPath);
        for (File file : templateRootDir.listFiles()) {
            this.viewResolvers.add(new LGViewResolver(templateRoot));
        }

    }

    private void initHandlerAdapters(LGApplicationContext context) {
        for (LGHandlerMapping handlerMapping : handlerMappings) {
            this.handlerAdapters.put(handlerMapping,new LGHandlerAdapter());
        }
    }

    private void initHandlerMappings(LGApplicationContext context) {
        if(this.applicationContext.getBeanDefinitionCount() == 0){ return;}

        for (String beanName : this.applicationContext.getBeanDefinitionNames()) {
            Object instance = applicationContext.getBean(beanName);
            Class<?> clazz = instance.getClass();

            if(!clazz.isAnnotationPresent(LGController.class)){ continue; }

            //相当于提取 class上配置的url
            String baseUrl = "";
            if(clazz.isAnnotationPresent(LGRequestMapping.class)){
                LGRequestMapping requestMapping = clazz.getAnnotation(LGRequestMapping.class);
                baseUrl = requestMapping.value();
            }

            //只获取public的方法
            for (Method method : clazz.getMethods()) {
                if(!method.isAnnotationPresent(LGRequestMapping.class)){continue;}
                //提取每个方法上面配置的url
                LGRequestMapping requestMapping = method.getAnnotation(LGRequestMapping.class);

                // //demo//query
                String regex = ("/" + baseUrl + "/" + requestMapping.value().replaceAll("\\*",".*")).replaceAll("/+","/");
                Pattern pattern = Pattern.compile(regex);
                //handlerMapping.put(url,method);
                handlerMappings.add(new LGHandlerMapping(pattern,instance,method));
                System.out.println("Mapped : " + regex + "," + method);
            }

        }
    }

}
