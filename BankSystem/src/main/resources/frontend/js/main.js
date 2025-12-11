// main.js - 数字银行前端交互逻辑（基于现代化界面设计）

// API基础配置
const API_CONFIG = {
    BASE_URL: 'http://localhost:8099/api',
    TIMEOUT: 10000,
    HEADERS: {
        'Content-Type': 'application/json'
    }
};

// 全局状态管理
let currentUser = null;
let authToken = localStorage.getItem('bank_auth_token') || '';
let accounts = [];
let transactions = [];

// DOM加载完成后初始化
document.addEventListener('DOMContentLoaded', function() {
    injectGlobalStyles();
    initApplication();
    setupEventListeners();

    // 检查是否有token，自动跳转到主界面
    if (authToken) {
        checkAuthAndRedirect();
    }
});

// 注入全局样式
function injectGlobalStyles() {
    const styleElement = document.createElement('style');
    styleElement.textContent = getGlobalStyles();
    document.head.appendChild(styleElement);
}

// 获取全局样式
function getGlobalStyles() {
    return `
        :root {
            --primary-color: #3b82f6;
            --primary-light: #60a5fa;
            --primary-dark: #2563eb;
            --secondary-color: #10b981;
            --danger-color: #ef4444;
            --warning-color: #f59e0b;
            --gray-color: #6b7280;
            --gray-light: #e5e7eb;
            --dark-color: #1f2937;
            --radius: 12px;
            --shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
            --transition: all 0.3s ease;
        }

        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            font-family: 'Inter', system-ui, -apple-system, sans-serif;
        }

        body {
            background-color: #f9fafb;
            color: var(--dark-color);
            line-height: 1.6;
        }

        .container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 0 20px;
        }

        /* 登录/注册样式 */
        #login-container {
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            padding: 20px;
        }

        .auth-card {
            background: white;
            border-radius: var(--radius);
            box-shadow: var(--shadow);
            width: 100%;
            max-width: 500px;
            padding: 40px;
        }

        .auth-header {
            text-align: center;
            margin-bottom: 30px;
        }

        .auth-header h2 {
            font-size: 1.8rem;
            font-weight: 700;
            margin-bottom: 10px;
        }

        .auth-header p {
            color: var(--gray-color);
        }

        .form-group {
            margin-bottom: 20px;
        }

        .form-group label {
            display: block;
            margin-bottom: 8px;
            font-weight: 500;
        }

        .form-control {
            width: 100%;
            padding: 12px 15px;
            border: 2px solid var(--gray-light);
            border-radius: 8px;
            font-size: 1rem;
            transition: var(--transition);
        }

        .form-control:focus {
            outline: none;
            border-color: var(--primary-light);
            box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
        }

        .password-input {
            position: relative;
        }

        .password-toggle {
            position: absolute;
            right: 15px;
            top: 50%;
            transform: translateY(-50%);
            cursor: pointer;
            color: var(--gray-color);
        }

        .btn {
            width: 100%;
            padding: 12px;
            border: none;
            border-radius: 8px;
            font-size: 1rem;
            font-weight: 600;
            cursor: pointer;
            transition: var(--transition);
        }

        .btn-primary {
            background: linear-gradient(to right, var(--primary-color), var(--primary-light));
            color: white;
        }

        .btn-primary:hover {
            background: linear-gradient(to right, var(--primary-dark), var(--primary-color));
        }

        .btn-secondary {
            background: white;
            border: 2px solid var(--gray-light);
            color: var(--dark-color);
        }

        .btn-secondary:hover {
            border-color: var(--gray-color);
        }

        .auth-footer {
            text-align: center;
            margin-top: 20px;
        }

        .auth-footer a {
            color: var(--primary-color);
            text-decoration: none;
            font-weight: 500;
        }

        .auth-footer a:hover {
            text-decoration: underline;
        }

        /* 主应用样式 */
        #app-container {
            display: none;
            min-height: 100vh;
        }

        .app-header {
            background: white;
            box-shadow: var(--shadow);
            padding: 15px 0;
            position: sticky;
            top: 0;
            z-index: 100;
        }

        .header-content {
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .logo {
            display: flex;
            align-items: center;
            gap: 10px;
            font-size: 1.5rem;
            font-weight: 700;
            color: var(--primary-color);
        }

        .logo i {
            font-size: 1.8rem;
        }

        .user-menu {
            display: flex;
            align-items: center;
            gap: 20px;
        }

        .user-info {
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .user-avatar {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            background-color: var(--primary-light);
            color: white;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: 600;
        }

        .logout-btn {
            background: transparent;
            border: none;
            color: var(--gray-color);
            cursor: pointer;
            display: flex;
            align-items: center;
            gap: 5px;
        }

        .logout-btn:hover {
            color: var(--danger-color);
        }

        .app-body {
            display: flex;
            padding: 20px 0;
            gap: 20px;
        }

        .sidebar {
            width: 250px;
            background: white;
            border-radius: var(--radius);
            box-shadow: var(--shadow);
            padding: 20px;
            height: calc(100vh - 120px);
            position: sticky;
            top: 80px;
        }

        .nav-menu {
            list-style: none;
        }

        .nav-item {
            margin-bottom: 5px;
        }

        .nav-link {
            display: flex;
            align-items: center;
            gap: 10px;
            padding: 12px 15px;
            border-radius: 8px;
            color: var(--gray-color);
            text-decoration: none;
            transition: var(--transition);
        }

        .nav-link:hover, .nav-link.active {
            background-color: var(--primary-color);
            color: white;
        }

        .main-content {
            flex: 1;
            background: white;
            border-radius: var(--radius);
            box-shadow: var(--shadow);
            padding: 25px;
            min-height: calc(100vh - 120px);
        }

        /* 通知样式 */
        #notification {
            position: fixed;
            top: 20px;
            right: 20px;
            background: white;
            border-radius: var(--radius);
            box-shadow: var(--shadow);
            padding: 15px 20px;
            display: flex;
            align-items: center;
            gap: 10px;
            z-index: 1000;
            transform: translateX(120%);
            transition: transform 0.3s ease;
            max-width: 400px;
        }

        #notification.show {
            transform: translateX(0);
        }

        #notification.success {
            border-left: 4px solid var(--secondary-color);
        }

        #notification.error {
            border-left: 4px solid var(--danger-color);
        }

        #notification.info {
            border-left: 4px solid var(--primary-color);
        }

        #notification i {
            font-size: 1.2rem;
        }

        #notification.success i {
            color: var(--secondary-color);
        }

        #notification.error i {
            color: var(--danger-color);
        }

        #notification.info i {
            color: var(--primary-color);
        }

        /* 加载样式 */
        .loading-container {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            padding: 50px 0;
        }

        .loading-spinner {
            width: 50px;
            height: 50px;
            border: 5px solid var(--gray-light);
            border-top: 5px solid var(--primary-color);
            border-radius: 50%;
            animation: spin 1s linear infinite;
            margin-bottom: 15px;
        }

        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }

        /* 卡片样式 */
        .cards-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }

        .card {
            background: white;
            border-radius: var(--radius);
            box-shadow: var(--shadow);
            overflow: hidden;
        }

        .card-header {
            padding: 20px;
            border-bottom: 1px solid var(--gray-light);
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .card-header h3 {
            font-size: 1.1rem;
            font-weight: 600;
        }

        .card-content {
            padding: 20px;
        }

        .card-footer {
            padding: 15px 20px;
            border-top: 1px solid var(--gray-light);
        }

        .card-link {
            color: var(--primary-color);
            text-decoration: none;
            font-weight: 500;
            display: flex;
            align-items: center;
            gap: 5px;
        }

        .card-link:hover {
            text-decoration: underline;
        }

        .amount {
            font-size: 1.8rem;
            font-weight: 700;
            margin: 10px 0;
        }

        .change {
            display: flex;
            align-items: center;
            gap: 5px;
            font-size: 0.9rem;
        }

        .change.positive {
            color: var(--secondary-color);
        }

        .change.negative {
            color: var(--danger-color);
        }

        .progress-bar {
            height: 8px;
            background-color: var(--gray-light);
            border-radius: 4px;
            margin: 10px 0;
            overflow: hidden;
        }

        .progress-fill {
            height: 100%;
            background-color: var(--primary-color);
            border-radius: 4px;
        }

        .progress-text {
            font-size: 0.85rem;
            color: var(--gray-color);
        }

        /* 图表样式 */
        .chart-container {
            margin-top: 30px;
        }

        .chart-card {
            height: 400px;
        }

        .chart-container-inner {
            height: calc(100% - 60px);
            padding: 10px;
        }

        .chart-select {
            padding: 5px 10px;
            border: 1px solid var(--gray-light);
            border-radius: 4px;
            background: white;
            cursor: pointer;
        }

        /* 交易列表样式 */
        .transactions-list {
            margin-bottom: 30px;
        }

        .section-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
        }

        .section-header h3 {
            font-size: 1.2rem;
            font-weight: 600;
        }

        .view-all {
            color: var(--primary-color);
            text-decoration: none;
            font-weight: 500;
        }

        .view-all:hover {
            text-decoration: underline;
        }

        .transaction-item {
            display: flex;
            align-items: center;
            gap: 15px;
            padding: 15px;
            border-radius: 8px;
            background-color: #f9fafb;
            margin-bottom: 10px;
        }

        .transaction-icon {
            width: 40px;
            height: 40px;
            border-radius: 8px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.2rem;
        }

        .transaction-icon.deposit {
            background-color: rgba(16, 185, 129, 0.1);
            color: var(--secondary-color);
        }

        .transaction-icon.withdraw {
            background-color: rgba(239, 68, 68, 0.1);
            color: var(--danger-color);
        }

        .transaction-icon.transfer {
            background-color: rgba(59, 130, 246, 0.1);
            color: var(--primary-color);
        }

        .transaction-details {
            flex: 1;
        }

        .transaction-details h4 {
            font-weight: 600;
            margin-bottom: 3px;
        }

        .transaction-desc, .transaction-time {
            font-size: 0.85rem;
            color: var(--gray-color);
        }

        .transaction-amount {
            font-weight: 600;
            font-size: 1.1rem;
        }

        .transaction-amount.positive {
            color: var(--secondary-color);
        }

        .transaction-amount.negative {
            color: var(--danger-color);
        }

        /* 页面通用样式 */
        .page-header {
            margin-bottom: 30px;
        }

        .page-header h1 {
            font-size: 1.8rem;
            font-weight: 700;
            margin-bottom: 10px;
        }

        .page-header p {
            color: var(--gray-color);
        }

        /* 表单样式 */
        .form-row {
            display: flex;
            gap: 20px;
            margin-bottom: 20px;
        }

        .form-group.half {
            flex: 1;
        }

        .input-with-suffix {
            position: relative;
        }

        .input-suffix {
            position: absolute;
            right: 15px;
            top: 50%;
            transform: translateY(-50%);
            color: var(--gray-color);
        }

        /* 表格样式 */
        .table-responsive {
            overflow-x: auto;
        }

        .transactions-table {
            width: 100%;
            border-collapse: collapse;
        }

        .transactions-table th {
            background-color: #f8fafc;
            padding: 15px;
            text-align: left;
            font-weight: 600;
            color: var(--gray-color);
            border-bottom: 2px solid var(--gray-light);
        }

        .transactions-table td {
            padding: 15px;
            border-bottom: 1px solid var(--gray-light);
            vertical-align: top;
        }

        .transactions-table tbody tr:hover {
            background-color: #f8fafc;
        }

        .transaction-type-badge {
            display: inline-block;
            padding: 4px 8px;
            border-radius: 4px;
            font-size: 0.8rem;
            font-weight: 500;
        }

        .transaction-type-badge.deposit {
            background-color: rgba(16, 185, 129, 0.1);
            color: var(--secondary-color);
        }

        .transaction-type-badge.withdraw {
            background-color: rgba(239, 68, 68, 0.1);
            color: var(--danger-color);
        }

        .transaction-type-badge.transfer {
            background-color: rgba(59, 130, 246, 0.1);
            color: var(--primary-light);
        }

        .status-badge {
            display: inline-block;
            padding: 4px 8px;
            border-radius: 4px;
            font-size: 0.8rem;
            font-weight: 500;
        }

        .status-badge.success {
            background-color: var(--secondary-color);
            color: white;
        }

        .status-badge.pending {
            background-color: var(--warning-color);
            color: white;
        }

        .status-badge.failed {
            background-color: var(--danger-color);
            color: white;
        }

        .text-success {
            color: var(--secondary-color);
        }

        .text-danger {
            color: var(--danger-color);
        }

        .text-center {
            text-align: center;
        }

        /* 分页样式 */
        .pagination {
            display: flex;
            justify-content: center;
            align-items: center;
            gap: 10px;
            margin-top: 20px;
        }

        .page-btn {
            width: 40px;
            height: 40px;
            border: 1px solid var(--gray-light);
            background: white;
            border-radius: 6px;
            cursor: pointer;
            transition: var(--transition);
        }

        .page-btn:hover:not(:disabled) {
            border-color: var(--primary-color);
            color: var(--primary-color);
        }

        .page-btn.active {
            background-color: var(--primary-color);
            color: white;
            border-color: var(--primary-color);
        }

        .page-btn.disabled {
            opacity: 0.5;
            cursor: not-allowed;
        }

        /* 响应式样式 */
        @media (max-width: 768px) {
            .app-body {
                flex-direction: column;
            }

            .sidebar {
                width: 100%;
                height: auto;
                position: static;
            }

            .cards-grid {
                grid-template-columns: 1fr;
            }

            .form-row {
                flex-direction: column;
                gap: 15px;
            }
        }
    `;
}

