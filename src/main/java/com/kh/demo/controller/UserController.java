package com.kh.demo.controller;

import com.kh.demo.domain.dto.AdminDTO;
import com.kh.demo.domain.dto.TrainerDTO;
import com.kh.demo.domain.dto.UserDTO;
import com.kh.demo.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;



@Controller
@RequestMapping("/user/*")
public class UserController {

    @Autowired
    @Qualifier("UserServiceImpl")
    private UserService service;

    @Autowired
    @Qualifier("TrainerServiceImpl")
    private TrainerService serviceTrainer;

    @Autowired
    @Qualifier("AdminServiceImpl")
    private AdminService serviceAdmin;

    @Autowired
    @Qualifier("UserMyPageServiceImpl")
    private UserMyPageService umpservice;

    @Autowired
    @Qualifier("TrainerMyPageServiceImpl")
    private TrainerMyPageService tmpservice;

    public UserController() {
    }


    @GetMapping("join")
    public void replace() {}

    @GetMapping("useAgree")
    public void showUseAgreePage(){}

    @GetMapping("policyAgree")
    public void showPolicyAgreePage() {}

    @PostMapping("join")
    @ResponseBody
    public String join(@RequestBody UserDTO user, RedirectAttributes ra) {
        if(service.join(user)) {
            ra.addAttribute("joinid",user.getUserId());
            System.out.println("Controller");
            return "true";
        }
        return "false";
    }
    @GetMapping("login")
    public void replaceLogin(){}

    @PostMapping("login")
    public String login(String userid, String userpw, HttpServletRequest req) {
        Object loginUser = service.login(userid, userpw);
        if (loginUser instanceof AdminDTO) {
            AdminDTO admin = serviceAdmin.getDetail(((AdminDTO) loginUser).getAdminId());
            req.getSession().setAttribute("loginUser",((AdminDTO) loginUser).getAdminId());
            req.getSession().setAttribute("admin",admin);
            return "redirect:/";
        }
        else if (loginUser instanceof TrainerDTO) {
            TrainerDTO trainer = serviceTrainer.getDetail(((TrainerDTO) loginUser).getTrainerId());
            req.getSession().setAttribute("loginUser",((TrainerDTO) loginUser).getTrainerId());
            req.getSession().setAttribute("trainer",trainer);
            req.getSession().setAttribute("profile", tmpservice.getProFileList(trainer.getTrainerId()));
            return "redirect:/";
        }
        else if (loginUser instanceof UserDTO) {
            UserDTO user = service.getDetail(((UserDTO) loginUser).getUserId());
            req.getSession().setAttribute("loginUser",((UserDTO) loginUser).getUserId());
            req.getSession().setAttribute("user",user);
            req.getSession().setAttribute("profile", umpservice.getProFileList(user.getUserId()));
            return "redirect:/";
        }
        else {
            return "redirect:/";
        }
    }

    @GetMapping("logout")
    public String logout(HttpServletRequest req) {
        req.getSession().invalidate();
        return "redirect:/";
    }

    @GetMapping("checkid")
    @ResponseBody
    public String checkId(String userid) {
        if(service.checkId(userid)) {
            return "O";
        }
        return "X";
    }

    @GetMapping("nickname")
    @ResponseBody
    public String checkNickname (String usernickname) {
        if(service.checkNickname(usernickname)) {
            return "X";
        } else {
            return "O";
        }
    }

    @Transactional
    @PostMapping("attend")
    @ResponseBody
    public ResponseEntity<Integer> attend(@RequestParam String userid) {
        try {
            // 출석 버튼을 클릭할 때마다 출석 및 포인트 증가
            service.updateUserAttendance(userid);

            // 출석 후 포인트 업데이트
            int filledGauges = service.getUserAttendance(userid);

            if (filledGauges >= 7) {
                service.updateUserPoint(userid);
                filledGauges = 0;  // 출석 포인트가 7 이상이면 초기화
                service.resetUserAttendance(userid);  // 출석 초기화
            }

            // 현재 포인트 가져오기
            Long userPoint = service.getUserPoint(userid);

            if (userPoint != null) {
                return ResponseEntity.ok(userPoint.intValue());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            // 예외 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

