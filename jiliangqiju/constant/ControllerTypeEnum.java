package cn.jiliangqiju.constant;


import cn.jiliangqiju.service.log.BjdLog;
import cn.jiliangqiju.service.log.EcqsLog;
import cn.jiliangqiju.service.log.LogStrategy;
import cn.jiliangqiju.service.log.OperationLog;

/**
 * 器具专业对应ECQS
 *
 * @author yxuin
 */
public enum ControllerTypeEnum {
    /**
     * 操作日志
     */
    OPERATION("1" , new OperationLog()),
    /**
     * ECQS
     */
    ECQS("2", new EcqsLog()),
    /**
     * BJD
     */
    BJD("3", new BjdLog());
    /**
     * 编码
     */
    final String code;
    final LogStrategy logStrategy;
    ControllerTypeEnum(String code, LogStrategy logStrategy) {
        this.code = code;
        this.logStrategy = logStrategy;
    }
    public LogStrategy getLogStrategy() {
        return this.logStrategy;
    }
    public String getCode() {
        return this.code;
    }
    public static ControllerTypeEnum getControllerType(String code){
        for (ControllerTypeEnum typeEnum : ControllerTypeEnum.values()) {
            //判断是需要保存操作日志、包间道日志还是ECQS日志
            if (code.equals(typeEnum.getCode())) {
                return typeEnum;
            }
        }
        return null;
    }
}
