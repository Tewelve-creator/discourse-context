let sessionId = null;
let sessionEnded = false;
let scenarios = [];

const LANG_LABELS = {
    English: '英语', Chinese: '中文', Japanese: '日语', French: '法语', Spanish: '西班牙语'
};

const PLACEHOLDERS = {
    English: '例如：The first thing I do is drink a glass of water…',
    Chinese: '例如：我醒来做的第一件事是喝一杯温水……',
    Japanese: '例如：起きて最初にすることは…',
    French: 'Par exemple : La première chose que je fais…',
    Spanish: 'Por ejemplo: Lo primero que hago es…'
};

const LEVEL_LABELS = {
    A1: '入门', A2: '基础', B1: '中级', B2: '中高级', C1: '高级'
};

const $ = (id) => document.getElementById(id);

function endButtons() {
    return ['endBtn', 'sidebarEndBtn'].map($).filter(Boolean);
}

function setEndButtonsHidden(hidden) {
    endButtons().forEach(btn => btn.classList.toggle('hidden', hidden));
}

function setEndButtonsDisabled(disabled) {
    endButtons().forEach(btn => { btn.disabled = disabled; });
}

function setEndButtonsLabel(html) {
    endButtons().forEach(btn => { btn.innerHTML = html; });
}

function currentLevel() {
    return $('level').value;
}

function currentLevelLabel() {
    return LEVEL_LABELS[currentLevel()] || currentLevel();
}

function currentLang() {
    return $('language').value;
}

function currentLangLabel() {
    return LANG_LABELS[currentLang()] || currentLang();
}

function updateInputUi() {
    const lang = currentLangLabel();
    const level = currentLevelLabel();
    $('userInput').placeholder = PLACEHOLDERS[currentLang()] || PLACEHOLDERS.English;
    if (sessionId) {
        $('inputHint').textContent = `⚠️ 设置已更改，请点击「开始对话」以 ${lang} · ${currentLevel()} 重新练习`;
        $('inputHint').classList.remove('hidden');
    } else {
        $('inputHint').textContent = `👇 选择语言和水平后点击「开始对话」，用${lang}（${level}）回答 Alex`;
    }
}

function onSettingsChange() {
    updateInputUi();
    if (sessionId) {
        sessionId = null;
        $('userInput').disabled = true;
        $('sendBtn').disabled = true;
        $('endBtn') && setEndButtonsHidden(true);
        $('restartBtn').classList.add('hidden');
        sessionEnded = false;
        $('sessionMeta').textContent = `已选 ${currentLangLabel()} · ${currentLevel()} ${currentLevelLabel()} · 请点击「开始对话」`;
    }
}

async function init() {
    await loadAiStatus();

    const data = await fetch('/api/scenarios').then(r => r.json());
    scenarios = data.scenarios;

    const select = $('scenario');
    select.innerHTML = scenarios.map(s =>
        `<option value="${s.id}">${s.category} · ${s.title}</option>`
    ).join('');

    select.addEventListener('change', () => { updateScenarioPreview(); onSettingsChange(); });
    $('language').addEventListener('change', onSettingsChange);
    $('level').addEventListener('change', onSettingsChange);
    updateScenarioPreview();
    updateInputUi();

    $('startBtn').addEventListener('click', startSession);
    $('sendBtn').addEventListener('click', sendMessage);
    $('endBtn').addEventListener('click', endSession);
    $('sidebarEndBtn').addEventListener('click', endSession);
    $('restartBtn').addEventListener('click', resetSession);
    $('closeFeedback').addEventListener('click', closeFeedback);
    $('saveAiBtn').addEventListener('click', saveAiConfig);
    $('aiProvider').addEventListener('change', onProviderChange);
    $('cameraBtn').addEventListener('click', toggleCamera);
    $('closeCamera').addEventListener('click', stopCamera);
    $('micBtn').addEventListener('click', toggleSpeech);

    document.querySelectorAll('.sidebar-tabs .tab').forEach(tab => {
        tab.addEventListener('click', () => {
            document.querySelectorAll('.sidebar-tabs .tab').forEach(t => t.classList.remove('active'));
            document.querySelectorAll('.tab-panel').forEach(p => p.classList.remove('active'));
            tab.classList.add('active');
            $('tab-' + tab.dataset.tab).classList.add('active');
        });
    });

    $('userInput').addEventListener('keydown', (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    });
}

