package cn.stylefeng.guns.cloud.system.log_framework.aspect;

import cn.stylefeng.guns.cloud.auth.api.constants.AuthConstants;
import cn.stylefeng.guns.cloud.auth.api.context.LoginUser;
import cn.stylefeng.guns.cloud.system.log_framework.annotation.ChangeParam;
import cn.stylefeng.guns.cloud.system.log_framework.annotation.OperationRecordLog;
import cn.stylefeng.guns.cloud.system.log_framework.util.OperationUtil;
import cn.stylefeng.guns.cloud.system.log_framework.util.parser.BaseParser;
import cn.stylefeng.guns.cloud.system.modular.ent.entity.OperationLog;
import cn.stylefeng.guns.cloud.system.modular.ent.service.OperationLogService;
import cn.stylefeng.roses.kernel.model.api.model.TokenReq;
import com.abmatrix.component.common.vo.CommonResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

import static cn.stylefeng.guns.cloud.auth.api.constants.AuthConstants.AUTH_HEADER;

/**
 * @author: lhz
 * @date: 2020/12/7
 **/
@Aspect
@Component
public class ApsectConfig implements BeanFactoryAware {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private OperationLogService operationLogService;

    private BeanFactory beanFactory;


    @Pointcut("@annotation(cn.stylefeng.guns.cloud.system.log_framework.annotation.OperationRecordLog)")
    public void pointCut() {

    }

