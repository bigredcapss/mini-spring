package com.lg.spring.framework.webmvc.servlet;

import java.util.Map;

/**
 * @author BigRedCaps
 * @date 2021/2/21 22:36
 */
public class LGModelAndView
{
    private String viewName;
    private Map<String,?> model;

    public LGModelAndView(String viewName, Map<String, ?> model) {
        this.viewName = viewName;
        this.model = model;
    }

    public LGModelAndView(String viewName) {
        this.viewName = viewName;
    }

    public String getViewName() {
        return viewName;
    }

    public Map<String, ?> getModel() {
        return model;
    }
}