// 初始化应用
function initApplication() {
    // 密码显示/隐藏切换
    document.querySelectorAll('.password-toggle').forEach(toggle => {
        toggle.addEventListener('click', function() {
            const targetId = this.getAttribute('data-target');
            const passwordInput = document.getElementById(targetId);
            if (passwordInput) {
                if (passwordInput.type === 'password') {
                    passwordInput.type = 'text';
                    this.classList.remove('fa-eye');
                    this.classList.add('fa-eye-slash');
                } else {
                    passwordInput.type = 'password';
                    this.classList.remove('fa-eye-slash');
                    this.classList.add('fa-eye');
                }
            }
        });
    });
}

// 设置事件监听器
function setupEventListeners() {
    // 登录按钮
    const loginBtn = document.getElementById('login-btn');
    if (loginBtn) {
        loginBtn.addEventListener('click', handleLogin);
    }

    // 注册按钮
    const registerBtn = document.getElementById('register-btn');
    if (registerBtn) {
        registerBtn.addEventListener('click', handleRegister);
    }

    // 登录/注册表单切换
    const signupLink = document.getElementById('signup-link');
    const loginLink = document.getElementById('login-link');

    if (signupLink) {
        signupLink.addEventListener('click', function(e) {
            e.preventDefault();
            document.getElementById('login-form').style.display = 'none';
            document.getElementById('register-form').style.display = 'block';
        });
    }

    if (loginLink) {
        loginLink.addEventListener('click', function(e) {
            e.preventDefault();
            document.getElementById('register-form').style.display = 'none';
            document.getElementById('login-form').style.display = 'block';
        });
    }

    // 登出按钮
    const logoutBtn = document.getElementById('logout-btn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', handleLogout);
    }

    // 导航菜单点击
    document.querySelectorAll('.nav-link').forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const page = this.getAttribute('data-page');
            loadPage(page);

            // 更新活动状态
            document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));
            this.classList.add('active');
        });
    });

    // 快捷操作按钮
    const quickDepositBtn = document.getElementById('quick-deposit');
    const quickTransferBtn = document.getElementById('quick-transfer');
    const quickCreditBtn = document.getElementById('quick-credit');

    if (quickDepositBtn) {
        quickDepositBtn.addEventListener('click', () => showQuickDeposit());
    }

    if (quickTransferBtn) {
        quickTransferBtn.addEventListener('click', () => loadPage('transfer'));
    }

    if (quickCreditBtn) {
        quickCreditBtn.addEventListener('click', () => applyForCreditCard());
    }
}

