package com.lg.spring.framework.aop.config;

import lombok.Data;

/**
 * @author BigRedCaps
 * @date 2021/2/21 23:05
 */
@Data
public class LGAopConfig
{
    private String pointCut;
    private String aspectClass;
    private String aspectBefore;
    private String aspectAfter;
    private String aspectAfterThrow;
    private String aspectAfterThrowingName;
}
