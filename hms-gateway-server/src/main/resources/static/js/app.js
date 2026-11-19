/* 云康医院挂号与问诊平台 —— 前端交互逻辑（原生 JS，无外部 CDN 依赖） */

var API = '';                 // 统一走网关，同源无跨域
var token = localStorage.getItem('hms_token') || '';
var user = JSON.parse(localStorage.getItem('hms_user') || 'null');
var curDept = '';

/* ---------------- 基础工具 ---------------- */
function $(id) { return document.getElementById(id); }

function toast(msg) {
    var t = $('toast');
    t.innerText = msg;
    t.classList.add('show');
    setTimeout(function () { t.classList.remove('show'); }, 2200);
}

function api(path, method, body) {
    var headers = { 'Content-Type': 'application/json' };
    if (token) { headers['Authorization'] = token; }
    return fetch(API + path, {
        method: method || 'GET',
        headers: headers,
        body: body ? JSON.stringify(body) : undefined
    }).then(function (r) {
        if (r.status === 401) { logout(); throw new Error('登录已失效'); }
        if (r.status === 429) { throw new Error('请求过于频繁，已被网关限流（挂号接口 10 次/秒）'); }
        return r.json();
    });
}

function fmtDate(d) {
    var dt = new Date(d);
    var m = dt.getMonth() + 1, day = dt.getDate();
    return dt.getFullYear() + '-' + (m < 10 ? '0' + m : m) + '-' + (day < 10 ? '0' + day : day);
}

function periodName(p) { return p === 'AM' ? '上午' : '下午'; }

function statusTag(status) {
    var map = { 0: ['tag-blue', '待就诊'], 1: ['tag-orange', '就诊中'], 2: ['tag-green', '已完成'], 3: ['tag-gray', '已退号'] };
    var m = map[status] || ['tag-gray', '未知'];
    return '<span class="tag ' + m[0] + '">' + m[1] + '</span>';
}

/* ---------------- 登录注册 ---------------- */
function switchTab(t) {
    $('loginForm').style.display = t === 'login' ? '' : 'none';
    $('regForm').style.display = t === 'reg' ? '' : 'none';
    var tabs = document.querySelectorAll('.tab');
    for (var i = 0; i < tabs.length; i++) { tabs[i].classList.toggle('active', tabs[i].dataset.tab === t); }
}

function doLogin(e) {
    e.preventDefault();
    $('loginMsg').innerText = '';
    api('/api/user/login', 'POST', { username: $('loginUser').value, password: $('loginPwd').value })
        .then(function (res) {
            if (res.code !== 200) { $('loginMsg').innerText = res.message; return; }
            token = res.data.token;
            user = res.data;
            localStorage.setItem('hms_token', token);
            localStorage.setItem('hms_user', JSON.stringify(user));
            afterLogin();
        }).catch(function (err) { $('loginMsg').innerText = err.message; });
}

function doRegister(e) {
    e.preventDefault();
    api('/api/user/register', 'POST', {
        username: $('regUser').value, password: $('regPwd').value,
        realName: $('regName').value, phone: $('regPhone').value, role: 'PATIENT'
    }).then(function (res) {
        if (res.code !== 200) { $('loginMsg').innerText = res.message; return; }
        toast('注册成功，请登录');
        switchTab('login');
    }).catch(function (err) { $('loginMsg').innerText = err.message; });
}

function logout() {
    token = ''; user = null;
    localStorage.removeItem('hms_token'); localStorage.removeItem('hms_user');
    $('topbar').style.display = 'none'; $('app').style.display = 'none'; $('loginPage').style.display = 'flex';
}

function afterLogin() {
    $('loginPage').style.display = 'none';
    $('topbar').style.display = 'flex';
    $('app').style.display = 'block';
    $('userInfo').innerText = user.realName + '（' + roleName(user.role) + '）';
    renderMenu();
    route();
}

function roleName(r) { return r === 'DOCTOR' ? '医生' : (r === 'ADMIN' ? '管理员' : '患者'); }