// ==================== API接口封装 ====================

// 通用API请求方法
async function apiRequest(endpoint, method = 'GET', data = null, requiresAuth = true) {
    const url = `${API_CONFIG.BASE_URL}${endpoint}`;
    const options = {
        method,
        headers: {
            ...API_CONFIG.HEADERS
        }
    };

    // 添加认证token
    if (requiresAuth && authToken) {
        options.headers['Authorization'] = `Bearer ${authToken}`;
    }

    // 添加请求体
    if (data && (method === 'POST' || method === 'PUT')) {
        options.body = JSON.stringify(data);
    }

    try {
        const response = await fetch(url, options);

        // 检查响应状态
        if (!response.ok) {
            let errorMessage = '请求失败';
            try {
                const errorData = await response.json();
                errorMessage = errorData.message || errorData.error || errorMessage;
            } catch (e) {
                errorMessage = `HTTP ${response.status} ${response.statusText}`;
            }
            throw new Error(errorMessage);
        }

        const result = await response.json();
        return result;

    } catch (error) {
        console.error('API请求错误:', error);
        showNotification(error.message || '网络错误，请稍后重试', 'error');
        throw error;
    }
}

// ==================== 用户认证相关接口 ====================

// 1. 用户登录
async function loginUser(credentials) {
    return await apiRequest('/auth/login', 'POST', credentials, false);
}

// 2. 用户注册
async function registerUser(userData) {
    return await apiRequest('/auth/register', 'POST', userData, false);
}

// 3. 获取当前用户信息
async function getCurrentUser() {
    return await apiRequest('/users/me', 'GET');
}

// 4. 修改密码
async function changePassword(passwordData) {
    return await apiRequest('/users/change-password', 'POST', passwordData);
}

// 5. 忘记密码 - 发送重置邮件
async function forgotPassword(email) {
    return await apiRequest('/auth/forgot-password', 'POST', { email }, false);
}

// 6. 重置密码
async function resetPassword(resetData) {
    return await apiRequest('/auth/reset-password', 'POST', resetData, false);
}

// ==================== 账户相关接口 ====================

// 7. 获取用户所有账户
async function getUserAccounts() {
    return await apiRequest('/accounts');
}

// 8. 获取账户详情
async function getAccountDetails(accountId) {
    return await apiRequest(`/accounts/${accountId}`);
}

// 9. 创建新账户
async function createAccount(accountData) {
    return await apiRequest('/accounts', 'POST', accountData);
}

// 10. 关闭账户
async function closeAccount(accountId) {
    return await apiRequest(`/accounts/${accountId}/close`, 'POST');
}

// ==================== 交易相关接口 ====================

// 11. 存款
async function deposit(accountId, amount, description = '存款') {
    return await apiRequest('/transactions/deposit', 'POST', {
        accountId,
        amount,
        description
    });
}

// 12. 取款
async function withdraw(accountId, amount, description = '取款') {
    return await apiRequest('/transactions/withdraw', 'POST', {
        accountId,
        amount,
        description
    });
}

// 13. 转账
async function transfer(transferData) {
    return await apiRequest('/transactions/transfer', 'POST', transferData);
}

// 14. 获取交易历史
async function getTransactionHistory(filters = {}) {
    const queryParams = new URLSearchParams(filters).toString();
    return await apiRequest(`/transactions/history${queryParams ? '?' + queryParams : ''}`);
}

// 15. 获取交易详情
async function getTransactionDetail(transactionId) {
    return await apiRequest(`/transactions/${transactionId}`);
}

// ==================== 账单相关接口 ====================

// 16. 获取月度账单
async function getMonthlyStatement(year, month) {
    return await apiRequest(`/statements/monthly?year=${year}&month=${month}`);
}

