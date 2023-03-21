package cn.jiliangqiju.constant;


/**
 * 器具专业对应ECQS
 *
 * @author yxuin
 */
public enum CategoryCodeEnum {
    /**
     * 力学
     */
    MECHANICS("004,012,014,015,020,021,023,027,028,029,059,060,061,062,063,064,065,071", "3"),
    /**
     * 电磁
     */
    ELECTROMAGNETISM("038,066", "4"),
    /**
     * 光学
     */
    OPTICS("057,069", "5"),
    /**
     * 化学
     */
    CHEMISTRY("052,056,070", "7"),
    /**
     * 声学
     */
    ACOUSTICS("04,042", "8"),
    /**
     * 电离辐射
     */
    IONIZING_RADIATION("067,068", "9");

    /**
     * ECQS编号
     */
    final String ecqsCode;
    /**
     * 包头系统编号
     */
    final String systemCode;

    CategoryCodeEnum(String ecqsCode, String systemCode) {
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