function renderMenu() {
    var menus = [{ k: 'home', n: '首页' }];
    if (user.role !== 'DOCTOR') { menus.push({ k: 'book', n: '预约挂号' }); }
    if (user.role === 'PATIENT') { menus.push({ k: 'my', n: '我的挂号' }); }
    if (user.role === 'DOCTOR') { menus.push({ k: 'workbench', n: '医生工作台' }); }
    menus.push({ k: 'records', n: '病历中心' });
    if (user.role === 'ADMIN') { menus.push({ k: 'admin', n: '运营管理' }); }
    menus.push({ k: 'arch', n: '架构说明' });
    var html = '';
    menus.forEach(function (m) { html += '<a data-k="' + m.k + '" href="#/' + m.k + '">' + m.n + '</a>'; });
    $('menu').innerHTML = html;
}

/* ---------------- 弹窗 ---------------- */
function openModal(title, html) {
    $('modalTitle').innerText = title;
    $('modalBody').innerHTML = html;
    $('modalMask').classList.add('show');
}
function closeModal() { $('modalMask').classList.remove('show'); }

/* ---------------- 路由 ---------------- */
function page() { return (location.hash || '#/home').replace('#/', ''); }

function route() {
    var p = page();
    var links = document.querySelectorAll('#menu a');
    for (var i = 0; i < links.length; i++) { links[i].classList.toggle('active', links[i].dataset.k === p); }
    if (p === 'home') { renderHome(); }
    else if (p === 'book') { renderBook(); }
    else if (p === 'my') { renderMy(); }
    else if (p === 'workbench') { renderWorkbench(); }
    else if (p === 'records') { renderRecords(); }
    else if (p === 'admin') { renderAdmin(); }
    else if (p === 'arch') { renderArch(); }
    else { renderHome(); }
}
window.addEventListener('hashchange', route);

/* ---------------- 首页：科室 ---------------- */
function renderHome() {
    $('app').innerHTML =
        '<div class="page-head"><h2>选择就诊科室</h2><p>共 8 个临床科室，点击科室查看医生排班并预约挂号</p></div>' +
        '<div class="toolbar"><input id="deptKw" placeholder="搜索科室名称 / 编码" style="min-width:240px">' +
        '<button class="btn btn-primary" onclick="renderHome()">搜索</button></div>' +
        '<div class="grid grid-4" id="deptList"><div class="empty">加载中…</div></div>';
    var kw = $('deptKw').value;
    api('/api/dept/list' + (kw ? '?keyword=' + encodeURIComponent(kw) : '')).then(function (res) {
        var list = res.data || [];
        if (!list.length) { $('deptList').innerHTML = '<div class="empty">未找到科室</div>'; return; }
        var html = '';
        list.forEach(function (d) {
            html += '<div class="dept-card" onclick="pickDept(\'' + d.deptCode + '\')">' +
                '<div class="dept-icon">🏥</div>' +
                '<div class="dept-name">' + d.deptName + '</div>' +
                '<div class="dept-meta">编码：' + d.deptCode + '<br>位置：' + (d.location || '-') +
                '<br>在册医生：<span class="hl">' + (d.doctorCount || 0) + '</span> 人</div></div>';
        });
        $('deptList').innerHTML = html;
    });
}

function pickDept(code) {
    curDept = code;
    location.hash = '#/book';
}

/* ---------------- 预约挂号 ---------------- */
function renderBook() {
    $('app').innerHTML =
        '<div class="page-head"><h2>预约挂号</h2><p>流程：选择科室 → 选择医生与出诊日期 → 确认挂号 → 扣减号源并同步建档</p></div>' +
        '<div class="card"><div class="card-title">筛选条件</div><div class="toolbar">' +
        '<select id="fDept"><option value="">全部科室</option></select>' +
        '<input type="date" id="fDate">' +
        '<input id="fKw" placeholder="医生姓名 / 擅长">' +
        '<button class="btn btn-primary" onclick="loadSchedule()">查询排班</button>' +
        '<button class="btn btn-ghost" onclick="loadSchedule(true)">仅看有号</button></div>' +
        '<div id="schedBox"><div class="empty">请选择条件后查询排班</div></div></div>';

    api('/api/dept/list').then(function (res) {
        var sel = $('fDept'), html = '';
        (res.data || []).forEach(function (d) {
            html += '<option value="' + d.deptCode + '"' + (d.deptCode === curDept ? ' selected' : '') + '>' + d.deptName + '</option>';
        });
        sel.innerHTML = '<option value="">全部科室</option>' + html;
    });
    $('fDate').value = fmtDate(new Date());
    if (curDept) { loadSchedule(); }
}

