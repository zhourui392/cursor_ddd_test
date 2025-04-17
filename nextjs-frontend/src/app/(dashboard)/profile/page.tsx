"use client";

import { useState, useEffect } from "react";
import { useUserStore } from "@/store/useUserStore";
import { toast } from "@/components/ui/use-toast";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from "@/components/ui/tabs";
import {
  Avatar,
  AvatarFallback,
  AvatarImage,
} from "@/components/ui/avatar";
import { 
  User, 
  Mail, 
  Phone, 
  Shield, 
  Key, 
  Save,
  RefreshCw
} from "lucide-react";

export default function ProfilePage() {
  const { userInfo, getUserInfo } = useUserStore();
  const [loading, setLoading] = useState(false);
  const [passwordLoading, setPasswordLoading] = useState(false);
  const [mounted, setMounted] = useState(false);
  
  const [profileForm, setProfileForm] = useState({
    username: "",
    nickname: "",
    email: "",
    phone: "",
  });
  
  const [passwordForm, setPasswordForm] = useState({
    oldPassword: "",
    newPassword: "",
    confirmPassword: "",
  });

  // Initialize form data when user info is available
  useEffect(() => {
    setMounted(true);
    
    if (userInfo) {
      setProfileForm({
        username: userInfo.username || "",
        nickname: userInfo.nickname || "",
        email: userInfo.email || "",
        phone: userInfo.phone || "",
      });
    }
  }, [userInfo]);

  // Handle profile form change
  const handleProfileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setProfileForm(prev => ({
      ...prev,
      [name]: value
    }));
  };

  // Handle password form change
  const handlePasswordChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setPasswordForm(prev => ({
      ...prev,
      [name]: value
    }));
  };

  // Handle profile update
  const handleProfileUpdate = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    
    try {
      // Here you would call your API to update the profile
      // For now, we'll just simulate a successful update
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      toast({
        title: "个人信息更新成功",
        description: "您的个人信息已成功更新",
      });
      
      // Refresh user info
      await getUserInfo();
    } catch (error) {
      console.error("Failed to update profile:", error);
      toast({
        variant: "destructive",
        title: "更新失败",
        description: "个人信息更新失败，请稍后重试",
      });
    } finally {
      setLoading(false);
    }
  };

  // Handle password update
  const handlePasswordUpdate = async (e: React.FormEvent) => {
    e.preventDefault();
    
    // Validate password
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      toast({
        variant: "destructive",
        title: "密码不匹配",
        description: "新密码和确认密码不匹配，请重新输入",
      });
      return;
    }
    
    if (passwordForm.newPassword.length < 6) {
      toast({
        variant: "destructive",
        title: "密码太短",
        description: "新密码长度至少为6个字符",
      });
      return;
    }
    
    setPasswordLoading(true);
    
    try {
      // Here you would call your API to update the password
      // For now, we'll just simulate a successful update
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      toast({
        title: "密码更新成功",
        description: "您的密码已成功更新",
      });
      
      // Reset password form
      setPasswordForm({
        oldPassword: "",
        newPassword: "",
        confirmPassword: "",
      });
    } catch (error) {
      console.error("Failed to update password:", error);
      toast({
        variant: "destructive",
        title: "更新失败",
        description: "密码更新失败，请稍后重试",
      });
    } finally {
      setPasswordLoading(false);
    }
  };

  if (!mounted) {
    return null;
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">个人中心</h1>
        <p className="text-muted-foreground">
          管理您的个人信息和账户设置
        </p>
      </div>
      
      <Tabs defaultValue="profile" className="space-y-4">
        <TabsList>
          <TabsTrigger value="profile">个人信息</TabsTrigger>
          <TabsTrigger value="password">修改密码</TabsTrigger>
        </TabsList>
        
        <TabsContent value="profile" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>个人信息</CardTitle>
              <CardDescription>
                更新您的个人信息和联系方式
              </CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleProfileUpdate} className="space-y-4">
                <div className="flex justify-center mb-6">
                  <Avatar className="h-24 w-24">
                    <AvatarImage src="" alt={userInfo?.username} />
                    <AvatarFallback className="text-2xl">
                      {userInfo?.nickname?.charAt(0).toUpperCase() || userInfo?.username?.charAt(0).toUpperCase()}
                    </AvatarFallback>
                  </Avatar>
                </div>
                
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="username">用户名</Label>
                    <div className="flex">
                      <div className="flex items-center px-3 border rounded-l-md bg-muted">
                        <User className="h-4 w-4 text-muted-foreground" />
                      </div>
                      <Input
                        id="username"
                        name="username"
                        value={profileForm.username}
                        disabled
                        className="rounded-l-none"
                      />
                    </div>
                    <p className="text-xs text-muted-foreground">
                      用户名不可修改
                    </p>
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="nickname">昵称</Label>
                    <div className="flex">
                      <div className="flex items-center px-3 border rounded-l-md bg-muted">
                        <User className="h-4 w-4 text-muted-foreground" />
                      </div>
                      <Input
                        id="nickname"
                        name="nickname"
                        value={profileForm.nickname}
                        onChange={handleProfileChange}
                        className="rounded-l-none"
                      />
                    </div>
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="email">邮箱</Label>
                    <div className="flex">
                      <div className="flex items-center px-3 border rounded-l-md bg-muted">
                        <Mail className="h-4 w-4 text-muted-foreground" />
                      </div>
                      <Input
                        id="email"
                        name="email"
                        type="email"
                        value={profileForm.email}
                        onChange={handleProfileChange}
                        className="rounded-l-none"
                      />
                    </div>
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="phone">手机号</Label>
                    <div className="flex">
                      <div className="flex items-center px-3 border rounded-l-md bg-muted">
                        <Phone className="h-4 w-4 text-muted-foreground" />
                      </div>
                      <Input
                        id="phone"
                        name="phone"
                        value={profileForm.phone}
                        onChange={handleProfileChange}
                        className="rounded-l-none"
                      />
                    </div>
                  </div>
                </div>
                
                <div className="space-y-2">
                  <Label>角色</Label>
                  <div className="flex flex-wrap gap-2">
                    {userInfo?.roles?.map(role => (
                      <div 
                        key={role.id}
                        className="flex items-center gap-1 px-3 py-1 rounded-full bg-blue-100 text-blue-800 text-sm"
                      >
                        <Shield className="h-3 w-3" />
                        <span>{role.name}</span>
                      </div>
                    ))}
                    {(!userInfo?.roles || userInfo.roles.length === 0) && (
                      <div className="text-muted-foreground text-sm">
                        无角色信息
                      </div>
                    )}
                  </div>
                </div>
                
                <div className="space-y-2">
                  <Label>权限</Label>
                  <div className="flex flex-wrap gap-2">
                    {userInfo?.roles?.flatMap(role => 
                      role.permissions?.map(permission => (
                        <div 
                          key={permission.id}
                          className="flex items-center gap-1 px-3 py-1 rounded-full bg-green-100 text-green-800 text-sm"
                        >
                          <Key className="h-3 w-3" />
                          <span>{permission.name}</span>
                        </div>
                      ))
                    )}
                    {(!userInfo?.roles || userInfo.roles.length === 0 || !userInfo.roles.some(role => role.permissions?.length)) && (
                      <div className="text-muted-foreground text-sm">
                        无权限信息
                      </div>
                    )}
                  </div>
                </div>
              </form>
            </CardContent>
            <CardFooter className="flex justify-end">
              <Button type="submit" onClick={handleProfileUpdate} disabled={loading}>
                {loading ? (
                  <>
                    <RefreshCw className="mr-2 h-4 w-4 animate-spin" />
                    保存中...
                  </>
                ) : (
                  <>
                    <Save className="mr-2 h-4 w-4" />
                    保存修改
                  </>
                )}
              </Button>
            </CardFooter>
          </Card>
        </TabsContent>
        
        <TabsContent value="password">
          <Card>
            <CardHeader>
              <CardTitle>修改密码</CardTitle>
              <CardDescription>
                更新您的账户密码
              </CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={handlePasswordUpdate} className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="oldPassword">当前密码</Label>
                  <Input
                    id="oldPassword"
                    name="oldPassword"
                    type="password"
                    value={passwordForm.oldPassword}
                    onChange={handlePasswordChange}
                  />
                </div>
                
                <div className="space-y-2">
                  <Label htmlFor="newPassword">新密码</Label>
                  <Input
                    id="newPassword"
                    name="newPassword"
                    type="password"
                    value={passwordForm.newPassword}
                    onChange={handlePasswordChange}
                  />
                  <p className="text-xs text-muted-foreground">
                    密码长度至少为6个字符
                  </p>
                </div>
                
                <div className="space-y-2">
                  <Label htmlFor="confirmPassword">确认新密码</Label>
                  <Input
                    id="confirmPassword"
                    name="confirmPassword"
                    type="password"
                    value={passwordForm.confirmPassword}
                    onChange={handlePasswordChange}
                  />
                </div>
              </form>
            </CardContent>
            <CardFooter className="flex justify-end">
              <Button onClick={handlePasswordUpdate} disabled={passwordLoading}>
                {passwordLoading ? (
                  <>
                    <RefreshCw className="mr-2 h-4 w-4 animate-spin" />
                    更新中...
                  </>
                ) : (
                  <>
                    <Save className="mr-2 h-4 w-4" />
                    更新密码
                  </>
                )}
              </Button>
            </CardFooter>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}
