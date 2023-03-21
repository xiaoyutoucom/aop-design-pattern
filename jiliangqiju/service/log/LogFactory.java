package cn.jiliangqiju.service.log;

import cn.jiliangqiju.constant.ControllerTypeEnum;
import cn.jiliangqiju.constant.LogIdentificationEnum;
import org.aspectj.lang.JoinPoint;

/**
 *	日志工厂
 *  @author yuxin
 *
 */
public class LogFactory {
    private final LogStrategy logStrategy;
    /**
     *	选择日志种类
     * @param type 日志种类
     */
    public LogFactory(String type) {
        ControllerTypeEnum controllerTypeEnum = ControllerTypeEnum.getControllerType(type);
        assert controllerTypeEnum != null;
        logStrategy = controllerTypeEnum.getLogStrategy();
    }
    /**
     *	监听保存日志
     * @param point 封装了代理方法信息的对象
     * @param typeEnum 日持操作信息
     * @param e 错误信息
     */
    public void logSave(JoinPoint point, LogIdentificationEnum typeEnum, Exception e) {
        logStrategy.logSave(point,typeEnum,e);
    }

}