function loadSchedule(onlyAvailable) {
    var dept = $('fDept').value, date = $('fDate').value, kw = $('fKw').value;
    var url = '/api/schedule/list?deptCode=' + dept + '&workDate=' + date +
        (onlyAvailable ? '&onlyAvailable=true' : '');
    $('schedBox').innerHTML = '<div class="empty">加载中…</div>';
    api(url).then(function (res) {
        var list = (res.data || []).filter(function (s) {
            return !kw || s.doctorName.indexOf(kw) >= 0;
        });
        if (!list.length) { $('schedBox').innerHTML = '<div class="empty">当日暂无排班，换个日期试试</div>'; return; }
        var html = '<table><thead><tr><th>医生</th><th>科室</th><th>日期</th><th>时段</th><th>诊室</th>' +
            '<th>剩余号源</th><th>挂号费</th><th>操作</th></tr></thead><tbody>';
        list.forEach(function (s) {
            var remain = s.remainingNum;
            var tag = remain > 5 ? 'tag-green' : (remain > 0 ? 'tag-orange' : 'tag-red');
            html += '<tr><td><b>' + s.doctorName + '</b></td><td>' + s.deptName + '</td><td>' + s.workDate +
                '</td><td>' + s.periodName + '</td><td>' + (s.room || '-') + '</td>' +
                '<td><span class="tag ' + tag + '">' + remain + ' / ' + s.totalNum + '</span></td>' +
                '<td>¥' + s.regFee + '</td>' +
                '<td>' + (remain > 0
                    ? '<button class="btn btn-primary btn-sm" onclick="openReg(' + s.id + ')">挂号</button>'
                    : '<button class="btn btn-ghost btn-sm" disabled>已满</button>') + '</td></tr>';
        });
        $('schedBox').innerHTML = html + '</tbody></table>';
    });
}

function openReg(scheduleId) {
    if (user.role !== 'PATIENT') { toast('仅患者账号可挂号'); return; }
    openModal('确认挂号信息',
        '<div class="form-row"><label>主诉（症状描述）</label><textarea id="mComplaint" placeholder="例如：近期头晕、血压偏高"></textarea></div>' +
        '<div class="form-row"><label>就诊人</label><input value="' + user.realName + '" disabled></div>' +
        '<div class="form-row"><button class="btn btn-primary btn-block" onclick="submitReg(' + scheduleId + ')">确认挂号</button></div>');
}

function submitReg(scheduleId) {
    api('/api/register/create', 'POST', {
        scheduleId: scheduleId,
        patientId: user.userId,
        chiefComplaint: $('mComplaint').value
    }).then(function (res) {
        if (res.code !== 200) { toast(res.message); return; }
        closeModal();
        openModal('挂号成功', '<div style="line-height:2.2">' +
            '<div>挂号单号：<span class="hl">' + res.data.regNo + '</span></div>' +
            '<div>就诊医生：' + res.data.doctorName + '</div>' +
            '<div>就诊科室：' + res.data.deptName + '</div>' +
            '<div>就诊时间：' + res.data.workDate + ' ' + periodName(res.data.period) + '</div>' +
            '<div>就诊序号：<span class="hl">第 ' + res.data.queueNo + ' 号</span></div>' +
            '<div>诊室：' + (res.data.room || '-') + '　　挂号费：¥' + res.data.fee + '</div>' +
            '<div style="margin-top:12px;color:#6b7280;font-size:12px">系统已自动：①扣减号源 ②生成挂号单 ③创建电子病历</div>' +
            '</div>');
    }).catch(function (e) { toast(e.message); });
}