// 17. 生成对账单PDF
async function generateStatementPDF(statementData) {
    return await apiRequest('/statements/generate-pdf', 'POST', statementData);
}

// ==================== 信用卡相关接口 ====================

// 18. 申请信用卡
async function applyCreditCard(applicationData) {
    return await apiRequest('/credit-cards/apply', 'POST', applicationData);
}

// 19. 获取我的信用卡列表
async function getMyCreditCards() {
    return await apiRequest('/credit-cards/my');
}

// 20. 信用卡还款
async function creditCardRepayment(cardId, amount) {
    return await apiRequest(`/credit-cards/${cardId}/repay`, 'POST', { amount });
}

// ==================== 通知相关接口 ====================

// 21. 获取未读通知
async function getUnreadNotifications() {
    return await apiRequest('/notifications/unread');
}

// 22. 标记通知为已读
async function markNotificationAsRead(notificationId) {
    return await apiRequest(`/notifications/${notificationId}/read`, 'POST');
}

// ==================== 业务逻辑处理 ====================

// 处理登录
async function handleLogin() {
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value;
    const rememberMe = document.getElementById('remember-me').checked;

    // 前端简单验证
    if (!username || !password) {
        showNotification('请输入用户名和密码', 'error');
        return;
    }

    try {
        showLoading(true);

        const response = await loginUser({
            username,
            password
        });

        if (response.success && response.data) {
            authToken = response.data.token;
            currentUser = response.data.user;

            // 存储token
            if (rememberMe) {
                localStorage.setItem('bank_auth_token', authToken);
                localStorage.setItem('bank_user_data', JSON.stringify(currentUser));
            } else {
                sessionStorage.setItem('bank_auth_token', authToken);
            }

            showNotification('登录成功！正在跳转...', 'success');

            // 延迟跳转
            setTimeout(() => {
                switchToAppView();
            }, 1500);
        }
    } catch (error) {
        showNotification(error.message || '登录失败，请检查用户名和密码', 'error');
    } finally {
        showLoading(false);
    }
}

// 处理注册
async function handleRegister() {
    const fullname = document.getElementById('register-fullname').value.trim();
    const username = document.getElementById('register-username').value.trim();
    const email = document.getElementById('register-email').value.trim();
    const phone = document.getElementById('register-phone').value.trim();
    const password = document.getElementById('register-password').value;
    const confirmPassword = document.getElementById('register-confirm-password').value;
    const terms = document.getElementById('register-terms').checked;

    // 前端简单验证
    if (!fullname || !username || !email || !phone || !password || !confirmPassword) {
        showNotification('请填写所有必填字段', 'error');
        return;
    }

    if (password !== confirmPassword) {
        showNotification('两次输入的密码不一致', 'error');
        return;
    }

    if (!terms) {
        showNotification('请同意服务协议和隐私政策', 'error');
        return;
    }

    try {
        showLoading(true);

        const response = await registerUser({
            fullname,
            username,
            email,
            phone,
            password
        });

        if (response.success) {
            showNotification('注册成功！请登录', 'success');

            // 切换到登录表单
            setTimeout(() => {
                document.getElementById('register-form').style.display = 'none';
                document.getElementById('login-form').style.display = 'block';
                clearRegisterForm();
            }, 2000);
        }
    } catch (error) {
        showNotification(error.message || '注册失败，请稍后重试', 'error');
    } finally {
        showLoading(false);
    }
}

// 检查认证并跳转
async function checkAuthAndRedirect() {
    try {
        const response = await getCurrentUser();
        if (response.success && response.data) {
            currentUser = response.data;
            switchToAppView();
        } else {
            // Token无效，清除本地存储
            clearAuthData();
        }
    } catch (error) {
        console.error('认证检查失败:', error);
        clearAuthData();
    }
}

// 处理登出
async function handleLogout() {
    try {
        // 调用后端登出接口（如果需要）
        // await apiRequest('/auth/logout', 'POST');

        clearAuthData();
        showNotification('已安全退出', 'success');
        switchToLoginView();
    } catch (error) {
        console.error('登出失败:', error);
        clearAuthData();
        switchToLoginView();
    }
}

// ==================== 页面加载和导航 ====================

// 加载页面内容
async function loadPage(pageName) {
    const pageContent = document.getElementById('page-content');

    switch (pageName) {
        case 'dashboard':
            await loadDashboard(pageContent);
            break;
        case 'accounts':
            await loadAccountsPage(pageContent);
            break;
        case 'transfer':
            await loadTransferPage(pageContent);
            break;
        case 'history':
            await loadHistoryPage(pageContent);
            break;
        default:
            await loadDashboard(pageContent);
    }
}

// 加载仪表板页面
async function loadDashboard(container) {
    const currentDate = new Date();
    const formattedDate = currentDate.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        weekday: 'long'
    });

    container.innerHTML = `
        <div class="dashboard-header">
            <div>
                <h2>欢迎回来, ${currentUser?.fullname || '用户'}</h2>
                <p>${formattedDate}</p>
            </div>
        </div>

        <div class="cards-grid">
            <div class="card">
                <div class="card-header">
                    <h3>总资产</h3>
                    <i class="fas fa-coins"></i>
                </div>
                <div class="card-content">
                    <div class="amount" id="total-assets">¥0.00</div>
                    <div class="change positive">
                        <i class="fas fa-arrow-up"></i>
                        较上月 +5.2%
                    </div>
                </div>
                <div class="card-footer">
                    <a href="#" class="card-link" onclick="loadPage('accounts')">
                        查看详情
                        <i class="fas fa-chevron-right"></i>
                    </a>
                </div>
            </div>

            <div class="card">
                <div class="card-header">
                    <h3>本月收入</h3>
                    <i class="fas fa-arrow-down"></i>
                </div>
                <div class="card-content">
                    <div class="amount" id="month-income">¥0.00</div>
                    <div class="progress-bar">
                        <div class="progress-fill" style="width: 65%"></div>
                    </div>
                    <div class="progress-text">预算使用: 65%</div>
                </div>
            </div>

            <div class="card">
                <div class="card-header">
                    <h3>本月支出</h3>
                    <i class="fas fa-arrow-up"></i>
                </div>
                <div class="card-content">
                    <div class="amount" id="month-expense">¥0.00</div>
                    <div class="progress-bar">
                        <div class="progress-fill" style="width: 45%"></div>
                    </div>
                    <div class="progress-text">预算使用: 45%</div>
                </div>
            </div>

            <div class="card">
                <div class="card-header">
                    <h3>信用卡额度</h3>
                    <i class="far fa-credit-card"></i>
                </div>
                <div class="card-content">
                    <div class="amount">¥50,000.00</div>
                    <div class="progress-bar">
                        <div class="progress-fill" style="width: 35%"></div>
                    </div>
                    <div class="progress-text">已用额度 ¥17,500</div>
                </div>
            </div>
        </div>

        <div class="cards-grid">
            <div class="card">
                <div class="card-header">
                    <h3>我的账户</h3>
                    <a href="#" class="card-link" onclick="loadPage('accounts')">查看全部</a>
                </div>
                <div class="card-content">
                    <div id="accounts-list">加载账户信息...</div>
                </div>
            </div>

            <div class="card">
                <div class="card-header">
                    <h3>最近交易</h3>
                    <a href="#" class="card-link" onclick="loadPage('history')">查看全部</a>
                </div>
                <div class="card-content">
                    <div id="recent-transactions">加载交易记录...</div>
                </div>
            </div>
        </div>

        <div class="chart-container">
            <div class="card chart-card">
                <div class="card-header">
                    <h3>支出统计</h3>
                    <select id="chart-period" class="chart-select">
                        <option value="week">本周</option>
                        <option value="month">本月</option>
                        <option value="year">本年</option>
                    </select>
                </div>
                <div class="chart-container-inner">
                    <canvas id="expense-chart"></canvas>
                </div>
            </div>
        </div>
    `;

    // 加载数据
    await loadDashboardData();

    // 初始化图表
    initializeCharts();
}

