package cn.jiliangqiju.service.log;

import cn.jiliangqiju.constant.LogIdentificationEnum;
import cn.jiliangqiju.repository.mapper.verification.JDelegateMasterMapper;
import cn.jiliangqiju.util.JacksonUtil;
import lombok.SneakyThrows;
import org.aspectj.lang.JoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
/**
 *	包间道日志保存
 *  @author yuxin
 *
 */
@Service
public class BjdLog implements LogStrategy{
    @Autowired
    private JDelegateMasterMapper jdelegateMasterMapper;
    public  static JDelegateMasterMapper jdelegateMasterMapperAuto ;
    @PostConstruct
    public void init(){jdelegateMasterMapperAuto = this.jdelegateMasterMapper;}
    /**
     *	监听保存日志
     * @param point 封装了代理方法信息的对象
     * @param typeEnum 日持操作信息
     * @param e 错误信息
     */
    @SneakyThrows
    @Override
    public void logSave(JoinPoint point, LogIdentificationEnum typeEnum,Exception e) {
        // 拦截的方法参数
        Object[] args = point.getArgs();
        // 日志记录
        Map<String, Object> bjdResp = new HashMap<>(2);
        bjdResp.put("responseJson", JacksonUtil.obj2json(args));
        bjdResp.put("createTime", new Date());
        jdelegateMasterMapper.insertBJDRes(bjdResp);
    }

}
