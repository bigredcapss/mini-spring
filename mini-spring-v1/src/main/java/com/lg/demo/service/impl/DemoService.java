package com.lg.demo.service.impl;

import com.lg.demo.service.IDemoService;
import com.lg.minispringframework.annotation.LGService;

/**
 * 定义业务实现类
 * @author BigRedCaps
 * @date 2021/1/23 15:07
 */
@LGService
public class DemoService implements IDemoService
{
    @Override
    public String get (String name)
    {
        return "My name is"+name+",from service.";
    }
}
