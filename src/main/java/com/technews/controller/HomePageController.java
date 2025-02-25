package com.technews.controller;

import com.technews.model.Comment;
import com.technews.model.Post;
import com.technews.model.User;
import com.technews.repository.CommentRepository;
import com.technews.repository.PostRepository;
import com.technews.repository.UserRepository;
import com.technews.repository.VoteRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller  // Ensure this is recognized as a Spring MVC Controller
public class HomePageController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private CommentRepository commentRepository;

    @GetMapping("/login")
    public String login(Model model, HttpServletRequest request) {
        if (request.getSession(false) != null) {
            return "redirect:/";
        }
        model.addAttribute("user", new User());
        return "login";
    }

    @GetMapping("/users/logout")
    public String logout(HttpServletRequest request) {
        if (request.getSession(false) != null) {
            request.getSession().invalidate();
        }
        return "redirect:/login";
    }

    @GetMapping("/")
    public String homepageSetup(Model model, HttpServletRequest request) {
        User sessionUser = new User();

        if (request.getSession(false) != null) {
            sessionUser = (User) request.getSession().getAttribute("SESSION_USER");
            model.addAttribute("loggedIn", sessionUser.isLoggedIn());
        } else {
            model.addAttribute("loggedIn", false);
        }

        List<Post> postList = postRepository.findAll();
        for (Post p : postList) {
            p.setVoteCount(voteRepository.countVotesByPostId(p.getId()));
            User user = userRepository.findById(p.getUserId()).orElse(null);
            if (user != null) {
                p.setUserName(user.getUsername());
            }
        }

        model.addAttribute("postList", postList);
        model.addAttribute("point", "point");
        model.addAttribute("points", "points");

        return "homepage";
    }

    @GetMapping("/dashboard")
    public String dashboardPageSetup(Model model, HttpServletRequest request) throws Exception {
        if (request.getSession(false) != null) {
            setupDashboardPage(model, request);
            return "dashboard";
        } else {
            model.addAttribute("user", new User());
            return "login";
        }
    }

    @GetMapping("/dashboardEmptyTitleAndLink")
    public String dashboardEmptyTitleAndLinkHandler(Model model, HttpServletRequest request) throws Exception {
        setupDashboardPage(model, request);
        model.addAttribute("notice", "To create a post, the Title and Link must be populated!");
        return "dashboard";
    }

    @GetMapping("/singlePostEmptyComment/{id}")
    public String singlePostEmptyCommentHandler(@PathVariable int id, Model model, HttpServletRequest request) {
        setupSinglePostPage(id, model, request);
        model.addAttribute("notice", "To add a comment, you must enter text in the comment area!");
        return "single-post";
    }

    @GetMapping("/post/{id}")
    public String singlePostPageSetup(@PathVariable int id, Model model, HttpServletRequest request) {
        setupSinglePostPage(id, model, request);
        return "single-post";
    }

    @GetMapping("/editPostEmptyComment/{id}")
    public String editPostEmptyCommentHandler(@PathVariable int id, Model model, HttpServletRequest request) {
        if (request.getSession(false) != null) {
            setupEditPostPage(id, model, request);
            model.addAttribute("notice", "To add a comment, you must enter text in the comment area!");
            return "edit-post";
        } else {
            model.addAttribute("user", new User());
            return "login";
        }
    }

    @GetMapping("/dashboard/edit/{id}")
    public String editPostPageSetup(@PathVariable int id, Model model, HttpServletRequest request) {
        if (request.getSession(false) != null) {
            setupEditPostPage(id, model, request);
            return "edit-post";
        } else {
            model.addAttribute("user", new User());
            return "login";
        }
    }

    public Model setupDashboardPage(Model model, HttpServletRequest request) throws Exception {
        User sessionUser = (User) request.getSession().getAttribute("SESSION_USER");
        Integer userId = sessionUser.getId();

        List<Post> postList = postRepository.findAllPostsByUserId(userId);
        for (Post p : postList) {
            p.setVoteCount(voteRepository.countVotesByPostId(p.getId()));
            User user = userRepository.findById(p.getUserId()).orElse(null);
            if (user != null) {
                p.setUserName(user.getUsername());
            }
        }

        model.addAttribute("user", sessionUser);
        model.addAttribute("postList", postList);
        model.addAttribute("loggedIn", sessionUser.isLoggedIn());
        model.addAttribute("post", new Post());

        return model;
    }

    public Model setupSinglePostPage(int id, Model model, HttpServletRequest request) {
        if (request.getSession(false) != null) {
            User sessionUser = (User) request.getSession().getAttribute("SESSION_USER");
            model.addAttribute("sessionUser", sessionUser);
            model.addAttribute("loggedIn", sessionUser.isLoggedIn());
        }

        Optional<Post> optionalPost = postRepository.findById(id);
        if (optionalPost.isEmpty()) {
            return model;
        }

        Post post = optionalPost.get();
        post.setVoteCount(voteRepository.countVotesByPostId(post.getId()));

        Optional<User> optionalUser = userRepository.findById(post.getUserId());
        optionalUser.ifPresent(user -> post.setUserName(user.getUsername()));

        List<Comment> commentList = commentRepository.findAllCommentsByPostId(post.getId());

        model.addAttribute("post", post);
        model.addAttribute("commentList", commentList);
        model.addAttribute("comment", new Comment());

        return model;
    }

    public Model setupEditPostPage(int id, Model model, HttpServletRequest request) {
        if (request.getSession(false) != null) {
            User sessionUser = (User) request.getSession().getAttribute("SESSION_USER");

            Optional<Post> optionalPost = postRepository.findById(id);
            if (optionalPost.isEmpty()) {
                return model;
            }

            Post returnPost = optionalPost.get();
            Optional<User> optionalUser = userRepository.findById(returnPost.getUserId());
            optionalUser.ifPresent(user -> returnPost.setUserName(user.getUsername()));

            returnPost.setVoteCount(voteRepository.countVotesByPostId(returnPost.getId()));

            List<Comment> commentList = commentRepository.findAllCommentsByPostId(returnPost.getId());

            model.addAttribute("post", returnPost);
            model.addAttribute("loggedIn", sessionUser.isLoggedIn());
            model.addAttribute("commentList", commentList);
            model.addAttribute("comment", new Comment());
        }

        return model;
    }
}
