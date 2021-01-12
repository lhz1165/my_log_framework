package cn.stylefeng.guns.cloud.system.log_framework.util.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.JoinPoint;

/**
 * @author: lhz
 * @date: 2020/12/25
 **/
public interface BaseParser {
    /**
     * 自定义的解析器,如果遇到特殊情况需要自定义解析方式
     * 接口参数比较特殊，不是很好解析和替换，可以自定义解析器
     * @param joinpoint
     * @param data
     * @return
     */
     String beforeParser(JoinPoint joinpoint, Object data, ObjectMapper writer) throws JsonProcessingException;

}
