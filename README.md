**优化内容需求：**
①　使用AOP统一处理API接口日志信息
重构前需求：
1.在包头系统中，与外部系统对接接口，都有保存日志的功能，以前的处理方式是每个接口自己调用自己保存。
![在这里插入图片描述](https://img-blog.csdnimg.cn/7b4f19050b3647d88beb061e4e1c38f1.png)
2.现在包头系统需要对接了ECQS系统，需要保存的日志参数更多，所以新建api_log_response表存储ECQS相关日志。
3.LogAspect中操作日志代码if else特别多，特别长，修改维护和阅读特别困难。
为了优化代码，利于以后的维护和修改进行优化
使用了工厂模式+策略模式+枚举的方式进行优化
**实际操作：**
首先使用sop对所有对外api日志保存进行监控同意操作

 1. 第一版

示例代码：

```java
/**
 * 第一版
 * @author yuxin
 *
 */
@Aspect
@Component
public class LogAspectBackUp {
	@Autowired
	private LogService logService;

	//==========rbac模块监听============
	@Pointcut("execution(* cn.jiliangqiju.controller.rbac.*.add*(..)) || execution(* cn.jiliangqiju.controller.rbac.*.update*(..)) || execution(* cn.jiliangqiju.controller.rbac.*.delete*(..))")
	private void rbacAUDMethod(){}
	@AfterReturning(value="rbacAUDMethod()")
	public void afterRbacAUD(JoinPoint point){
		String classname=point.getSignature().getDeclaringTypeName();
		if("cn.jiliangqiju.controller.rbac.UserController".equals(classname)){
			String methodname=point.getSignature().getName();
			if("add".equals(methodname)){
				handleAUD( point, null,"用户新增","1");
			}else if("update".equals(methodname)){
				handleAUD( point, null,"用户修改","2");
			}else if("delete".equals(methodname)){
				handleAUD( point, null,"用户删除","3");
			}else{
				handleAUD( point, null,"用户管理其他增删改","4");
			}

		}else if("cn.jiliangqiju.controller.rbac.RoleController".equals(classname)){
			String methodname=point.getSignature().getName();
			if("add".equals(methodname)){
				handleAUD( point, null,"角色新增","1");
			}else if("update".equals(methodname)){
				handleAUD( point, null,"角色修改","2");
			}else if("delete".equals(methodname)){
				handleAUD( point, null,"角色删除","3");
			}else{
				handleAUD( point, null,"角色管理其他增删改","4");
			}
		}else if("cn.jiliangqiju.controller.rbac.PermissionController".equals(classname)){
			String methodname=point.getSignature().getName();
			if("add".equals(methodname)){
				handleAUD( point, null,"权限新增","1");
			}else if("update".equals(methodname)){
				handleAUD( point, null,"权限修改","2");
			}else if("delete".equals(methodname)){
				handleAUD( point, null,"权限删除","3");
			}else{
				handleAUD( point, null,"权限管理其他增删改","4");
			}
		}
}
```
 2. 第二版
 阅读上述代码会发现，虽然统一对日志进行了操作，但是if else代码过多，不利于以后的维护，先进行部分优化
 首先新增二个枚举类，匹配对应的监控类和方法
 识别日志类型枚举
```java
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

```
识别日志具体方法枚举

```java
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

```
这样代码就可以简化很多了
```java
/**
	 *	模块监听
	 */
	@Pointcut("execution(* cn.jiliangqiju.controller.org.DepartmentController.delete*(..))|| " +
			"execution(* cn.jiliangqiju.controller.org.OrganizationController.add*(..)) || " +
			"execution(* cn.jiliangqiju.controller.org.OrganizationController.update*(..))|| " +
			"execution(* cn.jiliangqiju.controller.org.OrganizationController.delete*(..))||" +
			"execution(* cn.jiliangqiju.controller.meterManage.MeterController.delete*(..))||" +
			"execution(* cn.jiliangqiju.controller.rbac.RoleController.saveRolePermission(..))||" +
			"execution(* cn.jiliangqiju.controller.rbac.UserController.saveUserRole(..))||" +
			"execution(* cn.jiliangqiju.controller.rbac.UserController.saveBlackList(..))||" +
			"execution(* cn.jiliangqiju.controller.rbac.*.add*(..)) || " +
			"execution(* cn.jiliangqiju.controller.rbac.*.update*(..)) || " +
			"execution(* cn.jiliangqiju.controller.rbac.*.delete*(..))")
	private void logMethod(){}
	/**
	 *	监听保存日志
	 * @param point 封装了代理方法信息的对象
	 * @param returnValue 返回信息
	 */
	@AfterReturning(value="logMethod()", returning="returnValue")
	public void afterAUD(JoinPoint point, Object returnValue){
		//获取classname
		String classname=point.getSignature().getDeclaringTypeName();
		//获取调用的方法名
		String methodname=point.getSignature().getName();
		LogIdentificationEnum typeEnum = LogIdentificationEnum.getLogIdentification(classname);
		MethodEnum methodEnum = MethodEnum.getMethod(classname);
		//保存日志方法
		handleAUD( point, null,typeEnum.getName()+methodEnum.getName(),methodEnum.geMethodType());
	}
```
 3. 第三版
 上一个版本已经清除了所有if else，不过后续有新增需求，需要在调用其他两种对外接口自定义日志信息。
 还需要监听错误日志。
 代码如下
 

```java
	/**
	 *	模块监听
	 */
	@Pointcut(
			"execution(* cn.jiliangqiju.timer.EcqsJob.inData(..))|| " +
			"execution(* cn.jiliangqiju.controller.api.YMController.insertInspMeterInfo(..))|| " +
			"execution(* cn.jiliangqiju.controller.org.DepartmentController.delete*(..))|| " +
			"execution(* cn.jiliangqiju.controller.org.DepartmentController.add*(..)) || " +
			"execution(* cn.jiliangqiju.controller.org.DepartmentController.update*(..))|| " +
			"execution(* cn.jiliangqiju.controller.org.OrganizationController.add*(..)) || " +
			"execution(* cn.jiliangqiju.controller.org.OrganizationController.update*(..))|| " +
			"execution(* cn.jiliangqiju.controller.org.OrganizationController.delete*(..))||" +
			"execution(* cn.jiliangqiju.controller.meterManage.MeterController.delete*(..))||" +
			"execution(* cn.jiliangqiju.controller.rbac.RoleController.saveRolePermission(..))||" +
			"execution(* cn.jiliangqiju.controller.rbac.UserController.saveUserRole(..))||" +
			"execution(* cn.jiliangqiju.controller.rbac.UserController.saveBlackList(..))||" +
			"execution(* cn.jiliangqiju.controller.rbac.*.add*(..)) || " +
			"execution(* cn.jiliangqiju.controller.rbac.*.update*(..)) || " +
			"execution(* cn.jiliangqiju.controller.rbac.*.delete*(..))")
	private void logMethod(){}
	/**
	 *	监听保存日志
	 * @param point 封装了代理方法信息的对象
	 */
	@AfterReturning(value="logMethod()")
	public void afterAud(JoinPoint point){
		logTypeSelection(point,null);
	}
	/**
	 *	监听保存错误日志
	 * @param point 封装了代理方法信息的对象
	 * @param e 错误信息
	 */
	@AfterThrowing(value="logMethod()",throwing="e")
	public void afterThrowAud(JoinPoint point,Exception e){
		logTypeSelection(point,e);
	}
	@SneakyThrows
	private void logTypeSelection(JoinPoint point, Exception e){
		//获取classname
		String classname=point.getSignature().getDeclaringTypeName();
		//获取调用的方法名
		String methodname=point.getSignature().getName();
		// 拦截的方法参数
		Object[] args = point.getArgs();
		LogIdentificationEnum typeEnum = LogIdentificationEnum.getLogIdentification(classname);
		//找出对应的controller
			//判断是需要保存操作日志、包间道日志还是ECQS日志
			if (classname.equals(typeEnum.getController())) {
			if(typeEnum.geControllerType().equals(ControllerTypeEnum.OPERATION.getCode())) {
				MethodEnum methodEnum = MethodEnum.getMethod(classname);
				//保存日志方法
				handleAud( point, null,typeEnum.getName()+methodEnum.getName(),methodEnum.geMethodType());
			}
			//包间道回传接口
			else if(typeEnum.geControllerType().equals(ControllerTypeEnum.BJD.getCode())){
				// 日志记录
				Map<String, Object> bjdResp = new HashMap<>(2);
				bjdResp.put("responseJson", JacksonUtil.obj2json(args));
				bjdResp.put("createTime", new Date());
				jdelegateMasterMapper.insertBJDRes(bjdResp);
			}
			else{
				LogEntity logEntity = new LogEntity();
				logEntity.setCreateTime(new Date());
				logEntity.setResponseJson(JacksonUtil.obj2json(args));
				ecqsLogMapper.insertLog(logEntity);
			}
			}
	}
```

 5. 第四版
 阅读上一个版本代码发现if else又出现了
 现在使用工厂模式+策略模式
 代码如下
 首先定义策略接口

```java
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

```
定义对应的策略

```java
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
```

```java
/**
 *	系统操作日志保存
 *  @author yuxin
 *
 */
@Service
public class OperationLog implements LogStrategy{
    @Autowired
    private LogService logService;
    public  static LogService logServiceAuto ;
    @PostConstruct
    public void init(){logServiceAuto = this.logService;}
    /**
     *	监听保存日志
     * @param point 封装了代理方法信息的对象
     * @param typeEnum 日持操作信息
     * @param e 错误信息
     */
    @Override
    public void logSave(JoinPoint point, LogIdentificationEnum typeEnum,Exception e) {
        String methodname=point.getSignature().getName();
        MethodEnum methodEnum = MethodEnum.getMethod(methodname);
        if (methodEnum!=null) {
            if (methodname.equals(methodEnum.getMethod())) {
                //判断是否是错误日志
                if(ObjectUtil.isNull(e)) {
                    handleAud(point, null, typeEnum.getName() + methodEnum.getName(), methodEnum.geMethodType());
                }else{
                    handleAud( point, e,typeEnum.getName()+methodEnum.getName()+MethodEnum.ERROR.getName(),MethodEnum.ERROR.geMethodType());
                }
            }
        }
    }
    /**增删改监听处理方法
     * @param point JoinPoint
     * @param e 错误
     * @param title 标题
     * @param type 类型
     */
    public void handleAud(JoinPoint point, Exception e, String title, String type){
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            //从shiro的session中取User
            Subject subject = SecurityUtils.getSubject();
            //取身份信息
            User activeUser = (User) subject.getPrincipal();
            String usercode="";
            if(activeUser!=null){
                usercode=activeUser.getUsercode();
            }
            LogWithBLOBs log=new LogWithBLOBs();
            String uri= LogUtil.getIpAddr(request);
            String requesturi=request.getRequestURI();
            String method=point.getSignature().getDeclaringTypeName() + "." + point.getSignature().getName();
            String params= Arrays.toString(point.getArgs());
            String useragent=request.getHeader("User-Agent");
            log.setCreateBy(usercode);
            log.setCreateDate(new Date());
            log.setId(UuidUtil.get32UUID());
            log.setMethod(method);
            log.setRequestUri(requesturi);
            log.setRemoteAddr(uri);
            log.setParams(params);
            log.setTitle(title);
            log.setLogtype(type);
            log.setUserAgent(useragent);
            if(e!=null){
                log.setException(e.getMessage());
            }
            logService.insertLog(log);
            System.out.println("aop消息--@After：操作账号:"+usercode+",远程客户端IP:"+uri+"请求uri:"+requesturi+" 目标方法为：" + method+" 参数为：" + params+" 被织入的目标对象为：" + point.getTarget()+" 客户端类型："+request.getHeader("User-Agent"));
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

}

```

```java
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

```
定义工厂枚举类，用于判断对应策略

```java
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
```
定义日志工厂

```java
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
```
最终aop中的代码吧if else 全部去掉了

```java

@Aspect
@Component
public class LogAspect {
	/**
	 *	模块监听
	 */
	@Pointcut(
			"execution(* cn.jiliangqiju.timer.EcqsJob.inData(..))|| " +
			"execution(* cn.jiliangqiju.controller.api.YMController.insertInspMeterInfo(..))|| " +
			"execution(* cn.jiliangqiju.controller.org.DepartmentController.delete*(..))|| " +
			"execution(* cn.jiliangqiju.controller.org.DepartmentController.add*(..)) || " +
			"execution(* cn.jiliangqiju.controller.org.DepartmentController.update*(..))|| " +
			"execution(* cn.jiliangqiju.controller.org.OrganizationController.add*(..)) || " +
			"execution(* cn.jiliangqiju.controller.org.OrganizationController.update*(..))|| " +
			"execution(* cn.jiliangqiju.controller.org.OrganizationController.delete*(..))||" +
			"execution(* cn.jiliangqiju.controller.meterManage.MeterController.delete*(..))||" +
			"execution(* cn.jiliangqiju.controller.rbac.RoleController.saveRolePermission(..))||" +
			"execution(* cn.jiliangqiju.controller.rbac.UserController.saveUserRole(..))||" +
			"execution(* cn.jiliangqiju.controller.rbac.UserController.saveBlackList(..))||" +
			"execution(* cn.jiliangqiju.controller.rbac.*.add*(..)) || " +
			"execution(* cn.jiliangqiju.controller.rbac.*.update*(..)) || " +
			"execution(* cn.jiliangqiju.controller.rbac.*.delete*(..))")
	private void logMethod(){}
	/**
	 *	监听保存日志
	 * @param point 封装了代理方法信息的对象
	 */
	@AfterReturning(value="logMethod()")
	public void afterAud(JoinPoint point){
		logTypeSelection(point,null);
	}
	/**
	 *	监听保存错误日志
	 * @param point 封装了代理方法信息的对象
	 * @param e 错误信息
	 */
	@AfterThrowing(value="logMethod()",throwing="e")
	public void afterThrowAud(JoinPoint point,Exception e){
		logTypeSelection(point,e);
	}
	@SneakyThrows
	private void logTypeSelection(JoinPoint point, Exception e){
		//获取classname
		String classname=point.getSignature().getDeclaringTypeName();
		//找出对应的controller
		LogIdentificationEnum typeEnum = LogIdentificationEnum.getLogIdentification(classname);
		//初始化工厂
		LogFactory logFactory = new LogFactory(typeEnum.geControllerType());
		//保存日志信息
		logFactory.logSave(point, typeEnum, e);
	}
}
```
