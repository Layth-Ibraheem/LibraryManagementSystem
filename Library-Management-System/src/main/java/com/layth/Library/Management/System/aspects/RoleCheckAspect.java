package com.layth.Library.Management.System.aspects;

import com.layth.Library.Management.System.entities.User;
import com.layth.Library.Management.System.utils.annotations.RequireRole;
import com.layth.Library.Management.System.utils.jwt.CurrentUser;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RoleCheckAspect {
    @Around("@annotation(requireRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint, RequireRole requireRole) throws Throwable{
        CurrentUser currentUser = (CurrentUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = new User(currentUser.getId(),currentUser.getUserName(),"",currentUser.getRoles());
        if(user.hasAccessTo(requireRole.role())){
            return joinPoint.proceed();
        }
        // If user does not have the required role, throw an exception or return a forbidden response
        throw new AccessDeniedException("You don't have the required role to perform this action.");
    }
}
