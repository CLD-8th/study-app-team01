package com.example.study.application;

import com.example.study.application.dto.ApplicationResponse;
import com.example.study.common.BusinessException;
import com.example.study.common.ErrorCode;
import com.example.study.member.Member;
import com.example.study.member.MemberService;
import com.example.study.study.StudyPost;
import com.example.study.study.StudyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 신청 업무 계층.
 *
 * 판단 순서가 중요함. 대상 확인을 먼저 하지 않으면
 * 없는 모집글에 대해 다른 판단을 시도하게 됨.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final StudyService studyService;
    private final MemberService memberService;

    /**
     * 신청.
     *
     * 순서는 대상 확인 · 자기 모집글 · 상태 · 마감일 · 중복임.
     * 상태가 마감인 경우와 마감일이 지난 경우는 사유가 다르므로 나누어 판단함.
     */
    @Transactional
    public ApplicationResponse apply(Long studyPostId, String message, Long memberId) {
        StudyPost post = studyService.getWithWriter(studyPostId);

        if (post.isWrittenBy(memberId)) {
            throw new BusinessException(ErrorCode.SELF_APPLICATION, "자기 모집글에는 신청 불가");
        }
        if (!post.isRecruiting()) {
            throw new BusinessException(ErrorCode.STUDY_CLOSED, "마감된 모집글");
        }
        if (post.isDeadlinePassed()) {
            throw new BusinessException(ErrorCode.DEADLINE_PASSED, "마감일이 지남");
        }
        // 거절된 신청도 확인 대상임. 거절 후 재신청을 허용하지 않기로 정함.
        if (applicationRepository.findByStudyPostIdAndApplicantId(studyPostId, memberId).isPresent()) {
            throw new BusinessException(ErrorCode.DUPLICATE_APPLICATION, "이미 신청한 모집글");
        }

        Member applicant = memberService.getMember(memberId);
        Application saved = applicationRepository.save(new Application(post, applicant, message));

        log.info("신청 등록: id={} study={}", saved.getId(), studyPostId);
        return ApplicationResponse.from(saved);
    }

    /**
     * 신청 취소.
     *
     * 대기 상태만 취소 가능함. 수락된 신청을 취소하면
     * 마감된 모집글에 빈자리가 생기며 되돌릴 방법이 없음.
     */
    @Transactional
    public void cancel(Long applicationId, Long memberId) {
        Application application = getWithStudyPost(applicationId);

        if (!application.isAppliedBy(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "신청자만 취소 가능");
        }
        if (!application.isPending()) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED, "이미 처리된 신청");
        }

        applicationRepository.delete(application);
        log.info("신청 취소: id={}", applicationId);
    }

    public List<ApplicationResponse> findByStudy(Long studyPostId, Long memberId) {
        StudyPost post = studyService.getWithWriter(studyPostId);

        if (!post.isWrittenBy(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "모집자만 조회 가능");
        }
        return applicationRepository.findByStudyPostIdOrderByIdAsc(studyPostId)
                .stream().map(ApplicationResponse::from).toList();
    }

    public List<ApplicationResponse> findMine(Long memberId) {
        return applicationRepository.findByApplicantIdOrderByIdDesc(memberId)
                .stream().map(ApplicationResponse::from).toList();
    }

    /**
     * 수락.
     *
     * 마지막 자리를 채우면 모집글도 함께 마감함.
     * 별도 처리를 두지 않고 수락 시점에 판단함.
     */
    @Transactional
    public ApplicationResponse accept(Long applicationId, Long memberId) {
        Application application = processable(applicationId, memberId);
        StudyPost post = application.getStudyPost();

        long accepted = applicationRepository.countByStudyPostIdAndStatus(
                post.getId(), ApplicationStatus.ACCEPTED);
        if (accepted >= post.getCapacity()) {
            throw new BusinessException(ErrorCode.CAPACITY_EXCEEDED, "정원이 찼습니다");
        }

        application.accept();

        if (accepted + 1 >= post.getCapacity()) {
            post.close();
            log.info("정원 충족으로 마감: study={}", post.getId());
        }
        return ApplicationResponse.from(application);
    }

    /**
     * 거절.
     *
     * 정원을 확인하지 않음. 거절은 인원에 영향을 주지 않음.
     */
    @Transactional
    public ApplicationResponse reject(Long applicationId, Long memberId) {
        Application application = processable(applicationId, memberId);
        application.reject();
        return ApplicationResponse.from(application);
    }

    private Application processable(Long applicationId, Long memberId) {
        Application application = getWithStudyPost(applicationId);

        if (!application.getStudyPost().isWrittenBy(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "모집자만 처리 가능");
        }
        if (!application.isPending()) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED, "이미 처리된 신청");
        }
        return application;
    }

    private Application getWithStudyPost(Long id) {
        return applicationRepository.findWithStudyPostById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "신청 부재"));
    }
}