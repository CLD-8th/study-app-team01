/*
 * 마이페이지 · 담당 6
 *
 * SC-04 화면. 구역마다 요청이 따로 있음.
 */

async function loadProfile() {
    /*
     * TODO 66 · 내 정보 표시
     *
     * 기능        이메일 · 별명 · 가입일을 그림
     *             가입일은 앞 열 글자만 씀
     * 활용메소드  api.get() · escapeHtml()   제공됨
     *             GET /api/members/me        TODO 65 · 같은 담당
     * 받는자료    MemberResponse · TODO.md 응답 형태 참고
     * 그릴위치    SC-04 · #profile
     *             조각은 parts.html 의 "마이페이지 내 정보"
     * 동작결과    EP-15 · 토큰이 없으면 로그인 화면으로 보내짐
     */
    const member = await api.get('/api/members/me');

    document.getElementById('profile').innerHTML = `
        <div>이메일 &nbsp; ${escapeHtml(member.email)}</div>
        <div>별명 &nbsp; <b>${escapeHtml(member.nickname)}</b></div>
        <div>가입일 &nbsp; ${member.createdAt.substring(0, 10)}</div>
    `;
}

async function loadMyStudies() {
    /*
     * TODO 67 · 내 모집글 표시
     *
     * 기능        건수를 표시하고 목록을 그림
     *             마감된 것은 흐리게 표시하고 인원을 함께 보임
     *             한 건도 없으면 빈 화면 문구를 둠
     * 활용메소드  api.get() · badge() · escapeHtml()   제공됨
     *             GET /api/members/me/studies          TODO 65 · 같은 담당
     * 받는자료    List<StudyListResponse>
     * 그릴위치    SC-04 · #study-count 와 #my-studies
     *             조각은 parts.html 의 "마이페이지 목록 항목"
     * 동작결과    EP-16 · 제목을 누르면 상세로 이동
     */
    const studies = await api.get('/api/members/me/studies');

    document.getElementById('study-count').textContent = `${studies.length}건`;

    const listEl = document.getElementById('my-studies');
    listEl.innerHTML = '';

    if (studies.length === 0) {
        listEl.innerHTML = '<div class="empty">등록한 모집글이 없습니다</div>';
        return;
    }

    studies.forEach(post => {
        const item = document.createElement('div');
        item.className = 'item' + (post.status === 'CLOSED' ? ' closed' : '');
        item.innerHTML = `
            <div class="item-title">
                <a href="/study.html?id=${post.id}">${escapeHtml(post.title)}</a>
                ${badge(post.status)}
            </div>
            <span class="item-meta">${post.acceptedCount} / ${post.capacity}명</span>
        `;
        listEl.appendChild(item);
    });
}


async function loadMyApplications() {
    /*
     * TODO 68 · 내 신청 표시
     *
     * 기능        건수를 표시하고 목록을 그림
     *             모집글 제목과 상태 배지를 보임 · 취소 단추는 두지 않음
     *             한 건도 없으면 빈 화면 문구를 둠
     * 활용메소드  api.get() · badge() · shortDate()      제공됨
     *             GET /api/members/me/applications       TODO 65 · 같은 담당
     * 받는자료    List<ApplicationResponse> · studyPostTitle 로 제목을 표시
     * 그릴위치    SC-04 · #application-count 와 #my-applications
     * 동작결과    EP-17 · 취소는 상세 화면에서만 함
     */
    const applications = await api.get('/api/members/me/applications');

    document.getElementById('application-count').textContent = `${applications.length}건`;

    const listEl = document.getElementById('my-applications');
    listEl.innerHTML = '';

    if (applications.length === 0) {
        listEl.innerHTML = '<div class="empty">신청한 모집글이 없습니다</div>';
        return;
    }

    applications.forEach(app => {
        const item = document.createElement('div');
        item.className = 'item';
        item.innerHTML = `
            <div class="item-title">
                <a href="/study.html?id=${app.studyPostId}">${escapeHtml(app.studyPostTitle)}</a>
                ${badge(app.status)}
            </div>
            <span class="item-meta">${shortDate(app.createdAt)}에 신청함</span>
        `;
        listEl.appendChild(item);
    });
}


document.addEventListener('DOMContentLoaded', async () => {
    if (!requireLogin()) return;
    await loadProfile();
    await loadMyStudies();
    await loadMyApplications();
});