    @AfterReturning(value = "pointCut()", returning = "returnValue")
    public void recordOperation(JoinPoint joinpoint, Object returnValue) {
        try {
            if (!((CommonResult) returnValue).getCode()
                    .equals(CommonResult.RetCode.SUCCESS)) {
                return;
            }
            LoginUser loginUser = getLoginUserByToken(getToken());
            OperationLog log = new OperationLog();
            if (loginUser != null) {
                log.setUserId(loginUser.getUserId());
                log.setUserName(loginUser.getName());
            }
            log.setOperateTime(LocalDateTime.now());
            log.setOperateContent(OperationUtil.getMethodDesc(joinpoint));
            log.setPath(OperationUtil.getUrl(joinpoint));
            log.setType(OperationUtil.getControllerDesc(joinpoint));
            log.setParameter(getJsonParameter(joinpoint, ((CommonResult) returnValue).getData()));
            log.setMethodName(OperationUtil.getControllerClass(joinpoint)
                    .getName() + "." + OperationUtil.getControllerMethod(joinpoint)
                    .getName());

            log.setReturnValue(new ObjectMapper().writeValueAsString(returnValue));
            operationLogService.save(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String getJsonParameter(JoinPoint joinpoint, Object data) throws IOException {
        ObjectMapper writer = new ObjectMapper();
        //防止BigDecimal格式变成科学计数法
        writer = writer.setNodeFactory(JsonNodeFactory.withExactBigDecimals(true));

        //如果使用自定义解析器会在这结束
        //一般对参数比较奇葩特殊的接口来使用，需要自定义解析方法，大多数接口不走这里
        for (BaseParser baseParser : getBeforeParser()) {
            String result = baseParser.beforeParser(joinpoint, data,writer);
            if (!StringUtils.isEmpty(result)) {
                return result;
            }
        }
        //否则一般使用默认的解析器
        return defaultParameter2Json(joinpoint, data,writer);
    }


    public ObjectNode getJsonParameter(ObjectMapper mapper) throws JsonProcessingException {
        HttpServletRequest request = getRequest();
        ObjectNode newNode = mapper.createObjectNode();
        Enumeration<String> parameterNames = request.getParameterNames();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        while (parameterNames.hasMoreElements()) {
            String name = parameterNames.nextElement();
            String val = request.getParameter(name);
            if (name.toLowerCase().contains("time") || name.toLowerCase().contains("date")) {
                try {
                    String formatDate = sdf.format(new Date(Long.parseLong(val)));
                    newNode.put(name, formatDate);
                } catch (Exception e) {
                    newNode.put(name, request.getParameter(name));
                }
            }else {
                newNode.put(name, request.getParameter(name));
            }
        }
        return newNode;
    }

    public TokenReq getToken() {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert servletRequestAttributes != null;
        HttpServletRequest request = servletRequestAttributes.getRequest();
        //获取
        // 请求头的Auth token
        String agent = request.getHeader(AUTH_HEADER);
        TokenReq tokenReq = new TokenReq();
        tokenReq.setToken(agent);
        return tokenReq;
    }

    /**
     * 获取登录用户根据当前登录token
     */
    public LoginUser getLoginUserByToken(TokenReq tokenReq) {
        return (LoginUser) redisTemplate.boundValueOps(AuthConstants.TOKEN_PREFIX + tokenReq.getToken())
                .get();
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert servletRequestAttributes != null;
        return servletRequestAttributes.getRequest();
    }

    public List<BaseParser> getBeforeParser() {
        List<BaseParser> res = new ArrayList<>();
        String[] beforeParserNames = ((DefaultListableBeanFactory) beanFactory).getBeanNamesForType(BaseParser.class);
        for (String parserName : beforeParserNames) {
            BaseParser baseParser = (BaseParser) beanFactory.getBean(parserName);
            res.add(baseParser);
        }
        return res;
    }

    /**
     * 把前端传入的参数转化成json格式的内容记录在数据库
     * @param joinpoint
     * @param data
     * @return
     * @throws JsonProcessingException
     */
    public String defaultParameter2Json(JoinPoint joinpoint, Object data,ObjectMapper writer) throws JsonProcessingException {
        OperationRecordLog operationAnnotation = OperationUtil.getControllerMethod(joinpoint)
                .getAnnotation(OperationRecordLog.class);
        //1. 把请求参数解析为json格式字符串，一般情况下只执行这个方法，下面方法都是处理特殊情况
        ObjectNode jsonParameter = getJsonParameter(operationAnnotation, joinpoint, writer);

        //2. 如果有返回值那么也设置到参数里面去
        if (!StringUtils.isEmpty(operationAnnotation.returnName())) {
            jsonParameter.put(operationAnnotation.returnName(), String.valueOf(data));
        }

        //3. 如果需要转义
        //例如pauseFlag --》  true代表暂停，false代表开始
        //把jsonNode里面的   pauseFlag:true替换为  pauseFlag:暂停
        changeMeaning(operationAnnotation, jsonParameter);


        return writer.writeValueAsString(jsonParameter);
    }


    private ObjectNode getJsonParameter(OperationRecordLog operationAnnotation,JoinPoint joinpoint,ObjectMapper writer) throws JsonProcessingException {
        ObjectNode jsonParameter = null;
        if (operationAnnotation.isBody()) {
            Object body = joinpoint.getArgs()[0];
            //json格式的参数
            jsonParameter = writer.convertValue(body, ObjectNode.class);
        } else {
            //通过application/x-www-form-urlencoded传参的参数
            jsonParameter = getJsonParameter(writer);
        }
        return jsonParameter;
    }


    public void changeMeaning(OperationRecordLog annotation, ObjectNode jsonParameter) {
        for (ChangeParam changeParam : annotation.changeAttribute()) {
            String parameterName = changeParam.ChangeFiled();
            String[] keys = changeParam.ChangeFiledKey();
            String[] val = changeParam.changeFiledVal();
            HashMap<String, String> map = new HashMap<>();
            for (int i = 0; i < keys.length; i++) {
                map.put(keys[i], val[i]);
            }
            jsonParameter.put(parameterName, map.get(jsonParameter.get(parameterName)
                    .asText()));
        }
    }
    /**
     * 依赖注入
     * 使用beanFactory的api获取bean
     *
     * @param beanFactory
     * @throws BeansException
     */
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}