// 加载账户页面
async function loadAccountsPage(container) {
    container.innerHTML = `
        <div class="page-header">
            <div>
                <h1>我的账户</h1>
                <p>管理您的银行账户和查看账户详情</p>
            </div>
            <button class="btn btn-primary" id="add-account-btn">
                <i class="fas fa-plus"></i> 添加账户
            </button>
        </div>

        <div class="cards-grid" id="accounts-container">
            <div class="loading-container">
                <div class="loading-spinner"></div>
                <p>正在加载账户信息...</p>
            </div>
        </div>
    `;

    // 加载账户数据
    await loadAccountsData();
}

// 加载转账页面
async function loadTransferPage(container) {
    container.innerHTML = `
        <div class="page-header">
            <h1>转账汇款</h1>
            <p>向其他账户转账或汇款</p>
        </div>

        <div class="transfer-container">
            <div class="transfer-form-container">
                <div class="card">
                    <div class="card-header">
                        <h3>转账信息</h3>
                    </div>
                    <div class="card-content">
                        <form id="transfer-form">
                            <div class="form-group">
                                <label for="from-account">转出账户</label>
                                <select id="from-account" class="form-control" required>
                                    <option value="">请选择账户</option>
                                </select>
                            </div>

                            <div class="form-group">
                                <label for="to-account-type">收款方类型</label>
                                <select id="to-account-type" class="form-control">
                                    <option value="same-bank">本行账户</option>
                                    <option value="other-bank">他行账户</option>
                                </select>
                            </div>

                            <div class="form-group">
                                <label for="to-account">收款账号</label>
                                <input type="text" id="to-account" class="form-control" placeholder="请输入收款账号" required>
                            </div>

                            <div class="form-group">
                                <label for="to-name">收款人姓名</label>
                                <input type="text" id="to-name" class="form-control" placeholder="请输入收款人姓名" required>
                            </div>

                            <div class="form-group">
                                <label for="amount">转账金额</label>
                                <div class="input-with-suffix">
                                    <input type="number" id="amount" class="form-control" placeholder="0.00" min="0.01" step="0.01" required>
                                    <span class="input-suffix">元</span>
                                </div>
                            </div>

                            <div class="form-group">
                                <label for="description">备注</label>
                                <input type="text" id="description" class="form-control" placeholder="转账用途（可选）" maxlength="50">
                            </div>

                            <button type="submit" class="btn btn-primary btn-block" id="submit-transfer">
                                <i class="fas fa-paper-plane"></i> 确认转账
                            </button>
                        </form>
                    </div>
                </div>
            </div>

            <div class="transfer-info-container">
                <div class="card">
                    <div class="card-header">
                        <h3>转账说明</h3>
                    </div>
                    <div class="card-content">
                        <ul class="info-list">
                            <li><i class="fas fa-clock"></i> 普通转账：2小时内到账</li>
                            <li><i class="fas fa-bolt"></i> 快速转账：实时到账（手续费2元）</li>
                            <li><i class="fas fa-calendar"></i> 预约转账：可设置未来某个时间点转账</li>
                            <li><i class="fas fa-shield-alt"></i> 所有转账均受银行安全系统保护</li>
                        </ul>

                        <div class="recent-transfers">
                            <h4>最近转账</h4>
                            <div id="recent-transfers-list">加载中...</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `;

    // 初始化转账页面
    await initTransferPage();
}

// 加载交易历史页面
async function loadHistoryPage(container) {
    container.innerHTML = `
        <div class="page-header">
            <div>
                <h1>交易历史</h1>
                <p>查看您的账户交易记录</p>
            </div>
            <div class="header-actions">
                <button class="btn btn-secondary" id="export-btn">
                    <i class="fas fa-download"></i> 导出
                </button>
                <button class="btn btn-secondary" id="filter-btn">
                    <i class="fas fa-filter"></i> 筛选
                </button>
            </div>
        </div>

        <div class="filters-container" id="filters-container" style="display: none;">
            <div class="card">
                <div class="card-content">
                    <form id="history-filters">
                        <div class="form-row">
                            <div class="form-group half">
                                <label for="filter-type">交易类型</label>
                                <select id="filter-type" class="form-control">
                                    <option value="">全部</option>
                                    <option value="deposit">存款</option>
                                    <option value="withdraw">取款</option>
                                    <option value="transfer">转账</option>
                                </select>
                            </div>

                            <div class="form-group half">
                                <label for="filter-account">账户</label>
                                <select id="filter-account" class="form-control">
                                    <option value="">全部账户</option>
                                </select>
                            </div>
                        </div>

                        <div class="form-group">
                            <label for="filter-date">日期范围</label>
                            <input type="text" id="filter-date" class="form-control" placeholder="选择日期范围">
                        </div>

                        <div style="display: flex; gap: 10px;">
                            <button type="button" class="btn btn-secondary" id="reset-filters">重置</button>
                            <button type="submit" class="btn btn-primary">应用筛选</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>

        <div class="history-container">
            <div class="card">
                <div class="card-content">
                    <div class="table-responsive">
                        <table class="transactions-table">
                            <thead>
                                <tr>
                                    <th>日期</th>
                                    <th>交易类型</th>
                                    <th>账户</th>
                                    <th>对方信息</th>
                                    <th>金额</th>
                                    <th>余额</th>
                                    <th>状态</th>
                                </tr>
                            </thead>
                            <tbody id="transactions-table-body">
                                <tr>
                                    <td colspan="7" class="text-center">加载交易记录中...</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>

                    <div class="pagination" id="pagination">
                        <!-- 分页将通过JS动态生成 -->
                    </div>
                </div>
            </div>
        </div>
    `;

    // 加载交易历史数据
    await loadHistoryData();
}

