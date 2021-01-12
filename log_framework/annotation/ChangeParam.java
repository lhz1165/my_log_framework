package cn.stylefeng.guns.cloud.system.log_framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author: lhz
 * @date: 2020/12/25
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ChangeParam {
    /**
     *转义字段
     * 例如 ：前端输入 {"pauseFlag": "true"}
     * ChangeFiled = "pauseFlag"
     * @return
     */
    String ChangeFiled() default "";

    /**
     *转义字段值
     * 填入ChangeFiledKey = {"true","false"}
     * @return
     */
    String[] ChangeFiledKey() default "";

    /**
     *转义字段意义 分别代表的意思是 暂停 开始
     * 填入changeFiledVal = {"暂停","开始"}
     *
     * 在数据库记录的时候会把 true/false分别 替换为 暂停/开始
     * 结果就是  {"pauseFlag": "暂停"}
     * @return
     */
    String[] changeFiledVal() default{""};
}

