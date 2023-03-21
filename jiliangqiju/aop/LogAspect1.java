package cn.jiliangqiju.aop;

import cn.jiliangqiju.constant.LogIdentificationEnum;
import cn.jiliangqiju.constant.MethodEnum;
import cn.jiliangqiju.entity.bussi.LogWithBLOBs;
import cn.jiliangqiju.entity.rbac.User;
import cn.jiliangqiju.service.bussi.LogService;
import cn.jiliangqiju.util.LogUtil;
import cn.jiliangqiju.util.UuidUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;

/**
 *	第一次优化简化代码
 *  @author yuxin
 *
 */
@Aspect
@Component
public class LogAspect1 {
	@Autowired
	private LogService logService;

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
	/**
	 *	监听保存错误日志
	 *
	 */
	@AfterThrowing(value="logMethod()",throwing="e")
	public void afterThrowAUD(JoinPoint point,Exception e){
		//获取classname
		String classname=point.getSignature().getDeclaringTypeName();
		//获取调用的方法名
		String methodname=point.getSignature().getName();
		//找出对应的controller
		for (LogIdentificationEnum typeEnum : LogIdentificationEnum.values()) {
			if (classname.equals(typeEnum.getController())) {
				//找出对应的方法
				for (MethodEnum methodEnum : MethodEnum.values()) {
					if (methodname.equals(methodEnum.getMethod())) {
						//保存操作日志
						handleAUD( point, e,typeEnum.getName()+methodEnum.getName()+MethodEnum.ERROR.getName(),MethodEnum.ERROR.geMethodType());
					}
				}
			}
		}
	}
	/**增删改监听处理方法
	 * @param point
	 * @param e
	 */
    private void handleAUD(JoinPoint point,Exception e,String title,String type){
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
			String uri=LogUtil.getIpAddr(request);
			String requesturi=request.getRequestURI();
			String method=point.getSignature().getDeclaringTypeName() + "." + point.getSignature().getName();
			String params=Arrays.toString(point.getArgs());
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
