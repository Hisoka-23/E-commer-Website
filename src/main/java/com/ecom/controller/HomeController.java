package com.ecom.controller;

import com.ecom.model.Category;
import com.ecom.model.Product;
import com.ecom.model.UserDtls;
import com.ecom.repository.ProductRepository;
import com.ecom.service.CartService;
import com.ecom.service.CategeoryService;
import com.ecom.service.ProductService;
import com.ecom.service.UserService;
import com.ecom.util.CommonUtil;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Controller
public class HomeController {

	@Autowired
	private CategeoryService categeoryService;

	@Autowired
	private ProductService productService;

	@Autowired
	private UserService userService;

	@Autowired
	private CommonUtil commonUtil;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private CartService cartService;
	
	@ModelAttribute
	public void getUserDetails(Principal p, Model m) {
		if(p != null) {
			String email = p.getName();
			UserDtls userDtls = userService.getUserByEmail(email);
			m.addAttribute("user", userDtls);
			Integer countCart = cartService.getCountCart(userDtls.getId());
			m.addAttribute("countCart", countCart);
		}
		List<Category> allActiveCategory = categeoryService.getAllActiveCategory();
		m.addAttribute("categorys", allActiveCategory);
	}

	@GetMapping("/")
	public String index() {
		return "index";
	}
	
	@GetMapping("/signin")
	public String login() {
		return "login";
	}
	
	@GetMapping("/register")
	public String register() {
		return "register";
	}
	
	@GetMapping("/products")
	public String products(Model m, @RequestParam(value = "category", defaultValue = "") String category) {
		System.out.println("category: " + category);

		List<Category> categories = categeoryService.getAllActiveCategory();
		List<Product> products =  productService.getAllActiveProducts(category);

		m.addAttribute("categories", categories);
		m.addAttribute("products", products);
		m.addAttribute("paramValue", category);

		return "product";
	}
	
	@GetMapping("/product/{id}")
	public String product(@PathVariable int id, Model m) {
		Product productById = productService.getProductById(id);
		m.addAttribute("product", productById);

		return "view_product";
	}

	@PostMapping("/saveUser")
	public String saveUser(@ModelAttribute UserDtls user, @RequestParam("img") MultipartFile file, HttpSession session) throws IOException {
		String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
		user.setProfileImage(imageName);
		UserDtls saveUser = userService.saveUser(user);

		if(!ObjectUtils.isEmpty(saveUser)) {
			if(!file.isEmpty()){
				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator + file.getOriginalFilename());

				System.out.println(path);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}
			session.setAttribute("succMsg", "Saved Successfully");
		}else {
			session.setAttribute("errorMsg", "Something went wrong");
		}

		return "redirect:/register";
	}

	//forgot password logic
	@GetMapping("/forgot-password")
	public String showForgotPassword() {
		return "/forgot_password.html";
	}

	@PostMapping("/forgot-password")
	public String processForgotPassword(@RequestParam String email, HttpSession session, HttpServletRequest  request) throws MessagingException, UnsupportedEncodingException {
		UserDtls userByEmail = userService.getUserByEmail(email);

		if(ObjectUtils.isEmpty(userByEmail)) {
			session.setAttribute("errorMsg", "Invalid email");
		} else {
			String resetToken = UUID.randomUUID().toString();
			userService.updateUserResetToken(email, resetToken);

			//genrate URL =
			String url = CommonUtil.genrateUrl(request)+"/reset-password?token="+resetToken;

			Boolean sendMail = commonUtil.sendMail(url, email);

			if(sendMail) {
				session.setAttribute("succMsg", "Successfully send email");
			} else {
				session.setAttribute("errorMsg", "Failed to send email");
			}

		}

		return "redirect:/forgot-password";
	}

	//reset password logic
	@GetMapping("/reset-password")
	public String showResetPassword(@RequestParam String token, HttpSession session, Model m) {
		UserDtls userByToken = userService.getUserByToken(token);

		if(ObjectUtils.isEmpty(userByToken)) {
			m.addAttribute("msg", "Your is invalid or expired");
			return "message";
		}

		m.addAttribute("token", token);
		return "reset_password";
	}

	@PostMapping("/reset-password")
	public String resetPassword(@RequestParam String token,@RequestParam String password ,HttpSession session, Model m) {
		UserDtls userByToken = userService.getUserByToken(token);

		if(ObjectUtils.isEmpty(userByToken)) {
			m.addAttribute("errorMsg", "Your is invalid or expired");
			return "message";
		}else {
			userByToken.setPassword(passwordEncoder.encode(password));
			userByToken.setResetToken(null);
			userService.updateUser(userByToken);
			//session.setAttribute("succMsg", "Successfully reset Password");
			m.addAttribute("msg", "Successfully reset Password");
			return "message";
		}

	}
	
}