/* ---------------- 我的挂号 ---------------- */
function renderMy() {
    $('app').innerHTML = '<div class="page-head"><h2>我的挂号</h2><p>可查看就诊队列、发起在线问诊、退号</p></div>' +
        '<div class="card"><div id="myBox"><div class="empty">加载中…</div></div></div>';
    api('/api/register/my?patientId=' + user.userId).then(function (res) {
        var list = res.data || [];
        if (!list.length) { $('myBox').innerHTML = '<div class="empty">暂无挂号记录，去 <a href="#/book" class="hl">预约挂号</a></div>'; return; }
        var html = '<table><thead><tr><th>挂号单号</th><th>医生</th><th>科室</th><th>时间</th><th>序号</th><th>状态</th><th>操作</th></tr></thead><tbody>';
        list.forEach(function (r) {
            html += '<tr><td>' + r.regNo + '</td><td>' + r.doctorName + '</td><td>' + r.deptName + '</td>' +
                '<td>' + r.workDate + ' ' + periodName(r.period) + '</td><td>第 ' + r.queueNo + ' 号</td>' +
                '<td>' + statusTag(r.status) + '</td><td>' +
                '<button class="btn btn-ghost btn-sm" onclick="viewRecord(' + r.id + ')">病历</button> ' +
                '<button class="btn btn-teal btn-sm" onclick="openChat(' + r.id + ')">问诊</button> ' +
                (r.status === 0 ? '<button class="btn btn-danger btn-sm" onclick="cancelReg(' + r.id + ')">退号</button>' : '') +
                '</td></tr>';
        });
        $('myBox').innerHTML = html + '</tbody></table>';
    });
}

function cancelReg(id) {
    if (!confirm('确认退号？号源将归还排班池')) { return; }
    api('/api/register/cancel/' + id, 'POST').then(function (res) {
        toast(res.code === 200 ? '退号成功，号源已归还' : res.message);
        renderMy();
    });
}

/* ---------------- 医生工作台 ---------------- */
function renderWorkbench() {
    var today = fmtDate(new Date());
    $('app').innerHTML = '<div class="page-head"><h2>医生工作台</h2><p>' + user.realName + ' 医生，工号 ' + user.username + '</p></div>' +
        '<div class="card"><div class="card-title">今日候诊队列</div><div class="toolbar">' +
        '<input type="date" id="wbDate" value="' + today + '">' +
        '<button class="btn btn-primary" onclick="loadQueue()">刷新队列</button></div>' +
        '<div id="wbBox"><div class="empty">加载中…</div></div></div>';
    loadQueue();
}

function loadQueue() {
    var d = $('wbDate').value;
    api('/api/register/doctor?doctorCode=' + user.username + '&workDate=' + d).then(function (res) {
        var list = res.data || [];
        if (!list.length) { $('wbBox').innerHTML = '<div class="empty">当日暂无挂号患者</div>'; return; }
        var html = '<table><thead><tr><th>序号</th><th>患者</th><th>科室</th><th>主诉</th><th>状态</th><th>操作</th></tr></thead><tbody>';
        list.forEach(function (r) {
            html += '<tr><td>第 ' + r.queueNo + ' 号</td><td>' + r.patientName + '</td><td>' + r.deptName + '</td>' +
                '<td>' + (r.chiefComplaint || '-') + '</td><td>' + statusTag(r.status) + '</td><td>' +
                (r.status === 0 ? '<button class="btn btn-primary btn-sm" onclick="callReg(' + r.id + ')">叫号</button> ' : '') +
                (r.status === 1 ? '<button class="btn btn-teal btn-sm" onclick="openDiagnose(' + r.id + ')">写病历</button> ' +
                    '<button class="btn btn-ghost btn-sm" onclick="finishReg(' + r.id + ')">完成</button> ' : '') +
                '<button class="btn btn-ghost btn-sm" onclick="viewRecord(' + r.id + ')">查看</button>' +
                '</td></tr>';
        });
        $('wbBox').innerHTML = html + '</tbody></table>';
    });
}

function callReg(id) { api('/api/register/call/' + id, 'POST').then(function (r) { toast(r.message); loadQueue(); }); }
function finishReg(id) { api('/api/register/finish/' + id, 'POST').then(function (r) { toast(r.message); loadQueue(); }); }

function openDiagnose(regId) {
    api('/api/record/reg/' + regId).then(function (res) {
        var r = res.data || {};
        openModal('填写电子病历',
            '<div class="form-row"><label>主诉</label><textarea id="dComplaint">' + (r.chiefComplaint || '') + '</textarea></div>' +
            '<div class="form-row"><label>诊断结果</label><textarea id="dDiagnosis">' + (r.diagnosis || '') + '</textarea></div>' +
            '<div class="form-row"><label>处方</label><textarea id="dPrescription">' + (r.prescription || '') + '</textarea></div>' +
            '<div class="form-row"><label>医嘱</label><textarea id="dAdvice">' + (r.advice || '') + '</textarea></div>' +
            '<button class="btn btn-primary btn-block" onclick="saveDiagnose(' + (r.id || 0) + ',' + regId + ')">保存病历</button>');
    });
}

