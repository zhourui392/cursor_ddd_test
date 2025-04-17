"use client";

import Link from "next/link";
import { Button } from "@/components/ui/button";
import { AlertCircle } from "lucide-react";

export default function NotFound() {
  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-gray-100 px-4">
      <div className="text-center max-w-md">
        <div className="flex justify-center mb-4">
          <AlertCircle className="h-24 w-24 text-destructive" />
        </div>
        <h1 className="text-4xl font-bold mb-4">404</h1>
        <h2 className="text-2xl font-semibold mb-2">页面未找到</h2>
        <p className="text-muted-foreground mb-6">
          您访问的页面不存在或已被移除。
        </p>
        <Button asChild>
          <Link href="/dashboard">
            返回首页
          </Link>
        </Button>
      </div>
    </div>
  );
}
