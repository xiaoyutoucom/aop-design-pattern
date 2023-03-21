package cn.jiliangqiju.constant;


/**
 * 强检计量器具类型1、社会公用计量标准
 * 2、部门和企事业单位最高计量标准
 * 3、工作计量器具
 *
 * @author yxuin
 */
public enum MeterTypeEnum {
    /**
     * 社会公用计量标准
     */
    SOCIETY("1", "11"),
    /**
     * 部门和企事业单位最高计量标准
     */
    DEPARTMENT("2", "12"),
    /**
     * 工作计量器具
     */
    WORK("3", "");

    /**
     * ECQS编号
     */
    final String ecqsCode;
    /**
     * 包头系统编号
     */
    final String systemCode;

    MeterTypeEnum(String ecqsCode, String systemCode) {
        this.ecqsCode = ecqsCode;
        this.systemCode = systemCode;
    }

    public String getEcqsCode() {
        return this.ecqsCode;
    }

    public String getSystemCode() {
        return this.systemCode;
    }
}
