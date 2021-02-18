package com.lg.demo.service;

/**
 * 增删改业务
 * @author BigRedCaps
 * @date 2021/2/18 21:51
 */
public interface IModifyService
{
    /**
     * 增加
     */
    public String add(String name, String addr);

    /**
     * 修改
     */
    public String edit(Integer id, String name);

    /**
     * 删除
     */
    public String remove(Integer id);
}
