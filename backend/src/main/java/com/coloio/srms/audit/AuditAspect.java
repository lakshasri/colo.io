package com.coloio.srms.audit;

import com.coloio.srms.entity.UserEntity;
import com.coloio.srms.repository.UserRepository;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditAspect {

    private final AuditService auditService;
    private final UserRepository userRepository;

    public AuditAspect(AuditService auditService, UserRepository userRepository) {
        this.auditService = auditService;
        this.userRepository = userRepository;
    }

    @AfterReturning("execution(* com.coloio.srms.service.UserService.createUser(..))")
    public void afterCreateUser(JoinPoint jp) {
        log("CREATE_USER", "USER", null, "User created via UserService");
    }

    @AfterReturning("execution(* com.coloio.srms.service.UserService.deactivateUser(..))")
    public void afterDeactivateUser(JoinPoint jp) {
        Object[] args = jp.getArgs();
        Long targetId = args.length > 0 ? (Long) args[0] : null;
        log("DEACTIVATE_USER", "USER", targetId, "User deactivated");
    }

    @AfterReturning("execution(* com.coloio.srms.service.UserService.updateUser(..))")
    public void afterUpdateUser(JoinPoint jp) {
        Object[] args = jp.getArgs();
        Long targetId = args.length > 0 ? (Long) args[0] : null;
        log("UPDATE_USER", "USER", targetId, "User updated");
    }

    private void log(String action, String entityType, Long entityId, String details) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return;

        String username = auth.getName();
        userRepository.findByUsername(username).ifPresent(user ->
                auditService.log(user.getUserId(), action, entityType, entityId, details)
        );
    }
}