function saveDiagnose(id, regId) {
    api('/api/record/update', 'PUT', {
        id: id, regId: regId,
        chiefComplaint: $('dComplaint').value, diagnosis: $('dDiagnosis').value,
        prescription: $('dPrescription').value, advice: $('dAdvice').value, status: 1
    }).then(function (res) {
        toast(res.code === 200 ? '病历保存成功' : res.message);
        if (res.code === 200) { closeModal(); }
    });
}

/* ---------------- 病历中心 ---------------- */
function renderRecords() {
    var isDoc = user.role === 'DOCTOR';
    $('app').innerHTML = '<div class="page-head"><h2>病历中心</h2><p>' +
        (isDoc ? '本人接诊产生' : '本人就诊产生') + '的电子病历记录</p></div>' +
        '<div class="card"><div class="toolbar"><input id="rKw" placeholder="搜索患者姓名 / 诊断">' +
        '<button class="btn btn-primary" onclick="loadRecords()">搜索</button></div>' +
        '<div id="recBox"><div class="empty">加载中…</div></div></div>';
    loadRecords();
}

function loadRecords() {
    var kw = $('rKw') ? $('rKw').value : '';
    var url = '/api/record/list?' + (user.role === 'DOCTOR'
        ? 'doctorCode=' + user.username : 'patientId=' + user.userId) + (kw ? '&keyword=' + encodeURIComponent(kw) : '');
    api(url).then(function (res) {
        var list = res.data || [];
        if (!list.length) { $('recBox').innerHTML = '<div class="empty">暂无病历记录</div>'; return; }
        var html = '<table><thead><tr><th>病历号</th><th>患者</th><th>医生</th><th>科室</th><th>诊断</th><th>状态</th><th>操作</th></tr></thead><tbody>';
        list.forEach(function (r) {
            html += '<tr><td>' + (r.recordNo || '-') + '</td><td>' + r.patientName + '</td><td>' + r.doctorName +
                '</td><td>' + r.deptName + '</td><td>' + (r.diagnosis || '待诊断') + '</td>' +
                '<td>' + (r.status === 1 ? '<span class="tag tag-green">已完成</span>' : '<span class="tag tag-blue">问诊中</span>') + '</td>' +
                '<td><button class="btn btn-ghost btn-sm" onclick="viewRecordById(' + r.id + ',' + r.regId + ')">详情</button></td></tr>';
        });
        $('recBox').innerHTML = html + '</tbody></table>';
    });
}

function viewRecord(regId) {
    api('/api/record/reg/' + regId).then(function (res) {
        var r = res.data;
        if (!r) { toast('该挂号单尚未生成病历'); return; }
        openModal('电子病历详情', recordHtml(r));
    });
}

function viewRecordById(id, regId) {
    api('/api/record/' + id).then(function (res) { openModal('电子病历详情', recordHtml(res.data, regId)); });
}

function recordHtml(r, regId) {
    var html = '<div style="line-height:2.1">' +
        '<div>病历号：<span class="hl">' + (r.recordNo || '-') + '</span></div>' +
        '<div>患者：' + r.patientName + '　　医生：' + r.doctorName + '（' + r.deptName + '）</div>' +
        '<div>主诉：' + (r.chiefComplaint || '-') + '</div>' +
        '<div>诊断：' + (r.diagnosis || '待诊断') + '</div>' +
        '<div>处方：' + (r.prescription || '-') + '</div>' +
        '<div>医嘱：' + (r.advice || '-') + '</div></div>';
    if (regId) {
        html += '<div class="card-title" style="margin-top:16px">问诊记录</div><div class="chat-box" id="chatBox">加载中…</div>' +
            '<div class="toolbar" style="margin-top:10px"><input id="chatInput" style="flex:1" placeholder="输入问诊内容…">' +
            '<button class="btn btn-teal" onclick="sendChat(' + regId + ')">发送</button></div>';
        loadChat(regId);
    }
    return html;
}

