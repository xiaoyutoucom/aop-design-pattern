package cn.jiliangqiju.aop;

import cn.jiliangqiju.constant.LogIdentificationEnum;
import cn.jiliangqiju.service.log.LogFactory;
import lombok.SneakyThrows;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 *	保存所有日志信息（操作日志和api接口调用日志）
 *  @author yuxin
 *
 */
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
