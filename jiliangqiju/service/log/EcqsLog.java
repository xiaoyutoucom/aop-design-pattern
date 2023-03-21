package cn.jiliangqiju.service.log;

import cn.jiliangqiju.constant.LogIdentificationEnum;
import cn.jiliangqiju.dto.LogEntity;
import cn.jiliangqiju.repository.mapper.verification.EcqsLogMapper;
import cn.jiliangqiju.util.JacksonUtil;
import lombok.SneakyThrows;
import org.aspectj.lang.JoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.util.Date;
/**
 *	ECQS日志保存
 *  @author yuxin
 *
 */
@Component
public class EcqsLog implements LogStrategy{
    @Autowired
    private EcqsLogMapper ecqsLogMapper;
    public  static EcqsLogMapper ecqsLogMapperAuto ;
    @PostConstruct
    public void init(){ecqsLogMapperAuto = this.ecqsLogMapper;}

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
        LogEntity logEntity = new LogEntity();
        logEntity.setCreateTime(new Date());
        logEntity.setResponseJson(JacksonUtil.obj2json(args));
        ecqsLogMapperAuto.insertLog(logEntity);
    }

}
