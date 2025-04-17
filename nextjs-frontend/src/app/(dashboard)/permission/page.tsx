"use client";

import { useState, useEffect } from "react";
import { useUserStore } from "@/store/useUserStore";
import { getPermissionList, deletePermission } from "@/api/permission";
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
  Key
} from "lucide-react";

interface Permission {
  id: string;
  code: string;
  name: string;
  description?: string;
  createTime: string;
}

export default function PermissionPage() {
  const { hasPermission } = useUserStore();
  
  const [permissions, setPermissions] = useState<Permission[]>([]);
  const [loading, setLoading] = useState(false);
  const [keyword, setKeyword] = useState("");
  const [currentPermission, setCurrentPermission] = useState<Permission | null>(null);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [isPermissionFormOpen, setIsPermissionFormOpen] = useState(false);
  const [formMode, setFormMode] = useState<"add" | "edit">("add");
  
  const [formData, setFormData] = useState({
    code: "",
    name: "",
    description: "",
  });

  // Load permission list
  const loadPermissions = async () => {
    setLoading(true);
    try {
      const res = await getPermissionList({ keyword });
      if (res.data) {
        setPermissions(res.data);
      }
    } catch (error) {
      console.error("Failed to load permissions:", error);
      toast({
        variant: "destructive",
        title: "获取权限列表失败",
        description: "请检查网络连接或稍后重试",
      });
    } finally {
      setLoading(false);
    }
  };

  // Handle search
  const handleSearch = () => {
    loadPermissions();
  };

  // Handle delete permission
  const handleDelete = async () => {
    if (!currentPermission) return;
    
    try {
      await deletePermission(currentPermission.id);
      toast({
        title: "删除成功",
        description: `权限 ${currentPermission.name} 已成功删除`,
      });
      loadPermissions();
      setIsDeleteDialogOpen(false);
    } catch (error) {
      console.error("Failed to delete permission:", error);
      toast({
        variant: "destructive",
        title: "删除权限失败",
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

  // Handle form submit
  const handleFormSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    // Form validation and submission logic would go here
    // This is a placeholder for the actual implementation
    toast({
      title: formMode === "add" ? "添加成功" : "更新成功",
      description: formMode === "add" 
        ? `权限 ${formData.name} 已成功添加` 
        : `权限 ${formData.name} 已成功更新`,
    });
    setIsPermissionFormOpen(false);
    loadPermissions();
  };

  // Open edit form
  const openEditForm = (permission: Permission) => {
    setCurrentPermission(permission);
    setFormMode("edit");
    setFormData({
      code: permission.code,
      name: permission.name,
      description: permission.description || "",
    });
    setIsPermissionFormOpen(true);
  };

  // Open add form
  const openAddForm = () => {
    setCurrentPermission(null);
    setFormMode("add");
    setFormData({
      code: "",
      name: "",
      description: "",
    });
    setIsPermissionFormOpen(true);
  };

  // Load permissions on mount
  useEffect(() => {
    loadPermissions();
  }, []);

  return (
    <div className="space-y-4">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold">权限管理</h1>
        
        <div className="flex items-center gap-2">
          {hasPermission("PERMISSION_ADD") && (
            <Button onClick={openAddForm}>
              <Plus className="mr-2 h-4 w-4" />
              添加权限
            </Button>
          )}
        </div>
      </div>
      
      <div className="flex items-center gap-2 mb-4">
        <Input
          placeholder="搜索权限名称或编码"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          className="max-w-sm"
        />
        <Button variant="outline" onClick={handleSearch}>
          <Search className="h-4 w-4" />
        </Button>
        <Button variant="outline" onClick={loadPermissions}>
          <RefreshCw className="h-4 w-4" />
        </Button>
      </div>
      
      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>权限编码</TableHead>
              <TableHead>权限名称</TableHead>
              <TableHead>描述</TableHead>
              <TableHead>创建时间</TableHead>
              <TableHead className="text-right">操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={5} className="text-center py-8">
                  <div className="flex justify-center items-center">
                    <RefreshCw className="h-5 w-5 animate-spin mr-2" />
                    <span>加载中...</span>
                  </div>
                </TableCell>
              </TableRow>
            ) : permissions.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} className="text-center py-8">
                  <div className="flex justify-center items-center">
                    <AlertCircle className="h-5 w-5 mr-2" />
                    <span>暂无数据</span>
                  </div>
                </TableCell>
              </TableRow>
            ) : (
              permissions.map((permission) => (
                <TableRow key={permission.id}>
                  <TableCell>{permission.code}</TableCell>
                  <TableCell>
                    <div className="flex items-center gap-2">
                      <Key className="h-4 w-4 text-green-500" />
                      {permission.name}
                    </div>
                  </TableCell>
                  <TableCell>{permission.description || "-"}</TableCell>
                  <TableCell>
                    {new Date(permission.createTime).toLocaleString()}
                  </TableCell>
                  <TableCell className="text-right">
                    <div className="flex justify-end gap-2">
                      {hasPermission("PERMISSION_EDIT") && (
                        <Button 
                          variant="outline" 
                          size="sm"
                          onClick={() => openEditForm(permission)}
                        >
                          <Pencil className="h-4 w-4" />
                        </Button>
                      )}
                      {hasPermission("PERMISSION_DELETE") && (
                        <Button 
                          variant="outline" 
                          size="sm"
                          onClick={() => {
                            setCurrentPermission(permission);
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
              您确定要删除权限 <span className="font-medium">{currentPermission?.name}</span> 吗？此操作不可撤销。
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
      
      {/* Permission Form Dialog */}
      <Dialog open={isPermissionFormOpen} onOpenChange={setIsPermissionFormOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>
              {formMode === "add" ? "添加权限" : "编辑权限"}
            </DialogTitle>
            <DialogDescription>
              {formMode === "add" 
                ? "添加新权限到系统中" 
                : `编辑权限 ${currentPermission?.name} 的信息`}
            </DialogDescription>
          </DialogHeader>
          <form onSubmit={handleFormSubmit}>
            <div className="grid gap-4 py-4">
              <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="code" className="text-right">
                  权限编码
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
                  权限名称
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
