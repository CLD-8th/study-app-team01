package com.example.study.application;

import com.example.study.application.dto.AcceptedCount;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    /**
     * 신청 목록.
     *
     * 신청자를 함께 가져와 목록 건수만큼 조회가 늘어나지 않게 함.
     */
    @EntityGraph(attributePaths = {"applicant"})
    List<Application> findByStudyPostIdOrderByIdAsc(Long studyPostId);

    /**
     * 내 신청 목록.
     *
     * 제목을 표시하므로 모집글과 그 모집자를 함께 가져옴.
     */
    @EntityGraph(attributePaths = {"studyPost", "studyPost.writer"})
    List<Application> findByApplicantIdOrderByIdDesc(Long applicantId);

    @EntityGraph(attributePaths = {"studyPost", "applicant"})
    Optional<Application> findWithStudyPostById(Long id);

    Optional<Application> findByStudyPostIdAndApplicantId(Long studyPostId, Long applicantId);

    long countByStudyPostIdAndStatus(Long studyPostId, ApplicationStatus status);

    boolean existsByStudyPostIdAndApplicantIdAndStatusIn(
            Long studyPostId, Long applicantId, List<ApplicationStatus> statuses);

    /**
     * 모집글 묶음의 수락 인원을 한 번에 셈.
     *
     * 목록에서 건마다 세면 조회 구문이 건수에 비례함.
     * 식별자 묶음을 넘겨 한 구문으로 처리함.
     */
    @Query("""
            select new com.example.study.application.dto.AcceptedCount(a.studyPost.id, count(a))
            from Application a
            where a.studyPost.id in :studyPostIds and a.status = :status
            group by a.studyPost.id
            """)
    List<AcceptedCount> countAcceptedByStudyPostIds(List<Long> studyPostIds, ApplicationStatus status);
}