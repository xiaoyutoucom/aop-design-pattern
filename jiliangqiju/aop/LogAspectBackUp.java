package cn.jiliangqiju.aop;

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
 * 之前版本
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
	@AfterThrowing(value="rbacAUDMethod()",throwing="e")
	public void afterThrowRbacAUD(JoinPoint point,Exception e){
			String classname=point.getSignature().getDeclaringTypeName();
			if("cn.jiliangqiju.controller.rbac.UserController".equals(classname)){
				String methodname=point.getSignature().getName();
				if("add".equals(methodname)){
					handleAUD( point, null,"用户新增异常","0");
				}else if("update".equals(methodname)){
					handleAUD( point, null,"用户修改异常","0");
				}else if("delete".equals(methodname)){
					handleAUD( point, null,"用户删除异常","0");
				}else{
					handleAUD( point, null,"用户管理其他增删改异常","0");
				}

			}else if("cn.jiliangqiju.controller.rbac.RoleController".equals(classname)){
				String methodname=point.getSignature().getName();
				if("add".equals(methodname)){
					handleAUD( point, null,"角色新增异常","0");
				}else if("update".equals(methodname)){
					handleAUD( point, null,"角色修改异常","0");
				}else if("delete".equals(methodname)){
					handleAUD( point, null,"角色删除异常","0");
				}else{
					handleAUD( point, null,"角色管理其他增删改","0");
				}
			}else if("cn.jiliangqiju.controller.rbac.PermissionController".equals(classname)){
				String methodname=point.getSignature().getName();
				if("add".equals(methodname)){
					handleAUD( point, null,"权限新增异常","0");
				}else if("update".equals(methodname)){
					handleAUD( point, null,"权限修改异常","0");
				}else if("delete".equals(methodname)){
					handleAUD( point, null,"权限删除异常","0");
				}else{
					handleAUD( point, null,"权限管理其他增删改异常","0");
				}
			}
	}

	@Pointcut("execution(* cn.jiliangqiju.controller.rbac.UserController.saveBlackList(..))")
	private void rbacBlackMethod(){}
	@AfterReturning(value="rbacBlackMethod()")
	public void afterBlackAUD(JoinPoint point){
		try {
			handleAUD( point, null,"更改权限黑名单","2");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@AfterThrowing(value="rbacBlackMethod()",throwing="e")
	public void afterThrowBlackAUD(JoinPoint point,Exception e){
		try {
			handleAUD( point, e,"更改权限黑名单异常","0");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	@Pointcut("execution(* cn.jiliangqiju.controller.rbac.UserController.saveUserRole(..))")
	private void userRoleChangeMethod(){}
	@AfterReturning(value="userRoleChangeMethod()")
	public void afterUserRoleChange(JoinPoint point){
		try {
			handleAUD( point, null,"用户角色更改","2");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@AfterThrowing(value="userRoleChangeMethod()",throwing="e")
	public void afterThrowuserRoleChange(JoinPoint point,Exception e){
		try {
			handleAUD( point, e,"用户角色更改异常","0");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	@Pointcut("execution(* cn.jiliangqiju.controller.rbac.RoleController.saveRolePermission(..))")
	private void rolePermissionChangeMethod(){}
	@AfterReturning(value="rolePermissionChangeMethod()")
	public void afterRolePermissionChange(JoinPoint point){
		try {
			handleAUD( point, null,"角色权限更改","2");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@AfterThrowing(value="rolePermissionChangeMethod()",throwing="e")
	public void afterThrowuserRolePermissionChange(JoinPoint point,Exception e){
		try {
			handleAUD( point, e,"角色权限更改异常","0");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	//================================
	//=============器具删除==============
	@Pointcut("execution(* cn.jiliangqiju.controller.meterManage.MeterController.delete*(..))")
	private void meterDeleteMethod(){}
	@AfterReturning(value="meterDeleteMethod()")
	public void afterMeterDelete(JoinPoint point){
		try {
			handleAUD( point, null,"删除器具","3");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@AfterThrowing(value="meterDeleteMethod()",throwing="e")
	public void afterThrowMeterDelete(JoinPoint point,Exception e){
		try {
			handleAUD( point, e,"删除器具异常","0");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	//==========单位管理模块监听============
	@Pointcut("execution(* cn.jiliangqiju.controller.org.OrganizationController.add*(..)) || execution(* cn.jiliangqiju.controller.org.OrganizationController.update*(..)) || execution(* cn.jiliangqiju.controller.org.OrganizationController.delete*(..))"
			+ "|| execution(* cn.jiliangqiju.controller.org.DepartmentController.delete*(..))|| execution(* cn.jiliangqiju.controller.org.DepartmentController.delete*(..))|| execution(* cn.jiliangqiju.controller.org.DepartmentController.delete*(..))")
	private void orgdepAUDMethod(){}
	@AfterReturning(value="orgdepAUDMethod()")
	public void afterOrgDepAUD(JoinPoint point){
		String classname=point.getSignature().getDeclaringTypeName();
		if("cn.jiliangqiju.controller.org.OrganizationController".equals(classname)){
			String methodname=point.getSignature().getName();
			if("add".equals(methodname)){
				handleAUD( point, null,"单位新增","1");
			}else if("update".equals(methodname)){
				handleAUD( point, null,"单位修改","2");
			}else if("delete".equals(methodname)){
				handleAUD( point, null,"单位删除","3");
			}else{
				handleAUD( point, null,"单位管理其他增删改","4");
			}

		}else{
			String methodname=point.getSignature().getName();
			if("add".equals(methodname)){
				handleAUD( point, null,"部门新增","1");
			}else if("update".equals(methodname)){
				handleAUD( point, null,"部门修改","2");
			}else if("delete".equals(methodname)){
				handleAUD( point, null,"部门删除","3");
			}else{
				handleAUD( point, null,"部门管理其他增删改","4");
			}
		}
	}
	@AfterThrowing(value="orgdepAUDMethod()",throwing="e")
	public void afterThrowOrgDepAUD(JoinPoint point,Exception e){
		try{
			String classname=point.getSignature().getDeclaringTypeName();
			if("cn.jiliangqiju.controller.org.OrganizationController".equals(classname)){
				String methodname=point.getSignature().getName();
				if("add".equals(methodname)){
					handleAUD( point, null,"单位新增异常","0");
				}else if("update".equals(methodname)){
					handleAUD( point, null,"单位修改异常","0");
				}else if("delete".equals(methodname)){
					handleAUD( point, null,"单位删除异常","0");
				}else{
					handleAUD( point, null,"单位管理其他增删改","0");
				}

			}else{
				String methodname=point.getSignature().getName();
				if("add".equals(methodname)){
					handleAUD( point, null,"部门新增异常","0");
				}else if("update".equals(methodname)){
					handleAUD( point, null,"部门修改异常","0");
				}else if("delete".equals(methodname)){
					handleAUD( point, null,"部门删除异常","0");
				}else{
					handleAUD( point, null,"部门管理其他增删改异常","0");
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	//================================

	/**增删改监听处理方法
	 * @param point
	 * @param e
	 */
	public void handleAUD(JoinPoint point,Exception e,String title,String type){
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
