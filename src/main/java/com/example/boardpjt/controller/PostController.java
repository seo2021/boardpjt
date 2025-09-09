package com.example.boardpjt.controller;

import com.example.boardpjt.model.dto.PostDTO;
import com.example.boardpjt.model.entity.Post;
import com.example.boardpjt.model.entity.UserAccount;
import com.example.boardpjt.service.PostService;
import com.example.boardpjt.service.UserAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Controller // 스캔
@RequiredArgsConstructor // 의존성
@RequestMapping("/posts")
public class PostController {
    private final PostService postService;

    // 게시물 목록
    @GetMapping
    public String list(Model model,
//                       @RequestParam(defaultValue = "1", required = false) int page) {
                       @RequestParam(defaultValue = "1") int page,
                       @RequestParam(required = false) String keyword) {
        if (!StringUtils.hasText(keyword)) {
            keyword = ""; // 명백히 빈 텍스트 (null 이런거 처리)
        }
        // 유저는 페이지가 1씩 시작하는게 자연스러워요
        Page<Post> postPage = postService.findWithPagingAndSearch(keyword, page - 1);
        // 현재 페이지
        model.addAttribute("currentPage", page);
        // 전체 페이지
        model.addAttribute("totalPages", postPage.getTotalPages());
        // 전달할 게시물 데이터
        model.addAttribute("posts",
//                postService.findAll()
                // Page<Post>
                postPage.getContent() // list화
                        .stream().map(p -> new PostDTO.Response(
                                p.getId(),                          // 게시물 ID
                                p.getTitle(),                       // 제목
                                p.getContent(),                     // 내용
                                p.getAuthor().getUsername(),        // 작성자명
                                p.getCreatedAt().toString()         // 작성일시 (문자열 변환)
                        ))
        );

        return "post/list"; // templates/post/list.html 렌더링
    }

    private final UserAccountService userAccountService;

    // 개별 게시물
    @GetMapping("/{id}") // GET /posts/123 형태의 요청 처리
    public String detail(@PathVariable Long id, Model model,
                         Authentication authentication) {
        // @PathVariable: URL 경로의 {id} 부분을 메서드 매개변수로 바인딩
        UserAccount userAccount = userAccountService.findByUsername(authentication.getName());
        Post post = postService.findById(id);
        boolean followCheck = post.getAuthor().getFollowers().contains(userAccount);

        model.addAttribute("followCheck", followCheck);

        // 각각 개별이니까... 1개.
        model.addAttribute("post",post);
        // 구현하는 방법은 여러가지 -> 데이터를 불러오는 건 상관X.
        // '내 게시물'임을 어떻게 보여줄 것이냐
        // 1. controller에서 처리를 해서 authentication 등 비교 -> isMyPost...
        // 2. #authencation.name -> entity, dto -> username.
        return "post/detail"; // templates/post/list.html
    }
    // 게시물 작성
    @GetMapping("/new")
    public String createForm(
            Model model, Authentication authentication) {
        PostDTO.Request dto = new PostDTO.Request();
        dto.setUsername(authentication.getName());
        model.addAttribute("post", dto);
        return "post/form"; // templates/post/list.html
    }
    // POST 처리...
    @PostMapping("/new")
    public String create(@ModelAttribute PostDTO.Request dto, Authentication authentication) {
        // dto? -> username
        // 불일치할 때 에러를?
        dto.setUsername(authentication.getName());
        postService.createPost(dto);
        return "redirect:/posts";
    }

    // 삭제
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Authentication authentication) {
        // 나 자신만 삭제가 가능
        try {
            // 1번 : 삭제하려고 하는 사람과 주인이 다를 때
            Post post = postService.findById(id);
            String postUsername = post.getAuthor().getUsername();
            if (!postUsername.equals(authentication.getName())) {
                // 지금 자격증명의 유저(이름)와 게시물의 유저가 다르다.
                throw new SecurityException("삭제 권한 없음");
            }
            // 2번 : 없는 걸 삭제하려고 할 때 (2번은 Service에서 throw를 하게...)
            postService.deleteById(id);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return "redirect:/posts"; // 문제 발생 시 목록으로 보냄
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, Authentication authentication) {

        Post post = postService.findById(id);

        if (!post.getAuthor().getUsername().equals(authentication.getName())) {
            return "redirect:/posts/" + id; // 권한 없으면 세부 페이지로 이동
        }
        // form -> audit X
        model.addAttribute("post", post); // binding -> form
        return "post/edit"; // templates/post/edit.html
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id, @ModelAttribute PostDTO.Request dto, Authentication authentication) {
        dto.setUsername(authentication.getName()); // 인증 정보를 바탕으로 편집자 정보를 넣고
        try {
            postService.updatePost(id, dto); // service를 사용해서 수정 저장 처리
        } catch (Exception e) {
            return "redirect:/posts/" + id + "/edit";
        }
        return "redirect:/posts";
    }
}