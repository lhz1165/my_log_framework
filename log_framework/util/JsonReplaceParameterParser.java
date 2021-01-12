package cn.stylefeng.guns.cloud.system.log_framework.util;


import cn.stylefeng.guns.cloud.system.log_framework.entity.LogMethod;
import cn.stylefeng.guns.cloud.system.modular.ent.entity.OperationLog;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: lhz
 * @date: 2020/12/18
 **/
public class JsonReplaceParameterParser {
    /**
     * 配置的描述，参数必须用<>符号括起来然后，然后用数据库保存的参数来替换
     * 例如:
     * descRes= 生活馆列表情况类型 <hallType>,页数 <pageSize>当前页 <currentPage>
     * jsonParameter ={"hallType":"null","pageSize":"10","currentPage":"1"}
     * 替换后
     * result = 生活馆列表情况类型 null,页数 10当前页 1
     * @param
     * @param logMethod
     * @return
     * @throws Exception
     */
    public static String parseDescByJson(OperationLog log, LogMethod logMethod) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(log.getParameter());
        String descRes = logMethod.getDescription();
        List<String> paramList = Arrays.stream(descRes.split(">")).map(str -> str.substring(str.lastIndexOf("<")+1)).collect(Collectors.toList());
        for (String param : paramList) {
            System.out.println(jsonNode.get(param));
            String prev = "<";
            String tail = ">";
            descRes=descRes.replaceAll(prev+param+tail,String.valueOf(jsonNode.get(param)));
        }
        descRes = descRes.replaceAll("\"","");

        return descRes;
    }


}
