package cn.stylefeng.guns.cloud.system.log_framework;

import cn.stylefeng.guns.cloud.system.log_framework.entity.LogMethod;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;


/**
 * @author: lhz
 * @date: 2020/12/18
 **/
@Component
@Data
public class LogConfiguration {
    /**
     * 方法名 ---> LogMethod对象
     */
    Map<String, LogMethod> logStatement = new HashMap<>();

    /**
     * 方法名---> 配置文件的内容（返回描述，参数转化）
     */
    @Value("#{${method2Description}}")
    Map<String, String> methodToDescription;



}
