package cn.stylefeng.guns.cloud.system.log_framework.util;

import cn.stylefeng.guns.cloud.system.log_framework.annotation.OperationRecordLog;
import cn.stylefeng.guns.cloud.system.log_framework.LogConfiguration;
import cn.stylefeng.guns.cloud.system.log_framework.entity.LogMethod;
import cn.stylefeng.roses.kernel.scanner.modular.util.AopTargetUtils;
import io.swagger.annotations.Api;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author: lhz
 * @date: 2020/12/18
 **/
@Slf4j
@Component
public class LogPostProcess implements BeanPostProcessor {


    @Autowired
    LogConfiguration configuration;


    @SneakyThrows
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        Map<String, LogMethod> logStatement = configuration.getLogStatement();

        Map<String, String> methodToDescription = configuration.getMethodToDescription();
        //如果controller是代理对象,则需要获取原始类的信息
        Object aopTarget = AopTargetUtils.getTarget(bean);

        if (aopTarget == null) {
            aopTarget = bean;
        }
        Class<?> clazz = aopTarget.getClass();

        //判断是否有 @OperationLog的 标记
        boolean logFlag = getControllerFlag(clazz);
        if (!logFlag) {
            return bean;
        }
        String className = clazz.getName();
      //解析方法里面@OperationLog 的参数
        Method[] controllers = clazz.getDeclaredMethods();
        for (Method controller : controllers) {
            OperationRecordLog operationRecordLog = controller.getAnnotation(OperationRecordLog.class);
            if (operationRecordLog != null) {
                String desc = methodToDescription.get(className +"."+ controller.getName());
                if (desc == null) {
                    log.warn("{}类 的{}方法没有设置日志描述",className,controller.getName());
                    throw  new Exception(className+"类" +"的"+controller.getName()+"方法没有设置日志描述");
                }
                LogMethod logMethod = new LogMethod();
                logMethod.setMethod(controller);
                logMethod.setBody(operationRecordLog.isBody());
                logMethod.setDescription(desc);
                logMethod.setTypeName(clazz.getAnnotation(Api.class).tags()[0]);
                logStatement.put(className +"."+controller.getName(), logMethod);
            }

        }
        return bean;
    }



    private boolean getControllerFlag(Class<?> clazz) {
        Annotation[] annotations = clazz.getAnnotations();
        for (Annotation annotation : annotations) {
            if (OperationRecordLog.class.equals(annotation.annotationType())) {
                return true;
            }
        }
        return false;
    }
}
