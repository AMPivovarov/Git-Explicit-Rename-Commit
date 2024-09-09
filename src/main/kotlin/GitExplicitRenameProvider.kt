// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.git.explicit.rename

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Couple
import com.intellij.openapi.util.Key
import com.intellij.openapi.vcs.FilePath
import com.intellij.vcsUtil.VcsUtil
import git4idea.checkin.GitCheckinExplicitMovementProvider

val explicitRenameOldPath = Key.create<String>("GIT_EXPLICT_RENAME_PATH_BEFORE_RENAME")

class GitExplicitRenameProvider : GitCheckinExplicitMovementProvider() {
  override fun isEnabled(p0: Project): Boolean {
    return true
  }

  override fun getDescription(): String {
    return "Create extra commit with explicit renames"
  }

  override fun getCommitMessage(message: String): String {
    return message
  }

  override fun collectExplicitMovements(
    project: Project,
    beforePaths: List<FilePath>,
    afterPaths: List<FilePath>
  ): Collection<Movement> {
    val beforeMap = beforePaths.toSet()

    val movedChanges = ArrayList<Movement>()
    for (after in afterPaths) {
      val pathBeforeConversion = after.virtualFile?.getUserData(explicitRenameOldPath) ?: continue
      val filePath = VcsUtil.getFilePath(pathBeforeConversion, false)
      if (beforeMap.contains(filePath)) {
        movedChanges.add(Movement(filePath, after))
      }
    }

    return movedChanges
  }

  override fun afterMovementsCommitted(project: Project, movedPaths: List<Couple<FilePath>>) {
    movedPaths.forEach { it.second.virtualFile?.putUserData(explicitRenameOldPath, null) }
  }
}