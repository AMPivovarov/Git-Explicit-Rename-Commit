package com.jetbrains.git.explicit.rename

import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.newvfs.events.VFileMoveEvent
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent

class GitExplicitRenameListener : BulkFileListener {
  override fun after(events: List<VFileEvent>) {
    for (event in events) {
      if (event is VFileMoveEvent) {
        markRenamedFile(event.file, event.oldPath)
      }
      if (event is VFilePropertyChangeEvent && event.isRename) {
        markRenamedFile(event.file, event.oldPath)
      }
    }
  }

  private fun markRenamedFile(file: VirtualFile, oldPath: String) {
    if (file.isDirectory) {
      VfsUtilCore.visitChildrenRecursively(file, object : VirtualFileVisitor<Unit>(NO_FOLLOW_SYMLINKS) {
        override fun visitFile(child: VirtualFile): Boolean {
          val relativePath = VfsUtil.getRelativePath(child, file) ?: return true
          child.putUserData(explicitRenameOldPath, oldPath + "/" + relativePath)
          return true
        }
      })
    }
    else {
      if (file.getUserData(explicitRenameOldPath) == null) {
        file.putUserData(explicitRenameOldPath, oldPath)
      }
    }
  }
}