function updateScenarioPreview() {
    const s = scenarios.find(x => x.id === $('scenario').value);
    if (!s) return;
    $('scenarioDesc').textContent = s.description;
    $('keyPhrases').innerHTML = s.keyPhrases.map(p => `<span class="tag">${p}</span>`).join('');
}

async function startSession() {
    const btn = $('startBtn');
    btn.disabled = true;
    btn.innerHTML = '<span class="loading-dot">启动中</span>';

    try {
        const res = await fetch('/api/session/start', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                language: $('language').value,
                level: $('level').value,
                scenarioId: $('scenario').value
            })
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.error);

        sessionId = data.sessionId;
        sessionEnded = false;
        const lang = LANG_LABELS[data.language] || data.language;
        const s = data.scenario;

        $('messages').innerHTML = '';
        addMessage('coach', data.openingLine);
        $('sessionMeta').textContent = `${lang} · ${data.level} ${data.levelLabel || LEVEL_LABELS[data.level]} · ${s.title}`;
        $('userInput').disabled = false;
        $('sendBtn').disabled = false;
        $('micBtn').disabled = false;
        $('inputHint').textContent = `👇 对着摄像头说${lang} · 点 🎤 语音输入 · 练完点「结束练习」获取 AI 评价`;
        $('inputHint').classList.remove('hidden');
        setEndButtonsHidden(false);
        $('restartBtn').classList.remove('hidden');
        closeFeedback();
        $('userInput').focus();
    } catch (err) {
        alert('启动失败：' + err.message);
    } finally {
        btn.disabled = false;
        btn.innerHTML = '▶ 开始对话';
    }
}

async function sendMessage() {
    const input = $('userInput');
    const text = input.value.trim();
    if (!text || !sessionId || sessionEnded) return;

    input.value = '';
    addMessage('user', text);
    $('sendBtn').disabled = true;

    const typing = addMessage('coach', 'Alex 正在思考', true);

    try {
        const res = await fetch('/api/session/chat', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ sessionId, message: text })
        });
        const data = await res.json();
        typing.remove();
        if (!res.ok) throw new Error(data.error);
        addMessage('coach', data.reply);
    } catch (err) {
        typing.remove();
        addMessage('coach', '抱歉，出现了一些问题，请再试一次。');
    } finally {
        if (!sessionEnded) {
            $('sendBtn').disabled = false;
            input.focus();
        }
    }
}

async function fetchFeedback() {
    const res = await fetch('/api/session/feedback', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ sessionId })
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.error);
    return data;
}

async function endSession() {
    if (!sessionId || sessionEnded) return;

    stopSpeech();
    stopCamera();

    $('userInput').disabled = true;
    $('sendBtn').disabled = true;
    $('micBtn').disabled = true;

    setEndButtonsDisabled(true);
    setEndButtonsLabel('<span class="loading-dot">AI 评价中</span>');

    try {
        const data = await fetchFeedback();
        renderFeedback(data, true);
        sessionEnded = true;
        $('inputHint').textContent = '✅ 练习已结束 · 查看右侧 AI 评价 · 点击「重开」开始新练习';
        $('inputHint').classList.remove('hidden');
        setEndButtonsHidden(true);
    } catch (err) {
        alert('评价生成失败：' + err.message);
        $('userInput').disabled = false;
        $('sendBtn').disabled = false;
        $('micBtn').disabled = false;
    } finally {
        setEndButtonsDisabled(false);
        setEndButtonsLabel('⏹ 结束练习');
    }
}

function renderFeedback(r, fromEnd = false) {
    const pending = r.noParticipation === true;
    const score = (v) => pending ? '—' : v;

    if (fromEnd) {
        $('feedbackSubtitle').textContent = pending
            ? '尚未开口 · 练习引导'
            : '本次口语练习 AI 综合评价';
    } else {
        $('feedbackSubtitle').textContent = '';
    }
    $('feedbackContent').innerHTML = `
        <div class="score-grid${pending ? ' pending' : ''}">
            <div class="score-card overall">
                <div class="value">${score(r.overallScore)}</div>
                <div class="label">综合得分</div>
            </div>
            <div class="score-card">
                <div class="value">${score(r.fluencyScore)}</div>
                <div class="label">流利度</div>
            </div>
            <div class="score-card">
                <div class="value">${score(r.grammarScore)}</div>
                <div class="label">语法</div>
            </div>
            <div class="score-card">
                <div class="value">${score(r.vocabularyScore)}</div>
                <div class="label">词汇</div>
            </div>
            <div class="score-card">
                <div class="value">${score(r.confidenceScore)}</div>
                <div class="label">自信度</div>
            </div>
        </div>
        ${pending ? '<div class="pending-tip">💡 完成至少一轮对话后，AI 将给出详细评分与改进建议</div>' : ''}
        <div class="summary-box">${r.summary}</div>
        ${section('✅ 你的优势', r.strengths)}
        ${section('📈 待提升方向', r.improvements)}
        ${section('💡 推荐表达', r.suggestedPhrases)}
        <div class="challenge-box">
            <strong>🎯 下次挑战</strong>
            ${r.nextChallenge}
        </div>
    `;
    $('feedbackPanel').classList.remove('hidden');
    document.querySelector('.app').classList.add('has-feedback');
}

