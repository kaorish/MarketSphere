<%@ page language="java" contentType="text/html;charset=UTF-8"
         pageEncoding="UTF-8" isELIgnored="false" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MarketSphere 一起聊</title>
    <style>
        /* 全局样式 */
        * {
            box-sizing: border-box;
        }

        html, body {
            height: 100%;
            margin: 0;
            padding: 0;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background-color: #f0f2f5;
            display: flex;
            flex-direction: column;
        }

        /* 主容器布局 */
        #container {
            display: flex;
            flex-direction: column;
            height: 100%;
            width: 100%;
        }

        /* 主内容区：聊天窗口 + 侧边栏 */
        #main {
            display: flex;
            flex: 1;
            padding: 10px;
            box-sizing: border-box;
            overflow: hidden; /* 确保内容不会溢出 */
        }

        /* 聊天窗口 */
        #chat-window {
            flex: 1;
            display: flex;
            flex-direction: column;
            padding: 15px;
            background-color: #ffffff;
            box-shadow: 0 0 10px rgba(0,0,0,0.1);
            border-radius: 8px;
            margin-right: 10px;
            box-sizing: border-box;
            /* 允许聊天内容区域滚动 */
            display: flex;
            flex-direction: column;
        }

        /* 消息列表 */
        #messages {
            flex: 1;
            overflow-y: auto;
            padding-right: 10px;
            font-size: 16px;
            display: flex;
            flex-direction: column;
            gap: 10px;
        }

        /* 单条消息容器 */
        .message-container {
            display: flex;
            flex-direction: column;
            max-width: 80%;
            /* 根据是否是当前用户，设置对齐方式 */
            align-self: flex-start; /* 默认消息在左侧 */
        }

        .message-container.self {
            align-self: flex-end; /* 当前用户消息在右侧 */
        }

        /* 消息头部：用户名和时间戳 */
        .message-header {
            display: flex;
            align-items: center;
            gap: 10px; /* 用户名和时间戳之间的间距 */
            margin-bottom: 5px; /* 与消息气泡的间距 */
        }

        .message-header .username {
            font-weight: bold;
            color: #28a745;
        }

        .message-container.self .message-header .username {
            color: #0056b3;
        }

        .message-header .timestamp {
            font-size: 12px;
            color: #6c757d; /* 调整为较柔和的颜色 */
            background-color: rgba(255, 255, 255, 0.8);
            padding: 2px 5px;
            border-radius: 4px;
        }

        /* 消息气泡 */
        .message {
            padding: 10px 15px;
            border-radius: 10px;
            background-color: #e1ffc7;
            box-shadow: 0 1px 3px rgba(0,0,0,0.1);
            word-wrap: break-word;
            /* 移除固定高度，确保高度自适应 */
            /* 确保气泡宽度根据内容自适应 */
            white-space: pre-wrap; /* 保留换行符 */
        }

        .message.self {
            background-color: #c7e1ff;
        }

        .message-content {
            color: #333333;
        }

        /* 侧边栏 */
        #sidebar {
            width: 250px;
            background-color: #343a40;
            padding: 20px;
            color: #ffffff;
            overflow-y: auto;
            border-radius: 8px;
            box-sizing: border-box;
            flex-shrink: 0; /* 防止在小屏幕下缩小 */
        }

        #sidebar h4 {
            margin-top: 0;
            color: #ffc107;
            text-align: center;
        }

        #sidebar ul {
            list-style-type: none;
            padding-left: 0;
            margin: 0;
        }

        #sidebar ul li {
            padding: 8px 0;
            border-bottom: 1px solid #495057;
            text-align: center;
        }

        /* 输入框区域 */
        #message-input {
            display: flex;
            justify-content: center;
            align-items: center;
            padding: 10px 20px;
            background-color: #ffffff;
            border-top: 1px solid #ccc;
            /* 固定在底部 */
            flex-shrink: 0;
        }

        #message-input textarea {
            flex: 1;
            resize: none;
            padding: 10px 15px;
            font-size: 16px;
            border-radius: 25px;
            border: 1px solid #ccc;
            margin-right: 10px;
            max-width: 100%;
            outline: none;
            box-sizing: border-box;
            transition: border-color 0.3s, box-shadow 0.3s;
            height: auto;
            min-height: 40px;
            overflow: hidden;
        }

        #message-input textarea:focus {
            border-color: #28a745;
            box-shadow: 0 0 5px rgba(40, 167, 69, 0.5);
        }

        #message-input button {
            padding: 10px 25px;
            border: none;
            background-color: #28a745;
            color: white;
            border-radius: 25px;
            cursor: pointer;
            font-size: 16px;
            transition: background-color 0.3s, transform 0.2s;
            display: flex;
            align-items: center;
            gap: 5px;
        }

        #message-input button:hover {
            background-color: #218838;
            transform: scale(1.05);
        }

        /* 滚动条样式 */
        #messages::-webkit-scrollbar {
            width: 8px;
        }

        #messages::-webkit-scrollbar-track {
            background: #f1f1f1;
            border-radius: 4px;
        }

        #messages::-webkit-scrollbar-thumb {
            background: #ccc;
            border-radius: 4px;
        }

        #messages::-webkit-scrollbar-thumb:hover {
            background: #999;
        }

        /* 响应式调整 */
        @media (max-width: 768px) {
            #sidebar {
                display: none; /* 移动端隐藏侧边栏 */
            }

            #main {
                padding: 5px;
            }

            #chat-window {
                margin-right: 0;
                padding: 10px;
            }

            #message-input textarea {
                max-width: 100%;
            }

            .message-container {
                max-width: 90%;
            }
        }
    </style>
