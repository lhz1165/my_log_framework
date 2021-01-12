package cn.stylefeng.guns.cloud.system.log_framework.util.parser;

import cn.stylefeng.guns.cloud.system.log_framework.annotation.OperationRecordLog;
import cn.stylefeng.guns.cloud.system.log_framework.util.OperationUtil;
import com.abmatrix.component.common.vo.req.AdminMemberSet;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.aspectj.lang.JoinPoint;
import org.springframework.stereotype.Component;

/**
 * @author: lhz
 * @date: 2020/12/25
 **/
@Component
public class UserBalanceEditParamParser implements BaseParser {
    @Override
    public String beforeParser(JoinPoint joinpoint, Object data,ObjectMapper writer) throws JsonProcessingException {
        String methodDesc = OperationUtil.getMethodDesc(joinpoint);
        if ("修改用户资金".equals(methodDesc)) {
            AdminMemberSet memberSet = (AdminMemberSet) joinpoint.getArgs()[0];
            ObjectNode objectNode = new ObjectNode(JsonNodeFactory.withExactBigDecimals(true));
            objectNode.put("username", memberSet.getUsername());
            if (memberSet.getOystOptionBalance() != null) {
                objectNode.put("balance", memberSet.getOystOptionBalance());
                objectNode.put("type", "OYST_OPTION");
                return writer.writeValueAsString(objectNode);
            }
            if (memberSet.getOystBalance() != null) {
                objectNode.put("balance", memberSet.getOystBalance());
                objectNode.put("type", "OYST");
                return writer.writeValueAsString(objectNode);
            }
            if (memberSet.getCnytBalance() != null) {
                objectNode.put("balance", memberSet.getCnytBalance());
                objectNode.put("type", "CNYT");
                return writer.writeValueAsString(objectNode);
            }
            if (memberSet.getAdBalance() != null) {
                objectNode.put("balance", memberSet.getAdBalance());
                objectNode.put("type", "AD");
                return writer.writeValueAsString(objectNode);
            }
        }
        return null;
    }
}
