package com.ecom.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.ecom.model.UserDtls;
import com.ecom.repository.UserRepository;
import com.ecom.service.UserService;
import com.ecom.util.AppConstant;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthFailureHandlerImpl extends SimpleUrlAuthenticationFailureHandler {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private UserService userService;
	
	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		
		String email = request.getParameter("username");
		
		UserDtls userDtls =userRepository.findByEmail(email);

		if(userDtls!=null){
			if(userDtls.getIsEnable()) {
				if(userDtls.getIsAccountNonLocked()) {
					if(userDtls.getFailedAttempts() < AppConstant.ATTEMPT_TIME) {
						userService.increaseFailedAttempts(userDtls);
					}else {
						userService.userAccountLock(userDtls);
						exception = new LockedException("your account is Locked || failed attempt 3");
					}
				}else {

					if(userService.unlockAccountTimeExpire(userDtls)) {
						exception = new LockedException("your account is unlocked || try to login");
					} else {
						exception = new LockedException("your account is Locked || please try alter sometimes");
					}

				}
			}else {
				exception = new LockedException("your account is inactive");
			}
		} else{
			exception = new LockedException("Email &  password is invalid");
		}
		
		super.setDefaultFailureUrl("/signin?error");
		super.onAuthenticationFailure(request, response, exception);
	}
	
}
