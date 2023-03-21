package cn.jiliangqiju.aop;

import cn.hutool.core.util.ObjectUtil;
import cn.jiliangqiju.constant.ControllerTypeEnum;
import cn.jiliangqiju.constant.LogIdentificationEnum;
import cn.jiliangqiju.constant.MethodEnum;
import cn.jiliangqiju.dto.LogEntity;
import cn.jiliangqiju.entity.bussi.LogWithBLOBs;
import cn.jiliangqiju.entity.rbac.User;
import cn.jiliangqiju.repository.mapper.verification.EcqsLogMapper;
import cn.jiliangqiju.repository.mapper.verification.JDelegateMasterMapper;
import cn.jiliangqiju.service.bussi.LogService;
import cn.jiliangqiju.util.JacksonUtil;
import cn.jiliangqiju.util.LogUtil;
import cn.jiliangqiju.util.UuidUtil;
import lombok.SneakyThrows;
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
import java.util.HashMap;
import java.util.Map;

/**
 *	合并代码加入ECQS和包间道相关日志
 *  @author yuxin
 *
 */
@Aspect
@Component
public class LogAspect2 {
	@Autowired
	private LogService logService;
	@Autowired
	private JDelegateMasterMapper jdelegateMasterMapper;
	@Autowired
	private EcqsLogMapper ecqsLogMapper;
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
	/**增删改监听处理方法
	 * @param point JoinPoint
	 * @param e 错误
	 * @param title 标题
	 * @param type 类型
	 */
	public void handleAud(JoinPoint point,Exception e,String title,String type){
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
