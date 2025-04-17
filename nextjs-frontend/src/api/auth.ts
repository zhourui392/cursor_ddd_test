import request from './request';

interface LoginData {
  username: string;
  password: string;
}

/**
 * User login
 * @param data Login data
 */
export function login(data: LoginData) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  });
}

/**
 * Get current user info
 */
export function getUserInfo() {
  return request({
    url: '/users/current',
    method: 'get'
  });
}

/**
 * Logout
 */
export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post'
  });
}
