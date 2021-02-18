package com.lg.demo.service.impl;

import com.lg.demo.service.IQueryService;
import com.lg.spring.framework.annotation.LGService;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author BigRedCaps
 * @date 2021/2/18 21:54
 */
@LGService
@Slf4j
public class QueryService implements IQueryService
{
    /**
     * 查询
     */
    public String query(String name) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(new Date());
        String json = "{name:\"" + name + "\",time:\"" + time + "\"}";
        log.info("这是在业务方法中打印的：" + json);
        return json;
    }
}
