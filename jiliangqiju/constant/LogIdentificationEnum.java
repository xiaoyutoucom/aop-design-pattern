package cn.jiliangqiju.constant;


/**
 * 识别日志类型
 *
 * @author yxuin
 */
public enum LogIdentificationEnum {
    /**
     * 用户信息操作日志
     */
    UserController("cn.jiliangqiju.controller.rbac.UserController", ControllerTypeEnum.OPERATION.getCode(),"用户"),
    /**
     * 角色操作日志
     */
    RoleController("cn.jiliangqiju.controller.rbac.RoleController", ControllerTypeEnum.OPERATION.getCode(),"角色"),
    /**
     * 权限操作日志
     */
    PermissionController("cn.jiliangqiju.controller.rbac.PermissionController", ControllerTypeEnum.OPERATION.getCode(),"权限"),
    /**
     * 器具操作日志
     */
    MeterController("cn.jiliangqiju.controller.meterManage.MeterController", ControllerTypeEnum.OPERATION.getCode(),"器具"),
    /**
     * 单位操作日志
     */
    OrganizationController("cn.jiliangqiju.controller.org.OrganizationController", ControllerTypeEnum.OPERATION.getCode(),"单位"),
    /**
     * 部门操作日志
     */
    DepartmentController("cn.jiliangqiju.controller.org.DepartmentController", ControllerTypeEnum.OPERATION.getCode(),"部门"),
    /**
     * 包间道回传日志
     */
    YMController("cn.jiliangqiju.controller.api.YMController",ControllerTypeEnum.BJD.getCode(),"包间道"),
    /**
     * 包间道回传日志
     */
    EcqsJob("cn.jiliangqiju.timer.EcqsJob", ControllerTypeEnum.ECQS.getCode(),"ECQS"),
    ;
    /**
     * 对应Controller
     */
    final String controller;
    /**
     * 日志类型
     */
    final String controllerType;
    /**
     * 名称
     */
    final String name;
    LogIdentificationEnum(String controller, String controllerType,String name) {
        this.controller = controller;
        this.controllerType = controllerType;
        this.name = name;
    }
    public String getName() {
        return this.name;
    }
    public String getController() {
        return this.controller;
    }
    public String geControllerType() {
        return this.controllerType;
    }
    public static LogIdentificationEnum getLogIdentification(String controller){
        LogIdentificationEnum logIdentificationEnum =  LogIdentificationEnum.EcqsJob;
        for (LogIdentificationEnum typeEnum : LogIdentificationEnum.values()) {
            //判断是需要保存操作日志、包间道日志还是ECQS日志
            if (controller.equals(typeEnum.getController())) {
                logIdentificationEnum = typeEnum;
            }
        }
        return logIdentificationEnum;
    }
}
