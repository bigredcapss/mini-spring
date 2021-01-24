package com.lg.demo.action;

import com.lg.demo.service.IDemoService;
import com.lg.minispringframework.annotation.LGAutowired;
import com.lg.minispringframework.annotation.LGController;
import com.lg.minispringframework.annotation.LGRequestMapping;
import com.lg.minispringframework.annotation.LGRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * 定义请求入口类
 * @author BigRedCaps
 * @date 2021/1/23 15:11
 */
@LGController
@LGRequestMapping("/demo")
public class DemoAction
{
    @LGAutowired
    private IDemoService demoService;

    @LGRequestMapping("/query")
    public void query(HttpServletRequest request, HttpServletResponse response,@LGRequestParam("name") String name){
        String result = demoService.get(name);
        try {
            response.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @LGRequestMapping("/add")
    public void add(HttpServletRequest req, HttpServletResponse resp,
                    @LGRequestParam("a") Integer a, @LGRequestParam("b") Integer b){
        try {
            resp.getWriter().write(a + "+" + b + "=" + (a + b));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @LGRequestMapping("/sub")
    public void add(HttpServletRequest req, HttpServletResponse resp,
                    @LGRequestParam("a") Double a, @LGRequestParam("b") Double b){
        try {
            resp.getWriter().write(a + "-" + b + "=" + (a - b));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @LGRequestMapping("/remove")
    public String  remove(@LGRequestParam("id") Integer id){
        return "" + id;
    }


}
