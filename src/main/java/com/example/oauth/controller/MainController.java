package com.example.oauth.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import com.example.oauth.dao.AppUserDAO;
import com.example.oauth.entity.AppRole;
import com.example.oauth.entity.AppUser;
import com.example.oauth.form.AppUserForm;
import com.example.oauth.utils.SecurityUtil;
import com.example.oauth.utils.WebUtils;
import com.example.oauth.validator.AppUserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.connect.web.ProviderSignInUtils;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Transactional
public class MainController {

    @Autowired
    private AppUserDAO appUserDAO;

    @Autowired
    private ConnectionFactoryLocator connectionFactoryLocator;

    @Autowired
    private UsersConnectionRepository connectionRepository;

    @Autowired
    private AppUserValidator appUserValidator;

    @InitBinder
    protected void initBinder(WebDataBinder dataBinder) {
        Object target = dataBinder.getTarget();
        if (target == null) {
            return;
        }
        System.out.println("Target=" + target);

        if (target.getClass() == AppUserForm.class) {
            dataBinder.setValidator(appUserValidator);
        }
    }

    @GetMapping({"/", "/welcome"})
    public String welcomePage(Model model) {
        model.addAttribute("title", "Welcome");
        model.addAttribute("message", "This is welcome page!");
        return "welcomePage";
    }

    @GetMapping("/admin")
    public String adminPage(Model model, Principal principal) {

        String userName = principal.getName();

        System.out.println("User Name: " + userName);

        UserDetails loginedUser = (UserDetails) ((Authentication) principal).getPrincipal();

        String userInfo = WebUtils.toString(loginedUser);
        model.addAttribute("userInfo", userInfo);

        return "adminPage";
    }

    @GetMapping("/logoutSuccessful")
    public String logoutSuccessfulPage(Model model) {
        model.addAttribute("title", "Logout");
        return "logoutSuccessfulPage";
    }

    @GetMapping("/userInfo")
    public String userInfo(Model model, Principal principal) {
        String userName = principal.getName();
        System.out.println("User Name: " + userName);
        UserDetails loginedUser = (UserDetails) ((Authentication) principal).getPrincipal();
        String userInfo = WebUtils.toString(loginedUser);
        model.addAttribute("userInfo", userInfo);
        return "userInfoPage";
    }

    @GetMapping("/403")
    public String accessDenied(Model model, Principal principal) {
        if (principal != null) {
            UserDetails loginedUser = (UserDetails) ((Authentication) principal).getPrincipal();
            String userInfo = WebUtils.toString(loginedUser);
            model.addAttribute("userInfo", userInfo);
            String message = "Hi " + principal.getName()
                    + "<br> You do not have permission to access this page!";
            model.addAttribute("message", message);
        }
        return "403Page";
    }

    @GetMapping("/login")
    public String login(Model model) {
        return "loginPage";
    }

    @GetMapping("/signin")
    public String signInPage(Model model) {
        return "redirect:/login";
    }

    @GetMapping("/signup")
    public String signupPage(WebRequest request, Model model) {

        ProviderSignInUtils providerSignInUtils
                = new ProviderSignInUtils(connectionFactoryLocator, connectionRepository);

        Connection<?> connection = providerSignInUtils.getConnectionFromSession(request);

        AppUserForm myForm;

        if (connection != null) {
            myForm = new AppUserForm(connection);
        } else {
            myForm = new AppUserForm();
        }
        model.addAttribute("myForm", myForm);
        return "signupPage";
    }

    @PostMapping("/signup")
    public String signupSave(WebRequest request,
                             Model model,
                             @ModelAttribute("myForm") @Validated AppUserForm appUserForm,
                             BindingResult result,
                             final RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "signupPage";
        }
        List<String> roleNames = new ArrayList<String>();
        roleNames.add(AppRole.ROLE_USER);
        AppUser registered;
        try {
            registered = appUserDAO.registerNewUserAccount(appUserForm, roleNames);
        } catch (Exception ex) {
            ex.printStackTrace();
            model.addAttribute("errorMessage", "Error " + ex.getMessage());
            return "signupPage";
        }
        if (appUserForm.getSignInProvider() != null) {
            ProviderSignInUtils providerSignInUtils //
                    = new ProviderSignInUtils(connectionFactoryLocator, connectionRepository);
            providerSignInUtils.doPostSignUp(registered.getUserName(), request);
        }
        SecurityUtil.logInUser(registered, roleNames);
        return "redirect:/userInfo";
    }

}