function loadChat(regId) {
    api('/api/consult/list/' + regId).then(function (res) {
        var list = res.data || [];
        if (!list.length) { $('chatBox').innerHTML = '<div class="empty">暂无问诊对话</div>'; return; }
        var html = '';
        list.forEach(function (c) {
            var me = c.sender === (user.role === 'DOCTOR' ? 'DOCTOR' : 'PATIENT');
            html += '<div class="chat-item' + (me ? ' me' : '') + '"><div class="bubble">' +
                '<div class="chat-who">' + (c.sender === 'DOCTOR' ? '医生' : '患者') + '</div>' + c.content + '</div></div>';
        });
        $('chatBox').innerHTML = html;
        $('chatBox').scrollTop = $('chatBox').scrollHeight;
    });
}

function openChat(regId) {
    openModal('在线问诊', '<div class="chat-box" id="chatBox">加载中…</div>' +
        '<div class="toolbar" style="margin-top:10px"><input id="chatInput" style="flex:1" placeholder="描述你的症状…">' +
        '<button class="btn btn-teal" onclick="sendChat(' + regId + ')">发送</button></div>');
    loadChat(regId);
}

function sendChat(regId) {
    var txt = $('chatInput').value;
    if (!txt) { return; }
    api('/api/consult/add', 'POST', {
        regId: regId, patientId: user.userId, doctorCode: '',
        sender: user.role === 'DOCTOR' ? 'DOCTOR' : 'PATIENT', content: txt
    }).then(function () { $('chatInput').value = ''; loadChat(regId); });
}

/* ---------------- 运营管理 ---------------- */
function renderAdmin() {
    $('app').innerHTML = '<div class="page-head"><h2>运营管理</h2><p>挂号数据看板、排班发布与科室维护</p></div>' +
        '<div class="grid grid-4" id="statBox"></div>' +
        '<div class="grid grid-2">' +
        '<div class="card"><div class="card-title">发布医生排班</div>' +
        '<div class="form-row"><label>医生工号（如 D001）</label><input id="aDoctor"></div>' +
        '<div class="form-row"><label>出诊日期</label><input type="date" id="aDate"></div>' +
        '<div class="form-row"><label>时段</label><select id="aPeriod"><option value="AM">上午</option><option value="PM">下午</option></select></div>' +
        '<div class="form-row"><label>号源总数</label><input id="aTotal" value="20"></div>' +
        '<div class="form-row"><label>诊室</label><input id="aRoom" value="101"></div>' +
        '<button class="btn btn-primary btn-block" onclick="addSchedule()">发布排班</button></div>' +
        '<div class="card"><div class="card-title">新增科室</div>' +
        '<div class="form-row"><label>科室编码</label><input id="aDeptCode" placeholder="DEPT009"></div>' +
        '<div class="form-row"><label>科室名称</label><input id="aDeptName"></div>' +
        '<div class="form-row"><label>门诊位置</label><input id="aDeptLoc" value="门诊楼 1 层"></div>' +
        '<div class="form-row"><label>科室介绍</label><textarea id="aDeptDesc"></textarea></div>' +
        '<button class="btn btn-teal btn-block" onclick="addDept()">新增科室</button></div></div>' +
        '<div class="card"><div class="card-title">各科室挂号量（今日）</div><div id="deptStat"></div></div>';

    $('aDate').value = fmtDate(new Date());
    var today = fmtDate(new Date());
    api('/api/register/stats?workDate=' + today).then(function (res) {
        var d = res.data || {};
        var total = 0, byStatus = {};
        (d.byStatus || []).forEach(function (s) { byStatus[s.status] = s.total; total += s.total; });
        $('statBox').innerHTML =
            '<div class="stat"><div class="num">' + total + '</div><div class="lbl">今日挂号总量</div></div>' +
            '<div class="stat"><div class="num">' + (byStatus[0] || 0) + '</div><div class="lbl">待就诊</div></div>' +
            '<div class="stat"><div class="num">' + (byStatus[1] || 0) + '</div><div class="lbl">就诊中</div></div>' +
            '<div class="stat"><div class="num">' + (byStatus[2] || 0) + '</div><div class="lbl">已完成</div></div>';
        var rows = '';
        (d.byDept || []).forEach(function (x) {
            rows += '<tr><td>' + x.deptName + '</td><td>' + x.deptCode + '</td><td class="hl">' + x.regCount + '</td></tr>';
        });
        $('deptStat').innerHTML = rows
            ? '<table><thead><tr><th>科室</th><th>编码</th><th>挂号量</th></tr></thead><tbody>' + rows + '</tbody></table>'
            : '<div class="empty">今日暂无挂号数据</div>';
    });
}

