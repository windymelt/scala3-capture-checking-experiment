package dev.capslock.repository.db

import dev.capslock.model.*

/** 初期投入されるハードコードされたデータ。
  *
  * テナントは `tenant1`〜`tenant5` の 5 つで、それぞれに数名のユーザーが属する。
  * [[Database]] の保存形式に合わせて、ドメインモデルではなく named tuple（行）で
  * 定義する。
  */
object SeedData {

  /** 初期投入するテナント行（tenant1〜tenant5）。 */
  val tenants: Seq[(id: TenantId, name: String)] =
    (1 to 5).map(n => (id = TenantId(s"tenant$n"), name = s"Tenant $n Inc."))

  /** 初期投入するユーザー行。各テナントに 2 名ずつ属する。 */
  val users: Seq[(tenantId: TenantId, name: String, email: String)] =
    (1 to 5).flatMap { n =>
      val tid = TenantId(s"tenant$n")
      Seq(
        (tenantId = tid, name = s"Alice of Tenant $n", email = s"alice@tenant$n.example.com"),
        (tenantId = tid, name = s"Bob of Tenant $n", email = s"bob@tenant$n.example.com")
      )
    }
}