// ==================== 辅助函数 ====================

// 显示通知
function showNotification(message, type = 'info') {
    const notification = document.getElementById('notification');
    if (!notification) return;

    const icon = notification.querySelector('i');
    const text = notification.querySelector('span');

    // 设置图标和颜色
    switch(type) {
        case 'success':
            icon.className = 'fas fa-check-circle';
            notification.className = 'notification success show';
            break;
        case 'error':
            icon.className = 'fas fa-exclamation-circle';
            notification.className = 'notification error show';
            break;
        case 'warning':
            icon.className = 'fas fa-exclamation-triangle';
            notification.className = 'notification warning show';
            break;
        default:
            icon.className = 'fas fa-info-circle';
            notification.className = 'notification info show';
            break;
    }

    text.textContent = message;

    // 3秒后自动隐藏
    setTimeout(() => {
        notification.classList.remove('show');
    }, 3000);
}

// 显示加载状态
function showLoading(show) {
    const loginBtn = document.getElementById('login-btn');
    const registerBtn = document.getElementById('register-btn');

    if (loginBtn) {
        loginBtn.disabled = show;
        loginBtn.innerHTML = show ? '<i class="fas fa-spinner fa-spin"></i> 登录中...' : '登录';
    }

    if (registerBtn) {
        registerBtn.disabled = show;
        registerBtn.innerHTML = show ? '<i class="fas fa-spinner fa-spin"></i> 注册中...' : '注册';
    }
}

// 切换到应用视图
function switchToAppView() {
    document.getElementById('login-container').style.display = 'none';
    document.getElementById('app-container').style.display = 'block';

    // 加载默认页面
    loadPage('dashboard');

    // 更新用户信息
    updateUserInfo();
}

// 切换到登录视图
function switchToLoginView() {
    document.getElementById('app-container').style.display = 'none';
    document.getElementById('login-container').style.display = 'flex';

    // 重置登录表单
    document.getElementById('login-form').style.display = 'block';
    document.getElementById('register-form').style.display = 'none';
    clearLoginForm();
}

// 更新用户信息显示
function updateUserInfo() {
    const userNameSpan = document.querySelector('.user-name');
    const userIdSpan = document.querySelector('.user-id');
    const userAvatar = document.querySelector('.user-avatar');

    if (userNameSpan && currentUser) {
        userNameSpan.textContent = currentUser.fullname || currentUser.username;
    }

    if (userIdSpan && currentUser) {
        userIdSpan.textContent = `ID: ${currentUser.userId || currentUser.id}`;
    }

    if (userAvatar && currentUser) {
        const initial = (currentUser.fullname || currentUser.username || 'U').charAt(0);
        userAvatar.textContent = initial;
    }
}

// 清除认证数据
function clearAuthData() {
    authToken = '';
    currentUser = null;
    localStorage.removeItem('bank_auth_token');
    localStorage.removeItem('bank_user_data');
    sessionStorage.removeItem('bank_auth_token');
}

// 清除登录表单
function clearLoginForm() {
    document.getElementById('username').value = '';
    document.getElementById('password').value = '';
    document.getElementById('remember-me').checked = false;
}

// 清除注册表单
function clearRegisterForm() {
    document.getElementById('register-fullname').value = '';
    document.getElementById('register-username').value = '';
    document.getElementById('register-email').value = '';
    document.getElementById('register-phone').value = '';
    document.getElementById('register-password').value = '';
    document.getElementById('register-confirm-password').value = '';
    document.getElementById('register-terms').checked = false;
}

// 显示快速存款对话框
function showQuickDeposit() {
    // 这里可以添加快速存款的模态框逻辑
    showNotification('快速存款功能开发中...', 'info');
}

// 申请信用卡
function applyForCreditCard() {
    // 这里可以添加申请信用卡的模态框逻辑
    showNotification('信用卡申请功能开发中...', 'info');
}

// ==================== 数据加载函数 ====================

// 加载仪表板数据
async function loadDashboardData() {
    try {
        // 获取账户数据
        const accountsResponse = await getUserAccounts();
        if (accountsResponse.success) {
            accounts = accountsResponse.data;
            updateAccountsList();
            updateTotalAssets();
        }

        // 获取最近交易
        const transactionsResponse = await getTransactionHistory({ limit: 5 });
        if (transactionsResponse.success) {
            transactions = transactionsResponse.data;
            updateRecentTransactions();
        }

        // 获取月度统计数据
        const now = new Date();
        const statementResponse = await getMonthlyStatement(now.getFullYear(), now.getMonth() + 1);
        if (statementResponse.success) {
            updateMonthlyStats(statementResponse.data);
        }
    } catch (error) {
        console.error('加载仪表板数据失败:', error);
    }
}

// 更新账户列表
function updateAccountsList() {
    const accountsList = document.getElementById('accounts-list');
    if (!accountsList || !accounts || accounts.length === 0) return;

    const html = accounts.slice(0, 3).map(account => `
        <div class="transaction-item">
            <div class="transaction-icon ${account.type === 'SAVINGS' ? 'deposit' : 'transfer'}">
                <i class="fas fa-${account.type === 'SAVINGS' ? 'piggy-bank' : 'credit-card'}"></i>
            </div>
            <div class="transaction-details">
                <h4>${account.accountName || '储蓄账户'}</h4>
                <p class="transaction-desc">${account.accountNumber}</p>
            </div>
            <div class="transaction-amount">
                ¥${account.balance.toFixed(2)}
            </div>
        </div>
    `).join('');

    accountsList.innerHTML = html;
}

// 更新总资产
function updateTotalAssets() {
    const totalAssets = document.getElementById('total-assets');
    if (!totalAssets || !accounts) return;

    const total = accounts.reduce((sum, account) => sum + account.balance, 0);
    totalAssets.textContent = `¥${total.toFixed(2)}`;
}

// 更新最近交易
function updateRecentTransactions() {
    const recentTransactions = document.getElementById('recent-transactions');
    if (!recentTransactions || !transactions || transactions.length === 0) return;

    const html = transactions.map(transaction => `
        <div class="transaction-item">
            <div class="transaction-icon ${transaction.type.toLowerCase()}">
                <i class="fas fa-${transaction.type === 'DEPOSIT' ? 'arrow-down' : 'arrow-up'}"></i>
            </div>
            <div class="transaction-details">
                <h4>${transaction.description || transaction.type}</h4>
                <p class="transaction-time">${new Date(transaction.transactionTime).toLocaleDateString()}</p>
            </div>
            <div class="transaction-amount ${transaction.type === 'DEPOSIT' ? 'positive' : 'negative'}">
                ${transaction.type === 'DEPOSIT' ? '+' : '-'}¥${transaction.amount.toFixed(2)}
            </div>
        </div>
    `).join('');

    recentTransactions.innerHTML = html;
}

