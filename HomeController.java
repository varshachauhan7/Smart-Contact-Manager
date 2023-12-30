package com.smart.controller;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;



@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;

	@RequestMapping("/")
	private String home(Model model) {
		model.addAttribute("title","Home - Smart Contact Manager");
		return "home";
	}
	

	@RequestMapping("/about")
	private String about(Model model) {
		model.addAttribute("title","About - Smart Contact Manager");
		return "about";
	}
	
	@RequestMapping("/signup")
	private String signup(Model model) {
		model.addAttribute("title","Register - Smart Contact Manager");
		model.addAttribute("user",new User());
		return "signup";
	}
	 
	//handler for registering user
	@RequestMapping(value="/do_register", method = RequestMethod.POST)
	private String registerUser(@Valid @ModelAttribute("user") User user,BindingResult result1,@RequestParam(value="agreed",defaultValue = "false") boolean agreed, Model model,HttpSession session) {
	
		try {
			if(!agreed) {
				System.out.println("You have not agreed terms and conditions");
				throw new Exception("You have not agreed terms and conditions");
			}
			
			if(result1.hasErrors()) {
				System.out.println("ERROR" + result1.toString());
				model.addAttribute("user",user);
				return "signup";
			}
			
			user.setRole("ROLE_USER");
			user.setEnabled(true);
	//		user.setImageUrl("default.png);
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			
			System.out.println("Aggreement "+ agreed);
			System.out.println("USER"+user);
			
			User result = this.userRepository.save(user);
			
			model.addAttribute("user", new User());
			
			session.setAttribute("message", new Message("Successfully Registered!!","alert-success"));
			return "signup";
			
		}catch(Exception e) {
			e.printStackTrace();
			model.addAttribute("user",user);
			session.setAttribute("message", new Message("Something went wrong!!"+e.getMessage(),"alert-danger"));
			return "signup";
		}

		}
	//handler for custom login
	@GetMapping("/signin")
			public String customLogin(Model model) {
				
		model.addAttribute("title", "Login page");
				return "login";
		
	}
} 
