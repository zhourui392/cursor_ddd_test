"use client";

import { useEffect, useState } from "react";
import { useRouter, usePathname } from "next/navigation";
import Link from "next/link";
import { useUserStore } from "@/store/useUserStore";
import { toast } from "@/components/ui/use-toast";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { 
  ChevronDown, 
  Menu, 
  User, 
  UserCircle, 
  LogOut,
  LayoutDashboard,
  Users,
  Shield,
  Key
} from "lucide-react";

interface RouteItem {
  path: string;
  name: string;
  meta?: {
    title?: string;
    icon?: string;
    permission?: string;
  };
  children?: RouteItem[];
}

export default function DashboardLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  const router = useRouter();
  const pathname = usePathname();
  const { token, userInfo, hasPermission, logout } = useUserStore();
  
  const [isCollapsed, setIsCollapsed] = useState(false);
  const [mounted, setMounted] = useState(false);

  // Routes configuration
  const routes: RouteItem[] = [
    {
      path: "/dashboard",
      name: "Dashboard",
      meta: { title: "仪表盘", icon: "LayoutDashboard" },
    },
    {
      path: "/user",
      name: "User",
      meta: { title: "用户管理", icon: "Users", permission: "USER_VIEW" },
    },
    {
      path: "/role",
      name: "Role",
      meta: { title: "角色管理", icon: "Shield", permission: "ROLE_VIEW" },
    },
    {
      path: "/permission",
      name: "Permission",
      meta: { title: "权限管理", icon: "Key", permission: "PERMISSION_VIEW" },
    },
    {
      path: "/profile",
      name: "Profile",
      meta: { title: "个人中心", icon: "UserCircle" },
    },
  ];

  // Check authentication on mount
  useEffect(() => {
    setMounted(true);
    
    if (!token) {
      router.replace("/login");
      return;
    }
    
    // Check if user info exists, if not, try to get it
    if (!userInfo) {
      const loadingUserInfo = localStorage.getItem("loadingUserInfo");
      if (loadingUserInfo !== "true") {
        localStorage.setItem("loadingUserInfo", "true");
        
        useUserStore.getState().getUserInfo()
          .then(() => {
            localStorage.removeItem("loadingUserInfo");
            console.log("User info retrieved successfully");
          })
          .catch((error) => {
            localStorage.removeItem("loadingUserInfo");
            console.error("Failed to get user info:", error);
            localStorage.removeItem("token");
            router.replace("/login");
          });
      }
    }
  }, [token, userInfo, router]);

  // Handle toggle sidebar
  const toggleSidebar = () => {
    setIsCollapsed(!isCollapsed);
  };

  // Check permission for route
  const checkPermission = (route: RouteItem) => {
    if (!route.meta || !route.meta.permission) {
      return true;
    }
    return hasPermission(route.meta.permission);
  };

  // Handle dropdown menu commands
  const handleCommand = (command: string) => {
    if (command === "logout") {
      logout();
      router.push("/login");
      toast({
        title: "已退出登录",
        description: "您已成功退出系统",
      });
    } else if (command === "profile") {
      router.push("/profile");
    }
  };

  // Get icon component by name
  const getIconComponent = (iconName?: string) => {
    switch (iconName) {
      case "LayoutDashboard":
        return <LayoutDashboard className="h-5 w-5" />;
      case "Users":
        return <Users className="h-5 w-5" />;
      case "Shield":
        return <Shield className="h-5 w-5" />;
      case "Key":
        return <Key className="h-5 w-5" />;
      case "UserCircle":
        return <UserCircle className="h-5 w-5" />;
      default:
        return <User className="h-5 w-5" />;
    }
  };

  // If not mounted yet, don't render anything to avoid hydration issues
  if (!mounted) {
    return null;
  }

  return (
    <div className="layout-container">
      {/* Header */}
      <header className="layout-header">
        <div className="header-left">
          <div className="logo">管理系统</div>
          <Button 
            variant="ghost" 
            size="icon" 
            onClick={toggleSidebar}
            className="toggle-icon"
          >
            <Menu className="h-5 w-5" />
          </Button>
        </div>
        
        <div className="header-right">
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" className="flex items-center gap-2">
                <Avatar className="h-8 w-8">
                  <AvatarFallback>{userInfo?.username?.charAt(0).toUpperCase() || "U"}</AvatarFallback>
                </Avatar>
                <span className="hidden md:inline-block">
                  {userInfo?.nickname || userInfo?.username}
                </span>
                <ChevronDown className="h-4 w-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem onClick={() => handleCommand("profile")}>
                <UserCircle className="mr-2 h-4 w-4" />
                <span>个人中心</span>
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem onClick={() => handleCommand("logout")}>
                <LogOut className="mr-2 h-4 w-4" />
                <span>退出登录</span>
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </header>
      
      <div className="layout-content">
        {/* Sidebar */}
        <aside className={`layout-sidebar ${isCollapsed ? "w-16" : "w-56"}`}>
          <nav className="h-full py-4">
            <ul className="space-y-1 px-2">
              {routes.map((route) => {
                if (!checkPermission(route)) return null;
                
                const isActive = pathname === route.path || pathname.startsWith(route.path + "/");
                
                return (
                  <li key={route.path}>
                    <Link 
                      href={route.path} 
                      className={`flex items-center gap-3 rounded-md px-3 py-2 transition-colors ${
                        isActive 
                          ? "bg-primary text-primary-foreground" 
                          : "text-white hover:bg-white/10"
                      }`}
                    >
                      {getIconComponent(route.meta?.icon)}
                      {!isCollapsed && (
                        <span>{route.meta?.title || route.name}</span>
                      )}
                    </Link>
                  </li>
                );
              })}
            </ul>
          </nav>
        </aside>
        
        {/* Main content */}
        <main className="layout-main">
          <div className="breadcrumb">
            <nav className="flex" aria-label="Breadcrumb">
              <ol className="inline-flex items-center space-x-1 md:space-x-3">
                <li className="inline-flex items-center">
                  <Link 
                    href="/dashboard" 
                    className="inline-flex items-center text-sm text-gray-700 hover:text-primary"
                  >
                    <LayoutDashboard className="mr-2 h-4 w-4" />
                    首页
                  </Link>
                </li>
                {pathname !== "/dashboard" && (
                  <li>
                    <div className="flex items-center">
                      <span className="mx-2 text-gray-400">/</span>
                      <span className="text-sm text-gray-500">
                        {routes.find(r => pathname.startsWith(r.path))?.meta?.title || "页面"}
                      </span>
                    </div>
                  </li>
                )}
              </ol>
            </nav>
          </div>
          
          <div className="main-content">
            {children}
          </div>
        </main>
      </div>
    </div>
  );
}
