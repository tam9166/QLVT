const chatToggle = document.getElementById('chatToggle');
const chatPanel = document.getElementById('chatPanel');
const chatForm = document.getElementById('chatForm');
const chatInput = document.getElementById('chatInput');
const chatMessages = document.getElementById('chatMessages');
const quickActions = document.querySelectorAll('[data-chat-message]');
const sidebarNav = document.getElementById('sidebarNav');
const sidebarBackdrop = document.getElementById('sidebarBackdrop');

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
        const response = await fetch('/api/chatbot', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({message})
        });
        const data = await response.json();
        loading.className = 'bot';
        renderAnswer(loading, data.answer || 'Mình chưa nhận được phản hồi từ dữ liệu. Bạn thử hỏi lại giúp mình nhé.');
    } catch (error) {
        loading.className = 'bot';
        loading.textContent = 'Mình chưa kết nối được chatbot. Bạn thử lại sau một chút nhé.';
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

function renderAnswer(node, text) {
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
}