// 更新月度统计数据
function updateMonthlyStats(statementData) {
    const monthExpense = document.getElementById('month-expense');
    const monthIncome = document.getElementById('month-income');

    if (monthExpense && statementData.totalExpense !== undefined) {
        monthExpense.textContent = `¥${statementData.totalExpense.toFixed(2)}`;
    }

    if (monthIncome && statementData.totalIncome !== undefined) {
        monthIncome.textContent = `¥${statementData.totalIncome.toFixed(2)}`;
    }
}

// 初始化图表
function initializeCharts() {
    // 检查是否加载了Chart.js
    if (typeof Chart === 'undefined') {
        // 动态加载Chart.js
        const script = document.createElement('script');
        script.src = 'https://cdn.jsdelivr.net/npm/chart.js';
        script.onload = function() {
            renderChart();
        };
        document.head.appendChild(script);
    } else {
        renderChart();
    }
}

// 渲染图表
function renderChart() {
    const expenseChartCtx = document.getElementById('expense-chart');
    if (!expenseChartCtx) return;

    if (window.expenseChart) {
        window.expenseChart.destroy();
    }

    window.expenseChart = new Chart(expenseChartCtx.getContext('2d'), {
        type: 'doughnut',
        data: {
            labels: ['购物消费', '餐饮娱乐', '交通出行', '生活缴费', '其他'],
            datasets: [{
                data: [30, 25, 20, 15, 10],
                backgroundColor: [
                    '#3b82f6',
                    '#10b981',
                    '#f59e0b',
                    '#8b5cf6',
                    '#64748b'
                ],
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'right'
                }
            }
        }
    });
}

// 加载账户数据
async function loadAccountsData() {
    try {
        const response = await getUserAccounts();
        if (response.success) {
            accounts = response.data;
            renderAccountsList();
        }
    } catch (error) {
        console.error('加载账户数据失败:', error);
        showNotification('加载账户信息失败', 'error');
    }
}

// 渲染账户列表
function renderAccountsList() {
    const accountsContainer = document.getElementById('accounts-container');
    if (!accountsContainer || !accounts) return;

    if (accounts.length === 0) {
        accountsContainer.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-wallet fa-3x"></i>
                <h3>暂无账户</h3>
                <p>您还没有创建任何银行账户</p>
                <button class="btn btn-primary" id="create-first-account">创建第一个账户</button>
            </div>
        `;

        document.getElementById('create-first-account')?.addEventListener('click', showAddAccountModal);
        return;
    }

    const html = accounts.map(account => `
        <div class="card">
            <div class="card-header">
                <h3>${account.accountName || '储蓄账户'}</h3>
                <span class="status-badge ${account.status === 'ACTIVE' ? 'success' : 'pending'}">${account.status === 'ACTIVE' ? '正常' : '待激活'}</span>
            </div>
            <div class="card-content">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px;">
                    <span style="color: var(--gray-color);">账号:</span>
                    <strong>${account.accountNumber}</strong>
                </div>
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px;">
                    <span style="color: var(--gray-color);">余额:</span>
                    <div class="amount">¥${account.balance.toFixed(2)}</div>
                </div>
                <div style="display: flex; justify-content: space-between; align-items: center;">
                    <span style="color: var(--gray-color);">开户日期:</span>
                    <span>${new Date(account.openDate).toLocaleDateString()}</span>
                </div>
            </div>
            <div class="card-footer">
                <div style="display: flex; gap: 10px;">
                    <button class="btn btn-secondary" style="flex: 1;" onclick="handleDeposit('${account.id}')">
                        <i class="fas fa-plus"></i> 存款
                    </button>
                    <button class="btn btn-secondary" style="flex: 1;" onclick="handleWithdraw('${account.id}')">
                        <i class="fas fa-minus"></i> 取款
                    </button>
                    <button class="btn btn-secondary" style="flex: 1;" onclick="handleTransferFrom('${account.id}')">
                        <i class="fas fa-exchange-alt"></i> 转账
                    </button>
                </div>
            </div>
        </div>
    `).join('');

    accountsContainer.innerHTML = html;
}

// 初始化转账页面
async function initTransferPage() {
    try {
        // 加载账户列表
        const response = await getUserAccounts();
        if (response.success) {
            const fromAccountSelect = document.getElementById('from-account');
            const filterAccountSelect = document.getElementById('filter-account');

            if (fromAccountSelect) {
                fromAccountSelect.innerHTML = '<option value="">请选择账户</option>' +
                    response.data.map(account => `
                        <option value="${account.id}">
                            ${account.accountName} (${account.accountNumber}) - ¥${account.balance.toFixed(2)}
                        </option>
                    `).join('');
            }

            if (filterAccountSelect) {
                filterAccountSelect.innerHTML = '<option value="">全部账户</option>' +
                    response.data.map(account => `
                        <option value="${account.id}">${account.accountName} (${account.accountNumber})</option>
                    `).join('');
            }
        }

        // 设置表单提交事件
        const transferForm = document.getElementById('transfer-form');
        if (transferForm) {
            transferForm.addEventListener('submit', handleTransferSubmit);
        }

        // 加载最近转账记录
        await loadRecentTransfers();

    } catch (error) {
        console.error('初始化转账页面失败:', error);
        showNotification('加载转账页面失败', 'error');
    }
}

// 处理转账提交
async function handleTransferSubmit(e) {
    e.preventDefault();

    const fromAccountId = document.getElementById('from-account').value;
    const toAccount = document.getElementById('to-account').value;
    const toName = document.getElementById('to-name').value;
    const amount = parseFloat(document.getElementById('amount').value);
    const description = document.getElementById('description').value;
    const toAccountType = document.getElementById('to-account-type').value;

    if (!fromAccountId || !toAccount || !toName || !amount || amount <= 0) {
        showNotification('请填写完整的转账信息', 'error');
        return;
    }

    try {
        showLoadingForButton('submit-transfer', true);

        const transferData = {
            fromAccountId,
            toAccountNumber: toAccount,
            toAccountName: toName,
            amount,
            description: description || '转账',
            transferType: toAccountType === 'same-bank' ? 'INTERNAL' : 'EXTERNAL'
        };

        const response = await transfer(transferData);

        if (response.success) {
            showNotification('转账成功！', 'success');
            // 重置表单
            document.getElementById('transfer-form').reset();
            // 更新最近转账记录
            await loadRecentTransfers();
        }
    } catch (error) {
        showNotification(error.message || '转账失败', 'error');
    } finally {
        showLoadingForButton('submit-transfer', false);
    }
}

// 加载最近转账记录
async function loadRecentTransfers() {
    const recentTransfersList = document.getElementById('recent-transfers-list');
    if (!recentTransfersList) return;

    try {
        const response = await getTransactionHistory({
            type: 'transfer',
            limit: 5
        });

        if (response.success && response.data.length > 0) {
            const html = response.data.map(transaction => `
                <div class="transaction-item">
                    <div class="transaction-details">
                        <h4>${transaction.counterpartyName || '未知'}</h4>
                        <p class="transaction-time">${new Date(transaction.transactionTime).toLocaleDateString()}</p>
                    </div>
                    <div class="transaction-amount negative">
                        -¥${transaction.amount.toFixed(2)}
                    </div>
                </div>
            `).join('');

            recentTransfersList.innerHTML = html;
        } else {
            recentTransfersList.innerHTML = '<p class="text-center" style="color: var(--gray-color);">暂无转账记录</p>';
        }
    } catch (error) {
        recentTransfersList.innerHTML = '<p class="text-center" style="color: var(--danger-color);">加载失败</p>';
    }
}

// 加载交易历史数据
async function loadHistoryData(filters = {}) {
    try {
        const response = await getTransactionHistory(filters);

        if (response.success) {
            transactions = response.data;
            renderTransactionsTable();

            // 如果有分页信息，渲染分页控件
            if (response.pagination) {
                renderPagination(response.pagination);
            }
        }
    } catch (error) {
        console.error('加载交易历史失败:', error);
        showNotification('加载交易历史失败', 'error');
    }
}

// 渲染交易表格
function renderTransactionsTable() {
    const tableBody = document.getElementById('transactions-table-body');
    if (!tableBody) return;

    if (!transactions || transactions.length === 0) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="7" class="text-center">暂无交易记录</td>
            </tr>
        `;
        return;
    }

    const html = transactions.map(transaction => `
        <tr>
            <td>${new Date(transaction.transactionTime).toLocaleDateString()}</td>
            <td>
                <span class="transaction-type-badge ${transaction.type.toLowerCase()}">
                    ${getTransactionTypeText(transaction.type)}
                </span>
            </td>
            <td>${transaction.accountNumber || ''}</td>
            <td>${transaction.counterpartyName || transaction.counterpartyAccount || '-'}</td>
            <td class="${transaction.type === 'DEPOSIT' ? 'text-success' : 'text-danger'}">
                ${transaction.type === 'DEPOSIT' ? '+' : '-'}¥${transaction.amount.toFixed(2)}
            </td>
            <td>¥${transaction.afterBalance.toFixed(2)}</td>
            <td>
                <span class="status-badge ${transaction.status.toLowerCase()}">
                    ${getTransactionStatusText(transaction.status)}
                </span>
            </td>
        </tr>
    `).join('');

    tableBody.innerHTML = html;
}

