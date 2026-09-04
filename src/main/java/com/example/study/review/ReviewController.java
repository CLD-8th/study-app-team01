package com.example.study.review;

import com.example.study.review.dto.ReviewRequest;
import com.example.study.review.dto.ReviewResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /*
     * TODO 56 · 후기 주소 셋
     *
     * 기능        GET /api/studies/{studyId}/reviews 는 손님도 볼 수 있음
     *             POST 는 201 · DELETE 는 204
     * 활용메소드  ReviewService.findByStudy()   TODO 52 · 같은 담당
     *
     *             ReviewService.create()        TODO 53 · 같은 담당
     *             ReviewService.delete()        TODO 54 · 같은 담당
     * 반환형태    List<ReviewResponse> · ReviewResponse
     * 동작결과    EP-12 · EP-13 · EP-14 · 목록은 토큰 없이 200
     *
     */
    //52번 컨트롤러
    @GetMapping("/api/studies/{studyId}/reviews")
    public List<ReviewResponse> findByStudyAllReview(@PathVariable Long studyId){
      return reviewService.findByStudy(studyId);
    };



}
