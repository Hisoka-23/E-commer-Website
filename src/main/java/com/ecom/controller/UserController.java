package com.ecom.controller;

import java.security.Principal;
import java.util.List;

import com.ecom.model.Cart;
import com.ecom.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ecom.model.Category;
import com.ecom.model.UserDtls;
import com.ecom.service.CategeoryService;
import com.ecom.service.UserService;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private CategeoryService categoryService;

	@Autowired
	private CartService cartService;

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
			m.addAttribute("totalOrderPrice", carts.get(carts.size()-1).getTotalOrderPrice());
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
	public String orderPage(){
		return "/user/order";
	}

}
