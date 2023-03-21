package cn.jiliangqiju.service.log;

import cn.jiliangqiju.constant.LogIdentificationEnum;
import org.aspectj.lang.JoinPoint;
/**
 *	日志策略
 *  @author yuxin
 *
 */
public interface LogStrategy {
    /**
     *	监听保存日志
     * @param point 封装了代理方法信息的对象
     * @param typeEnum 日持操作信息
     * @param e 错误信息
     */
    public abstract void logSave(JoinPoint point, LogIdentificationEnum typeEnum,Exception e);
}