function section(title, items) {
    if (!items || !items.length) return '';
    return `<div class="feedback-section"><h4>${title}</h4><ul>${items.map(i => `<li>${i}</li>`).join('')}</ul></div>`;
}

function addMessage(role, text, isTyping = false) {
    const row = document.createElement('div');
    row.className = `msg-row ${role}`;

    const avatar = document.createElement('div');
    avatar.className = 'msg-avatar';
    avatar.textContent = role === 'coach' ? 'A' : '我';

    const bubble = document.createElement('div');
    bubble.className = `msg-bubble${isTyping ? ' typing' : ''}`;
    bubble.textContent = text;

    row.appendChild(avatar);
    row.appendChild(bubble);
    $('messages').appendChild(row);
    $('messages').scrollTop = $('messages').scrollHeight;
    return row;
}

function closeFeedback() {
    $('feedbackPanel').classList.add('hidden');
    document.querySelector('.app').classList.remove('has-feedback');
}

function resetSession() {
    sessionId = null;
    sessionEnded = false;
    stopSpeech();
    stopCamera();
    $('messages').innerHTML = `
        <div class="welcome">
            <h3>在真实场景中练口语</h3>
            <p>左侧选场景，点「开始对话」即可练习</p>
            <div class="feature-chips">
                <span>💼 面试</span><span>🌅 习惯</span><span>🎤 演讲</span>
                <span>🤝 社交</span><span>🎯 目标</span><span>📖 读书会</span>
            </div>
        </div>`;
    $('sessionMeta').textContent = '选择场景，开启口语练习';
    $('userInput').disabled = true;
    $('sendBtn').disabled = true;
    $('micBtn').disabled = true;
    $('inputHint').classList.add('hidden');
    setEndButtonsHidden(true);
    $('restartBtn').classList.add('hidden');
    closeFeedback();
}

init();

async function loadAiStatus() {
    try {
        const status = await fetch('/api/llm/config').then(r => r.json());
        updateAiUi(status);
    } catch {
        $('modeHint').textContent = '💡 演示模式 · 请在下方配置 API Key';
    }
}

function updateAiUi(status) {
    const live = status.mode === 'live';
    $('modeHint').textContent = live
        ? `✅ AI 已启用 · ${status.provider}`
        : '💡 演示模式 · 切到「AI 配置」启用智能对话';
    $('modeHint').classList.toggle('live', live);

    $('aiStatusHint').textContent = live
        ? `✅ 已连接 ${status.provider}，Alex 智能回复中`
        : '配置 API Key 后启用智能对话';
    $('aiStatusHint').classList.toggle('live', live);

    if (status.provider) $('aiProvider').value = status.provider;
    if (status.baseUrl) $('baseUrl').value = status.baseUrl;
    if (status.model) $('modelName').value = status.model;
    if (status.apiKeyHint) $('apiKey').placeholder = status.apiKeyHint || 'sk-...';
    onProviderChange();
}

function onProviderChange() {
    const custom = $('aiProvider').value === 'custom';
    $('aiAdvanced').classList.toggle('hidden', !custom);
    const presets = {
        deepseek: { baseUrl: 'https://api.deepseek.com/v1', model: 'deepseek-chat' },
        openai: { baseUrl: 'https://api.openai.com/v1', model: 'gpt-4o-mini' },
        qwen: { baseUrl: 'https://dashscope.aliyuncs.com/compatible-mode/v1', model: 'qwen-plus' }
    };
    const p = presets[$('aiProvider').value];
    if (p && !custom) {
        $('baseUrl').value = p.baseUrl;
        $('modelName').value = p.model;
    }
}

