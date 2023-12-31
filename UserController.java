package com.smart.controller;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	
	//method for adding common data to response
	@ModelAttribute
	public void addCommonData(Model m, Principal principal) {
		
        String userName=principal.getName();
		
        //get the user using username(Email)
		User user=userRepository.getUserByUserName(userName);
		System.out.println("USER" + user);
		m.addAttribute("user",user);
	}
	
	//dashborad home
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal) {
		
		model.addAttribute("title","User Dashboard");
		return "normal/user_dashboard";
	} 
	
	//open add form handler
	@GetMapping("/add-contact")
	public String openAddFormContactForm(Model model) {
		model.addAttribute("title","Add Contact");
		model.addAttribute("contact",new Contact());
		return "normal/add_contact_form";
	}
	
	//processing add contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,Principal principal,HttpSession session) {
		
		try {
			String name=principal.getName();
			User user=this.userRepository.getUserByUserName(name);
			
			//processing and uploading file........
			if(file.isEmpty()) {
				//if file is empty try our message
				contact.setImage("contact.png");
			}
			else {
				//upload the file to the folder and upload the name to contact
				contact.setImage(file.getOriginalFilename());
				
				File saveFile=new ClassPathResource("static/img").getFile();
				
				Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING );
				System.out.println("Successfully uploaded");
				
			}
			contact.setUser(user);
			user.getContacts().add(contact);
			this.userRepository.save(user);
			
			System.out.println("DATA "+contact);
			System.out.println("Added to data base ");
			
			//success message
			session.setAttribute("message",new Message("Your contact is added!! Add more..","success"));
			
			
		}catch(Exception e) {
			System.out.println("ERROR"+e.getMessage());
			e.printStackTrace();
			
			//error message
			session.setAttribute("message",new Message("Something went wrong!! Try again..","danger"));
            
			
		}
		return "normal/add_contact_form";
	}
	
	//show contact_handler
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model m, Principal principal) {
		
		m.addAttribute("title","Show User Contacts");
		
        String userName=principal.getName();
		User user=userRepository.getUserByUserName(userName);
		
		Pageable pageable=PageRequest.of(page, 3);
		
		Page<Contact> contacts=this.contactRepository.findContactsByUser(user.getId(),pageable);
		
		m.addAttribute("contacts",contacts);
		m.addAttribute("currentPage",page);
		m.addAttribute("totalPages",contacts.getTotalPages());
		
		return "normal/show_contacts";
	}
	
	
	//showing particular contact details
	@RequestMapping("/{cId}/contact/")
	public String showContactDetail(@PathVariable("cId") Integer cId, Model model,Principal principal) {
		System.out.println("CID"+cId);
		
		Optional<Contact> contactOptional=this.contactRepository.findById(cId);
		Contact contact=contactOptional.get();
		
		String userName=principal.getName();
		User user=this.userRepository.getUserByUserName(userName);
		
		if(user.getId()==contact.getUser().getId())
		{
		model.addAttribute("contact",contact);
		model.addAttribute("title",contact.getName());

		}
		
		return "normal/contact_detail";
	}
	
	//delete contact handler
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cId,Model model, HttpSession session,Principal principal) {
		
		Optional<Contact> contactOptional=this.contactRepository.findById(cId);
		Contact contact=contactOptional.get();
		
		User user = this.userRepository.getUserByUserName(principal.getName());
		user.getContacts().remove(contact);
		this.userRepository.save(user);
		
		session.setAttribute("message", new Message("Contact deleted successfully.....","success"));
		
		return "redirect:/user/show-contacts/0";
	}

	
	//open update form handler
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid,Model m) {
		
		Contact contact=this.contactRepository.findById(cid).get();
		m.addAttribute("contact",contact);
		
		m.addAttribute("title","Update Contact");
		return "normal/update_form";
		
	}
	
	//upadte contact handler
	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,Model model, HttpSession session,Principal principal) {
		
		try {
			//old contact detail
			Contact oldcontactDetail=this.contactRepository.findById(contact.getcId()).get();
			//image..
			if(!file.isEmpty()) {
				//file rewrite
				//deleting old photo
				  File deleteFile=new ClassPathResource("static/img").getFile();
				   
				  File file1=new File(deleteFile, oldcontactDetail.getImage());
				  file1.delete();
				
				
				//update new photo
				File saveFile=new ClassPathResource("static/img").getFile();
				
				Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING );
				contact.setImage(file.getOriginalFilename());
				
			}else
			{
				contact.setImage(oldcontactDetail.getImage());
			}
			
			User user=this.userRepository.getUserByUserName(principal.getName());
			
			contact.setUser(user);
			
			this.contactRepository.save(contact);
			
			session.setAttribute("message", new Message("Your contact is updated.....","success"));
			
		}catch(Exception e) {
		   e.printStackTrace();
		}
		
		System.out.println("CONTACT NAME"+contact.getName());
		System.out.println("CONTACT Id"+contact.getcId());
		
		
		return "redirect:/user/"+contact.getcId()+"/contact/";
	}
	
	//your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model m, Principal principal) {
		
        String userName=principal.getName();
		
        //get the user using username(Email)
		User user=userRepository.getUserByUserName(userName);
		user.setImageUrl("Sunny.jpg");
		m.addAttribute("title","Profile Page");
		
		return "normal/profile";
	}
	
}
