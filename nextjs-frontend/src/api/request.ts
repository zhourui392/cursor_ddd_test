import axios, { AxiosError, AxiosRequestConfig, AxiosResponse } from 'axios';
import { toast } from '@/components/ui/use-toast';
import { redirect } from 'next/navigation';

// Create axios instance
const service = axios.create({
  baseURL: '/api', // API base URL
  timeout: 15000 // Request timeout
});

// Request interceptor
service.interceptors.request.use(
  (config) => {
    console.log('Preparing to send request:', config.url);
    
    // Add token to request headers
    if (typeof window !== 'undefined') {
      const token = localStorage.getItem('token');
      if (token) {
        // Check if token already has Bearer prefix
        if (token.startsWith('Bearer ')) {
          console.log('Using complete token format');
          config.headers['Authorization'] = token;
        } else {
          console.log('Adding Bearer prefix to token');
          config.headers['Authorization'] = `Bearer ${token}`;
        }
        
        console.log('Authorization header set');
      } else {
        console.log('No token found');
      }
    }
    
    return config;
  },
  (error) => {
    console.error('Request error:', error);
    return Promise.reject(error);
  }
);

// Response interceptor
service.interceptors.response.use(
  (response: AxiosResponse) => {
    console.log('Received response:', response.config.url, response.data);
    const res = response.data;
    
    // Normal response - compatible with string type code
    if (res.code === 200 || res.code === "200") {
      return res;
    }
    
    // Handle special error codes
    if (res.code === 401 || res.code === "401") {
      // Unauthorized, clear token and redirect to login page
      console.warn('Response status code 401, unauthorized');
      if (typeof window !== 'undefined') {
        localStorage.removeItem('token');
        toast({
          variant: "destructive",
          title: "Session expired",
          description: "Please log in again",
        });
        
        // Use window.location for hard redirect
        window.location.href = '/login';
      }
    } else {
      // Other errors
      console.error('Response error code:', res.code, res.message);
      toast({
        variant: "destructive",
        title: "Error",
        description: res.message || 'Server error',
      });
    }
    
    return Promise.reject(new Error(res.message || 'Server error'));
  },
  (error: AxiosError) => {
    console.error('Request error:', error.config?.url, error);
    
    if (error.response) {
      console.error('Error response status code:', error.response.status, error.response.data);
      
      // Show different error messages based on response status code
      switch (error.response.status) {
        case 401:
          if (typeof window !== 'undefined') {
            localStorage.removeItem('token');
            toast({
              variant: "destructive",
              title: "Session expired",
              description: "Please log in again",
            });
            window.location.href = '/login';
          }
          break;
        case 403:
          toast({
            variant: "destructive",
            title: "Access denied",
            description: "You don't have permission to access this resource",
          });
          break;
        case 404:
          toast({
            variant: "destructive",
            title: "Not found",
            description: "The requested resource does not exist",
          });
          break;
        case 500:
          toast({
            variant: "destructive",
            title: "Server error",
            description: "An internal server error occurred",
          });
          break;
        default:
          toast({
            variant: "destructive",
            title: "Error",
            description: error.message || 'Unknown error',
          });
      }
    } else {
      // Timeout or network error
      if (error.message?.includes('timeout')) {
        console.error('Request timeout');
        toast({
          variant: "destructive",
          title: "Request timeout",
          description: "Please try again later",
        });
      } else {
        console.error('Network error');
        toast({
          variant: "destructive",
          title: "Network error",
          description: "Please check your network connection",
        });
      }
    }
    
    return Promise.reject(error);
  }
);

export default service;
