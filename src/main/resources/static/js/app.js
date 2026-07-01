const chatToggle = document.getElementById('chatToggle');
const chatPanel = document.getElementById('chatPanel');
const chatForm = document.getElementById('chatForm');
const chatInput = document.getElementById('chatInput');
const chatMessages = document.getElementById('chatMessages');
const quickActions = document.querySelectorAll('[data-chat-message]');
const sidebarNav = document.getElementById('sidebarNav');
const sidebarBackdrop = document.getElementById('sidebarBackdrop');
const chatContextKey = 'qlvt.chat.context';
let chatContext = loadChatContext();

if (sidebarNav && sidebarBackdrop) {
    sidebarNav.addEventListener('shown.bs.collapse', () => sidebarBackdrop.classList.add('show'));
    sidebarNav.addEventListener('hidden.bs.collapse', () => sidebarBackdrop.classList.remove('show'));
    sidebarBackdrop.addEventListener('click', () => {
        const instance = bootstrap.Collapse.getOrCreateInstance(sidebarNav, {toggle: false});
        instance.hide();
    });

    document.querySelectorAll('.sidebar-subnav a').forEach(link => {
        link.addEventListener('click', () => {
            if (window.innerWidth < 992) {
                bootstrap.Collapse.getOrCreateInstance(sidebarNav, {toggle: false}).hide();
            }
        });
    });
}

if (chatToggle && chatPanel) {
    chatToggle.addEventListener('click', async () => {
        chatPanel.classList.toggle('open');
        if (chatPanel.classList.contains('open') && chatMessages.dataset.loaded !== 'true') {
            await loadChatHistory();
        }
    });
}

quickActions.forEach(button => button.addEventListener('click', () => sendChat(button.dataset.chatMessage)));

if (chatForm) {
    chatForm.addEventListener('submit', async (event) => {
        event.preventDefault();
        await sendChat(chatInput.value.trim());
    });
}

async function loadChatHistory() {
    if (!chatMessages) return;
    try {
        const response = await fetch('/api/chatbot/history');
        const items = await response.json();
        if (Array.isArray(items) && items.length > 0) {
            chatMessages.innerHTML = '';
            items.forEach(item => appendChat(item.sender === 'USER' ? 'me' : 'bot', item.message));
        }
    } finally {
        chatMessages.dataset.loaded = 'true';
    }
}

async function sendChat(message) {
    if (!message || !chatMessages) return;
    appendChat('me', message);
    if (chatInput) chatInput.value = '';
    const loading = appendChat('bot typing', 'Mình đang kiểm tra dữ liệu...');
    try {
        const response = await fetch('/api/chatbot/message', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({message, context: chatContext})
        });
        const data = await response.json();
        updateChatContext(data);
        loading.className = 'bot';
        renderAnswer(loading, data.answer || data.message || 'Mình chưa nhận được phản hồi từ dữ liệu. Bạn thử hỏi lại giúp mình nhé.', data);
    } catch (error) {
        loading.className = 'bot';
        loading.textContent = 'Mình chưa kết nối được chatbot. Bạn thử lại sau một chút nhé.';
    }
}

function updateChatContext(data = {}) {
    if (!Array.isArray(data.items) || data.items.length === 0) {
        return;
    }
    const item = data.items.find(candidate => candidate && (candidate.materialId || candidate.materialCode));
    if (!item) {
        return;
    }
    chatContext = {
        materialId: item.materialId,
        materialCode: item.materialCode,
        batchCode: item.batchCode && item.batchCode !== '-' ? item.batchCode : undefined
    };
    saveChatContext(chatContext);
}

function loadChatContext() {
    try {
        const saved = window.sessionStorage?.getItem(chatContextKey);
        return saved ? JSON.parse(saved) : {};
    } catch (error) {
        return {};
    }
}

function saveChatContext(context) {
    try {
        window.sessionStorage?.setItem(chatContextKey, JSON.stringify(context || {}));
    } catch (error) {
        // Ignore storage failures; follow-up context still works for the current page.
    }
}

function appendChat(type, text) {
    const item = document.createElement('div');
    item.className = type;
    item.textContent = text;
    chatMessages.appendChild(item);
    chatMessages.scrollTop = chatMessages.scrollHeight;
    return item;
}

