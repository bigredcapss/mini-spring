package com.lg.demo.action;

import com.lg.demo.service.IModifyService;
import com.lg.demo.service.IQueryService;
import com.lg.spring.framework.annotation.LGAutowired;
import com.lg.spring.framework.annotation.LGController;
import com.lg.spring.framework.annotation.LGRequestMapping;
import com.lg.spring.framework.annotation.LGRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author BigRedCaps
 * @date 2021/2/18 21:50
 */
@LGController
@LGRequestMapping("/web")
public class MyAction
{
    @LGAutowired
    IQueryService queryService;
    @LGAutowired
    IModifyService modifyService;

    @LGRequestMapping("/query.json")
    public void query(HttpServletRequest request, HttpServletResponse response,
                      @LGRequestParam("name") String name){
        String result = queryService.query(name);
        out(response,result);
    }

    @LGRequestMapping("/add*.json")
    public void add(HttpServletRequest request, HttpServletResponse response,
                    @LGRequestParam("name") String name, @LGRequestParam("addr") String addr){
        String result = modifyService.add(name,addr);
        out(response,result);
    }

    @LGRequestMapping("/remove.json")
    public void remove(HttpServletRequest request,HttpServletResponse response,
                       @LGRequestParam("id") Integer id){
        String result = modifyService.remove(id);
        out(response,result);
    }

    @LGRequestMapping("/edit.json")
    public void edit(HttpServletRequest request,HttpServletResponse response,
                     @LGRequestParam("id") Integer id,
                     @LGRequestParam("name") String name){
        String result = modifyService.edit(id,name);
        out(response,result);
    }



    private void out(HttpServletResponse resp,String str){
        try {
            resp.getWriter().write(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
