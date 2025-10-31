/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.momento.vectorstore;

import com.alibaba.langengine.momento.MomentoException;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;


@Slf4j
public class MomentoAccessControl {
    
    public enum Permission {
        READ("read"),
        WRITE("write"),
        DELETE("delete"),
        ADMIN("admin");
        
        private final String value;
        
        Permission(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    public enum Role {
        VIEWER(new Permission[]{Permission.READ}),
        EDITOR(new Permission[]{Permission.READ, Permission.WRITE}),
        ADMIN(new Permission[]{Permission.READ, Permission.WRITE, Permission.DELETE, Permission.ADMIN});
        
        private final Set<Permission> permissions;
        
        Role(Permission[] permissions) {
            this.permissions = new HashSet<>();
            for (Permission p : permissions) {
                this.permissions.add(p);
            }
        }
        
        public boolean hasPermission(Permission permission) {
            return permissions.contains(permission);
        }
    }
    
    private final String userId;
    private final Role role;
    
    public MomentoAccessControl(String userId, Role role) {
        this.userId = userId;
        this.role = role;
    }
    
    /**
     * Check if the user has the required permission.
     */
    public boolean hasPermission(Permission permission) {
        return role.hasPermission(permission);
    }
    
    /**
     * Enforce permission check.
     */
    public void checkPermission(Permission permission) {
        if (!hasPermission(permission)) {
            log.warn("User {} attempted {} operation without permission", userId, permission.value);
            throw new MomentoException("PERMISSION_DENIED",
                "User " + userId + " does not have " + permission.value + " permission");
        }
    }
    
    /**
     * Enforce read permission.
     */
    public void checkReadPermission() {
        checkPermission(Permission.READ);
    }
    
    /**
     * Enforce write permission.
     */
    public void checkWritePermission() {
        checkPermission(Permission.WRITE);
    }
    
    /**
     * Enforce delete permission.
     */
    public void checkDeletePermission() {
        checkPermission(Permission.DELETE);
    }
    
    /**
     * Get the user ID.
     */
    public String getUserId() {
        return userId;
    }
    
    /**
     * Get the user's role.
     */
    public Role getRole() {
        return role;
    }
}
