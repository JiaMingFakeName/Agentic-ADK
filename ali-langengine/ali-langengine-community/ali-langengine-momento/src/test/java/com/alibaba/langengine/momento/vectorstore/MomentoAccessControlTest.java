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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class MomentoAccessControlTest {

    @Test
    void testViewerRole() {
        MomentoAccessControl control = new MomentoAccessControl("viewer1", 
            MomentoAccessControl.Role.VIEWER);
        
        assertTrue(control.hasPermission(MomentoAccessControl.Permission.READ));
        assertFalse(control.hasPermission(MomentoAccessControl.Permission.WRITE));
        assertFalse(control.hasPermission(MomentoAccessControl.Permission.DELETE));
    }

    @Test
    void testEditorRole() {
        MomentoAccessControl control = new MomentoAccessControl("editor1", 
            MomentoAccessControl.Role.EDITOR);
        
        assertTrue(control.hasPermission(MomentoAccessControl.Permission.READ));
        assertTrue(control.hasPermission(MomentoAccessControl.Permission.WRITE));
        assertFalse(control.hasPermission(MomentoAccessControl.Permission.DELETE));
    }

    @Test
    void testAdminRole() {
        MomentoAccessControl control = new MomentoAccessControl("admin1", 
            MomentoAccessControl.Role.ADMIN);
        
        assertTrue(control.hasPermission(MomentoAccessControl.Permission.READ));
        assertTrue(control.hasPermission(MomentoAccessControl.Permission.WRITE));
        assertTrue(control.hasPermission(MomentoAccessControl.Permission.DELETE));
        assertTrue(control.hasPermission(MomentoAccessControl.Permission.ADMIN));
    }

    @Test
    void testCheckReadPermission_Success() {
        MomentoAccessControl control = new MomentoAccessControl("viewer1", 
            MomentoAccessControl.Role.VIEWER);
        
        assertDoesNotThrow(() -> control.checkReadPermission());
    }

    @Test
    void testCheckWritePermission_Denied() {
        MomentoAccessControl control = new MomentoAccessControl("viewer1", 
            MomentoAccessControl.Role.VIEWER);
        
        assertThrows(MomentoException.class, () -> control.checkWritePermission());
    }

    @Test
    void testCheckDeletePermission_Denied() {
        MomentoAccessControl control = new MomentoAccessControl("editor1", 
            MomentoAccessControl.Role.EDITOR);
        
        assertThrows(MomentoException.class, () -> control.checkDeletePermission());
    }

    @Test
    void testCheckDeletePermission_Success() {
        MomentoAccessControl control = new MomentoAccessControl("admin1", 
            MomentoAccessControl.Role.ADMIN);
        
        assertDoesNotThrow(() -> control.checkDeletePermission());
    }

    @Test
    void testGetUserInfo() {
        MomentoAccessControl control = new MomentoAccessControl("testuser", 
            MomentoAccessControl.Role.EDITOR);
        
        assertEquals("testuser", control.getUserId());
        assertEquals(MomentoAccessControl.Role.EDITOR, control.getRole());
    }
}
