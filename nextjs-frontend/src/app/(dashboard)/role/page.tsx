"use client";

import { useState, useEffect } from "react";
import { useUserStore } from "@/store/useUserStore";
import { getRoleList, deleteRole, getAllPermissions } from "@/api/role";
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
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Pencil,
  Trash2,
  Plus,
  Search,
  RefreshCw,
  AlertCircle,
  Shield
} from "lucide-react";

interface Permission {
  id: string;
  code: string;
  name: string;
  description?: string;
}

interface Role {
  id: string;
  code: string;
  name: string;
  description?: string;
  createTime: string;
  permissions: Permission[];
}

export default function RolePage() {
  const { hasPermission } = useUserStore();
  
  const [roles, setRoles] = useState<Role[]>([]);
  const [allPermissions, setAllPermissions] = useState<Permission[]>([]);
  const [loading, setLoading] = useState(false);
  const [keyword, setKeyword] = useState("");
  const [currentRole, setCurrentRole] = useState<Role | null>(null);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [isRoleFormOpen, setIsRoleFormOpen] = useState(false);
  const [formMode, setFormMode] = useState<"add" | "edit">("add");
  
  const [formData, setFormData] = useState({
    code: "",
    name: "",
    description: "",
    permissionIds: [] as string[],
  });

  // Load role list
  const loadRoles = async () => {
    setLoading(true);
    try {
      const res = await getRoleList({ keyword });
      if (res.data) {
        setRoles(res.data);
      }
    } catch (error) {
      console.error("Failed to load roles:", error);
      toast({
        variant: "destructive",
        title: "获取角色列表失败",
        description: "请检查网络连接或稍后重试",
      });
    } finally {
      setLoading(false);
    }
  };

  // Load all permissions
  const loadPermissions = async () => {
    try {
      const res = await getAllPermissions();
      if (res.data) {
        setAllPermissions(res.data);
      }
    } catch (error) {
      console.error("Failed to load permissions:", error);
      toast({
        variant: "destructive",
        title: "获取权限列表失败",
        description: "请检查网络连接或稍后重试",
      });
    }
  };

  // Handle search
  const handleSearch = () => {
    loadRoles();
  };

  // Handle delete role
  const handleDelete = async () => {
    if (!currentRole) return;
    
    try {
      await deleteRole(currentRole.id);
      toast({
        title: "删除成功",
        description: `角色 ${currentRole.name} 已成功删除`,
      });
      loadRoles();
      setIsDeleteDialogOpen(false);
    } catch (error) {
      console.error("Failed to delete role:", error);
      toast({
        variant: "destructive",
        title: "删除角色失败",
        description: "请检查网络连接或稍后重试",
      });
    }
  };

  // Handle form input change
  const handleFormChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value,
    });
  };

  // Handle permission checkbox change
  const handlePermissionChange = (permissionId: string) => {
    setFormData(prev => {
      const permissionIds = [...prev.permissionIds];
      const index = permissionIds.indexOf(permissionId);
      
      if (index === -1) {
        permissionIds.push(permissionId);
      } else {
        permissionIds.splice(index, 1);
      }
      
      return {
        ...prev,
        permissionIds,
      };
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
        ? `角色 ${formData.name} 已成功添加` 
        : `角色 ${formData.name} 已成功更新`,
    });
    setIsRoleFormOpen(false);
    loadRoles();
  };

  // Open edit form
  const openEditForm = (role: Role) => {
    setCurrentRole(role);
    setFormMode("edit");
    setFormData({
      code: role.code,
      name: role.name,
      description: role.description || "",
      permissionIds: role.permissions.map(p => p.id),
    });
    setIsRoleFormOpen(true);
  };

  // Open add form
  const openAddForm = () => {
    setCurrentRole(null);
    setFormMode("add");
    setFormData({
      code: "",
      name: "",
      description: "",
      permissionIds: [],
    });
    setIsRoleFormOpen(true);
  };

  // Load roles and permissions on mount
  useEffect(() => {
    loadRoles();
    loadPermissions();
  }, []);

  return (
    <div className="space-y-4">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold">角色管理</h1>
        
        <div className="flex items-center gap-2">
          {hasPermission("ROLE_ADD") && (
            <Button onClick={openAddForm}>
              <Plus className="mr-2 h-4 w-4" />
              添加角色
            </Button>
          )}
        </div>
      </div>
      
      <div className="flex items-center gap-2 mb-4">
        <Input
          placeholder="搜索角色名称或编码"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          className="max-w-sm"
        />
        <Button variant="outline" onClick={handleSearch}>
          <Search className="h-4 w-4" />
        </Button>
        <Button variant="outline" onClick={loadRoles}>
          <RefreshCw className="h-4 w-4" />
        </Button>
      </div>
      
      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>角色编码</TableHead>
              <TableHead>角色名称</TableHead>
              <TableHead>描述</TableHead>
              <TableHead>权限数量</TableHead>
              <TableHead>创建时间</TableHead>
              <TableHead className="text-right">操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={6} className="text-center py-8">
                  <div className="flex justify-center items-center">
                    <RefreshCw className="h-5 w-5 animate-spin mr-2" />
                    <span>加载中...</span>
                  </div>
                </TableCell>
              </TableRow>
            ) : roles.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} className="text-center py-8">
                  <div className="flex justify-center items-center">
                    <AlertCircle className="h-5 w-5 mr-2" />
                    <span>暂无数据</span>
                  </div>
                </TableCell>
              </TableRow>
            ) : (
              roles.map((role) => (
                <TableRow key={role.id}>
                  <TableCell>{role.code}</TableCell>
                  <TableCell>
                    <div className="flex items-center gap-2">
                      <Shield className="h-4 w-4 text-blue-500" />
                      {role.name}
                    </div>
                  </TableCell>
                  <TableCell>{role.description || "-"}</TableCell>
                  <TableCell>
                    <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                      {role.permissions.length}
                    </span>
                  </TableCell>
                  <TableCell>
                    {new Date(role.createTime).toLocaleString()}
                  </TableCell>
                  <TableCell className="text-right">
                    <div className="flex justify-end gap-2">
                      {hasPermission("ROLE_EDIT") && (
                        <Button 
                          variant="outline" 
                          size="sm"
                          onClick={() => openEditForm(role)}
                        >
                          <Pencil className="h-4 w-4" />
                        </Button>
                      )}
                      {hasPermission("ROLE_DELETE") && (
                        <Button 
                          variant="outline" 
                          size="sm"
                          onClick={() => {
                            setCurrentRole(role);
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
              您确定要删除角色 <span className="font-medium">{currentRole?.name}</span> 吗？此操作不可撤销。
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
      
      {/* Role Form Dialog */}
      <Dialog open={isRoleFormOpen} onOpenChange={setIsRoleFormOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>
              {formMode === "add" ? "添加角色" : "编辑角色"}
            </DialogTitle>
            <DialogDescription>
              {formMode === "add" 
                ? "添加新角色到系统中" 
                : `编辑角色 ${currentRole?.name} 的信息`}
            </DialogDescription>
          </DialogHeader>
          <form onSubmit={handleFormSubmit}>
            <div className="grid gap-4 py-4">
              <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="code" className="text-right">
                  角色编码
                </Label>
                <Input
                  id="code"
                  name="code"
                  value={formData.code}
                  onChange={handleFormChange}
                  disabled={formMode === "edit"}
                  className="col-span-3"
                />
              </div>
              <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="name" className="text-right">
                  角色名称
                </Label>
                <Input
                  id="name"
                  name="name"
                  value={formData.name}
                  onChange={handleFormChange}
                  className="col-span-3"
                />
              </div>
              <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="description" className="text-right">
                  描述
                </Label>
                <Input
                  id="description"
                  name="description"
                  value={formData.description}
                  onChange={handleFormChange}
                  className="col-span-3"
                />
              </div>
              
              <div className="grid grid-cols-4 gap-4">
                <Label className="text-right pt-2">
                  权限
                </Label>
                <div className="col-span-3 border rounded-md p-3 max-h-60 overflow-y-auto">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-2">
                    {allPermissions.map(permission => (
                      <div key={permission.id} className="flex items-center space-x-2">
                        <input
                          type="checkbox"
                          id={`permission-${permission.id}`}
                          checked={formData.permissionIds.includes(permission.id)}
                          onChange={() => handlePermissionChange(permission.id)}
                          className="h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary"
                        />
                        <label 
                          htmlFor={`permission-${permission.id}`}
                          className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
                        >
                          {permission.name}
                        </label>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
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
