import { create } from 'zustand';
import { login, getUserInfo } from '@/api/auth';
import request from '@/api/request';
import { toast } from '@/components/ui/use-toast';

interface UserInfo {
  username: string;
  nickname?: string;
  roles?: Array<{
    id: string;
    code: string;
    name: string;
    permissions?: Array<{
      id: string;
      code: string;
      name: string;
    }>;
  }>;
}

interface UserState {
  token: string;
  userInfo: UserInfo | null;
  permissions: string[];
  
  // Getters
  isAuthenticated: boolean;
  username: string;
  nickname: string;
  roles: Array<any>;
  
  // Actions
  loginAction: (loginData: { username: string; password: string }) => Promise<any>;
  getUserInfo: () => Promise<any>;
  hasPermission: (permission: string) => boolean;
  logout: () => void;
}

export const useUserStore = create<UserState>((set, get) => ({
  token: typeof window !== 'undefined' ? localStorage.getItem('token') || '' : '',
  userInfo: null,
  permissions: [],
  
  // Getters
  get isAuthenticated() {
    return !!get().token && !!get().userInfo;
  },
  
  get username() {
    return get().userInfo?.username || '';
  },
  
  get nickname() {
    return get().userInfo?.nickname || '';
  },
  
  get roles() {
    return get().userInfo?.roles || [];
  },
  
  // Actions
  loginAction: async (loginData) => {
    try {
      console.log('Calling login API', loginData.username);
      const res = await login(loginData);
      console.log('Login API response:', res);
      
      // Check if response contains token
      if (!res.data) {
        console.error('Login response has no data field', res);
        return Promise.reject(new Error('Login response format error, missing data field'));
      }
      
      // Check for token field, compatible with different return formats
      let token = null;
      if (res.data.token) {
        // Direct token field
        token = res.data.token;
      } else if (res.data.tokenType && res.data.accessToken) {
        // OAuth2 format
        token = `${res.data.tokenType} ${res.data.accessToken}`;
      } else if (res.data.tokenType && res.data.token) {
        // Current interface return format
        token = res.data.token;
      }
      
      if (!token) {
        console.error('Cannot extract token from response', res.data);
        return Promise.reject(new Error('Login response format error, cannot extract token'));
      }
      
      console.log('Got token:', token);
      
      set({ token });
      localStorage.setItem('token', token);
      console.log('Token saved to localStorage');
      
      return Promise.resolve(res);
    } catch (error) {
      console.error('Login operation failed:', error);
      return Promise.reject(error);
    }
  },
  
  getUserInfo: async () => {
    try {
      console.log('-------- Starting to get user info --------');
      
      // Try multiple possible API paths
      const apiPaths = [
        '/users/current'
      ];
      
      let lastError = null;
      let userData = null;
      
      // Try different paths in sequence
      for (const path of apiPaths) {
        console.log('Trying API path:', path);
        try {
          const res = await request({
            url: path,
            method: 'get'
          });
          
          if (res.data) {
            console.log('User info retrieved successfully, using path:', path);
            userData = res;
            break;
          }
        } catch (error) {
          console.log(`Path ${path} failed:`, error.message);
          lastError = error;
        }
      }
      
      if (!userData) {
        console.error('All API paths failed', lastError);
        return Promise.reject(lastError || new Error('Failed to get user info'));
      }
      
      console.log('User info API response:', userData);
      
      if (!userData.data) {
        console.error('User info response format error', userData);
        return Promise.reject(new Error('Failed to get user info, response format error'));
      }
      
      set({ userInfo: userData.data });
      console.log('User info saved to store:', JSON.stringify(userData.data, null, 2));
      
      // Extract all permission codes
      let permissions: string[] = [];
      
      if (userData.data.roles && userData.data.roles.length > 0) {
        console.log('Processing user role permissions...');
        console.log('User roles:', JSON.stringify(userData.data.roles, null, 2));
        
        // Check if has admin role
        const hasAdminRole = userData.data.roles.some(role => role.code === 'ROLE_ADMIN');
        
        if (hasAdminRole) {
          // Admin role has all permissions
          console.log('Admin role detected, granting all permissions');
          permissions = [
            'USER_VIEW', 'USER_ADD', 'USER_EDIT', 'USER_DELETE',
            'ROLE_VIEW', 'ROLE_ADD', 'ROLE_EDIT', 'ROLE_DELETE',
            'PERMISSION_VIEW', 'PERMISSION_ADD', 'PERMISSION_EDIT', 'PERMISSION_DELETE',
            'MENU_VIEW', 'MENU_ADD', 'MENU_EDIT', 'MENU_DELETE'
          ];
        } else {
          // Regular user, get permissions from role permission list
          userData.data.roles.forEach(role => {
            if (role.permissions && role.permissions.length > 0) {
              role.permissions.forEach(permission => {
                if (permission.code && !permissions.includes(permission.code)) {
                  permissions.push(permission.code);
                }
              });
            }
          });
        }
      }
      
      console.log('Role permission processing complete, current permission list:', permissions);
      
      // Try to get additional permission info - always call
      console.log('Starting to get additional permission info...');
      try {
        const permRes = await request({
          url: '/users/current/permissions',
          method: 'get'
        });
        
        console.log('Permission interface returns:', JSON.stringify(permRes, null, 2));
        
        if (permRes.data && permRes.data.permissions) {
          console.log('Got additional permission list:', permRes.data.permissions);
          permRes.data.permissions.forEach(perm => {
            if (!permissions.includes(perm)) {
              permissions.push(perm);
            }
          });
        }
      } catch (err) {
        console.warn('Failed to get additional permission info:', err);
        // Failure doesn't affect main process
      }
      
      set({ permissions });
      console.log('Final permission list updated:', permissions);
      
      // Also store permission list in localStorage for use after page refresh
      localStorage.setItem('permissions', JSON.stringify(permissions));
      
      console.log('-------- User info retrieval complete --------');
      return Promise.resolve(userData);
    } catch (error) {
      console.error('Final failure getting user info:', error);
      return Promise.reject(error);
    }
  },
  
  hasPermission: (permission) => {
    if (!permission) return true;
    
    const state = get();
    
    // Load permissions from localStorage (for page refresh case)
    if (state.permissions.length === 0 && typeof window !== 'undefined') {
      const storedPermissions = localStorage.getItem('permissions');
      if (storedPermissions) {
        const permissions = JSON.parse(storedPermissions);
        set({ permissions });
        console.log('Restored permission list from localStorage:', permissions);
      }
    }
    
    // Check if has admin role
    const hasAdminRole = state.userInfo?.roles?.some(role => role.code === 'ROLE_ADMIN') || false;
    if (hasAdminRole) {
      console.log(`Admin role permission check: ${permission} - passed`);
      return true;
    }
    
    const result = state.permissions.includes(permission);
    console.log(`Permission check: ${permission} - ${result ? 'passed' : 'denied'}`);
    return result;
  },
  
  logout: () => {
    set({ token: '', userInfo: null, permissions: [] });
    
    if (typeof window !== 'undefined') {
      localStorage.removeItem('token');
      localStorage.removeItem('permissions');
      localStorage.removeItem('loadingUserInfo');
    }
    
    // Can call logout API here
    request({
      url: '/auth/logout',
      method: 'post'
    }).catch(err => {
      console.log('Logout API call failed, but doesn\'t affect local logout:', err);
    });
  }
}));
