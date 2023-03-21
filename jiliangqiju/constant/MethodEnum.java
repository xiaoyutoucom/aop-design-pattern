package cn.jiliangqiju.constant;


/**
 * 识别日志方法类型
 *
 * @author yxuin
 */
public enum MethodEnum {
    /**
     * 新增方法
     */
    ADD("add", LogTypeConstants.ADD,"新增"),
    /**
     * 更新方法
     */
    UPDATE("update", LogTypeConstants.UPDATE,"修改"),
    /**
     * 删除方法
     */
    DELETE("delete",  LogTypeConstants.DELETE,"删除"),
    /**
     * 更改权限黑名单
     */
    SAVEBLACKLIST("saveBlackList",  LogTypeConstants.UPDATE,"更改权限黑名单"),
    /**
     * 用户角色更改
     */
    SAVEUSERROLE("saveUserRole",  LogTypeConstants.UPDATE,"用户角色更改"),
    /**
     * 角色权限更改
     */
    SAVEROLEPERMISSION("saveRolePermission",  LogTypeConstants.UPDATE,"角色权限更改"),
    /**
     * 其他操作日志
     */
    OTHER("",  LogTypeConstants.OTHER,"管理其他增删改"),
    /**
     * 其他操作日志
     */
    ERROR("",  LogTypeConstants.ERROR,"异常"),
    ;
    /**
     * 对应Controller
     */
    final String method;
    /**
     * 日志类型
     */
    final String methodType;
    /**
     * 名称
     */
    final String name;
    MethodEnum(String method, String methodType, String name) {
        this.method = method;
        this.methodType = methodType;
        this.name = name;
    }
    public String getName() {
        return this.name;
    }
    public String getMethod() {
        return this.method;
    }
    public String geMethodType() {
        return this.methodType;
    }
    public static MethodEnum getMethod(String method){
        for (MethodEnum typeEnum : MethodEnum.values()) {
            //判断是需要保存操作日志、包间道日志还是ECQS日志
            if (method.equals(typeEnum.getMethod())) {
                return typeEnum;
            }
        }
        return null;
    }
}