async function saveAiConfig() {
    const btn = $('saveAiBtn');
    btn.disabled = true;
    btn.textContent = '连接中…';
    try {
        const res = await fetch('/api/llm/config', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                provider: $('aiProvider').value,
                apiKey: $('apiKey').value,
                baseUrl: $('baseUrl').value,
                model: $('modelName').value
            })
        });
        const status = await res.json();
        if (!res.ok) throw new Error(status.error);
        updateAiUi(status);
        $('apiKey').value = '';
        alert(status.mode === 'live' ? '✅ AI 智能对话已启用！请重新开始对话。' : '请先输入有效的 API Key');
    } catch (err) {
        alert('配置失败：' + err.message);
    } finally {
        btn.disabled = false;
        btn.textContent = '保存并启用 AI';
    }
}

/* ── Camera ── */
let cameraStream = null;

async function toggleCamera() {
    if (cameraStream) {
        stopCamera();
        return;
    }
    if (!navigator.mediaDevices?.getUserMedia) {
        alert('你的浏览器不支持摄像头访问');
        return;
    }
    try {
        cameraStream = await navigator.mediaDevices.getUserMedia({
            video: { facingMode: 'user', width: { ideal: 640 }, height: { ideal: 480 } },
            audio: false
        });
        $('cameraVideo').srcObject = cameraStream;
        $('cameraPip').classList.remove('hidden');
        $('chatBody').classList.add('has-camera');
        $('cameraBtn').classList.add('active');
        $('cameraStatus').textContent = '对着镜子练口语';
    } catch (err) {
        alert('无法打开摄像头：' + (err.message || '请检查浏览器权限'));
    }
}

function stopCamera() {
    if (cameraStream) {
        cameraStream.getTracks().forEach(t => t.stop());
        cameraStream = null;
    }
    $('cameraVideo').srcObject = null;
    $('cameraPip').classList.add('hidden');
    $('chatBody').classList.remove('has-camera');
    $('cameraBtn').classList.remove('active');
}

/* ── Speech Recognition ── */
const SPEECH_LANG = {
    English: 'en-US', Chinese: 'zh-CN', Japanese: 'ja-JP', French: 'fr-FR', Spanish: 'es-ES'
};

let recognition = null;
let isRecording = false;

function createRecognition() {
    const SR = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (!SR) return null;

    const rec = new SR();
    rec.lang = SPEECH_LANG[currentLang()] || 'en-US';
    rec.continuous = false;
    rec.interimResults = true;

    rec.onstart = () => {
        isRecording = true;
        $('micBtn').classList.add('recording');
        $('speechPreview').classList.remove('hidden');
        $('speechText').textContent = '正在聆听…';
    };

    rec.onresult = (e) => {
        let interim = '';
        let final = '';
        for (let i = e.resultIndex; i < e.results.length; i++) {
            const t = e.results[i][0].transcript;
            if (e.results[i].isFinal) final += t;
            else interim += t;
        }
        const text = final || interim;
        $('speechText').textContent = text;
        $('userInput').value = text;
    };

    rec.onend = () => {
        isRecording = false;
        $('micBtn').classList.remove('recording');
        $('speechPreview').classList.add('hidden');
        const text = $('userInput').value.trim();
        if (text && sessionId) {
            sendMessage();
        }
    };

    rec.onerror = (e) => {
        isRecording = false;
        $('micBtn').classList.remove('recording');
        $('speechPreview').classList.add('hidden');
        if (e.error !== 'aborted') {
            $('speechText').textContent = '语音识别失败，请检查麦克风权限';
            $('speechPreview').classList.remove('hidden');
            setTimeout(() => $('speechPreview').classList.add('hidden'), 2000);
        }
    };

    return rec;
}

function toggleSpeech() {
    if (!sessionId || sessionEnded) return;

    if (!(window.SpeechRecognition || window.webkitSpeechRecognition)) {
        alert('你的浏览器不支持语音识别，请使用 Chrome 或 Edge');
        return;
    }

    if (isRecording) {
        stopSpeech();
        return;
    }

    if (!cameraStream) {
        toggleCamera();
    }

    recognition = createRecognition();
    if (!recognition) return;
    recognition.lang = SPEECH_LANG[currentLang()] || 'en-US';
    recognition.start();
}

function stopSpeech() {
    if (recognition && isRecording) {
        try { recognition.stop(); } catch (_) {}
    }
    isRecording = false;
    $('micBtn').classList.remove('recording');
    $('speechPreview').classList.add('hidden');
}
