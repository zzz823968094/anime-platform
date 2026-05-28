package com.anime.admin.controller;

import com.anime.admin.service.SystemUpdateService;
import com.anime.common.result.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 系统更新状态管理控制器
 */
@RestController
@RequestMapping("/api/admin/system")
public class SystemUpdateController {

    @Resource
    private SystemUpdateService systemUpdateService;

    /**
     * 获取系统更新状态
     */
    @GetMapping("/update-status")
    public Result getUpdateStatus() {
        return Result.ok(systemUpdateService.getStatusInfo());
    }

    /**
     * 设置系统更新状态
     */
    @PostMapping("/update-status")
    public Result setUpdateStatus(@RequestBody UpdateStatusRequest request) {
        systemUpdateService.setUpdating(request.isUpdating());
        if (request.getMessage() != null && !request.getMessage().isEmpty()) {
            systemUpdateService.setUpdateMessage(request.getMessage());
        }
        return Result.ok("系统更新状态已设置为: " + (request.isUpdating() ? "维护中" : "正常运行"));
    }

    /**
     * 快速切换系统更新状态（开启/关闭）
     */
    @PostMapping("/toggle-update")
    public Result toggleUpdateStatus() {
        boolean newState = systemUpdateService.toggleUpdating();
        return Result.ok("系统更新状态已切换为: " + (newState ? "维护中" : "正常运行"));
    }

    public static class UpdateStatusRequest {
        private boolean updating;
        private String message;

        public boolean isUpdating() {
            return updating;
        }

        public void setUpdating(boolean updating) {
            this.updating = updating;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