// 渲染分页控件
function renderPagination(pagination) {
    const paginationContainer = document.getElementById('pagination');
    if (!paginationContainer) return;

    const { currentPage, totalPages, hasNext, hasPrevious } = pagination;

    let html = `
        <button class="page-btn ${!hasPrevious ? 'disabled' : ''}" 
                ${!hasPrevious ? 'disabled' : ''} 
                onclick="goToPage(${currentPage - 1})">
            <i class="fas fa-chevron-left"></i>
        </button>
    `;

    // 显示页码
    for (let i = 1; i <= totalPages; i++) {
        if (i === currentPage) {
            html += `<button class="page-btn active">${i}</button>`;
        } else {
            html += `<button class="page-btn" onclick="goToPage(${i})">${i}</button>`;
        }
    }

    html += `
        <button class="page-btn ${!hasNext ? 'disabled' : ''}" 
                ${!hasNext ? 'disabled' : ''} 
                onclick="goToPage(${currentPage + 1})">
            <i class="fas fa-chevron-right"></i>
        </button>
    `;

    paginationContainer.innerHTML = html;
}

// 跳转到指定页面
function goToPage(page) {
    const currentFilters = getCurrentFilters();
    currentFilters.page = page;
    loadHistoryData(currentFilters);
}

// 获取当前筛选条件
function getCurrentFilters() {
    const filters = {};

    const type = document.getElementById('filter-type')?.value;
    const account = document.getElementById('filter-account')?.value;
    const date = document.getElementById('filter-date')?.value;

    if (type) filters.type = type;
    if (account) filters.accountId = account;
    if (date) filters.dateRange = date;

    return filters;
}

// 获取交易类型文本
function getTransactionTypeText(type) {
    const types = {
        'DEPOSIT': '存款',
        'WITHDRAW': '取款',
        'TRANSFER': '转账',
        'PAYMENT': '支付'
    };
    return types[type] || type;
}

// 获取交易状态文本
function getTransactionStatusText(status) {
    const statuses = {
        'SUCCESS': '成功',
        'PENDING': '处理中',
        'FAILED': '失败',
        'CANCELLED': '已取消'
    };
    return statuses[status] || status;
}

// 显示/隐藏加载状态（针对特定按钮）
function showLoadingForButton(buttonId, show) {
    const button = document.getElementById(buttonId);
    if (!button) return;

    if (show) {
        const originalText = button.innerHTML;
        button.dataset.originalText = originalText;
        button.innerHTML = '<i class="fas fa-spinner fa-spin"></i> 处理中...';
        button.disabled = true;
    } else {
        const originalText = button.dataset.originalText;
        if (originalText) {
            button.innerHTML = originalText;
        }
        button.disabled = false;
    }
}

// 显示添加账户模态框
function showAddAccountModal() {
    // 这里可以添加创建账户的模态框逻辑
    showNotification('创建账户功能开发中...', 'info');
}

// 全局函数供HTML调用
window.handleDeposit = function(accountId) {
    showNotification(`向账户存款功能开发中...`, 'info');
};

window.handleWithdraw = function(accountId) {
    showNotification(`从账户取款功能开发中...`, 'info');
};

window.handleTransferFrom = function(accountId) {
    loadPage('transfer');
    // 设置选中的账户
    setTimeout(() => {
        const fromAccountSelect = document.getElementById('from-account');
        if (fromAccountSelect) {
            fromAccountSelect.value = accountId;
        }
    }, 100);
};

// 动态加载Font Awesome图标库
(function loadFontAwesome() {
    const link = document.createElement('link');
    link.rel = 'stylesheet';
    link.href = 'https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css';
    document.head.appendChild(link);
})();