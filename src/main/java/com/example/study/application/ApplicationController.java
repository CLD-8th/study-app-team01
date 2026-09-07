package com.example.study.application;

import com.example.study.application.dto.ApplicationRequest;
import com.example.study.application.dto.ApplicationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 신청 표현 계층.
 *
 * 대상 모집글에 종속된 주소와 신청 자체를 가리키는 주소가 섞여 있어
 * 묶음 주소를 클래스에 두지 않음.
 */
@RestController
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping("/api/studies/{studyId}/applications")
    public ResponseEntity<ApplicationResponse> apply(@PathVariable Long studyId,
                                                     @Valid @RequestBody ApplicationRequest request,
                                                     @AuthenticationPrincipal Long memberId) {
        return ResponseEntity.status(201)
                .body(applicationService.apply(studyId, request.message(), memberId));
    }

    @GetMapping("/api/studies/{studyId}/applications")
    public List<ApplicationResponse> findByStudy(@PathVariable Long studyId,
                                                 @AuthenticationPrincipal Long memberId) {
        return applicationService.findByStudy(studyId, memberId);
    }

    @DeleteMapping("/api/applications/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id,
                                       @AuthenticationPrincipal Long memberId) {
        applicationService.cancel(id, memberId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/applications/{id}/accept")
    public ApplicationResponse accept(@PathVariable Long id,
                                      @AuthenticationPrincipal Long memberId) {
        return applicationService.accept(id, memberId);
    }

    @PatchMapping("/api/applications/{id}/reject")
    public ApplicationResponse reject(@PathVariable Long id,
                                      @AuthenticationPrincipal Long memberId) {
        return applicationService.reject(id, memberId);
    }
}