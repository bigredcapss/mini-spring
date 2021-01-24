package com.lg.minispringframework.v2.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author BigRedCaps
 * @date 2021/1/23 14:33
 */
public class LgDispatcherServlet extends HttpServlet
{

    @Override
    protected void doGet (HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        // 6.委派，通过URL去找到一个对应的Method，并通过response返回
        doDispatch(req,resp);
    }

    private void doDispatch (HttpServletRequest req, HttpServletResponse resp)
    {
    }

    @Override
    protected void doPost (HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        super.doPost(req, resp);
    }

    @Override
    public void init (ServletConfig config) throws ServletException
    {
        // 1.调用init方法，加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));

        // 2.扫描相关的类
        doScanner();

        // 3.初始化IoC容器，并进行实例化，保存到IoC容器中
        doInstance();

        // 4.完成依赖注入（DI操作）
        doAutowired();

        // 5.初始化HandlerMapping
        doInitHandlerMapping();

        System.out.println("LG Spring framework is init.");


    }

    private void doInitHandlerMapping ()
    {
    }

    private void doAutowired ()
    {
    }

    private void doInstance ()
    {

    }

    private void doScanner ()
    {

    }

    private void doLoadConfig (String contextConfigLocation)
    {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);

    }
}
