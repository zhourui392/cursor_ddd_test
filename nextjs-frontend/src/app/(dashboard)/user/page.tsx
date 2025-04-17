"use client";

import { useState, useEffect } from "react";
import { useUserStore } from "@/store/useUserStore";
import { getUserList, deleteUser } from "@/api/user";
import { toast } from "@/components/ui/use-toast";
import { Button } from "@/components/ui/button";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { 
  Pencil, 
  Trash2, 
  Plus, 
  Search, 
  RefreshCw,
  AlertCircle
} from "lucide-react";

interface User {
  id: string;
  username: string;
  nickname?: string;
  email?: string;
  phone?: string;
  status: number;
  createTime: string;
  roles?: Array<{
    id: string;
    name: string;
    code: string;
  }>;
}

export default function UserPage() {
  const { hasPermission } = useUserStore();
  
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(false);
  const [keyword, setKeyword] = useState("");
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [isUserFormOpen, setIsUserFormOpen] = useState(false);
  const [formMode, setFormMode] = useState<"add" | "edit">("add");
  
  const [formData, setFormData] = useState({
    username: "",
    nickname: "",
    email: "",
    phone: "",
    password: "",
    confirmPassword: "",
    roleIds: [] as string[],
  });

  // Load user list
  const loadUsers = async () => {
    setLoading(true);
    try {
      const res = await getUserList({ keyword });
      if (res.data) {
        setUsers(res.data);
      }
    } catch (error) {
      console.error("Failed to load users:", error);
      toast({
        variant: "destructive",
        title: "获取用户列表失败",
        description: "请检查网络连接或稍后重试",
      });
    } finally {
      setLoading(false);
    }
  };

  // Handle search
  const handleSearch = () => {
    loadUsers();
  };

  // Handle delete user
  const handleDelete = async () => {
    if (!currentUser) return;
    
    try {
      await deleteUser(currentUser.id);
      toast({
        title: "删除成功",
        description: `用户 ${currentUser.username} 已成功删除`,
      });
      loadUsers();
      setIsDeleteDialogOpen(false);
    } catch (error) {
      console.error("Failed to delete user:", error);
      toast({
        variant: "destructive",
        title: "删除用户失败",
        description: "请检查网络连接或稍后重试",
      });
    }
  };

  // Handle form input change
  const handleFormChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value,
    });
  };

  // Handle form submit
  const handleFormSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    // Form validation and submission logic would go here
    // This is a placeholder for the actual implementation
    toast({
      title: formMode === "add" ? "添加成功" : "更新成功",
      description: formMode === "add" 
        ? `用户 ${formData.username} 已成功添加` 
        : `用户 ${formData.username} 已成功更新`,
    });
    setIsUserFormOpen(false);
    loadUsers();
  };

  // Open edit form
  const openEditForm = (user: User) => {
    setCurrentUser(user);
    setFormMode("edit");
    setFormData({
      username: user.username,
      nickname: user.nickname || "",
      email: user.email || "",
      phone: user.phone || "",
      password: "",
      confirmPassword: "",
      roleIds: user.roles?.map(role => role.id) || [],
    });
    setIsUserFormOpen(true);
  };

  // Open add form
  const openAddForm = () => {
    setCurrentUser(null);
    setFormMode("add");
    setFormData({
      username: "",
      nickname: "",
      email: "",
      phone: "",
      password: "",
      confirmPassword: "",
      roleIds: [],
    });
    setIsUserFormOpen(true);
  };

  // Load users on mount
  useEffect(() => {
    loadUsers();
  }, []);

  return (
    <div className="space-y-4">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold">用户管理</h1>
        
        <div className="flex items-center gap-2">
          {hasPermission("USER_ADD") && (
            <Button onClick={openAddForm}>
              <Plus className="mr-2 h-4 w-4" />
              添加用户
            </Button>
          )}
        </div>
      </div>
      
      <div className="flex items-center gap-2 mb-4">
        <Input
          placeholder="搜索用户名或昵称"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          className="max-w-sm"
        />
        <Button variant="outline" onClick={handleSearch}>
          <Search className="h-4 w-4" />
        </Button>
        <Button variant="outline" onClick={loadUsers}>
          <RefreshCw className="h-4 w-4" />
        </Button>
      </div>
      
      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>用户名</TableHead>
              <TableHead>昵称</TableHead>
              <TableHead>邮箱</TableHead>
              <TableHead>角色</TableHead>
              <TableHead>状态</TableHead>
              <TableHead>创建时间</TableHead>
              <TableHead className="text-right">操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={7} className="text-center py-8">
                  <div className="flex justify-center items-center">
                    <RefreshCw className="h-5 w-5 animate-spin mr-2" />
                    <span>加载中...</span>
                  </div>
                </TableCell>
              </TableRow>
            ) : users.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} className="text-center py-8">
                  <div className="flex justify-center items-center">
                    <AlertCircle className="h-5 w-5 mr-2" />
                    <span>暂无数据</span>
                  </div>
                </TableCell>
              </TableRow>
            ) : (
              users.map((user) => (
                <TableRow key={user.id}>
                  <TableCell>{user.username}</TableCell>
                  <TableCell>{user.nickname || "-"}</TableCell>
                  <TableCell>{user.email || "-"}</TableCell>
                  <TableCell>
                    <div className="flex flex-wrap gap-1">
                      {user.roles?.map((role) => (
                        <span 
                          key={role.id}
                          className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800"
                        >
                          {role.name}
                        </span>
                      ))}
                    </div>
                  </TableCell>
                  <TableCell>
                    <span 
                      className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${
                        user.status === 1 
                          ? "bg-green-100 text-green-800" 
                          : "bg-red-100 text-red-800"
                      }`}
                    >
                      {user.status === 1 ? "正常" : "禁用"}
                    </span>
                  </TableCell>
                  <TableCell>
                    {new Date(user.createTime).toLocaleString()}
                  </TableCell>
                  <TableCell className="text-right">
                    <div className="flex justify-end gap-2">
                      {hasPermission("USER_EDIT") && (
                        <Button 
                          variant="outline" 
                          size="sm"
                          onClick={() => openEditForm(user)}
                        >
                          <Pencil className="h-4 w-4" />
                        </Button>
                      )}
                      {hasPermission("USER_DELETE") && (
                        <Button 
                          variant="outline" 
                          size="sm"
                          onClick={() => {
                            setCurrentUser(user);
                            setIsDeleteDialogOpen(true);
                          }}
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      )}
                    </div>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>
      
      {/* Delete Confirmation Dialog */}
      <Dialog open={isDeleteDialogOpen} onOpenChange={setIsDeleteDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>确认删除</DialogTitle>
            <DialogDescription>
              您确定要删除用户 <span className="font-medium">{currentUser?.username}</span> 吗？此操作不可撤销。
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsDeleteDialogOpen(false)}>
              取消
            </Button>
            <Button variant="destructive" onClick={handleDelete}>
              删除
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
      
      {/* User Form Dialog */}
      <Dialog open={isUserFormOpen} onOpenChange={setIsUserFormOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>
              {formMode === "add" ? "添加用户" : "编辑用户"}
            </DialogTitle>
            <DialogDescription>
              {formMode === "add" 
                ? "添加新用户到系统中" 
                : `编辑用户 ${currentUser?.username} 的信息`}
            </DialogDescription>
          </DialogHeader>
          <form onSubmit={handleFormSubmit}>
            <div className="grid gap-4 py-4">
              <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="username" className="text-right">
                  用户名
                </Label>
                <Input
                  id="username"
                  name="username"
                  value={formData.username}
                  onChange={handleFormChange}
                  disabled={formMode === "edit"}
                  className="col-span-3"
                />
              </div>
              <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="nickname" className="text-right">
                  昵称
                </Label>
                <Input
                  id="nickname"
                  name="nickname"
                  value={formData.nickname}
                  onChange={handleFormChange}
                  className="col-span-3"
                />
              </div>
              <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="email" className="text-right">
                  邮箱
                </Label>
                <Input
                  id="email"
                  name="email"
                  type="email"
                  value={formData.email}
                  onChange={handleFormChange}
                  className="col-span-3"
                />
              </div>
              <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="phone" className="text-right">
                  手机号
                </Label>
                <Input
                  id="phone"
                  name="phone"
                  value={formData.phone}
                  onChange={handleFormChange}
                  className="col-span-3"
                />
              </div>
              {formMode === "add" && (
                <>
                  <div className="grid grid-cols-4 items-center gap-4">
                    <Label htmlFor="password" className="text-right">
                      密码
                    </Label>
                    <Input
                      id="password"
                      name="password"
                      type="password"
                      value={formData.password}
                      onChange={handleFormChange}
                      className="col-span-3"
                    />
                  </div>
                  <div className="grid grid-cols-4 items-center gap-4">
                    <Label htmlFor="confirmPassword" className="text-right">
                      确认密码
                    </Label>
                    <Input
                      id="confirmPassword"
                      name="confirmPassword"
                      type="password"
                      value={formData.confirmPassword}
                      onChange={handleFormChange}
                      className="col-span-3"
                    />
                  </div>
                </>
              )}
              {/* Role selection would go here */}
            </div>
            <DialogFooter>
              <Button type="submit">
                {formMode === "add" ? "添加" : "保存"}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}
