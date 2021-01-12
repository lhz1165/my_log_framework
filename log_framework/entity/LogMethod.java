package cn.stylefeng.guns.cloud.system.log_framework.entity;

import java.lang.reflect.Method;

/**
 * @author: lhz
 * @date: 2020/12/18
 **/
public class LogMethod {
    String typeName;

    Method method;

    String description;

    boolean isBody;

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public boolean isBody() {
        return isBody;
    }

    public void setBody(boolean body) {
        isBody = body;
    }

}