function renderAnswer(node, text, data = {}) {
    node.textContent = '';
    text.split('\n').forEach((line, index) => {
        if (index > 0) node.appendChild(document.createElement('br'));
        const linkMatch = line.match(/(\/[a-zA-Z0-9\-\/?=&]+)/);
        if (!linkMatch) {
            node.appendChild(document.createTextNode(line));
            return;
        }
        node.appendChild(document.createTextNode(line.slice(0, linkMatch.index)));
        const link = document.createElement('a');
        link.href = linkMatch[0];
        link.textContent = linkMatch[0];
        link.className = 'chat-link';
        node.appendChild(link);
        node.appendChild(document.createTextNode(line.slice(linkMatch.index + linkMatch[0].length)));
    });
    if (Array.isArray(data.items) && data.items.length > 0) {
        node.appendChild(renderChatItems(data.items));
    }
    if (Array.isArray(data.suggestions) && data.suggestions.length > 0) {
        node.appendChild(renderChatSuggestions(data.suggestions));
    }
}

function renderChatItems(items) {
    const visibleItems = items.slice(0, 8);
    const table = document.createElement('div');
    table.className = 'chat-result-table';
    const rows = visibleItems.map(item => `
        <tr>
            <td>${escapeHtml(item.materialName || '-')}<small>${escapeHtml(item.materialCode || '')}</small></td>
            <td>
                <strong>${escapeHtml(formatQuantity(item.availableQuantity, item.unit))}</strong>
                <small>Thực tế ${escapeHtml(formatQuantity(item.actualQuantity, item.unit))}</small>
                <small>Đang giữ ${escapeHtml(formatQuantity(item.reservedQuantity, item.unit))}</small>
            </td>
            <td>${escapeHtml(item.warehouseName || '-')}<small>${escapeHtml(item.locationName || '-')}</small></td>
            <td>
                ${escapeHtml(item.batchCode || '-')}
                <small>Nhập ${escapeHtml(item.importDate || '-')}</small>
                <small>HSD ${escapeHtml(item.expiryDate || '-')}${escapeHtml(expirySuffix(item.daysToExpiry))}</small>
            </td>
            <td>
                <span class="chat-status ${escapeHtml(statusClass(item.status))}">${escapeHtml(statusLabel(item.status))}</span>
                ${item.note ? `<small>${escapeHtml(item.note)}</small>` : ''}
            </td>
        </tr>
    `).join('');
    const summary = items.length > visibleItems.length
        ? `<div class="chat-result-summary">Hiển thị ${visibleItems.length}/${items.length} dòng phù hợp nhất. Hỏi rõ mã vật tư hoặc kho để lọc thêm.</div>`
        : `<div class="chat-result-summary">${items.length} dòng dữ liệu tồn kho được tìm thấy.</div>`;
    table.innerHTML = `
        ${summary}
        <table>
            <thead><tr><th>Vật tư</th><th>Tồn</th><th>Kho/vị trí</th><th>Lô/HSD</th><th>Trạng thái</th></tr></thead>
            <tbody>${rows}</tbody>
        </table>
    `;
    return table;
}

function renderChatSuggestions(suggestions) {
    const wrap = document.createElement('div');
    wrap.className = 'chat-suggestions';
    suggestions.slice(0, 4).forEach(text => {
        const button = document.createElement('button');
        button.type = 'button';
        button.textContent = text;
        button.addEventListener('click', () => sendChat(text));
        wrap.appendChild(button);
    });
    return wrap;
}

function formatQuantity(value, unit) {
    const number = Number.isFinite(Number(value)) ? Number(value).toLocaleString('vi-VN') : '-';
    return `${number} ${unit || ''}`.trim();
}

function statusLabel(status) {
    switch (status) {
        case 'AVAILABLE': return 'Còn';
        case 'LOW_STOCK': return 'Tồn thấp';
        case 'OUT_OF_STOCK': return 'Hết';
        case 'NEAR_EXPIRY': return 'Sắp HSD';
        case 'EXPIRED': return 'Hết HSD';
        case 'RESERVED': return 'Đã giữ';
        case 'PENDING': return 'Chờ xử lý';
        case 'BLOCKED': return 'Đang khóa';
        default: return status || '-';
    }
}

function statusClass(status) {
    return String(status || '').toLowerCase().replace(/[^a-z0-9_-]/g, '');
}

function expirySuffix(daysToExpiry) {
    if (daysToExpiry === null || daysToExpiry === undefined || daysToExpiry === '') {
        return '';
    }
    const days = Number(daysToExpiry);
    if (!Number.isFinite(days)) {
        return '';
    }
    if (days < 0) {
        return `, quá hạn ${Math.abs(days).toLocaleString('vi-VN')} ngày`;
    }
    if (days === 0) {
        return ', hết hạn hôm nay';
    }
    return `, còn ${days.toLocaleString('vi-VN')} ngày`;
}

function escapeHtml(value) {
    return String(value ?? '').replace(/[&<>"']/g, (char) => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#39;'
    }[char]));
}
