package com.lg.demo.service.impl;

import com.lg.demo.service.IModifyService;
import com.lg.spring.framework.annotation.LGService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author BigRedCaps
 * @date 2021/2/18 21:56
 */
@LGService
@Slf4j
public class ModifyService implements IModifyService
{
    /**
     * 增加
     */
    public String add(String name,String addr) {
        return "modifyService add,name=" + name + ",addr=" + addr;
    }

    /**
     * 修改
     */
    public String edit(Integer id,String name) {
        return "modifyService edit,id=" + id + ",name=" + name;
    }

    /**
     * 删除
     */
    public String remove(Integer id) {
        return "modifyService id=" + id;
    }
}
