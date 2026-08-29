package com.ecom.controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;

import com.ecom.model.Product;
import com.ecom.model.ProductOrder;
import com.ecom.model.UserDtls;
import com.ecom.service.*;

import com.ecom.util.CommonUtil;
import com.ecom.util.OrderStatus;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Category;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {
	
	@Autowired
	private CategeoryService categoryService;

	@Autowired
	private ProductService productService;

	@Autowired
	private UserService userService;

	@Autowired
	private CartService cartService;

	@Autowired
	private OrderService orderService;;

	@Autowired
	private CommonUtil commonUtil;
	
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
	
	@GetMapping("/")
	public String index() {
		return "admin/index";
	}
	
	@GetMapping("/loadAddProduct")
	public String loadAddProduct(Model m) {
		List<Category> categoryList = categoryService.getAllCategory();
		m.addAttribute("categories", categoryList);
		return "admin/add_product";
	}
	
	@GetMapping("/category")
	public String category(Model m) {
		m.addAttribute("categorys", categoryService.getAllCategory());
		return "admin/category";
	}
	
	@PostMapping("/saveCategory")
	public String saveCategory(@ModelAttribute Category category,@RequestParam("file") MultipartFile file, HttpSession session) throws IOException {

		String imageName = file != null ? file.getOriginalFilename() : "default.jpg";
		category.setImageName(imageName);

		Boolean existCategory = categoryService.existCategory(category.getName());

		if(existCategory) {
			session.setAttribute("errorMsg", "Category Name already exists");
		} else {
			Category saveCategory = categoryService.saveCategory(category);
			if(ObjectUtils.isEmpty(saveCategory)) {
				session.setAttribute("erroryMsg", "Not saved ! internal server error");
			} else {
				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+"category_img"+File.separator+file.getOriginalFilename());
				System.out.println(path);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				session.setAttribute("succMsg", "Saved successfully");
			}
		}
		categoryService.saveCategory(category);
		return "redirect:/admin/category";
	}

	@GetMapping("/deleteCategory/{id}")
	public String deleteCategory(@PathVariable int id, HttpSession session) {
		Boolean deleteCategory = categoryService.deleteCategory(id);

		if(deleteCategory) {
			session.setAttribute("succMsg", "Category deleted successfully");
		} else {
		 session.setAttribute("erroryMsg", "Failed to delete category");
		}

		return "redirect:/admin/category";
	}

	@GetMapping("/loadEditCategory/{id}")
	public String loadEditCategory(@PathVariable int id, Model m){
		m.addAttribute("category", categoryService.getCategoryById(id));
		return "admin/edit_category";
	}

	@PostMapping("/updateCategory")
	public String updateCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file, HttpSession session) throws IOException {
		Category oldCategory = categoryService.getCategoryById(category.getId());
		String imageName = file.isEmpty() ? oldCategory.getImageName() : file.getOriginalFilename();

		if(!ObjectUtils.isEmpty(category)) {
			oldCategory.setName(category.getName());
			oldCategory.setIsActive(category.getIsActive());
			oldCategory.setImageName(imageName);
		}

		Category updateCategory = categoryService.saveCategory(category);

		if(!ObjectUtils.isEmpty(updateCategory)) {
			if(!file.isEmpty()) {
				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "category_img" + File.separator + file.getOriginalFilename());

				System.out.println(path);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}

			session.setAttribute("succMsg", "Category updated successfully");
		}else{
			session.setAttribute("erroryMsg", "Failed to save category");
		}

		return "redirect:/admin/loadEditCategory/" + category.getId();
	}

	@PostMapping("/saveProduct")
	public String saveProduct(@ModelAttribute  Product product, @RequestParam("file") MultipartFile image,  HttpSession session) throws IOException {
		String imageName = image.isEmpty() ? "default.jpg" :  image.getOriginalFilename();

		product.setImage(imageName);
		product.setDiscount(0);
		product.setDiscountPrice(product.getPrice());

		Product saveProduct = productService.saveProduct(product);

		if(!ObjectUtils.isEmpty(saveProduct)){
			if(!image.isEmpty()) {
				File saveFile = new ClassPathResource("/static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "product_img" + File.separator + image.getOriginalFilename());

				System.out.println(path);
				Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}
			session.setAttribute("succMsg", "Product saved successfully");
		}else {
			session.setAttribute("errorMsg", "Something wrong on server");
		}

		return "redirect:/admin/loadAddProduct";
	}

	@GetMapping("/products")
	public String loadViewProduct(Model m, @RequestParam(defaultValue = "") String ch){
		List<Product> product = null;
		if(ch != null && ch.length() > 0) {
			product =  productService.searchProduct(ch);
		} else{
			product = productService.getAllProducts();
		}

		m.addAttribute("products", product);
		return "admin/products";
	}

	@GetMapping("/deleteProduct/{id}")
	public String deleteProduct(@PathVariable int id, HttpSession session) {
		Boolean deleteProduct = productService.deleteProduct(id);

		if(deleteProduct) {
			session.setAttribute("succMsg", "Product deleted successfully");
		} else{
			session.setAttribute("errorMsg", "Failed to delete product");
		}

		return "redirect:/admin/products";
	}

	@GetMapping("/editProduct/{id}")
	public String editProduct(@PathVariable int id, Model m){
		m.addAttribute("product", productService.getProductById(id));
		m.addAttribute("categories", categoryService.getAllCategory());
		return "admin/edit_product";
	}

	@PostMapping("/updateProduct")
	public String updateProduct(@ModelAttribute Product product, @RequestParam("file")  MultipartFile image,HttpSession session ,Model m){
		if(product.getDiscount() < 0 || product.getDiscount() > 100) {
			session.setAttribute("errorMsg", "Invalid discount");
		} else {
			Product updateProduct = productService.updateProduct(product, image);

			if(!ObjectUtils.isEmpty(updateProduct)) {
				session.setAttribute("succMsg", "Product updated successfully");
			}else {
				session.setAttribute("errorMsg", "Failed to save product");
			}
		}

		return "redirect:/admin/editProduct/" + product.getId();
	}

	@GetMapping("/users")
	public String getAllUsers(Model m){
		List<UserDtls> users = userService.getUsers("Role_User");
		m.addAttribute("users", users);
		return "/admin/users";
	}

	@GetMapping("/updateStatus")
	public String updateUserAccountStatus(@RequestParam boolean status, @RequestParam Integer id, HttpSession session ){
		Boolean fale = userService.updateAccountStatus(id,  status);

		if(fale) {
			session.setAttribute("succMsg", "Account updated successfully");
		} else {
			session.setAttribute("errorMsg", "Failed to update account");
		}

		return  "redirect:/admin/users";
	}

	@GetMapping("/orders")
	public String getAllOrders(Model m){
		List<ProductOrder> allOrders = orderService.getAllOrders();
		m.addAttribute("orders", allOrders);
		m.addAttribute("srch", false);
		return "/admin/orders";
	}

	@PostMapping("/update-order-status")
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

        if(!ObjectUtils.isEmpty(updateOrder)){
			session.setAttribute("succMsg", "Order updated successfully");
		}else {
			session.setAttribute("errorMsg", "Order update failed");
		}

		return "redirect:/admin/orders";
	}

	@GetMapping("/search-order")
	public String searchProduct(@RequestParam String orderId, Model m, HttpSession session) {
		if(orderId!=null && orderId.length()>0) {
		ProductOrder order = orderService.getOrderById(orderId.trim());
		if(ObjectUtils.isEmpty(order)){
			session.setAttribute("errorMsg", "Incorrect order id");
			m.addAttribute("orderDtls", null);
		}else {
			m.addAttribute("orderDtls", order);
		}

		m.addAttribute("srch", true);
		}else {
			List<ProductOrder> allOrders = orderService.getAllOrders();
			m.addAttribute("orders", allOrders);
			m.addAttribute("srch", false);
		}
		return "/admin/orders";
	}

}
