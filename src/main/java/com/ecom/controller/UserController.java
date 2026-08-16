package com.ecom.controller;

import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.List;

import com.ecom.model.*;
import com.ecom.service.CartService;
import com.ecom.service.OrderService;
import com.ecom.util.CommonUtil;
import com.ecom.util.OrderStatus;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import com.ecom.service.CategeoryService;
import com.ecom.service.UserService;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private OrderService orderService;

	@Autowired
	private UserService userService;
	
	@Autowired
	private CategeoryService categoryService;

	@Autowired
	private CartService cartService;

	@Autowired
	private CommonUtil commonUtil;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@GetMapping("/")
	public String home() {
		
		return "user/home";
	}
	
	@ModelAttribute
	public void getUserDetails(Principal p, Model m) {
		if(p != null) {
			String email = p.getName();
			UserDtls userDtls = userService.getUserByEmail(email);
			m.addAttribute("user", userDtls);
			Integer countCart = cartService.getCountCart(userDtls.getId());
			m.addAttribute("countCart", countCart);
		}
		List<Category> allActiveCategory = categoryService.getAllActiveCategory();
		m.addAttribute("categorys", allActiveCategory);
	}

	@GetMapping("/addCart")
	public String addToCart(@RequestParam Integer pid, @RequestParam Integer uid, HttpSession session) {
		Cart saveCart =  cartService.seveCart(pid, uid);

		if(ObjectUtils.isEmpty(saveCart)) {
			session.setAttribute("errorMsg", "Product not added to cart");
		} else {
			session.setAttribute("succMsg", "Product added to cart successfully");
		}

		return "redirect:/product/" + pid;
	}

	@GetMapping("/cart")
	public String loadCartPage(Principal p, Model m) {
		UserDtls user = getLoggedInUserDetails(p);
		List<Cart> carts = cartService.getCartsByUser(user.getId());
		m.addAttribute("carts", carts);

		if(carts.size() > 0){
			Double totalOrderPrice = carts.get(carts.size()-1).getTotalOrderPrice();
			m.addAttribute("totalOrderPrice", totalOrderPrice);
		}

		return "user/cart";
	}

	@GetMapping("/cartQuantityUpdate")
	public String updateCartQuantity(@RequestParam String sy, @RequestParam Integer cid) {
		cartService.updateQuantity(sy, cid);

		return "redirect:/user/cart";
	}

	private UserDtls getLoggedInUserDetails(Principal p) {
		String email = p.getName();
		UserDtls userDtls = userService.getUserByEmail(email);
		return userDtls;
	}

	@GetMapping("/orders")
	public String orderPage(Principal p, Model m){
		UserDtls user = getLoggedInUserDetails(p);
		List<Cart> carts = cartService.getCartsByUser(user.getId());
		m.addAttribute("carts", carts);

		if(carts.size() > 0){
			Double orderPrice = carts.get(carts.size()-1).getTotalOrderPrice();
			Double totalOrderPrice = carts.get(carts.size()-1).getTotalOrderPrice()+250+100;
			m.addAttribute("orderPrice", orderPrice);
			m.addAttribute("totalOrderPrice", totalOrderPrice);
		}

		return "/user/order";
	}

	@PostMapping("/save-order")
	public String saveOrder(@ModelAttribute OrderRequest request, Principal p) {
		//System.out.println(request.toString());
		UserDtls user = getLoggedInUserDetails(p);
		orderService.saveOrder(user.getId(), request);
		return "redirect:/user/success";
	}

	@GetMapping("/success")
	public String loadSuccess(){

		return "/user/success";
	}

	@GetMapping("/user-orders")
	public String myOrder(Model m, Principal p){
		UserDtls loggingUser =  getLoggedInUserDetails(p);
		List<ProductOrder> orders =  orderService.getOrders(loggingUser.getId());
		m.addAttribute("orders", orders);

		return "/user/my_orders";
	}

	@GetMapping("/update-status")
	public String UpdateOrderStatus(@RequestParam Integer id, @RequestParam Integer st, HttpSession session){
		OrderStatus[] value = OrderStatus.values();
		String status = null;

		for(OrderStatus orderSt : value){
			if(orderSt.getId().equals(st)){
				status = orderSt.getName();
			}
		}

		ProductOrder updateOrder = orderService.updateOrderStatus(id, status);
        try {
            commonUtil.sendMailForProductOrder(updateOrder, status);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(!ObjectUtils.isEmpty(updateOrder)) {
			session.setAttribute("succMsg", "Order updated successfully");
		}else {
			session.setAttribute("errorMsg", "Order update failed");
		}

		return "redirect:/user/user-orders";
	}

    @GetMapping("/profile")
	public String profile(){
		return "/user/profile";
	}

	@PostMapping("/update-profile")
	public String updateProfile(@ModelAttribute UserDtls user, @RequestParam MultipartFile img, HttpSession  session){
		UserDtls updateUserProfile = userService.updateUserProfile(user, img);

		if(ObjectUtils.isEmpty(updateUserProfile)) {
			session.setAttribute("errorMsg", "Profile not updated");
		}else {
			session.setAttribute("succMsg", "Profile updated successfully");
		}

		return "redirect:/user/profile";
	}

	@PostMapping("/change-password")
	public String changePassword(@RequestParam String newPassword, @RequestParam String currentPassword,  Principal p,HttpSession  session){
		UserDtls loggedInUserDetails = getLoggedInUserDetails(p);

		boolean matches = passwordEncoder.matches(currentPassword, loggedInUserDetails.getPassword());

		if(matches) {
			String encodePassword = passwordEncoder.encode(newPassword);
			loggedInUserDetails.setPassword(encodePassword);
			UserDtls userDtls = userService.updateUser(loggedInUserDetails);
			if (ObjectUtils.isEmpty(userDtls)){
				session.setAttribute("errorMsg", "Password not updated Error in server");
			}else{
				session.setAttribute("succMsg", "Password updated successfully");
			}
		}else {
			session.setAttribute("errorMsg", "Current Password incorrect");
		}

		return "redirect:/user/profile";
	}

}
