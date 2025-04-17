"use client";

import { useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import * as z from "zod";
import { useUserStore } from "@/store/useUserStore";
import { toast } from "@/components/ui/use-toast";
import { Button } from "@/components/ui/button";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";

// Form validation schema
const formSchema = z.object({
  username: z.string().min(3, {
    message: "用户名长度至少为3个字符",
  }).max(20, {
    message: "用户名长度不能超过20个字符",
  }),
  password: z.string().min(6, {
    message: "密码长度至少为6个字符",
  }).max(30, {
    message: "密码长度不能超过30个字符",
  }),
});

export default function LoginPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const redirectPath = searchParams.get("redirect") || "/";
  
  const [loading, setLoading] = useState(false);
  const [debugInfo, setDebugInfo] = useState<string | null>(null);
  
  const { loginAction, getUserInfo } = useUserStore();

  // Initialize form
  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      username: "",
      password: "",
    },
  });

  // Handle form submission
  async function onSubmit(values: z.infer<typeof formSchema>) {
    // Clear previous debug info
    setDebugInfo(null);
    
    setLoading(true);
    try {
      console.log("Starting login operation");
      const loginResult = await loginAction(values);
      console.log("Login request successful:", loginResult);
      
      toast({
        title: "登录成功",
        description: "欢迎回来！",
      });
      
      // Ensure token is updated in local storage
      const currentToken = localStorage.getItem("token");
      console.log("Local token update status:", !!currentToken);
      
      // Get user info
      console.log("Getting user info");
      try {
        const userInfoResult = await getUserInfo();
        console.log("User info retrieved successfully:", userInfoResult);
        
        // Redirect to redirect page or default page
        console.log("Preparing to redirect to:", redirectPath);
        
        // Use window.location for hard navigation
        setTimeout(() => {
          console.log("Executing route navigation");
          if (redirectPath === "/") {
            window.location.href = "/";
          } else {
            router.replace(redirectPath);
          }
        }, 10);
      } catch (userError: any) {
        // Failed to get user info, log error and show debug info
        console.error("Failed to get user info:", userError);
        
        if (userError.response) {
          setDebugInfo(JSON.stringify(userError.response.data, null, 2));
          toast({
            variant: "destructive",
            title: "获取用户信息失败",
            description: userError.response.data?.message || "请检查API路径是否正确",
          });
        } else {
          setDebugInfo(userError.message || "未知错误");
          toast({
            variant: "destructive",
            title: "获取用户信息失败",
            description: "请联系管理员",
          });
        }
      }
    } catch (error: any) {
      console.error("Login process error:", error);
      if (error.response) {
        console.error("Login response error:", error.response.data);
        setDebugInfo(JSON.stringify(error.response.data, null, 2));
        toast({
          variant: "destructive",
          title: "登录失败",
          description: error.response.data?.message || "请检查网络连接",
        });
      } else if (error.request) {
        console.error("No response received:", error.request);
        setDebugInfo("服务器未响应");
        toast({
          variant: "destructive",
          title: "登录失败",
          description: "服务器未响应，请检查网络连接",
        });
      } else {
        console.error("Login failed:", error.message);
        setDebugInfo(error.message || "未知错误");
        toast({
          variant: "destructive",
          title: "登录失败",
          description: error.message || "请重试",
        });
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="login-container">
      <div className="login-box">
        <div className="login-title">
          <h2 className="text-2xl font-bold">系统登录</h2>
        </div>
        
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="login-form space-y-4">
            <FormField
              control={form.control}
              name="username"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>用户名</FormLabel>
                  <FormControl>
                    <Input placeholder="请输入用户名" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            
            <FormField
              control={form.control}
              name="password"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>密码</FormLabel>
                  <FormControl>
                    <Input type="password" placeholder="请输入密码" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            
            <Button type="submit" className="w-full" disabled={loading}>
              {loading ? "登录中..." : "登录"}
            </Button>
          </form>
        </Form>
        
        {/* Debug info */}
        {debugInfo && (
          <div className="debug-info mt-4 p-4 bg-gray-100 rounded-md">
            <h3 className="text-sm font-semibold">API调试信息：</h3>
            <pre className="text-xs overflow-auto mt-2">{debugInfo}</pre>
          </div>
        )}
      </div>
    </div>
  );
}
