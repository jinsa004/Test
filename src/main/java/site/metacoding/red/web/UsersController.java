package site.metacoding.red.web;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import lombok.RequiredArgsConstructor;
import site.metacoding.red.domain.boards.Boards;
import site.metacoding.red.domain.users.Users;
import site.metacoding.red.domain.users.UsersDao;
import site.metacoding.red.web.dto.request.users.JoinDto;
import site.metacoding.red.web.dto.request.users.LoginDto;
import site.metacoding.red.web.dto.request.users.UpdateDto;

@RequiredArgsConstructor
@Controller
public class UsersController {

	private final HttpSession session; // 스프링이 서버시작시에 IoC 컨테이너에 보관함.
	private final UsersDao usersDao;

	@GetMapping("/logout")
	public String logout() {
		session.invalidate();
		return "redirect:/";
	}

	@PostMapping("/login") // 로그인만 예외로 select인데 post로 함.
	public String login(LoginDto loginDto) {
		Users usersPS = usersDao.login(loginDto);

		if (usersPS != null) { // 인증됨.
			session.setAttribute("principal", usersPS);
			return "redirect:/";
		} else { // 인증안됨.

			return "redirect:/loginForm";
		}

	}

	@PostMapping("/join")
	public String join(JoinDto joinDto) {
		System.out.println("/join 호출시 ================");
		System.out.println("username : " + joinDto.getUsername());
		System.out.println("password : " + joinDto.getPassword());
		System.out.println("email : " + joinDto.getEmail());
		System.out.println("================");
		usersDao.insert(joinDto);
		return "redirect:/loginForm";
	}

	
	@PostMapping("/users/{id}/update")
	public String update(@PathVariable Integer id, UpdateDto updateDto) {
		Users usersPS = usersDao.findById(id);
		Users principal = (Users) session.getAttribute("principal");

		if (usersPS == null) {
			return "errors/badPage";
		}
		// 인증 체크
		if (principal == null) {
			return "redirect:/loginForm";
		}
		// 권한 체크 ( 세션 principal.getId() 와 boardsPS의 userId를 비교)
		if (principal.getId() != usersPS.getId()) {
			return "errors/badPage";
		}
		
		usersPS.회원정보수정(updateDto);
		usersDao.update(usersPS);
		
		return "redirect:/";
	}
	
	@GetMapping("/users/{id}/updateForm")
	public String updateForm(@PathVariable Integer id, Model model) {
		Users usersPS = usersDao.findById(id);
		Users principal = (Users) session.getAttribute("principal");

		// 비정상 요청 체크
		if (usersPS == null) {
			return "errors/badPage";
		}
		// 인증 체크
		if (principal == null) {
			return "redirect:/loginForm";
		}
		// 권한 체크 ( 세션 principal.getId() 와 boardsPS의 userId를 비교)
		if (principal.getId() != usersPS.getId()) {
			return "errors/badPage";
		}
		
		model.addAttribute("users", usersPS);
		
		return "users/updateForm";
	}
	
	@PostMapping("/users/{id}/delete")
	public String deleteBoards(@PathVariable Integer id) {
		Users principal = (Users) session.getAttribute("principal");
		Users usersPS = usersDao.findById(id);

		// 비정상 요청 체크
		if (usersPS == null) { // if는 비정상 로직을 타게 해서 걸러내는 필터 역할을 하는게 좋다.
			return "errors/badPage";
		}

		// 인증 체크
		if (principal == null) {
			return "redirect:/loginForm";
		}

		// 권한 체크 ( 세션 principal.getId() 와 boardsPS의 userId를 비교)
		if (principal.getId() != usersPS.getId()) {
			return "redirect:/boards/" + id;
		}

		usersDao.delete(id); // 핵심 로직
		session.invalidate();
		return "redirect:/";
	}
	
	@GetMapping("/loginForm")
	public String loginForm() {
		return "users/loginForm";
	}

	@GetMapping("/joinForm")
	public String joinForm() {
		return "users/joinForm";
	}
}
