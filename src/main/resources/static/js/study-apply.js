/*
 * 신청 구획 · 담당 3
 *
 * StudyPage.study 와 StudyPage.myApplication 을 읽어 표시함.
 * 자료를 직접 조회하지 않음. 이미 읽어 둔 것을 씀.
 */

StudyPage.register(async function renderApply() {
    /*
     * TODO 34 · 구획 표시 조건과 신청 전 화면
     *
     * 기능        손님 · 모집자 본인 · 마감된 모집글에는 구획을 두지 않음
     *             내 신청이 없으면 메시지 입력란과 신청하기 단추를 그림
     *             신청에 성공하면 다시 그려 상태가 바뀐 화면이 나옴
     * 활용메소드  auth.loggedIn                       api.js · 제공됨
     *             StudyPage.isOwner() · StudyPage.study   제공됨
     *             StudyPage.reload()                  제공됨 · 네 구획을 다시 그림
     *             api.post() · showError()            제공됨
     *             POST /api/studies/{id}/applications   TODO 33 · 같은 담당
     * 받는자료    ApplicationResponse · TODO.md 응답 형태 참고
     * 그릴위치    SC-02 · #apply-panel
     *             조각은 parts.html 의 "신청 전"
     * 동작결과    로그아웃 상태에서 구획이 보이지 않음
     *             자기 글이면 400 SELF_APPLICATION 이 아니라 구획 자체가 없음
     */

    /*
     * TODO 35 · 신청 후 화면
     *
     * 기능        내 신청이 있으면 상태와 신청일을 보임
     *             대기 상태일 때만 취소 단추를 둠
     *             취소에 성공하면 다시 그려 신청 전 화면으로 돌아감
     * 활용메소드  StudyPage.myApplication   제공됨 · 없으면 null
     *             badge() · shortDate()     common.js · 제공됨
     *             api.del()                 api.js · 제공됨
     *             DELETE /api/applications/{id}   TODO 33 · 같은 담당
     * 받는자료    ApplicationResponse · status 는 PENDING · ACCEPTED · REJECTED
     * 그릴위치    SC-02 · #apply-panel
     *             조각은 parts.html 의 "신청 후 · 대기" 와 "신청 후 · 수락됨"
     * 동작결과    대기 건은 취소 단추가 보이고 수락된 건은 보이지 않음
     */
    const panel = document.getElementById('apply-panel');
    panel.innerHTML = '';
    panel.classList.add('hidden');

    if (!auth.loggedIn || !StudyPage.study || StudyPage.isOwner()
        || StudyPage.study.status !== 'RECRUITING') {
        return;
    }

    panel.classList.remove('hidden');
    const application = StudyPage.myApplication;
    const heading = '<div class="card-head"><div class="card-title">신청</div></div>';
    const errorBox = '<div class="alert alert-error hidden" id="apply-error" role="alert"></div>';

    if (!application) {
        panel.innerHTML = heading + errorBox +
            '<form id="apply-form">' +
            '<div class="field">' +
            '<label for="apply-message">신청 메시지 (선택, 최대 300자)</label>' +
            '<textarea id="apply-message" name="message" maxlength="300" ' +
            'placeholder="신청 메시지"></textarea>' +
            '</div>' +
            '<div class="actions">' +
            '<button type="submit" class="primary" id="apply-submit">신청하기</button>' +
            '</div>' +
            '</form>';

        const form = panel.querySelector('#apply-form');
        const message = panel.querySelector('#apply-message');
        const button = panel.querySelector('#apply-submit');
        const error = panel.querySelector('#apply-error');

        form.addEventListener('submit', async (event) => {
            event.preventDefault();
            if (button.disabled) return;
            error.classList.add('hidden');
            if (message.value.length > 300) {
                showError(error, { message: '신청 메시지는 300자 이하로 입력해 주세요.' });
                return;
            }
            button.disabled = true;
            button.textContent = '신청 중…';
            try {
                await api.post('/api/studies/' + StudyPage.id + '/applications', {
                    message: message.value
                });
            } catch (cause) {
                showError(error, cause);
                button.disabled = false;
                button.textContent = '신청하기';
                return;
            }
            try {
                await StudyPage.reload();
            } catch (cause) {
                // 신청은 저장됐으므로 재전송하지 않도록 단추를 잠근 채 안내함.
                button.textContent = '신청 완료';
                showError(error, { message: '신청은 완료됐지만 화면을 갱신하지 못했습니다. 새로고침해 주세요.' });
            }
        });
        return;
    }

    // badge()의 HTML에는 정해진 상태만 전달함.
    const knownStatuses = ['PENDING', 'ACCEPTED', 'REJECTED', 'CANCELED'];
    const status = knownStatuses.includes(application.status)
        ? badge(application.status)
        : escapeHtml(application.status);
    panel.innerHTML = heading + errorBox +
        '<div class="item">' +
        '<div class="item-meta">' + status +
        '<span>' + escapeHtml(shortDate(application.createdAt)) + '에 신청함</span>' +
        '</div>' +
        (application.status === 'PENDING'
            ? '<button type="button" id="apply-cancel">신청 취소</button>'
            : '') +
        '</div>';

    const button = panel.querySelector('#apply-cancel');
    if (!button) return;
    const error = panel.querySelector('#apply-error');
    button.addEventListener('click', async () => {
        if (button.disabled) return;
        error.classList.add('hidden');
        button.disabled = true;
        button.textContent = '취소 중…';
        try {
            await api.del('/api/applications/' + application.id);
        } catch (cause) {
            showError(error, cause);
            button.disabled = false;
            button.textContent = '신청 취소';
            return;
        }
        try {
            await StudyPage.reload();
        } catch (cause) {
            button.textContent = '취소 완료';
            showError(error, { message: '취소는 완료됐지만 화면을 갱신하지 못했습니다. 새로고침해 주세요.' });
        }
    });
});
