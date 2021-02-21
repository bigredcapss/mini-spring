package com.lg.demo.action;

import com.lg.demo.service.IQueryService;
import com.lg.spring.framework.annotation.LGAutowired;
import com.lg.spring.framework.annotation.LGController;
import com.lg.spring.framework.annotation.LGRequestMapping;
import com.lg.spring.framework.annotation.LGRequestParam;
import com.lg.spring.framework.webmvc.servlet.LGModelAndView;

import java.util.HashMap;
import java.util.Map;

/**
 * 公布接口url
 * @author BigRedCaps
 * @date 2021/2/21 23:08
 */
@LGController
@LGRequestMapping("/")
public class PageAction {

    @LGAutowired
    IQueryService queryService;

    @LGRequestMapping("/first.html")
    public LGModelAndView query(@LGRequestParam("teacher") String teacher){
        String result = queryService.query(teacher);
        Map<String,Object> model = new HashMap<String,Object>();
        model.put("teacher", teacher);
        model.put("data", result);
        model.put("token", "123456");
        return new LGModelAndView("first.html",model);
    }

}