function addSchedule() {
    api('/api/schedule/add', 'POST', {
        doctorCode: $('aDoctor').value, workDate: $('aDate').value, period: $('aPeriod').value,
        totalNum: parseInt($('aTotal').value, 10), remainingNum: parseInt($('aTotal').value, 10), room: $('aRoom').value
    }).then(function (res) { toast(res.code === 200 ? '排班发布成功' : res.message); });
}

function addDept() {
    api('/api/dept/add', 'POST', {
        deptCode: $('aDeptCode').value, deptName: $('aDeptName').value,
        location: $('aDeptLoc').value, description: $('aDeptDesc').value, parentCode: ''
    }).then(function (res) { toast(res.code === 200 ? '科室新增成功' : res.message); });
}

/* ---------------- 架构说明 ---------------- */
function renderArch() {
    $('app').innerHTML =
        '<div class="page-head"><h2>系统架构说明</h2><p>Spring Boot 2.3.7 + Spring Cloud Hoxton.SR5 微服务技术栈</p></div>' +
        '<div class="card"><div class="card-title">服务清单</div>' +
        '<table><thead><tr><th>微服务</th><th>端口</th><th>职责</th></tr></thead><tbody>' +
        '<tr><td>hms-eureka-server</td><td>8761</td><td>服务注册与发现中心</td></tr>' +
        '<tr><td>hms-config-server</td><td>8888</td><td>分布式配置中心（Git 仓库存储配置）</td></tr>' +
        '<tr><td>hms-gateway-server</td><td>8500</td><td>统一入口：路由转发 / JWT 鉴权 / 令牌桶限流 / 静态前端</td></tr>' +
        '<tr><td>hms-user-service</td><td>8071</td><td>用户认证、患者与医生账号</td></tr>' +
        '<tr><td>hms-department-service</td><td>8072</td><td>科室与医生信息</td></tr>' +
        '<tr><td>hms-schedule-service</td><td>8073</td><td>医生排班与号源管理（行锁防超卖）</td></tr>' +
        '<tr><td>hms-register-service</td><td>8074</td><td>挂号编排核心：Feign 调用 + Hystrix 降级 + 事务补偿</td></tr>' +
        '<tr><td>hms-record-service</td><td>8075</td><td>电子病历与在线问诊记录</td></tr>' +
        '<tr><td>hms-hystrix-dashboard</td><td>8070</td><td>熔断监控仪表盘</td></tr>' +
        '</tbody></table></div>' +
        '<div class="card"><div class="card-title">挂号调用链路</div>' +
        '<p style="line-height:2.1">浏览器 → <span class="hl">Gateway(8500)</span>（鉴权 + 限流 10 次/秒）→ <span class="hl">Register(8074)</span> → ' +
        '① Feign 调 Schedule 校验并扣减号源（SELECT … FOR UPDATE 行锁）→ ② Feign 调 User 校验患者 → ' +
        '③ Feign 调 Department 获取医生信息 → ④ 本地事务写入挂号单 → ⑤ Feign 调 Record 创建电子病历；' +
        '任一步失败则回滚本地事务并调用 Schedule 的 restore 接口归还号源（补偿机制）。</p></div>';
}

/* ---------------- 启动 ---------------- */
/* 演示模式：URL 携带 ?demo=patient1 时自动登录（汇报演示 / 截图用，密码统一为 123456） */
(function () {
    var m = /[?&]demo=([^&]+)/.exec(location.search);
    if (m) {
        var u = decodeURIComponent(m[1]);
        fetch(API + '/api/user/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: u, password: '123456' })
        }).then(function (r) { return r.json(); }).then(function (res) {
            if (res.code === 200) {
                token = res.data.token;
                user = res.data;
                localStorage.setItem('hms_token', token);
                localStorage.setItem('hms_user', JSON.stringify(user));
                afterLogin();
            }
        });
        return;
    }
    if (token && user) { afterLogin(); }
})();
