// API Client for WTD Backend
const API_BASE_URL = 'http://localhost:8080/api';

let currentUserId = null;
let currentUser = null;

// Get stored authentication
function loadAuth() {
    const stored = localStorage.getItem('wtdAuth');
    if (stored) {
        const auth = JSON.parse(stored);
        currentUserId = auth.userId;
        currentUser = auth.user;
        return true;
    }
    return false;
}

// Save authentication
function saveAuth(userId, user) {
    currentUserId = userId;
    currentUser = user;
    localStorage.setItem('wtdAuth', JSON.stringify({ userId, user }));
}

// Clear authentication
function clearAuth() {
    currentUserId = null;
    currentUser = null;
    localStorage.removeItem('wtdAuth');
}

// Get all users (for login/selection)
async function getUsers() {
    try {
        const response = await fetch(`${API_BASE_URL}/users`);
        if (!response.ok) throw new Error('Failed to fetch users');
        return await response.json();
    } catch (error) {
        console.error('Error fetching users:', error);
        return [];
    }
}

// Get user by ID
async function getUserById(userId) {
    try {
        const response = await fetch(`${API_BASE_URL}/users/${userId}`);
        if (!response.ok) throw new Error('Failed to fetch user');
        return await response.json();
    } catch (error) {
        console.error('Error fetching user:', error);
        return null;
    }
}

// Get tasks for a date range
async function getTasksByDateRange(fromDate, toDate) {
    if (!currentUserId) {
        console.error('Not authenticated');
        return [];
    }

    const params = new URLSearchParams({
        userId: currentUserId,
        from: fromDate,
        to: toDate
    });

    try {
        const response = await fetch(`${API_BASE_URL}/tasks?${params}`);
        if (!response.ok) throw new Error('Failed to fetch tasks');
        return await response.json();
    } catch (error) {
        console.error('Error fetching tasks:', error);
        return [];
    }
}

// Get tasks for a specific date
async function getTasksByDate(date) {
    if (!currentUserId) {
        console.error('Not authenticated');
        return [];
    }

    const params = new URLSearchParams({
        userId: currentUserId,
        date: date
    });

    try {
        const response = await fetch(`${API_BASE_URL}/tasks?${params}`);
        if (!response.ok) throw new Error('Failed to fetch tasks');
        return await response.json();
    } catch (error) {
        console.error('Error fetching tasks:', error);
        return [];
    }
}

// Get all tasks for current user
async function getAllTasks() {
    if (!currentUserId) {
        console.error('Not authenticated');
        return [];
    }

    const params = new URLSearchParams({
        userId: currentUserId
    });

    try {
        const response = await fetch(`${API_BASE_URL}/tasks?${params}`);
        if (!response.ok) throw new Error('Failed to fetch tasks');
        return await response.json();
    } catch (error) {
        console.error('Error fetching tasks:', error);
        return [];
    }
}

// Create a new task
async function createTask(taskData) {
    if (!currentUserId) {
        console.error('Not authenticated');
        return null;
    }

    // Ensure createdBy is set to current user
    const task = {
        ...taskData,
        createdBy: { id: currentUserId }
    };

    try {
        const response = await fetch(`${API_BASE_URL}/tasks`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(task)
        });

        if (!response.ok) throw new Error('Failed to create task');
        return await response.json();
    } catch (error) {
        console.error('Error creating task:', error);
        return null;
    }
}

// Update a task
async function updateTask(taskId, taskData) {
    try {
        const response = await fetch(`${API_BASE_URL}/tasks/${taskId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(taskData)
        });

        if (!response.ok) throw new Error('Failed to update task');
        return await response.json();
    } catch (error) {
        console.error('Error updating task:', error);
        return null;
    }
}

// Delete a task
async function deleteTask(taskId) {
    try {
        const response = await fetch(`${API_BASE_URL}/tasks/${taskId}`, {
            method: 'DELETE'
        });

        if (!response.ok) throw new Error('Failed to delete task');
        return true;
    } catch (error) {
        console.error('Error deleting task:', error);
        return false;
    }
}

// Get categories (no auth required)
async function getCategories() {
    try {
        const response = await fetch(`${API_BASE_URL}/tasks/categories`);
        if (!response.ok) throw new Error('Failed to fetch categories');
        return await response.json();
    } catch (error) {
        console.error('Error fetching categories:', error);
        return [];
    }
}

// Share task with user
async function shareTask(taskId, userId) {
    try {
        const response = await fetch(`${API_BASE_URL}/tasks/${taskId}/share/${userId}`, {
            method: 'POST'
        });

        if (!response.ok) throw new Error('Failed to share task');
        return await response.json();
    } catch (error) {
        console.error('Error sharing task:', error);
        return null;
    }
}

// Unshare task with user
async function unshareTask(taskId, userId) {
    try {
        const response = await fetch(`${API_BASE_URL}/tasks/${taskId}/share/${userId}`, {
            method: 'DELETE'
        });

        if (!response.ok) throw new Error('Failed to unshare task');
        return await response.json();
    } catch (error) {
        console.error('Error unsharing task:', error);
        return null;
    }
}

// Initialize - load auth if available
loadAuth();