</head>
<body onload="init()">
<div id="container">
    <!-- 主内容区：聊天窗口 + 侧边栏 -->
    <div id="main">
        <!-- 聊天窗口 -->
        <div id="chat-window">
            <div id="messages"></div>
        </div>
        <!-- 侧边栏 -->
        <div id="sidebar">
            <div id="online-users">
                <h4>在线人数: 0</h4>
                <ul></ul>
            </div>
        </div>
    </div>
    <!-- 输入框区域 -->
    <div id="message-input">
        <textarea id="message" placeholder="输入消息..." rows="1"></textarea>
        <button onclick="sendMessage()">发送</button>
    </div>
</div>

<script>
    // 当前登录的用户名（需要从后端传递，如果有）
    // 示例中暂时未实现，所有消息左对齐
    // 若需要区分当前用户，可以在后端传递当前用户名，并在渲染时添加 .self 类

    // 假设有一个变量 currentUser 存储当前用户名
    const currentUser = '当前用户'; // 需要从后端传递

    // 初始化函数
    function init() {
        console.log('Initializing chat...');
        updateSidebar();
        updateMessages();
        setInterval(updateSidebar, 5000); // 每 5 秒更新在线用户
        setInterval(updateMessages, 3000); // 每 3 秒更新消息
        bindEnterKey();
        window.addEventListener('beforeunload', notifyLeaveChat); // 离开聊天室时通知后端
    }

    // 更新在线用户列表
    function updateSidebar() {
        fetch('/foregetOnlineUsers')
            .then(response => response.json())
            .then(data => {
                const onlineUsersContainer = document.getElementById('online-users');
                let userListHtml = '<h4>在线人数: ' + data.onlineCount + '</h4><ul>';
                data.users.forEach(user => {
                    userListHtml += '<li>' + sanitizeHTML(user) + '</li>';
                });
                userListHtml += '</ul>';
                onlineUsersContainer.innerHTML = userListHtml;
            })
            .catch(err => console.error('Error fetching online users:', err));
    }

    // 更新聊天消息
    function updateMessages() {
        fetch('/foregetMessages')
            .then(response => response.json())
            .then(data => {
                const messagesContainer = document.getElementById('messages');
                messagesContainer.innerHTML = ''; // 清空之前的内容

                data.messages.forEach(msg => {
                    const username = sanitizeHTML(msg.username) || '未知用户';
                    const content = sanitizeHTML(msg.content) || '(无内容)';
                    const formattedTimestamp = formatTimestamp(msg.timestamp) || '';

                    // **调试输出**
                    console.log('Received message:', msg);
                    console.log('Formatted timestamp:', formattedTimestamp);

                    // 创建单条消息的容器
                    const messageContainer = document.createElement('div');
                    messageContainer.classList.add('message-container');

                    // 根据是否是当前用户，添加 .self 类
                    if (username === currentUser) {
                        messageContainer.classList.add('self');
                    }

                    // 消息头部：用户名和时间戳
                    const messageHeader = document.createElement('div');
                    messageHeader.classList.add('message-header');

                    // 用户名
                    const userElement = document.createElement('div');
                    userElement.classList.add('username');
                    userElement.textContent = username;
                    messageHeader.appendChild(userElement);

                    // 时间戳
                    const timeElement = document.createElement('div');
                    timeElement.classList.add('timestamp');
                    timeElement.textContent = formattedTimestamp; // 直接设置格式化后的时间
                    console.log('Appending formatted timestamp:', formattedTimestamp);
                    messageHeader.appendChild(timeElement);

                    messageContainer.appendChild(messageHeader);

                    // 消息内容气泡
                    const messageBubble = document.createElement('div');
                    messageBubble.classList.add('message');

                    const contentElement = document.createElement('div');
                    contentElement.classList.add('message-content');
                    contentElement.textContent = content;
                    messageBubble.appendChild(contentElement);

                    messageContainer.appendChild(messageBubble);

                    messagesContainer.appendChild(messageContainer);

                    // **调试输出**
                    console.log('Appended messageContainer:', messageContainer);
                });

                // 自动滚动到底部
                messagesContainer.scrollTop = messagesContainer.scrollHeight;
            })
            .catch(err => {
                console.error('Error fetching messages:', err);
            });
    }

    // 格式化时间戳（假设后端返回的是ISO格式或标准日期格式）
    function formatTimestamp(timestamp) {
        const date = new Date(timestamp);
        if (isNaN(date.getTime())) {
            console.log('Invalid date:', timestamp);
            return 'Invalid Date';
        }

        // 定义格式化选项
        const options = {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            hour12: false, // 使用24小时制
        };

        // 使用 'zh-CN' 区域设置进行格式化
        const formatted = date.toLocaleString('zh-CN', options).replace(/\//g, '-');
        return formatted;
    }

    // 发送消息
    function sendMessage() {
        const messageInput = document.getElementById('message');
        const message = messageInput.value.trim();
        if (message) {
            fetch('/foresendMessage', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' },
                body: 'content=' + encodeURIComponent(message)
            })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        messageInput.value = '';
                        messageInput.style.height = '40px'; // 重置高度
                        updateMessages();
                        updateSidebar(); // 可选：发送消息后更新在线用户
                    } else {
                        alert(data.message || '发送失败');
                    }
                })
                .catch(err => console.error('Error sending message:', err));
        }
    }

    // 绑定回车键发送消息
    function bindEnterKey() {
        const messageInput = document.getElementById('message');
        messageInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                sendMessage();
            }
        });

        // 自动调整textarea高度
        messageInput.addEventListener('input', autoResizeTextarea);
    }

    // 自动调整textarea高度
    function autoResizeTextarea() {
        const textarea = this;
        textarea.style.height = '40px'; // 重置高度
        textarea.style.height = (textarea.scrollHeight) + 'px';
    }

    // 离开聊天室时通知后端
    function notifyLeaveChat() {
        navigator.sendBeacon('/foreleaveChat');
    }

    // 安全处理 HTML，防止 XSS
    function sanitizeHTML(str) {
        if (!str) return '';
        return str
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    }
</script>
</body>
</html>
