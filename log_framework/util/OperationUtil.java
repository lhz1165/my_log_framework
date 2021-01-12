package cn.stylefeng.guns.cloud.system.log_framework.util;

import cn.stylefeng.roses.kernel.scanner.modular.annotation.GetResource;
import cn.stylefeng.roses.kernel.scanner.modular.annotation.PostResource;
import cn.stylefeng.roses.kernel.scanner.modular.stereotype.ApiResource;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import java.lang.reflect.Method;

/**
 * @author: lhz
 * @date: 2020/12/16
 **/
public class OperationUtil {
    /**
     * 返回url
     */
    public static String getUrl(JoinPoint joinpoint) {
        return getControllerClass(joinpoint).getAnnotation(ApiResource.class).path()[0] + getMethodUrl(joinpoint);
    }


    /**
     * 获取方法的url
     * 方法要么是GetResource注解要么是ApiResource注解
     * @param joinpoint
     * @return
     */
    public static String getMethodUrl(JoinPoint joinpoint) {
        GetResource getResource = getControllerMethod(joinpoint).getAnnotation(GetResource.class);
        String url ;
        if (getResource == null) {
            PostResource postResource = getControllerMethod(joinpoint).getAnnotation(PostResource.class);
            url =postResource == null ? getControllerMethod(joinpoint).getAnnotation(ApiResource.class).path()[0] : postResource.path()[0];
        }else {
            url = getResource.path()[0];
        }
        return url;
    }


    /**
     * 获取方法的注解名称
     * @param joinpoint
     * @return
     */
    public static String getMethodDesc(JoinPoint joinpoint) {
        Method controllerMethod = getControllerMethod(joinpoint);
        ApiOperation annotation = controllerMethod.getAnnotation(ApiOperation.class);
        return annotation.value();
    }

    /**
     * 获取controller的注解名称
     * @param joinpoint
     * @return
     */
    public static String getControllerDesc(JoinPoint joinpoint) {
        Api api = getControllerClass(joinpoint).getAnnotation(Api.class);
        return api.tags()[0];
    }

    /**
     * 获取controller 方法
     * @param joinpoint
     * @return
     */
    public static Method getControllerMethod(JoinPoint joinpoint) {
        Signature signature = joinpoint.getSignature();
        MethodSignature methodSignature = (MethodSignature)signature;

        return methodSignature.getMethod();
    }

    /**
     * 获取controller 类
     * @param joinpoint
     * @return
     */
    public static Class<?> getControllerClass(JoinPoint joinpoint) {
        return joinpoint.getTarget().getClass();
    }




}
