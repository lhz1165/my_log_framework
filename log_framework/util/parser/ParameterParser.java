package cn.stylefeng.guns.cloud.system.log_framework.util.parser;

import cn.stylefeng.guns.cloud.system.log_framework.annotation.OperationRecordLog;
import cn.stylefeng.guns.cloud.system.log_framework.util.OperationUtil;
import com.abmatrix.component.common.enume.Parameter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.aspectj.lang.JoinPoint;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * @author: lhz
 * @date: 2020/12/29
 **/
@Component
public class ParameterParser implements BaseParser{
    @Override
    public String beforeParser(JoinPoint joinpoint, Object data,ObjectMapper writer) throws JsonProcessingException {
        String methodDesc = OperationUtil.getMethodDesc(joinpoint);
        if ("设置参数".equals(methodDesc)) {
            ObjectNode objectNode = new ObjectNode(JsonNodeFactory.withExactBigDecimals(true));
            Parameter parameterArg = (Parameter) joinpoint.getArgs()[0];
            BigDecimal value = (BigDecimal) joinpoint.getArgs()[1];
            objectNode.put("parameter",parameterArg.getDescribe());
            objectNode.put("value",value);
            return writer.writeValueAsString(objectNode);
        }
        return null;
    }
}
