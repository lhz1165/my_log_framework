package cn.stylefeng.guns.cloud.system.log_framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author: lhz
 * @date: 2020/12/7
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.TYPE})
public @interface OperationRecordLog {
    /**
     * 参数是否RequestBody
     * @return
     */
    boolean isBody() default false;

    /**
     * 接口完成需要记录的返回的信息
     * 例如用于记录新增订单之后的数据库自增的订单id
     * @return
     */
    String returnName() default "";


    /**
     * 需要转义的参数，前端传递的特殊值必须把对应的真实意思对应起来
     * 例如true 代表 开始 ，false 代表结束。
     * @return
     */
    ChangeParam[] changeAttribute() default {};




}

