package cn.jiliangqiju.constant;


/**
 * 工作计量器具用途代码1、安全防护 2、医疗卫生 3、环境监测 4、贸易结算
 *
 * @author yxuin
 */
public enum PurposeCodeEnum {
    /**
     * 安全防护
     */
    Safety("1", "32","安全防护"),
    Medical_Treatment("2", "33","医疗卫生"),
    Monitoring("3", "34","环境监测"),
    Trade("4", "31","安全贸易结算防护");

    /**
     * ECQS编号
     */
    final String ecqsCode;
    /**
     * 包头系统编号
     */
    final String systemCode;
    /**
     * 名称
     */
    final String name;
    PurposeCodeEnum(String ecqsCode, String systemCode,String name) {
        this.ecqsCode = ecqsCode;
        this.systemCode = systemCode;
        this.name = name;
    }
    public String getName() {
        return this.name;
    }
    public String getEcqsCode() {
        return this.ecqsCode;
    }

    public String getSystemCode() {
        return this.systemCode;
    }
}
