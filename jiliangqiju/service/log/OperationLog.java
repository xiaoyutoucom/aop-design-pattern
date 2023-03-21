package cn.jiliangqiju.service.log;

import cn.hutool.core.util.ObjectUtil;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
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
